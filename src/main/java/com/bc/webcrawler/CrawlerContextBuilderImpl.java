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

import com.bc.net.RetryConnectionFilter;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2017 8:05:54 PM
 */
public class CrawlerContextBuilderImpl<E> implements CrawlerContext<E>, CrawlerContextBuilder<E> {

    private static final Logger logger = Logger.getLogger(CrawlerContextBuilderImpl.class.getName());

    private boolean buildAttempted;
    
    private final boolean crawlQueryUrls = true;
    
    private final int connectTimeoutForLinkValidation = 5_000;
    private final int readTimeoutForLinkValidation = 10_000;
    
    private int maxFailsAllowed = Integer.MAX_VALUE;
    
    private int batchSize = 10;
    private long batchInterval = 10_000;
    private long parseLimit = Long.MAX_VALUE;
    private long crawlLimit = Long.MAX_VALUE;
    private long timeoutMillis = Long.MAX_VALUE;
    
    private Predicate<String> parseUrlTest = (link) -> true;
    private Predicate<String> crawlUrlTest;
    private UnaryOperator<String> urlFormatter = (link) -> link;
    private Supplier<Predicate<Throwable>> retryOnExceptionTestSupplier;
    private Predicate<E> pageIsNoIndexTest = (doc) -> false;
    private Predicate<E> pageIsNoFollowTest = (doc) -> false;
    private Function<E, Set<String>> linksExtractor;

    private ResumeHandler resumeHandler;
    private ConnectionProvider connectionProvider;
    private UrlParser<E> urlParser;
    
    private Predicate<String> preferredLinkTest = (link) -> false;
    
    public CrawlerContextBuilderImpl() { 
        this.retryOnExceptionTestSupplier = () -> new RetryConnectionFilter(2, 2_000);
    }
    
    @Override
    public CrawlerContext build() {
        
        this.checkBuildAttempted();
        
        if(this.resumeHandler == null) {
            this.resumeHandler = new ResumeHandlerInMemoryCache();
        }

        Objects.requireNonNull(this.urlParser);
        
        if(this.crawlUrlTest == null) {
            
            this.crawlUrlTest = new HtmlLinkIsToBeCrawledTest(
                    connectionProvider,
                    urlParser, 
                    connectTimeoutForLinkValidation, 
                    readTimeoutForLinkValidation, 
                    crawlQueryUrls
            );
        }

        return this;
    }

    private void checkBuildAttempted() {
        if(this.buildAttempted) {
            throw new IllegalStateException("build() method may only be called once");
        }
        this.buildAttempted = true;
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
    public Predicate<String> getPreferredLinkTest() {
        return preferredLinkTest;
    }

    @Override
    public CrawlerContextBuilder<E> preferredLinkTest(Predicate<String> preferredLinkTest) {
        this.preferredLinkTest = preferredLinkTest;
        return this;
    }
    
    @Override
    public Predicate<String> getParseUrlTest() {
        return parseUrlTest;
    }

    @Override
    public CrawlerContextBuilder<E> parseUrlTest(Predicate<String> urlTest) {
        this.parseUrlTest = urlTest;
        return this;
    }
    
    @Override
    public Predicate<String> getCrawlUrlTest() {
        return crawlUrlTest;
    }

    @Override
    public CrawlerContextBuilder<E> crawlUrlTest(Predicate<String> urlTest) {
        this.crawlUrlTest = urlTest;
        return this;
    }

    @Override
    public Function<E, Set<String>> getLinksExtractor() {
        return linksExtractor;
    }

    @Override
    public CrawlerContextBuilder<E> linksExtractor(Function<E, Set<String>> linksExtractor) {
        this.linksExtractor = linksExtractor;
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
    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public CrawlerContextBuilder<E> connectionProvider(ConnectionProvider connProvider) {
        this.connectionProvider = connProvider;
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
    public ResumeHandler getResumeHandler() {
        return resumeHandler;
    }

    @Override
    public CrawlerContextBuilder<E> resumeHandler(ResumeHandler resumeHandler) {
        this.resumeHandler = resumeHandler;
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
    public long getCrawlLimit() {
        return crawlLimit;
    }

    @Override
    public CrawlerContextBuilder<E> crawlLimit(long crawlLimit) {
        this.crawlLimit = crawlLimit;
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
    public UnaryOperator<String> getUrlFormatter() {
        return urlFormatter;
    }

    @Override
    public CrawlerContextBuilder<E> urlFormatter(UnaryOperator<String> urlFormatter) {
        this.urlFormatter = urlFormatter;
        return this;
    }

    @Override
    public Supplier<Predicate<Throwable>> getRetryOnExceptionTestSupplier() {
        return retryOnExceptionTestSupplier;
    }

    @Override
    public CrawlerContextBuilder<E> retryOnExceptionTestSupplier(Supplier<Predicate<Throwable>> retryOnExceptionTestSupplier) {
        this.retryOnExceptionTestSupplier = retryOnExceptionTestSupplier;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getName() + '@' + Integer.toHexString(hashCode()) + 
                "{crawlQueryUrls=" + crawlQueryUrls + 
                ", parseLimit=" + parseLimit + ", crawlLimit=" + crawlLimit + 
                ", batchSize=" + batchSize + ", batchInterval=" + batchInterval + 
                ", timeoutMillis=" + timeoutMillis + 
                ", connectTimeoutForLinkValidation=" + connectTimeoutForLinkValidation + 
                ", readTimeoutForLinkValidation=" + readTimeoutForLinkValidation + 
                '}';
    }
}
