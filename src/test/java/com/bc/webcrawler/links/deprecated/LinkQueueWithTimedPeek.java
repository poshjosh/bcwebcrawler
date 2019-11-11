/*
 * Copyright 2019 NUROX Ltd.
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

import com.bc.webcrawler.links.LinkQueueImpl;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 20, 2019 6:20:38 PM
 */
public class LinkQueueWithTimedPeek<E> extends LinkQueueImpl<E> {

    public LinkQueueWithTimedPeek(Predicate isPreferred, LinkedBlockingQueueWithTimedPeek<E>... chain) {
        super(isPreferred, chain);
    }
    
    public E peek(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final LinkedBlockingQueueWithTimedPeek<E> queue = 
                (LinkedBlockingQueueWithTimedPeek<E>)this.getFirstNonEmptyQueue(this.getQueueAt(0));
        return queue.peek(timeout, timeUnit);
    }
}
