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

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 6:10:46 PM
 */
public interface Crawler<E> extends Iterator<E> {
    
    Logger CRAWLER_LOGGER = Logger.getLogger(Crawler.class.getName());
    
    boolean isShutdown();
    
    void shutdown(long timeout, TimeUnit timeUnit);    
    
    Optional<String> getCurrentUrl();
    
    CrawlSnapshot getSnapshot();
    
    /**
     * <p><b>May return <code>null</code></b></p>
     * Calls {@link #parseNext()} but returns <code>null</code> if an {@link java.io.IOException} is thrown.
     * @return The next element or <code>null</code> if an {@link java.io.IOException} is thrown.
     * @see #parseNext() 
     */
    @Override
    default E next() {
        try{
            return parseNext();
        }catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    E parseNext() throws IOException, InterruptedException;

    default Stream<E> stream() {
        return StreamSupport.stream(
                  Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
                  false);
    }
}
