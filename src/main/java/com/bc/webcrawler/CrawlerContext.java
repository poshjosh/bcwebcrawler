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

import com.bc.webcrawler.links.LinkCollectionContext;
import com.bc.webcrawler.links.LinkCollector;
import com.bc.webcrawler.util.ComparatorForPredicate;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2017 8:07:38 PM
 */
public interface CrawlerContext<E> extends LinkCollectionContext<E>{

    static <T> CrawlerContextBuilder<T> builder(Class<T> type) {
        return new CrawlerContextBuilderImpl<>();
    }
    
    default Crawler<E> newCrawler(Set<String> seedUrls) {
        return new CrawlerImpl(this, seedUrls);
    }

    
    default BlockingQueue<String> createQueue(Collection<String> seedUrls) {
    
        final int crawlLimit = (int)getCrawlLimit();
        final int max = Math.max(seedUrls.size(), crawlLimit);
        final int initialCapacity = max <= 0 ? 1 : max;
        
        BlockingQueue<String> linkQueue = createQueue(initialCapacity);
        
        if( ! seedUrls.isEmpty()) {
            
            linkQueue.addAll(seedUrls);
        }
        
        return linkQueue;
    }

    default BlockingQueue<String> createQueue(int initialCapacity) {
    
//        LOG.fine(() -> "Seed urls: " + seedUrls.size());
        BlockingQueue<String> linkQueue = new PriorityBlockingQueue<>(
                initialCapacity, new ComparatorForPredicate(getPreferredLinkTest()));
        
        return linkQueue;
    }
    
    long getBatchInterval();

    int getBatchSize();
    
    long getParseLimit();
    
    long getTimeoutMillis();
    
    int getMaxFailsAllowed();
    
    String getBaseUrl();
    
    default Predicate<String> getPreferredLinkTest() {
        return (link) -> false;
    }
    
    default Supplier<Predicate<Throwable>> getRetryOnExceptionTestSupplier() {
        return () -> (exception) -> false;
    }

    default UnaryOperator<String> getUrlFormatter() {
        return (link) -> link;
    }
    
    default Predicate<String> getParseUrlTest() {
        return (link) -> true;
    }

    default Predicate<E> getPageIsNoIndexTest() {
        return (page) -> false;
    }
    
    default Predicate<E> getPageIsNoFollowTest() {
        return (page) -> false;
    }
    
    UrlParser<E> getUrlParser();
    
    LinkCollector<E> getLinkCollector();
}
