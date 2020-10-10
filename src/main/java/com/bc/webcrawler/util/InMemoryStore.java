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

package com.bc.webcrawler.util;

import com.bc.webcrawler.util.Store;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 11:10:12 AM
 */
public class InMemoryStore<E> implements Store<E> {

    private final Set<E> store;

    public InMemoryStore() {
        this(Collections.EMPTY_SET);
    }
    
    public InMemoryStore(Set<E> set) {
        this.store = Collections.synchronizedSet(new HashSet());
        this.store.addAll(set);
    }

    @Override
    public boolean contains(E name) {
        return store.contains(name);
    }

    @Override
    public Iterable<E> getAll(int offset, int limit) {
        int end = offset + limit;
        end = Math.min(store.size(), end);
        return new ArrayList(store).subList(offset, end);
    }

    @Override
    public void flush() { }

    @Override
    public E save(E elem) {
        store.add(elem);
        return elem;
    }

    @Override
    public E delete(E elem) {
        store.remove(elem);
        return elem;
    }

    @Override
    public long count() {
        return store.size();
    }
}
