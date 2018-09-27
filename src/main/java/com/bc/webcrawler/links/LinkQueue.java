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

import java.util.concurrent.TimeUnit;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2018 1:50:20 AM
 * @param <E>
 */
public interface LinkQueue<E> {

    boolean add(E e);
    
    boolean isEmpty();
    
    E peek(E outputIfNone);
    
    E peek(long timeout, TimeUnit timeUnit, E outputIfNone);

    E poll(E outputIfNone);
    
    E poll(long timeout, TimeUnit timeUnit, E outputIfNone);
    
    int size();
    
    <E> E[] toArray(E[] a);
}
