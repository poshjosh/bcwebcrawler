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

import com.bc.webcrawler.links.LinkCollectionContextBuilderImpl;
import com.bc.net.RetryConnectionFilter;
import com.bc.webcrawler.links.LinkCollectionContext;
import com.bc.webcrawler.links.LinkCollector;
import com.bc.webcrawler.links.LinkCollectorAsync;
import com.bc.webcrawler.links.LinkCollectorImpl;
import com.bc.webcrawler.links.LinksExtractor;
import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.predicates.ParseUrlTest;
import com.bc.webcrawler.predicates.PreferredLinkTest;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2017 8:05:54 PM
 */
public class CrawlerContextBuilderImpl<E> extends LinkCollectionContextBuilderImpl<E> 
        implements CrawlerContext<E>, CrawlerContextBuilder<E> {

//    private static final Logger LOG = Logger.getLogger(CrawlerContextBuilderImpl.class.getName());

    private boolean buildAttempted;
    
    private int maxFailsAllowed = Integer.MAX_VALUE;
    
    private int batchSize = 10;
    private long batchInterval = 10_000;
    private long parseLimit = Long.MAX_VALUE;
    private long timeoutMillis = Long.MAX_VALUE;
    private String baseUrl;
    
    private ParseUrlTest parseUrlTest = (link) -> true;
    private UrlFormatter urlFormatter = (link) -> link;
    private RetryOnExceptionTestSupplier retryOnExceptionTestSupplier;
    private Predicate<E> pageIsNoIndexTest = (doc) -> false;
    private Predicate<E> pageIsNoFollowTest = (doc) -> false;
    private LinkCollector<E> linkCollector;
    private ContentTypeRequest contentTypeRequest;
    private UrlParser<E> urlParser;
    
    private PreferredLinkTest preferredLinkTest = (link) -> false;
    
    public CrawlerContextBuilderImpl() { 
        this.retryOnExceptionTestSupplier = () -> new RetryConnectionFilter(2, 2_000);
    }
    
    @Override
    public CrawlerContext build() {
        
        this.checkBuildAttempted();
        
        this.buildAttempted = true;

        super.build();
        
        Objects.requireNonNull(this.urlParser);
        
        if(this.linkCollector == null) {
            this.linkCollector = isAsyncLinkCollection() ? 
                    new LinkCollectorAsync(this, baseUrl, 1, (int)this.getCrawlLimit()) :
                    new LinkCollectorImpl(this, baseUrl);
        }

        return this;
    }

    private void checkBuildAttempted() {
        if(this.buildAttempted) {
            throw new IllegalStateException("build() method may only be called once");
        }
    }

    @Override
    public CrawlerContextBuilder<E> linkCollectionContext(LinkCollectionContext<E> c) {
        this.attemptedLinkBuffer(c.getAttemptedLinkBuffer());
        this.contentTypeRequest(c.getContentTypeRequest());
        this.crawlLimit(c.getCrawlLimit());
        this.crawlUrlTest(c.getCrawlUrlTest());
        this.linksExtractor(c.getLinksExtractor());
        this.asyncLinkCollection(c.isAsyncLinkCollection());
        this.resumeHandler(c.getResumeHandler());
        return this;
    }

    @Override
    public int getMaxFailsAllowed() {
        return maxFailsAllowed;
    }

    @Override
    public CrawlerContextBuilder<E> maxFailsAllowed(int maxFailsAllowed) {
        this.maxFailsAllowed = maxFailsAllowed;
        return this;
    }

    @Override
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    @Override
    public CrawlerContextBuilder<E> timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public CrawlerContextBuilder<E> baseUrl(String baseUrl) {
        this.baseUrl = com.bc.util.UrlUtil.getBaseURL(baseUrl);
        return this;
    }

    @Override
    public PreferredLinkTest getPreferredLinkTest() {
        return preferredLinkTest;
    }

    @Override
    public CrawlerContextBuilder<E> preferredLinkTest(PreferredLinkTest preferredLinkTest) {
        this.preferredLinkTest = preferredLinkTest;
        return this;
    }
    
    @Override
    public ParseUrlTest getParseUrlTest() {
        return parseUrlTest;
    }

    @Override
    public CrawlerContextBuilder<E> parseUrlTest(ParseUrlTest urlTest) {
        this.parseUrlTest = urlTest;
        return this;
    }

    @Override
    public LinkCollector<E> getLinkCollector() {
        return linkCollector;
    }

    @Override
    public CrawlerContextBuilder<E> linkCollector(LinkCollector<E> linkCollector) {
        this.linkCollector = linkCollector;
        return this;
    }

    @Override
    public Predicate<E> getPageIsNoIndexTest() {
        return pageIsNoIndexTest;
    }

    @Override
    public CrawlerContextBuilder<E> pageIsNoIndexTest(Predicate<E> pageIsNoIndexTest) {
        this.pageIsNoIndexTest = pageIsNoIndexTest;
        return this;
    }

    @Override
    public Predicate<E> getPageIsNoFollowTest() {
        return pageIsNoFollowTest;
    }

    @Override
    public CrawlerContextBuilder<E> pageIsNoFollowTest(Predicate<E> pageIsNoFollowTest) {
        this.pageIsNoFollowTest = pageIsNoFollowTest;
        return this;
    }

    @Override
    public ContentTypeRequest getContentTypeRequest() {
        return contentTypeRequest;
    }

    @Override
    public CrawlerContextBuilder<E> contentTypeRequest(ContentTypeRequest connProvider) {
        this.contentTypeRequest = connProvider;
        return this;
    }

    @Override
    public UrlParser<E> getUrlParser() {
        return urlParser;
    }

    @Override
    public CrawlerContextBuilder<E> urlParser(UrlParser urlParser) {
        this.urlParser = urlParser;
        return this;
    }

    @Override
    public long getParseLimit() {
        return parseLimit;
    }

    @Override
    public CrawlerContextBuilder<E> parseLimit(long parseLimit) {
        this.parseLimit = parseLimit;
        return this;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public CrawlerContextBuilder<E> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public long getBatchInterval() {
        return batchInterval;
    }

    @Override
    public CrawlerContextBuilder<E> batchInterval(long batchInterval) {
        this.batchInterval = batchInterval;
        return this;
    }

    @Override
    public UrlFormatter getUrlFormatter() {
        return urlFormatter;
    }

    @Override
    public CrawlerContextBuilder<E> urlFormatter(UrlFormatter urlFormatter) {
        this.urlFormatter = urlFormatter;
        return this;
    }

    @Override
    public RetryOnExceptionTestSupplier getRetryOnExceptionTestSupplier() {
        return retryOnExceptionTestSupplier;
    }

    @Override
    public CrawlerContextBuilder<E> retryOnExceptionTestSupplier(RetryOnExceptionTestSupplier retryOnExceptionTestSupplier) {
        this.retryOnExceptionTestSupplier = retryOnExceptionTestSupplier;
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> crawlLimit(long crawlLimit) {
        super.crawlLimit(crawlLimit); 
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> resumeHandler(ResumeHandler resumeHandler) {
        super.resumeHandler(resumeHandler); 
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> attemptedLinkBuffer(Buffer<String> attemptedLinkBuffer) {
        super.attemptedLinkBuffer(attemptedLinkBuffer); 
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> linksExtractor(LinksExtractor linksExtractor) {
        super.linksExtractor(linksExtractor);
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> crawlUrlTest(CrawlUrlTest urlTest) {
        super.crawlUrlTest(urlTest); 
        return this;
    }

    @Override
    public CrawlerContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection) {
        super.asyncLinkCollection(asyncLinkCollection); 
        return this;
    }

    @Override
    public String toString() {
        return CrawlerContext.class.getName() + '@' + Integer.toHexString(hashCode()) + 
                "{parseLimit=" + parseLimit + 
                ", batchSize=" + batchSize + ", batchInterval=" + batchInterval + 
                ", timeoutMillis=" + timeoutMillis + 
                "\n" + super.toString() +
                '}';
    }
}
