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

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 4, 2018 9:44:52 PM
 */
public class MultiQueue<E> extends AbstractQueue<E> {

    private transient static final Logger LOG = Logger.getLogger(MultiQueue.class.getName());

    private final Queue<E> [] pages;
    
    public MultiQueue(Queue<E>... chain) {
        this.pages = Objects.requireNonNull(chain);
    }
    
    /**
     * <b>Modifications to the returned Queue are reflected</b>
     * @param pageIndex The pageIndex whose page will be returned
     * @return The queue at the specified index
     */
    public Queue<E> getPageAt(int pageIndex) {
        return this.pages[pageIndex];
    }
    
    public int getPageCount() {
        return pages.length;
    }
    
    /**
     * <b>Modifications to the returned Queue are reflected</b>
     * @param index The index of the item whose containing queue will be returned
     * @return The queue containing the item at the specified index
     */
    public Queue<E> getPageFor(int index) {
        int pos = index;
        for(Queue<E> queue : this.pages) {
            final int size = queue.size();
            if(pos < size) {
                return queue;
            }
            pos = pos - size;
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }
    
    @Override
    public int size() {
        int size = 0;
        for(Queue<E> queue : this.pages) {
            size += queue.size();
        }
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new MultiIterator(this.pages);
    }
    
    @Override
    public boolean contains(Object o) {
        for(int pageIndex=0; pageIndex<this.pages.length; pageIndex++) {
            final Queue page = this.getPageAt(pageIndex);
            if(page.contains(o)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean offer(E e) {
        final boolean output;
        if(this.pages.length == 0) {
            output = false;
        }else {
            output = pages[pages.length - 1].offer(e);
        }
        return output;
    }

    @Override
    public E poll() {
        final E output;
        if(this.pages.length == 0) {
            output = null;
        }else{
            final Queue<E> queue = this.getFirstNonEmptyQueue(null);
            if(queue == null) {
                output = null;
            }else{
                output = queue.poll();
            }
        }
        return output;
    }

    @Override
    public E peek() {
        final E output;
        if(this.pages.length == 0) {
            output = null;
        }else{
            final Queue<E> queue = this.getFirstNonEmptyQueue(null);
            if(queue == null) {
                output = null;
            }else{
                output = queue.peek(); 
            }
        }
        return output;
    }
    
    public Queue<E> getFirstNonEmptyQueueOrException() {
        final Queue<E> output = this.getFirstNonEmptyQueue(null);
        return Objects.requireNonNull(output);
    }
    
    public Queue<E> getFirstNonEmptyQueue(Queue outputIfNone) {
        for(Queue queue : this.pages) {
            if(!queue.isEmpty()) {
                return queue;
            }
        }
        return outputIfNone;
    }
}
