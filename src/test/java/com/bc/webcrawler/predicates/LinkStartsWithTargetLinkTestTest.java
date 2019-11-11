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

import com.bc.webcrawler.predicates.deprecated.LinkStartsWithTargetLinkTest;
import java.net.MalformedURLException;
import java.util.function.Predicate;

/**
 *
 * @author Josh
 */
public class LinkStartsWithTargetLinkTestTest extends SameHostTestTest {
    
    public LinkStartsWithTargetLinkTestTest() { }
    
    @Override
    public Predicate<String> newPredicate(String str) throws MalformedURLException {
        return new LinkStartsWithTargetLinkTest(str);
    }
}
