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

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 1:10:01 PM
 */
public class MultiList<E> extends AbstractList<E> {

    private final ReentrantLock takeLock = new ReentrantLock();

    private final List<E> [] pages;
    
    private int indexInList = -1;
    
    public MultiList(List<E>... chain) {
        this.pages = Objects.requireNonNull(chain);
    }
    
    public E first(long timeout, TimeUnit unit, boolean remove, E outputIfNone) 
            throws InterruptedException {
        final E output;
        final long start = System.nanoTime();
        final long nanos = unit.toNanos(timeout); 
        takeLock.lockInterruptibly();
        try {
            while (this.isEmpty()) {
                if (System.nanoTime() - start >= nanos) {
                    break;
                }
            }
            output = this.isEmpty() ? outputIfNone : remove ? this.remove(0) : this.get(0);
        } finally {
            takeLock.unlock();
        }
        return output;
    }
    
    /**
     * <b>Modifications to the returned List are reflected</b>
     * @param pageIndex The pageIndex whose page will be returned
     * @return The list at the specified index
     */
    public List<E> getPageAt(int pageIndex) {
        return this.pages[pageIndex];
    }
    
    /**
     * <b>Modifications to the returned List are reflected</b>
     * @param index The index of the item whose containing list will be returned
     * @return The list containing the item at the specified index
     */
    public List<E> getPageFor(int index) {
        int pos = index;
        for(List<E> list : this.pages) {
            final int size = list.size();
            if(pos < size) {
                this.indexInList = pos;
                return list;
            }
            pos = pos - size;
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public E get(int index) {
        try{
            final List<E> page = this.getPageFor(index);
            return page.get(this.indexInList);
        }finally{
            this.indexInList = -1;
        }
    }
    
    @Override
    public int size() {
        int size = 0;
        for(List<E> list : this.pages) {
            size += list.size();
        }
        return size;
    }

    @Override
    public E remove(int index) {
        try{
            final List<E> page = this.getPageFor(index);
            return page.remove(this.indexInList);
        }finally{
            this.indexInList = -1;
        }
    }

    @Override
    public void add(int index, E element) {
        try{
            final List<E> page = this.getPageFor(index);
            page.add(this.indexInList, element);
        }finally{
            this.indexInList = -1;
        }
    }

    @Override
    public E set(int index, E element) {
        try{
            final List<E> page = this.getPageFor(index);
            return page.set(this.indexInList, element);
        }finally{
            this.indexInList = -1;
        }
    }
}
