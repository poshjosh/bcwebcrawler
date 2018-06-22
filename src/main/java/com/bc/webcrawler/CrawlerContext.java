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
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2017 8:07:38 PM
 */
public interface CrawlerContext<E> {

    static <T> CrawlerContextBuilder<T> builder(Class<T> type) {
        return new CrawlerContextBuilderImpl<>();
    }
    
    default Crawler<E> newCrawler(Set<String> seedUrls) {
        return new CrawlerImpl(this, seedUrls);
    }

    long getBatchInterval();

    int getBatchSize();

    long getCrawlLimit();
    
    long getParseLimit();
    
    long getTimeoutMillis();
    
    int getMaxFailsAllowed();
    
    default Predicate<String> getPreferredLinkTest() {
        return (link) -> false;
    }
    
    ResumeHandler getResumeHandler();

    default Supplier<Predicate<Throwable>> getRetryOnExceptionTestSupplier() {
        return () -> (t) -> false;
    }

    default UnaryOperator<String> getUrlFormatter() {
        return (link) -> link;
    }
    
    default Predicate<String> getParseUrlTest() {
        return (link) -> true;
    }

    default Predicate<String> getCrawlUrlTest() {
        return (link) -> true;
    }

    default Predicate<E> getPageIsNoIndexTest() {
        return (doc) -> false;
    }
    
    default Predicate<E> getPageIsNoFollowTest() {
        return (doc) -> false;
    }
    
    ConnectionProvider getConnectionProvider();
    
    UrlParser<E> getUrlParser();
    
    Function<E, Set<String>> getLinksExtractor();
}
