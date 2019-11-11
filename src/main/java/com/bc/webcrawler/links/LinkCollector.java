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

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2018 12:55:13 AM
 * @param <E>
 */
public interface LinkCollector<E> {

    void collectLinks(E doc, Consumer<String> consumer);

    void collectLinks(Set<String> linksToCollect, Consumer<String> consumer);

    int getCollected();

    boolean isShutdown();

    boolean isToBeCollected(String link);

    boolean isWithinCollectLimit();

    void shutdown(long timeout, TimeUnit timeUnit);
}
