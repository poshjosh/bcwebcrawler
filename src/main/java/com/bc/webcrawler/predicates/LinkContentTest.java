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

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import com.bc.webcrawler.ContentTypeRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 6, 2017 3:29:25 PM
 */
public class LinkContentTest implements Serializable, Predicate<String> {

    private transient static final Logger LOG = Logger.getLogger(LinkContentTest.class.getName());
    
    private final ContentTypeRequest contentTypeProvider;
    
    private final String requiredContentTypePart;
    
    private final Predicate<String> validLinkSuffixTest;
    
    private final Predicate<String> invalidLinkSuffixTest;
    
    private final int connectTimeout;

    private final int readTimeout;
    
    private final BiPredicate<String, String> linkFileEndsWithTest;
    
    private final boolean resultIfNone;
    
    public LinkContentTest(
            ContentTypeRequest contentTypeRequest,
            Set<String> requiredLinkSuffixes, 
            Set<String> unwantedLinkSuffixes,
            String requiredContentTypePart,
            int connectTimeout, int readTimeout,
            boolean resultIfNone) {
        this.contentTypeProvider = Objects.requireNonNull(contentTypeRequest);
        this.validLinkSuffixTest = this.createLinkSuffixTest(requiredLinkSuffixes);
        this.invalidLinkSuffixTest = this.createLinkSuffixTest(unwantedLinkSuffixes);
        this.requiredContentTypePart = Objects.requireNonNull(requiredContentTypePart);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.resultIfNone = resultIfNone;
        this.linkFileEndsWithTest = new LinkFileEndsWithTest();
    }
    
    private Predicate<String> createLinkSuffixTest(Set<String> linkSuffixes) {
        final Predicate<String> linkSuffixTest;
        if(linkSuffixes.isEmpty()) {
            linkSuffixTest = (link) -> false;
        }else{
            linkSuffixTest = (link) -> {
                final Predicate<String> endsWith = (suffix) -> linkFileEndsWithTest.test(link.toLowerCase(), suffix);
                return linkSuffixes.stream().filter(endsWith).findAny().isPresent();
            };
        }
        return linkSuffixTest;
    }
    
    @Override
    public boolean test(String link) {
        final long mb4 = com.bc.util.Util.availableMemory();
        boolean output;
        if(this.validLinkSuffixTest.test(link)) {
            output = true;
            LOG.log(Level.FINER, "Ends with valid suffix: {0}", link);
        }else if(this.invalidLinkSuffixTest.test(link)) {
            output = false;
            LOG.log(Level.FINER, "Ends with invalid suffix: {0}", link);
        }else{
            output = this.hasRequiredContentType(link);
            LOG.finer(() -> "Success: " + output + ", consumed memory: " + 
                    (com.bc.util.Util.usedMemory(mb4)) + ", checking content of: " + link);
        }
        return output;
    }
    
    public boolean hasRequiredContentType(String link) {
        final boolean output;
        final String contentType = contentTypeProvider.apply(link, null);
        if (contentType == null) {
            output = resultIfNone;
        } else {
            output = contentType.toLowerCase().contains(this.requiredContentTypePart);
        }
        return output;
    }

    public final ContentTypeRequest getContentTypeProvider() {
        return contentTypeProvider;
    }

    public final String getRequiredContentTypePart() {
        return requiredContentTypePart;
    }

    public final Predicate<String> getLinkSuffixTest() {
        return validLinkSuffixTest;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final int getReadTimeout() {
        return readTimeout;
    }
}
