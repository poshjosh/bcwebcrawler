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

import com.bc.net.UrlUtil;
import java.net.MalformedURLException;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 10:37:13 AM
 */
public class LinkStartsWithTargetLinkTest implements Predicate<String> {

    private static final Logger logger = Logger.getLogger(LinkStartsWithTargetLinkTest.class.getName());

    private final String targetLink;

    public LinkStartsWithTargetLinkTest(String targetLink) throws MalformedURLException {
        this.targetLink =  format(targetLink);
    }

    @Override
    public boolean test(String link) {
        try{
            return link.startsWith(targetLink) || format(link).startsWith(targetLink);
        }catch(MalformedURLException e) {
            logger.fine(e.toString());
            return false;
        }
    }
    
    private String format(String link) throws MalformedURLException {
        return UrlUtil.toWWWFormat(link).replaceFirst("https", "http");
    }
}
