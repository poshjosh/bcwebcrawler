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

import com.bc.webcrawler.CrawlerContext;
import com.bc.webcrawler.ResumeHandler;
import com.bc.webcrawler.predicates.LinkStartsWithTargetLinkTest;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2018 3:01:44 AM
 */
public class LinkCollectorImpl<E> implements LinkCollector<E> {

    private transient static final Logger LOG = Logger.getLogger(LinkCollectorImpl.class.getName());

    private final CrawlerContext<E> context;
    
    private final Predicate<String> isLinkAttempted;
    
    private final Consumer<String> accumulator;

    private final Predicate<String> linkStartsWithBaseUrlTest;
    
    private boolean shutdown;

    private int crawled;

    public LinkCollectorImpl(
            CrawlerContext<E> context, 
            Predicate<String> isLinkAttempted, 
            Consumer<String> accumulator, 
            String baseUrl) {

        this.context = Objects.requireNonNull(context);
        
        this.isLinkAttempted = Objects.requireNonNull(isLinkAttempted);
        
        this.accumulator = Objects.requireNonNull(accumulator);
        
        try{
            this.linkStartsWithBaseUrlTest = new LinkStartsWithTargetLinkTest(baseUrl);
        }catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
    
    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        this.shutdown = true;
    }

    @Override
    public void collectLinks(String url, E doc) {
        
        LOG.finer(() -> MessageFormat.format("Collected: {0}, collecting: {1}", this.getCollected(), url));
    
        final Set<String> docLinks = this.context.getLinksExtractor().apply(doc);
        
        LOG.finer(() -> "Extracted: "+docLinks.size()+" links from: " + url);
        
        if(docLinks.isEmpty()) {
            LOG.fine(() -> "No links to collected from: " + url);
        }else{
            this.collectLinks(url, docLinks);
        }
    }
    
    @Override
    public int collectLinks(String url, Set<String> linksToCollect) {
        int collected = 0;
        for(String link : linksToCollect) {
            if(this.collectLink(link)) {
                ++collected;
            }
        }
        final int n = collected;
        LOG.finer(() -> "Collected: " + n + " links from: " + url);
        return collected;
    }

    @Override
    public boolean collectLink(String linkToCollect) {

        boolean collected = false;
        
        final boolean collect = this.isToBeCollected(linkToCollect);
        
        LOG.finer(() -> "Will be collected: " + collect + ", URL: " + linkToCollect);
        
        if(collect){

            LOG.finest(() -> "Collecting " + linkToCollect);
            
            this.accumulator.accept(linkToCollect);
            
            collected = true;

            ++crawled;
        }
        
        return collected;
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
        final ResumeHandler resumeHandler = context.getResumeHandler();
        return alreadyAttempted || resumeHandler.isExisting(link); 
    }

    @Override
    public int getCollected() {
        return crawled;
    }
}
