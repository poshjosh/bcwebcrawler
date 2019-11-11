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

package com.bc.webcrawler.predicates;

import java.io.Serializable;
import java.util.function.BiPredicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2018 6:17:09 PM
 */
public class LinkFileEndsWithTest implements BiPredicate<String, String>, Serializable {

    @Override
    public boolean test(String link, String extension) {
        boolean accept = false;
        if (link.endsWith(extension)) {
            accept = true;
        } else {
            final int offset = link.lastIndexOf('/');
            if (link.indexOf(extension + "?", offset) != -1) {
                accept = true;
            }
        }
        return accept;
    }
}
