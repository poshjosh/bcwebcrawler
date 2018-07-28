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

package com.bc.webcrawler;

import com.bc.util.Util;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 6:28:39 PM
 */
public class SampleValues {
    
    public static final String getRandomLink() {
        return LINKS[Util.randomInt(LINKS.length)];
    }

    public static final String [] LINKS = {
        "http://www.looseboxes.com/idisc/feed.jsp?feedid=23098",
        "https://www.google.com/",
        "http://www.looseboxes.com/idisc/feed.jsp#comment",
        "https://www.bellanaija.com",
        "https://mail.google.com"
    };
}
