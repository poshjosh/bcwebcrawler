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

import com.bc.webcrawler.links.LinkCollector;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import com.bc.util.Util;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class CrawlerImpl<E> implements Serializable, 
        Crawler<E>, CrawlSnapshot {
    
    private transient static final Logger LOG = Logger.getLogger(CrawlerImpl.class.getName());
    
    private final CrawlerContext<E> context;

    private final Buffer<String> attempted;

    private final Set<String> failed;

    private final BlockingQueue<String> linkQueue;

    private final Set<String> robotsExcludedLinks;
    
    private final LinkCollector<E> linkCollector;
    
    private final Consumer<String> collectCrawledLink;
    
    private int indexWithinBatch;
    
    private long startTime = -1L;
    
    private boolean shutdownAttempted;
    
    private String currentLink;

    public CrawlerImpl(CrawlerContext<E> context, Set<String> seedUrls) {

        LOG.finer("Creating");
        
        this.context = Objects.requireNonNull(context);
        
        if(seedUrls.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        final int crawlLimit = (int)this.context.getCrawlLimit();
        
        final int initialCapacity = Math.max(seedUrls.size(), crawlLimit);
        this.linkQueue = new PriorityBlockingQueue(
                (initialCapacity <= 0 ? 1 : initialCapacity), new ComparatorForPredicate(context.getPreferredLinkTest()));
        
        this.linkQueue.addAll(seedUrls);
        
        LOG.fine(() -> "URL queue capacity: " + crawlLimit +
                ", " + seedUrls.size() + " seed URLs");
        
        this.attempted = Objects.requireNonNull(context.getAttemptedLinkBuffer());

        this.collectCrawledLink = (link) -> linkQueue.add(link);

        this.linkCollector = Objects.requireNonNull(context.getLinkCollector());

        this.failed = new HashSet();

        this.robotsExcludedLinks = new HashSet<>();
        
        LOG.fine(() -> "Done creating: " + this);
    }

    public boolean isShutdownAttempted() {
        return shutdownAttempted;
    }

    @Override
    public boolean isShutdown() {
        return this.linkCollector.isShutdown();
    }
    
    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        
        if(this.isShutdownAttempted()) {
            return;
        }
        
        this.shutdownAttempted = true;
        
        LOG.log(Level.FINE, "SHUTTING DOWN {0}", this);
        
        this.linkCollector.shutdown(timeout, timeUnit);
        
        this.context.getAttemptedLinkBuffer().delete();
    }
    
    @Override
    public Optional<String> getCurrentUrl() {
        return Optional.ofNullable(this.getCurrentUrl(null));
    }

    @Override
    public CrawlSnapshot getSnapshot() {
        return this;
    }
    
    protected void preParse(String url) { }

    protected void postParse(String url, E doc) { }

    @Override
    public boolean hasNext() {

        if(this.startTime < 0) {
            this.startTime = System.currentTimeMillis();
        }
        
        boolean hasNext = false;
        
        final Predicate<String> parseUrlTest = this.context.getParseUrlTest();
        
        while(this.mayParseNext()) {
            
            final String next = this.waitAndRemoveFirstLink(null);
            
            if(next == null) {
                break;
            }
            
            if(parseUrlTest.test(next)) {
                
                hasNext = true;
                
                break;
                
            }else{
            
                LOG.log(Level.FINE, "Rejected by ParseURLTest, URL: ", next);

                linkQueue.poll();
            }
        }

        final Level level = hasNext ? Level.FINEST : Level.FINE;

        LOG.log(level, "HasNext: {0}", hasNext);

        return hasNext;
    }
    
    @Override
    public E parseNext() throws IOException {

        if(this.startTime < 0) {
            this.startTime = System.currentTimeMillis();
        }

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

        final String nextLink = this.getCurrentLink();
        
        final String rawUrl = nextLink == null ? this.waitAndRemoveFirstLink(null) : nextLink;
        
        if(rawUrl == null) {
            
            return null;
        }

        String url = null;

        E doc;

        try {

            url = this.context.getUrlFormatter().apply(rawUrl);

            preParse(rawUrl);

            if(LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Raw: {0}\nURL: {1}", new Object[]{rawUrl, url});
            }

            this.attempted.add(url);

            try{

                doc = parse(url, this.context.getRetryOnExceptionTestSupplier().get());
                
                this.context.getResumeHandler().saveIfNotExists(url);
                
            }catch(IOException e) {
//                if(url.contains("&amp;")) {
//                    url = url.replace("&amp;", "&");
//                    doc = parse(url, this.context.getRetryOnExceptionTestSupplier().get());
//                }else{
//                    throw e;
//                }
                throw e;
            }
            
            if(doc != null) {
                final boolean alreadyExcluded = this.robotsExcludedLinks.contains(url);
                if (alreadyExcluded || 
                        this.context.getPageIsNoIndexTest().test(doc) ||
                        this.context.getPageIsNoFollowTest().test(doc)) {
                    if(!alreadyExcluded) {
                        this.robotsExcludedLinks.add(url);
                    }
                }else{
                    this.linkCollector.collectLinks(doc, this.collectCrawledLink);
                }
            }

            postParse(url, doc);

        }catch (IOException | RuntimeException e){

            final String addToFailed = url == null ? rawUrl : url;
            
            final boolean added = this.failed.add(addToFailed);

            if(added) { 
                LOG.warning(() -> "Parse failed for: " + addToFailed + ". Reason: " + e.toString());
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
        final boolean withinLimit = isWithLimit(this.getCrawled(), this.context.getCrawlLimit());
        LOG.finest(() -> MessageFormat.format("URLs: {0}, limit: {1}, within crawl limit: {2}", 
                this.linkQueue.size(), this.context.getCrawlLimit(), withinLimit));
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

    @Override
    public boolean isWithinTimeLimit() {
        return ! this.isTimedout();
    }

    public E parse(String link, Predicate<Throwable> test) 
            throws MalformedURLException, IOException {

        LOG.finer(() -> MessageFormat.format("Pages left: {0}, parsing: {1}", 
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

    public boolean mayProceed() {
        final boolean mayProceed = !this.isTimedout() && this.isWithinFailLimit();
        LOG.finer(() -> "May proceed: " + mayProceed);
        return mayProceed;
    }
    
    public String waitAndRemoveFirstLink(String resultIfNone) {

        final long timeoutMillis = this.getPollTimeout();
        LOG.fine(() -> "Poll timeout: " + timeoutMillis);
        
        final long tb4 = System.currentTimeMillis();
        final String link;
        if(timeoutMillis <= 0L) {
            link = linkQueue.isEmpty() ? resultIfNone : linkQueue.poll();
        }else if( ! linkQueue.isEmpty()) {
            link = linkQueue.poll();
        }else{

            LOG.finer(() -> "Queue size: " + linkQueue.size() + 
                    ", will wait at most " + timeoutMillis + " millis for next link.");

            try{
                link = linkQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            }catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            LOG.fine(() -> "Queue size: " + linkQueue.size() + ", waited " + 
                    (System.currentTimeMillis() - tb4) + " millis for link: " + link);
        }
        
        final String result = link == null ? resultIfNone : link;
        
        this.setCurrentLink(result);
        
        return result;
    }

    public long getPollTimeout() {
        return this.context.isAsyncLinkCollection() ? this.getTimeLeftMillis(0) : 1;
    }
    
    public boolean isTimedout() {
        return this.getTimeSpentMillis() > this.context.getTimeoutMillis();
    }
    
    public long getTimeLeftMillis(long outputIfNone) {
        final long timeLeftMillis = this.context.getTimeoutMillis() - this.getTimeSpentMillis();
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

    @Override
    public String getCurrentUrl(String resultIfNone) {
        final String next = this.getCurrentLink();
        return next == null ? resultIfNone : next;
    }
    
    public void setCurrentLink(String s) {
        currentLink = s;
    }
    
    public String getCurrentLink() {
        return currentLink;
    }

    public BlockingQueue<String> getLinkQueue() {
        return linkQueue;
    }

    @Override
    public int getFailed() {
        return this.failed.size();
    }

    @Override
    public int getRemaining() {
        return this.linkQueue.size();
    }

    @Override
    public int getCrawled() {
        return this.linkCollector.getCollected();
    }

    @Override
    public int getAttempted() {
        return this.attempted.isDeleted() ? -1 : this.attempted.size();
    }

    @Override
    public Set<String> getFailedLinks() {
        return Collections.unmodifiableSet(this.failed);
    }

    @Override
    public List<String> getRemainingLinks() {
        return Arrays.asList(this.linkQueue.toArray(new String[0]));
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

