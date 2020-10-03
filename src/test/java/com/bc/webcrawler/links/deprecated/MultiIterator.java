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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 5, 2018 2:34:46 AM
 */
public class MultiIterator<E> implements Iterator<E> {

    private final Iterator<E> [] pages;
    
    private int pos = -1;

    public MultiIterator(Collection<E>... chain) {
        this.pages = (Iterator[])Arrays.asList(chain).stream()
                .map((c) -> c.iterator()).toArray();
    }
    
    public MultiIterator(Iterator<E>... chain) {
        this.pages = Objects.requireNonNull(chain);
    }

    @Override
    public boolean hasNext() {
        for(int i=0; i<pages.length; i++) {
            final Iterator iter = pages[i];
            if(iter.hasNext()) {
                pos = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public E next() {
        try{
            if(pos == -1) {
                if(!hasNext()) {
                    throw new IllegalStateException("Cannot call next() when hasNext() returns false");
                }
            }
            return pages[pos].next();
        }finally{
            pos = -1;
        }
    }
}
