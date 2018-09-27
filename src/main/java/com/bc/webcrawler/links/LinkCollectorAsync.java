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

import com.bc.util.Util;
import com.bc.util.concurrent.BoundedExecutorService;
import com.bc.webcrawler.CrawlerContext;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 10, 2018 11:35:00 PM
 */
public class LinkCollectorAsync<E> extends LinkCollectorImpl<E> {

    private transient static final Logger LOG = Logger.getLogger(LinkCollectorAsync.class.getName());

    private final ExecutorService linkCollectionExecSvc;
    
    private boolean shutdown;
    
    private final Thread shutdownHook;

    public LinkCollectorAsync(
            CrawlerContext<E> context, 
            Predicate<String> isLinkAttempted, 
            Consumer<String> accumulator, 
            String baseUrl) {
        
        super(context, isLinkAttempted, accumulator, baseUrl);

        final String threadPoolName = this.getClass().getName() + "-LinkCollectionThreadPool";
        this.linkCollectionExecSvc = new BoundedExecutorService(threadPoolName, 1, 1, false);

        this.shutdownHook = new Thread(() -> doShutdown(999, TimeUnit.MILLISECONDS));
        
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
    
    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        try{
            this.doShutdown(timeout, timeUnit);
        }finally{
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }
    }

    private void doShutdown(long timeout, TimeUnit timeUnit) {
        if(this.isShutdown()) {
            return;
        }
        LOG.finer(() -> "Shutting down");
        Util.shutdownAndAwaitTermination(linkCollectionExecSvc, timeout, timeUnit);
        this.shutdown = true;
    }

    @Override
    public int collectLinks(String url, Set<String> linkSet) {
        this.linkCollectionExecSvc.submit(() -> super.collectLinks(url, linkSet));
        return -1;
    }
}
