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
    
    public LinkQueueImpl(Predicate<E> isPreferred, LinkedBlockingQueueWithTimedPeek<E>... chain) {
        super(chain);
        if(chain.length < 2) {
            throw new IllegalArgumentException();
        }
        this.isPreferred = Objects.requireNonNull(isPreferred);
        
    }
    
    @Override
    public boolean add(E e) {
        final Queue<E> target;
        if(this.isEmpty()) {
            target = this.getPageAt(0);
        }else if(this.isPreferred.test(e)) {    
            target = this.getPageAt(0);
        }else{
            target = this.getPageAt(1);
        }

        return target.add(e);
    }
    
    @Override
    public E peek(E outputIfNone) {
        final E found = this.peek();
        return found == null ? outputIfNone : found;
    }

    @Override
    public E peek(long timeout, TimeUnit timeUnit, E outputIfNone) {
        return this.waitForFirstLink(false, timeout, timeUnit, outputIfNone);
    }

    @Override
    public E poll(E outputIfNone) {
        final E found = this.poll();
        return found == null ? outputIfNone : found;
    }

    @Override
    public E poll(long timeout, TimeUnit timeUnit, E outputIfNone) {
        return this.waitForFirstLink(true, timeout, timeUnit, outputIfNone);
    }
    
    public E waitForFirstLink(boolean remove, long timeout, TimeUnit timeUnit, E outputIfNone) {
        try{
            final long tb4 = System.currentTimeMillis();
            final E link;
            if(timeout <= 0L) {
                link = this.isEmpty() ? outputIfNone : remove ? this.poll() : this.peek();
            }else if(!this.isEmpty()) {
                link = remove ? super.poll() : super.peek();
            }else{
                
                LOG.finer(() -> "Will wait at most " + timeout + " millis for link at position zero");
                
                link = remove ? this.poll(timeout, timeUnit) : this.peek(timeout, timeUnit);
                
                LOG.finer(() -> "Waited " + (System.currentTimeMillis() - tb4) + 
                        " millis for link at position zero: " + link);
            }
            return link == null ? outputIfNone : link;
        }catch(InterruptedException e) {
            LOG.log(Level.WARNING, null, e);
            return outputIfNone;
        }
    }

    public E take() throws InterruptedException {
        final BlockingQueue<E> queue = (BlockingQueue)this.getFirstNonEmptyQueue(getPageAt(0));
        final E output = queue.take();
        return output;
    }

    public boolean offer(E e, long timeout, TimeUnit timeUnit) throws InterruptedException {
        final boolean output;
        if(this.getPageCount() == 0) {
            output = false;
        }else {
            output = ((BlockingQueue)getPageAt(getPageCount() - 1)).offer(e, timeout, timeUnit);
        }
        return output;
    }

    public E poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final BlockingQueue<E> queue = (BlockingQueue)this.getFirstNonEmptyQueue(this.getPageAt(0));
        final E output = queue.poll(timeout, timeUnit);
        return output;
    }
    
    public E peek(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final LinkedBlockingQueueWithTimedPeek<E> queue = 
                (LinkedBlockingQueueWithTimedPeek<E>)this.getFirstNonEmptyQueue(this.getPageAt(0));
        return queue.peek(timeout, timeUnit);
    }
}
