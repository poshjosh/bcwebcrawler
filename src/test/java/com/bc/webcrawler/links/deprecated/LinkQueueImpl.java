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

package com.bc.webcrawler.links.deprecated;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2018 1:57:02 AM
 */
public class LinkQueueImpl<E> extends MultiQueue<E> implements LinkQueue<E> {

    private transient static final Logger LOG = Logger.getLogger(LinkQueueImpl.class.getName());

    private final Predicate<E> isPreferred;
    
    public LinkQueueImpl(Predicate<E> isPreferred, BlockingQueue<E>... chain) {
        super(chain);
        if(chain.length < 2) {
            throw new IllegalArgumentException();
        }
        this.isPreferred = Objects.requireNonNull(isPreferred);
        
    }
    
    @Override
    public boolean add(E e) {
        final int pageIndex = this.isPreferred.test(e) || this.getQueueCount() <= 1 ? 0 : 1;
        final Queue<E> target = this.getQueueAt(pageIndex);
        return target.add(e);
    }
    
    @Override
    public E peek(E outputIfNone) {
        final E found = this.peek();
        return found == null ? outputIfNone : found;
    }

    @Override
    public E poll(E outputIfNone) {
        final E found = this.poll();
        return found == null ? outputIfNone : found;
    }

    @Override
    public E poll(long timeout, TimeUnit timeUnit, E outputIfNone) {
        try{
            final E result = this.poll(timeout, timeUnit);
            return result == null ? outputIfNone : result;
        }catch(InterruptedException e) {
            LOG.log(Level.WARNING, "{0}", e.toString());
            LOG.log(Level.FINE, null, e);
            return outputIfNone;
        }
    }

    public E take() throws InterruptedException {
        final BlockingQueue<E> queue = (BlockingQueue)this.getFirstNonEmptyQueue(getQueueAt(0));
        final E output = queue.take();
        return output;
    }

    public boolean offer(E e, long timeout, TimeUnit timeUnit) throws InterruptedException {
        final boolean output;
        if(this.getQueueCount() == 0) {
            output = false;
        }else {
            output = ((BlockingQueue)getQueueAt(getQueueCount() - 1)).offer(e, timeout, timeUnit);
        }
        return output;
    }

    public E poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final BlockingQueue<E> queue = (BlockingQueue)this.getFirstNonEmptyQueue(this.getQueueAt(0));
        final E output = queue.poll(timeout, timeUnit);
        return output;
    }
}
