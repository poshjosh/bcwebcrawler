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

import java.util.List;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 6:13:37 PM
 */
public interface CrawlSnapshot {

    String getCurrentUrl(String resultIfNone);
    
    int getAttempted();

    int getCrawled();
    
    int getFailed();
    
    Set<String> getFailedLinks();

    int getRemaining();

    List<String> getRemainingLinks();
    
    long getTimeSpentMillis();
    
    boolean isWithinTimeLimit();

    boolean isWithinCrawlLimit();

    boolean isWithinParseLimit();
    
    boolean isWithinFailLimit();
}
