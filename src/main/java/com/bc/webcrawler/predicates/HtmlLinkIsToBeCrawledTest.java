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

import com.bc.webcrawler.UrlParser;
import java.util.Objects;
import java.util.function.Predicate;
import com.bc.webcrawler.ConnectionProvider;
import java.util.function.BiPredicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 5:36:04 PM
 */
public class HtmlLinkIsToBeCrawledTest implements Predicate<String> {

    private final Predicate<String> linkIsToBeCrawledTest;
    
    private final Predicate<String> linkHasExtensionTest;
        
    private final BiPredicate<String, Boolean> linkContentIsHtmlTest;
        
    public HtmlLinkIsToBeCrawledTest(
            ConnectionProvider urlConnProvider,
            UrlParser urlParser, int connectTimeoutForLinkValidation, 
            int readTimeoutForLinkValidation, boolean crawlQueryLinks) {

        Objects.requireNonNull(urlParser);
        
        this.linkIsToBeCrawledTest = new LinkIsToBeCrawledTest(crawlQueryLinks);
        
        this.linkHasExtensionTest = new LinkHasExtensionTest();
        
        this.linkContentIsHtmlTest = new LinkContentIsHtmlTest(
                urlConnProvider, connectTimeoutForLinkValidation, readTimeoutForLinkValidation);
    }

    @Override
    public boolean test(String link) {
        return linkIsToBeCrawledTest.test(link) && 
                (linkHasExtensionTest.test(link) || linkContentIsHtmlTest.test(link, Boolean.TRUE));
    }
}
