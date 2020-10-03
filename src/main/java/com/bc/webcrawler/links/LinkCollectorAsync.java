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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 10, 2018 11:35:00 PM
 */
public class LinkCollectorAsync<E> extends LinkCollectorImpl<E> {

    private transient static final Logger LOG = Logger.getLogger(LinkCollectorAsync.class.getName());

    private final ExecutorService linkCollectionExecSvc;
    
    public LinkCollectorAsync(LinkCollectionContext<E> context, String baseUrl) {
    
        this(context, baseUrl, 1, 1024 * 16);
    }
    
    public LinkCollectorAsync(
            LinkCollectionContext<E> context, 
            String baseUrl,
            int poolSize, 
            int queueCapacity) {
        
        super(context, baseUrl);

        final String threadPoolName = this.getClass().getName() + "-ThreadFactory";

        this.linkCollectionExecSvc = new BoundedExecutorService(threadPoolName, poolSize, queueCapacity, false);
    }
    
    @Override
    public void doShutdown(long timeout, TimeUnit timeUnit) {
        Util.shutdownAndAwaitTermination(linkCollectionExecSvc, timeout, timeUnit);
    }

    @Override
    public void collectLinks(Set<String> linkSet, Consumer<String> consumer) {
        
        if(this.isShutdownAttempted()) {
            
            LOG.finer("Shutdown. Exiting");
            
            return;
        }
        
        this.linkCollectionExecSvc.submit(() -> {
            try{

                super.collectLinks(linkSet, consumer);
                
            }catch(RuntimeException e) {
                LOG.log(Level.WARNING, e.toString(), e);
                LOG.log(Level.FINE, "Exception collecting links", e);
            }
        });
    }
}
