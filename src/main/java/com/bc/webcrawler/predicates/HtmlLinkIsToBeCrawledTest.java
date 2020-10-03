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
import com.bc.webcrawler.ContentTypeRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 5:36:04 PM
 */
public class HtmlLinkIsToBeCrawledTest implements CrawlUrlTest {

    private final Predicate<String> linkIsToBeCrawledTest;
    
    private final Predicate<String> linkContentIsHtmlTest;
    
    public HtmlLinkIsToBeCrawledTest(
            ContentTypeRequest contentTypeProvider,
            int connectTimeoutForLinkValidation, 
            int readTimeoutForLinkValidation, 
            boolean crawlQueryLinks) {

        this.linkIsToBeCrawledTest = new LinkIsToBeCrawledTest(crawlQueryLinks);
        
        this.linkContentIsHtmlTest = new LinkContentIsHtmlTest(
                contentTypeProvider, connectTimeoutForLinkValidation, readTimeoutForLinkValidation, true);
    }

    @Override
    public boolean test(String link) {
        return linkIsToBeCrawledTest.test(link) && linkContentIsHtmlTest.test(link);
    }
}
