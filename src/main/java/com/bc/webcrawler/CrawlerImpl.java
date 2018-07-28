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
import com.bc.util.ListPages;
import com.bc.util.Util;
import com.bc.util.concurrent.NamedThreadFactory;
import com.bc.webcrawler.predicates.LinkStartsWithTargetLinkTest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class CrawlerImpl<E> implements Serializable, 
        Iterator<E>, Crawler<E>, CrawlMetaData {
    
    private static final Logger LOG = Logger.getLogger(CrawlerImpl.class.getName());
    
    private final String startUrl;
    
    private final String baseUrl;
    
    private final List<String> seedUrls;

    private final CrawlerContext<E> context;

    private final Set<String> attempted;

    private final Set<String> failed;

    private final ListPages<String> links;

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
        
        this.links = new ListPages(this.seedUrls, new ArrayList());
        
        this.startUrl = this.links.get(0);
        
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

        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        final String threadPoolName = this.getClass().getName() + "-LinkCollectionThreadPool";

        this.linkCollectionExecSvc = Executors.newFixedThreadPool(
                numberOfThreads, new NamedThreadFactory(threadPoolName));
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
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
        this.seedUrls.clear();
        this.attempted.clear();
        this.failed.clear();
        this.robotsExcludedLinks.clear();
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
        return links.isEmpty() ? Optional.empty() : Optional.of(links.get(0));
    }

    @Override
    public CrawlMetaData getMetaData() {
        return this;
    }
    
    protected void preParse(String url) { }

    protected void postParse(String url, E doc) { }

    @Override
    public boolean hasNext() {

        boolean hasNext = false;
        
        final Predicate<String> parseUrlTest = this.context.getParseUrlTest();
        
        while(this.mayParseNext()) {
            
            final String next = this.links.get(0);
            
            if(parseUrlTest.test(next)) {
                
                hasNext = true;
                
                break;
            }
            
            LOG.fine(() -> "Rejected by Parse URL Filter: " + 
                    parseUrlTest.getClass().getName() + ", URL: " + next);
            
            links.remove(0);
        }

        final Level level = hasNext ? Level.FINEST : Level.FINE;

        LOG.log(level, "HasNext: {0}", hasNext);

        return hasNext;
    }
    
    @Override
    public E parseNext() throws IOException {

        if(this.startTime == -1L) {
            this.startTime = System.currentTimeMillis();
        }
        
        if(!this.mayParseNext()) {
            throw new IllegalStateException("Operation not allowed since #hasNext() returns false");
        }
        
        final int batchSize = this.context.getBatchSize();

        if ((batchSize > 0) && (++this.indexWithinBatch >= batchSize)) {

            this.indexWithinBatch = 0;

            waitBeforeNextBatch(context.getBatchInterval());
        }

        final String rawUrl = links.get(0);

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
                doc = parse(url);
            }catch(IOException e) {
                if(url.contains("&amp;")) {
                    url = url.replace("&amp;", "&");
                    doc = parse(url);
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
                    
                    final E myRef = doc;
                    this.linkCollectionExecSvc.submit(() -> this.collectLinks(myRef));
                }
            }

            postParse(url, doc);

        }catch (IOException | RuntimeException e){

            boolean added = this.failed.add(rawUrl);

            if(added) { 
                LOG.warning(() -> "Parse failed for: " + rawUrl + ". Reason: " + e.toString());
            }

            throw e;
            
        }finally{

            this.links.remove(0);
        }

        return doc;
    }

    public boolean mayParseNext() {
        return this.mayProceed() &&
                isWithinParseLimit() && 
                !this.links.isEmpty();
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

    public E parse(String link) throws MalformedURLException, IOException {

        LOG.fine(() -> MessageFormat.format("Pages left: {0}, crawling: {1}", 
                this.getRemaining(), link));
        
//        System.out.println(this + "\nCrawling: " + link);

        E doc;

        try {

            doc = this.context.getUrlParser().parse(link);

        }catch (IOException e) {

            final boolean retry = this.context.getRetryOnExceptionTestSupplier().get().test(e);

            if (retry) {

                doc = parse(link);

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

    public int collectLinks(E doc) {
        
        LOG.finest(() -> "Extracting links from doc: " + doc);
        
        final Set<String> docLinks = this.context.getLinksExtractor().apply(doc);
        LOG.finer(() -> "Extracted: "+docLinks.size()+" links from doc: " + doc);
        
        final int collected = docLinks.isEmpty() ? 0 : this.collectLinks(docLinks);
        LOG.fine(() -> "Collected: "+collected+" links from doc: " + doc);
        
        return collected;
    }
    
    public int collectLinks(Set<String> linkSet) {
        int collected = 0;
        for(String link : linkSet) {
            if(this.collectLink(link)) {
                ++collected;
            }
        }
        return collected;
    }

    public boolean collectLink(String link) {

        boolean collected = false;
        
        final boolean collect = this.isToBeCollected(link);
        
        LOG.finer(() -> "Will be collected: " + collect + ", URL: " + link);
        
        if(collect){

            final List<String> target;
            final boolean isPreferredLink = this.context.getPreferredLinkTest().test(link);
            if(isPreferredLink) {
                target = this.links.getPageAt(0);
            }else{
                target = this.links.getPageAt(1);
            }

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
                    (this.mayProceed()) &&
                    (this.isWithinCrawlLimit()) && 
                    (this.linkStartsWithBaseUrlTest.test(link)) &&
                    !(this.isAttempted(link)) && 
                    !(links.contains(link)) &&
                    (this.context.getCrawlUrlTest().test(link));
            return collect;
        }
    }
    
    private boolean isToBeCollectedWithMetricsLog(String link, Level level) {
        
        final boolean mayProceed;
        Boolean withinCrawlLimit = null;
        Boolean startsWithBaseUrl = null;
        Boolean alreadyAttempted = null;
        Boolean alreadyCollected = null;
        Boolean filterAccepted = null;
        
        final boolean collect = 
                (mayProceed = this.mayProceed()) &&
                (withinCrawlLimit = this.isWithinCrawlLimit()) && 
                (startsWithBaseUrl = this.linkStartsWithBaseUrlTest.test(link)) &&
                !(alreadyAttempted = this.isAttempted(link)) && 
                !(alreadyCollected = links.contains(link)) &&
                (filterAccepted = this.context.getCrawlUrlTest().test(link));
        
//        if(!collect) 
            LOG.log(level, "Collect: " + collect + 
                ", proceed: " + mayProceed + ", within crawl limit: " + withinCrawlLimit + 
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
    
    @Override
    public long getTimeSpentMillis() {
        return this.startTime < 0 ? 0 : System.currentTimeMillis() - startTime;
    }

    private synchronized void waitBeforeNextBatch(long interval) {
        try {
            if (interval > 0L) {
             
                final long mb4 = com.bc.util.Util.availableMemory();
                LOG.fine(() -> "Waiting for "+interval+" milliseconds, free memory: " + mb4);

                wait(interval);

                LOG.fine(() -> "Done waiting for "+interval+" milliseconds, memory used: " + 
                    (Util.usedMemory(mb4)));
            }
        } catch (InterruptedException e) {
            LOG.log(Level.FINE, "Crawler interrupted waiting before next batch. Crawler: \n" + this, e);
        } finally {
            notifyAll();
        }
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
        return Collections.unmodifiableList(this.links);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        print(builder);
        return builder.toString();
    }

    public void print(StringBuilder builder) {
        builder.append(getClass().getName());
        builder.append('@').append(this.hashCode());
        builder.append("{\nTime spent: ").append(this.getTimeSpentMillis());
        builder.append(". URLs:: attempted: ").append(this.getAttempted());
        builder.append(", failed: ").append(this.getFailed());
        builder.append(", remaining: ").append(this.getRemaining());
        builder.append('\n').append('}');
    }
}
