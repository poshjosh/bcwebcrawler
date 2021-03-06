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

package com.bc.webcrawler.predicates;

import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 11:52:23 AM
 */
public class LinkIsToBeCrawledTest implements Predicate<String> {

    private static final Logger logger = Logger.getLogger(LinkIsToBeCrawledTest.class.getName());
    
    private final boolean crawlQueryLinks;

    public LinkIsToBeCrawledTest(boolean crawlQueryLinks) {
        this.crawlQueryLinks = crawlQueryLinks;
    }

    @Override
    public boolean test(String link) {

        final boolean toBeCrawled;  
        if(link.trim().isEmpty() || 
                link.contains("#") ||
                (!this.crawlQueryLinks && link.contains("?"))) {
            toBeCrawled = false;
        }else{
            toBeCrawled = true;
        }

        logger.finer(() -> "To be crawled: "+toBeCrawled+", link: " + link);

        return toBeCrawled;
    }
}
