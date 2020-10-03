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

import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.links.LinkCollectionContext;
import com.bc.webcrawler.links.LinkCollectionContextBuilder;
import com.bc.webcrawler.links.LinkCollector;
import com.bc.webcrawler.links.LinksExtractor;
import com.bc.webcrawler.predicates.ParseUrlTest;
import com.bc.webcrawler.predicates.PreferredLinkTest;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 5:59:31 PM
 */
public interface CrawlerContextBuilder<E> extends LinkCollectionContextBuilder<E>{
    
    CrawlerContextBuilder<E> linkCollectionContext(LinkCollectionContext<E> linkCollectionContext);
    
    CrawlerContextBuilder<E> baseUrl(String url);

    CrawlerContextBuilder<E> batchInterval(long batchInterval);

    CrawlerContextBuilder<E> batchSize(int batchSize);

    @Override
    CrawlerContext<E> build();

    CrawlerContextBuilder<E> linkCollector(LinkCollector<E> linkCollector);
    
    CrawlerContextBuilder<E> maxFailsAllowed(int maxFailsAllowed);

    CrawlerContextBuilder<E> parseLimit(long parseLimit);
    
    CrawlerContextBuilder<E> preferredLinkTest(PreferredLinkTest preferredLinkTest);

    CrawlerContextBuilder<E> retryOnExceptionTestSupplier(RetryOnExceptionTestSupplier retryOnExceptionTestSupplier);

    CrawlerContextBuilder<E> pageIsNoIndexTest(Predicate<E> pageIsNoIndexTest);
    
    CrawlerContextBuilder<E> pageIsNoFollowTest(Predicate<E> pageIsNoFollowTest);
    
    CrawlerContextBuilder<E> timeoutMillis(long timeoutMillis);

    CrawlerContextBuilder<E> urlFormatter(UrlFormatter urlFormatter);

    CrawlerContextBuilder<E> urlParser(UrlParser urlParser);

    CrawlerContextBuilder<E> parseUrlTest(ParseUrlTest urlTest);
    
    //
    //

    @Override
    public CrawlerContextBuilder<E> contentTypeRequest(ContentTypeRequest connProvider);

    @Override
    public CrawlerContextBuilder<E> crawlUrlTest(CrawlUrlTest urlTest);

    @Override
    public CrawlerContextBuilder<E> resumeHandler(ResumeHandler resumeHandler);

    @Override
    public CrawlerContextBuilder<E> linksExtractor(LinksExtractor linksExtractor);

    @Override
    public CrawlerContextBuilder<E> crawlLimit(long crawlLimit);

    @Override
    public CrawlerContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection);

    @Override
    public CrawlerContextBuilder<E> attemptedLinkBuffer(Buffer<String> buffer);
    
}
