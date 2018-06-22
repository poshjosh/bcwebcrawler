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

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 5:59:31 PM
 */
public interface CrawlerContextBuilder<E> {

    CrawlerContextBuilder<E> batchInterval(long batchInterval);

    CrawlerContextBuilder<E> batchSize(int batchSize);

    CrawlerContext<E> build();

    CrawlerContextBuilder<E> crawlLimit(long crawlLimit);

    CrawlerContextBuilder<E> linksExtractor(Function<E, Set<String>> linksExtractor);
    
    CrawlerContextBuilder<E> maxFailsAllowed(int maxFailsAllowed);

    CrawlerContextBuilder<E> parseLimit(long parseLimit);
    
    CrawlerContextBuilder<E> preferredLinkTest(Predicate<String> preferredLinkTest);

    CrawlerContextBuilder<E> resumeHandler(ResumeHandler resumeHandler);

    CrawlerContextBuilder<E> retryOnExceptionTestSupplier(Supplier<Predicate<Throwable>> retryOnExceptionTestSupplier);

    CrawlerContextBuilder<E> pageIsNoIndexTest(Predicate<E> pageIsNoIndexTest);
    
    CrawlerContextBuilder<E> pageIsNoFollowTest(Predicate<E> pageIsNoFollowTest);
    
    CrawlerContextBuilder<E> timeoutMillis(long timeoutMillis);

    CrawlerContextBuilder<E> urlFormatter(UnaryOperator<String> urlFormatter);

    CrawlerContextBuilder<E> connectionProvider(ConnectionProvider connProvider);
    
    CrawlerContextBuilder<E> urlParser(UrlParser urlParser);

    CrawlerContextBuilder<E> parseUrlTest(Predicate<String> urlTest);
    
    CrawlerContextBuilder<E> crawlUrlTest(Predicate<String> urlTest);
}
