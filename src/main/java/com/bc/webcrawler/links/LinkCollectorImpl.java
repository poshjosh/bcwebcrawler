/*
 * Copyright 2018 NUROX Ltd.
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

package com.bc.webcrawler.links;

import com.bc.util.UrlUtil;
import com.bc.webcrawler.util.Buffer;
import com.bc.webcrawler.predicates.SameHostTest;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.webcrawler.util.Store;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2018 3:01:44 AM
 */
public class LinkCollectorImpl<E> implements LinkCollector<E> {

    private transient static final Logger LOG = Logger.getLogger(LinkCollectorImpl.class.getName());

    private final LinkCollectionContext<E> context;
    
    private final Predicate<String> isLinkAttempted;
    
    private final Predicate<String> linkStartsWithBaseUrlTest;
    
    private boolean shutdownAttempted;
    
    private boolean shutdown;

    private int crawled;

    public LinkCollectorImpl(LinkCollectionContext<E> context, String baseUrl) {
        
        this.context = Objects.requireNonNull(context);
        
        final Buffer<String> buffer = Objects.requireNonNull(context.getAttemptedLinkBuffer());
        
        this.isLinkAttempted = (link) -> buffer.contains(link);
        
        try{
            this.linkStartsWithBaseUrlTest = new SameHostTest(baseUrl);
        }catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean isShutdownAttempted() {
        return shutdownAttempted;
    }
    
    @Override
    public boolean isShutdown() {
        return shutdown;
    }
    
    @Override
    public final void shutdown(long timeout, TimeUnit timeUnit) {

        if(this.isShutdownAttempted()) {
            return;
        }
        
        this.shutdownAttempted = true;
        LOG.fine(() -> "Shutting down");
        this.doShutdown(timeout, timeUnit);
        this.shutdown = true;
    }

    protected void doShutdown(long timeout, TimeUnit timeUnit) { }

    @Override
    public void collectLinks(E doc, Consumer<String> consumer) {

        if(this.isShutdownAttempted()) {

            return;
        }
        
        LOG.finer(() -> MessageFormat.format("Collected: {0}, collecting: {1}", this.getCollected(), doc));
    
        final Set<String> docLinks = this.context.getLinksExtractor().apply(doc);
        
        LOG.finer(() -> "Extracted: "+docLinks.size()+" links from: " + doc);
        
        if(docLinks.isEmpty()) {
            LOG.fine(() -> "No links to collected from: " + doc);
        }else{
            this.collectLinks(docLinks, consumer);
            LOG.finer(() -> "Collected links from: " + doc);
        }
    }
    
    @Override
    public void collectLinks(Set<String> linksToCollect, Consumer<String> consumer) {

        if(this.isShutdownAttempted()) {

            LOG.finer("Shutdown. Exiting");

            return;
        }
        
        for(String link : linksToCollect) {
            
            if(this.isShutdownAttempted()) {

                LOG.finer("Shutdown. Exiting");
            
                break;
            }
            
            this.collectLink(link, consumer);
        }
    }

    public boolean collectLink(final String s, Consumer<String> consumer) {
        
        boolean collected = false;
        
        if(this.isShutdownAttempted()) {
            
            LOG.finer("Shutdown. Exiting");
            
            return collected;
        }
        
        final String linkToCollect = this.formatLink(s);
        
        final boolean collect = this.isToBeCollected(linkToCollect);
        
        LOG.finer(() -> "Will be collected: " + collect + ", URL: " + linkToCollect);
        
        if(collect){

            LOG.finest(() -> "Collecting " + linkToCollect);
            
            consumer.accept(linkToCollect);
            
            collected = true;

            ++crawled;
        }
        
        return collected;
    }
    
    public String formatLink(String link) {
        return UrlUtil.removeHashPart(link);
    }
    
    @Override
    public boolean isToBeCollected(String link) {
        if(LOG.isLoggable(Level.FINER)) {
            return this.isToBeCollectedWithMetricsLog(link, Level.FINER);
        }else{
            final boolean collect = 
                    (this.isWithinCollectLimit()) && 
                    (this.linkStartsWithBaseUrlTest.test(link)) &&
                    !(this.isAttempted(link)) && 
                    (this.context.getCrawlUrlTest().test(link));
            return collect;
        }
    }
    
    private boolean isToBeCollectedWithMetricsLog(String link, Level level) {
        
        final boolean withinCrawlLimit;
        Boolean startsWithBaseUrl = null;
        Boolean alreadyAttempted = null;
        Boolean filterAccepted = null;
        
        final boolean collect = 
                (withinCrawlLimit = this.isWithinCollectLimit()) && 
                (startsWithBaseUrl = this.linkStartsWithBaseUrlTest.test(link)) &&
                !(alreadyAttempted = this.isAttempted(link)) && 
                (filterAccepted = this.context.getCrawlUrlTest().test(link));
        
        if(LOG.isLoggable(level)) {
            LOG.log(level, "To be collected: {0}, within crawl limit: {1}, starts with baseURL: {2}, already attempted: {3}, filter rejected: {4}\nLink: {5}", 
                    new Object[]{collect, withinCrawlLimit, startsWithBaseUrl,
                    alreadyAttempted, (filterAccepted == null ? null : !filterAccepted), link});
        }
            
        return collect;    
    }

    @Override
    public boolean isWithinCollectLimit() {
        final boolean withinLimit = isWithLimit(this.crawled, this.context.getCrawlLimit());
        LOG.finest(() -> MessageFormat.format("Crawl: {0}, crawl limit: {1}, within crawl limit: {2}", 
                this.crawled, this.context.getCrawlLimit(), withinLimit));
        return withinLimit;
    }

    protected boolean isWithLimit(long offset, long limit) {
        boolean withinLimit = true;
        if (limit > 0) {
            withinLimit = offset < limit;
        }
        return withinLimit;
    }

    public boolean isAttempted(String link) {
        final boolean alreadyAttempted = this.isLinkAttempted.test(link);
        LOG.log(alreadyAttempted?Level.FINER:Level.FINEST, "Already attempted: {0}", link);  
        final Store<String> linkStore = context.getLinkStore();
        return alreadyAttempted || linkStore.contains(link); 
    }

    @Override
    public int getCollected() {
        return crawled;
    }
}
