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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 11:10:12 AM
 */
public class ResumeHandlerInMemoryStore implements ResumeHandler {

    private final Set<String> store;

    public ResumeHandlerInMemoryStore() {
        this(Collections.EMPTY_SET);
    }
    
    public ResumeHandlerInMemoryStore(Set<String> pendingUrls) {
        this.store = new HashSet();
        this.store.addAll(pendingUrls);
    }

    @Override
    public boolean isExisting(String name) {
        return store.contains(name);
    }

    @Override
    public boolean saveIfNotExists(String name) {
        return store.add(name);
    }
}
