/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bc.webcrawler;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import com.bc.util.Util;
import com.bc.util.concurrent.BoundedExecutorService;
import com.bc.webcrawler.predicates.LinkStartsWithTargetLinkTest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class CrawlerImpl<E> implements Serializable, 
        Iterator<E>, Crawler<E>, CrawlMetaData {
    
    private transient static final Logger LOG = Logger.getLogger(CrawlerImpl.class.getName());
    
    private final String startUrl;
    
    private final String baseUrl;
    
    private final List<String> seedUrls;

    private final CrawlerContext<E> context;

    private final Set<String> attempted;

    private final Set<String> failed;

    private final MultiQueue<String> links;

    private final Set<String> robotsExcludedLinks;
    
    private final Predicate<String> linkStartsWithBaseUrlTest;

    private final ExecutorService linkCollectionExecSvc;
    
    private boolean shutdown;

    private int crawled;

    private int indexWithinBatch;
    
    private long startTime = -1L;
            
    public CrawlerImpl(CrawlerContext<E> context, Set<String> seedUrls) {

        LOG.finer("Creating");
        
        this.context = Objects.requireNonNull(context);

        LOG.fine(() -> "Seeding Crawler with " + seedUrls.size() + " URLs");
        
        if(seedUrls.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        this.seedUrls = new ArrayList(seedUrls);
        
        final int queueCapacity = (int)this.context.getCrawlLimit();
        this.links = new MultiQueue(
                new LinkedBlockingQueueWithTimedPeek(seedUrls), 
                new LinkedBlockingQueueWithTimedPeek(queueCapacity));
        
        this.startUrl = Objects.requireNonNull(this.links.peek());
        
        Objects.requireNonNull(this.startUrl);
        
        this.baseUrl = com.bc.util.Util.getBaseURL(startUrl);
        
        LOG.fine(() -> "\n Base URL: " + this.baseUrl + "\nStart URL: " + this.startUrl);

        try{
            this.linkStartsWithBaseUrlTest = new LinkStartsWithTargetLinkTest(this.baseUrl);
        }catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
        
        this.attempted = new HashSet();

        this.failed = new HashSet();

        this.robotsExcludedLinks = new HashSet<>();

        final String threadPoolName = this.getClass().getName() + "-LinkCollectionThreadPool";

        final int size = (int)(Util.availableMemory() / 1_000_000);
        LOG.fine(() -> "Size: " + size + ", of queue for " + threadPoolName + " ExecutorService");
        this.linkCollectionExecSvc = new BoundedExecutorService(
                threadPoolName, 1, size < 2 ? 2 : size, false);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        
        LOG.fine(() -> "Done creating: " + this);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
    
    @Override
    public void shutdown() {
        if(this.isShutdown()) {
            return;
        }
        LOG.log(Level.FINE, "SHUTTING DOWN {0}", this);
        Util.shutdownAndAwaitTermination(linkCollectionExecSvc, 3, TimeUnit.SECONDS);
        this.shutdown = true;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public List<String> getSeedUrls() {
        return Collections.unmodifiableList(seedUrls);
    }
    
    @Override
    public Optional<String> getCurrentUrl() {
        return links.isEmpty() ? Optional.empty() : Optional.ofNullable(links.peek());
    }

    @Override
    public CrawlMetaData getMetaData() {
        return this;
    }
    
    protected void preParse(String url) { }

    protected void postParse(String url, E doc) { }

    @Override
    public boolean hasNext() {

        if(this.startTime == -1L) {
            this.startTime = System.currentTimeMillis();
        }
        
        boolean hasNext = false;
        
        final Predicate<String> parseUrlTest = this.context.getParseUrlTest();
        
        while(this.mayParseNext()) {
            
            final String next = this.waitAndGetFirstLink(null);
            
            if(next == null) {
                break;
            }
            
            if(parseUrlTest.test(next)) {
                
                hasNext = true;
                
                break;
                
            }else{
            
                LOG.fine(() -> "Rejected by: " + parseUrlTest.getClass().getName() + ", URL: " + next);

                links.remove();
            }
        }

        final Level level = hasNext ? Level.FINEST : Level.FINE;

        LOG.log(level, "HasNext: {0}", hasNext);

        return hasNext;
    }
    
    @Override
    public E parseNext() throws IOException {

        if(!this.mayParseNext()) {
            
            if(this.isTimedout()) {
                return null;
            }
            
            throw new IllegalStateException("Operation not allowed since #hasNext() returns false");
        }
        
        final int batchSize = this.context.getBatchSize();

        if ((batchSize > 0) && (++this.indexWithinBatch >= batchSize)) {

            this.indexWithinBatch = 0;

            waitBeforeNextBatch(context.getBatchInterval());
        }

        final String rawUrl = this.waitAndRemoveFirstLink(null);
        
        if(rawUrl == null) {
            
            return null;
        }

        String url;

        E doc;

        try {

            url = this.context.getUrlFormatter().apply(rawUrl);

            preParse(rawUrl);

            if(LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Raw: {0}\nURL: {1}", new Object[]{rawUrl, url});
            }

            this.context.getResumeHandler().saveIfNotExists(rawUrl);

            this.attempted.add(rawUrl);

            try{
                doc = parse(url, this.context.getRetryOnExceptionTestSupplier().get());
            }catch(IOException e) {
                if(url.contains("&amp;")) {
                    url = url.replace("&amp;", "&");
                    doc = parse(url, this.context.getRetryOnExceptionTestSupplier().get());
                }else{
                    throw e;
                }
            }
            
            if(doc != null) {
                final boolean alreadyExcluded = this.robotsExcludedLinks.contains(url);
                if (alreadyExcluded || this.context.getPageIsNoFollowTest().test(doc)) {
                    if(!alreadyExcluded) {
                        this.robotsExcludedLinks.add(url);
                    }
                }else{
                    
                    this.collectLinks(doc, url);
                }
            }

            postParse(url, doc);

        }catch (IOException | RuntimeException e){

            boolean added = this.failed.add(rawUrl);

            if(added) { 
                LOG.warning(() -> "Parse failed for: " + rawUrl + ". Reason: " + e.toString());
            }

            throw e;
        }

        return doc;
    }

    public boolean mayParseNext() {
        return this.mayProceed() && isWithinParseLimit();
    }
    
    @Override
    public void remove() {
      throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    public boolean isWithinParseLimit() {
        final int parsed = this.getAttempted();
        final boolean withinLimit = isWithLimit(parsed, this.context.getParseLimit());
        LOG.finest(() -> MessageFormat.format("Parse pos: {0}, limit: {1}, within parse limit: {2}", 
                parsed, context.getParseLimit(), withinLimit));
        return withinLimit;
    }

    @Override
    public boolean isWithinCrawlLimit() {
        final boolean withinLimit = isWithLimit(this.crawled, this.context.getCrawlLimit());
        LOG.finest(() -> MessageFormat.format("URLs: {0}, limit: {1}, within crawl limit: {2}", 
                this.links.size(), this.context.getCrawlLimit(), withinLimit));
        return withinLimit;
    }

    @Override
     public boolean isWithinFailLimit() {
        final int fails = this.getFailed();
        final int maxFails = this.context.getMaxFailsAllowed();
        final boolean withinLimit = isWithLimit(fails, maxFails);
        LOG.finest(() -> "Within failed limit: " + withinLimit + ", failed: "+fails+", max fails allowed: " + maxFails);
        return withinLimit;
    }
    
   protected boolean isWithLimit(long offset, long limit) {
        boolean withinLimit = true;
        if (limit > 0) {
            withinLimit = offset < limit;
        }
        return withinLimit;
    }

    public E parse(String link, Predicate<Throwable> test) 
            throws MalformedURLException, IOException {

        LOG.fine(() -> MessageFormat.format("Pages left: {0}, parsing: {1}", 
                this.getRemaining(), link));
        
        E doc;

        try {

            doc = this.context.getUrlParser().parse(link);

        }catch (IOException e) {

            final boolean retry = test.test(e);

            if (retry) {

                doc = parse(link, test);

            } else {

                throw e;
            }
        }

    //    final int size = doc == null ? -1 : doc.childNodeSize() + 1;
    //    logger.fine(() -> "Nodes found: "+size+", URL: " + link);

    //    if(logger.isLoggable(Level.FINEST)) {
    //      logger.log(Level.FINEST, "Nodes found HTML:\n{0}", doc.outerHtml());
    //    }

        return doc;
    }

    public void collectLinks(E doc, String url) {
        
        LOG.finer(() -> MessageFormat.format("Pages left: {0}, crawling: {1}", 
            this.getRemaining(), url));
    
        final Set<String> docLinks = this.context.getLinksExtractor().apply(doc);
        
        LOG.finer(() -> "Extracted: "+docLinks.size()+" links from: " + url);
        
        if(docLinks.isEmpty()) {
            LOG.fine(() -> "No links to collect from: " + url);
        }else{
            this.linkCollectionExecSvc.submit(() -> this.collectLinks(docLinks, url));
        }
    }
    
    public int collectLinks(Set<String> linkSet, String url) {
        int collected = 0;
        for(String link : linkSet) {
            if(this.collectLink(link)) {
                ++collected;
            }
        }
        final int n = collected;
        LOG.finer(() -> "Collected: " + n + " links from: " + url);
        return collected;
    }

    public boolean collectLink(String link) {

        boolean collected = false;
        
        final boolean collect = this.isToBeCollected(link);
        
        LOG.finer(() -> "Will be collected: " + collect + ", URL: " + link);
        
        if(collect){

            final Queue<String> target;
            if(links.isEmpty()) {
                target = this.links.getPageAt(0);
            }else if(this.context.getPreferredLinkTest().test(link)) {    
                target = this.links.getPageAt(0);
            }else{
                target = this.links.getPageAt(1);
            }

            LOG.finest(() -> "Collecting " + link);
            
            collected = target.add(link);

            ++crawled;
        }
        
        return collected;
    }
    
    public boolean isToBeCollected(String link) {
        if(LOG.isLoggable(Level.FINER)) {
            return this.isToBeCollectedWithMetricsLog(link, Level.FINER);
        }else{
            final boolean collect = 
                    (this.isWithinCrawlLimit()) && 
                    (this.linkStartsWithBaseUrlTest.test(link)) &&
                    !(this.isAttempted(link)) && 
                    !(links.contains(link)) &&
                    (this.context.getCrawlUrlTest().test(link));
            return collect;
        }
    }
    
    private boolean isToBeCollectedWithMetricsLog(String link, Level level) {
        
        final boolean withinCrawlLimit;
        Boolean startsWithBaseUrl = null;
        Boolean alreadyAttempted = null;
        Boolean alreadyCollected = null;
        Boolean filterAccepted = null;
        
        final boolean collect = 
                (withinCrawlLimit = this.isWithinCrawlLimit()) && 
                (startsWithBaseUrl = this.linkStartsWithBaseUrlTest.test(link)) &&
                !(alreadyAttempted = this.isAttempted(link)) && 
                !(alreadyCollected = links.contains(link)) &&
                (filterAccepted = this.context.getCrawlUrlTest().test(link));
        
            LOG.log(level, "Collect: " + collect + ", within crawl limit: " + withinCrawlLimit + 
                ", starts with BaseURL: " + startsWithBaseUrl + ", collected: " + alreadyCollected + 
                ", attempted: " + alreadyAttempted + ", collected: " + alreadyCollected + 
                ", filter rejected: " + (filterAccepted == null ? null : !filterAccepted) + "\n" + link);
            
        return collect;    
    }

    public boolean mayProceed() {
        final boolean mayProceed = !this.isTimedout() && this.isWithinFailLimit();
        LOG.finer(() -> "May proceed: " + mayProceed);
        return mayProceed;
    }

    public boolean isAttempted(String link) {
        final boolean alreadyAttempted = this.attempted.contains(link);
        LOG.log(alreadyAttempted?Level.FINER:Level.FINEST, "Already attempted: {0}", link);  
        final ResumeHandler resumeHandler = context.getResumeHandler();
        return alreadyAttempted || resumeHandler.isExisting(link); 
    }
    
    public boolean isTimedout() {
        return this.getTimeSpentMillis() > this.context.getTimeoutMillis();
    }
    
    public long getTimeLeftMillis(int outputIfNone) {
        final long timeLeftMillis = context.getTimeoutMillis() - this.getTimeSpentMillis();
        return timeLeftMillis < 0 ? outputIfNone : timeLeftMillis;
    }
    
    @Override
    public long getTimeSpentMillis() {
        return this.startTime < 0 ? 0 : System.currentTimeMillis() - startTime;
    }

    private synchronized void waitBeforeNextBatch(long interval) {
        try {
            if (interval > 0L) {
             
                final long mb4 = com.bc.util.Util.availableMemory();
                LOG.finer(() -> "Waiting for "+interval+" milliseconds, free memory: " + mb4);

                wait(interval);

                LOG.finer(() -> "Done waiting for "+interval+" milliseconds, memory used: " + 
                    (Util.usedMemory(mb4)));
            }
        } catch (InterruptedException e) {
            LOG.log(Level.FINE, "Crawler interrupted waiting before next batch. Crawler: \n" + this, e);
        } finally {
            notifyAll();
        }
    }

    public String waitAndGetFirstLink(String outputIfNone) {
        return this.waitForFirstLink(false, outputIfNone);
    }
    
    public String waitAndRemoveFirstLink(String outputIfNone) {
        return this.waitForFirstLink(true, outputIfNone);
    }
    
    public String waitForFirstLink(boolean remove, String outputIfNone) {
        try{
            final long tb4 = System.currentTimeMillis();
            final long timeLeftMillis = this.getTimeLeftMillis(0);
            final String link;
            if(timeLeftMillis <= 0L) {
                link = links.isEmpty() ? outputIfNone : remove ? links.poll() : links.peek();
            }else if(!links.isEmpty()) {
                link = remove ? links.poll() : links.peek();
            }else{
                
                LOG.finer(() -> "Will wait at most " + timeLeftMillis + " millis for link at position zero");
                
                link = remove ? 
                        links.poll(timeLeftMillis, TimeUnit.MILLISECONDS) :
                        this.peek(timeLeftMillis, TimeUnit.MILLISECONDS);
                
                LOG.finer(() -> "Waited " + (System.currentTimeMillis() - tb4) + 
                        " millis for link at position zero: " + link);
            }
            return link == null ? outputIfNone : link;
        }catch(InterruptedException e) {
            LOG.log(Level.WARNING, null, e);
            return outputIfNone;
        }
    }

    public String peek(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final LinkedBlockingQueueWithTimedPeek<String> queue = 
                (LinkedBlockingQueueWithTimedPeek)links.getFirstNonEmptyQueue(links.getPageAt(0));
        return queue.peek(timeout, timeUnit);
    }

    @Override
    public int getFailed() {
        return this.failed.size();
    }

    @Override
    public int getRemaining() {
        return this.links.size();
    }

    @Override
    public int getCrawled() {
        return crawled;
    }

    @Override
    public int getAttempted() {
        return this.attempted.size();
    }

    @Override
    public Set<String> getFailedLinks() {
        return Collections.unmodifiableSet(this.failed);
    }

    @Override
    public List<String> getRemainingLinks() {
        return Arrays.asList(links.toArray(new String[0]));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        print(builder);
        return builder.toString();
    }

    public void print(StringBuilder builder) {
        builder.append(getClass().getName());
        builder.append('@').append(Integer.toHexString(this.hashCode()));
        builder.append("{\nTime: ").append(this.getTimeSpentMillis()).append('/').append(context.getTimeoutMillis());
        builder.append(". URLs:: attempted: ").append(this.getAttempted());
        builder.append(", failed: ").append(this.getFailed());
        builder.append(", remaining: ").append(this.getRemaining());
        builder.append('\n').append(context);
        builder.append('\n').append('}');
    }
}
