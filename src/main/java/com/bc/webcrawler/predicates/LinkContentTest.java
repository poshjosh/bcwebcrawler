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
import java.net.URLConnection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import com.bc.webcrawler.ConnectionProvider;
import java.util.function.BiPredicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 6, 2017 3:29:25 PM
 */
public class LinkContentTest implements Serializable, BiPredicate<String, Boolean> {

    private transient static final Logger logger = Logger.getLogger(LinkContentTest.class.getName());
    
    private final ConnectionProvider connectionProvider;
    
    private final String requiredContentTypePart;
    
    private final Predicate<String> linkSuffixTest;
    
    private final int connectTimeout;

    private final int readTimeout;
    
    public LinkContentTest(
            ConnectionProvider connectionProvider,
            Set<String> requiredLinkSuffixes, String requiredContentTypePart,
            int connectTimeout, int readTimeout) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
        Objects.requireNonNull(requiredLinkSuffixes);
        if(requiredLinkSuffixes.isEmpty()) {
            linkSuffixTest = (link) -> false;
        }else{
            linkSuffixTest = (link) -> {
                final Predicate<String> suffixTest = (suffix) -> endsWith(link.toLowerCase(), suffix);
                return requiredLinkSuffixes.stream().filter(suffixTest).findFirst().isPresent();
            };
        }
        this.requiredContentTypePart = Objects.requireNonNull(requiredContentTypePart);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
    
    @Override
    public boolean test(String link, Boolean outputIfNone) {
        final long mb4 = com.bc.util.Util.availableMemory();
        boolean output;
        if(this.linkSuffixTest.test(link)) {
            output = true;
        }else{
            final URLConnection conn = connectionProvider.of(link, false, null);
            if(conn == null) {
                output = outputIfNone;
            }else{
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
                final String contentType = conn.getContentType();
                if (contentType == null) {
                    output = outputIfNone;
                } else {
                    output = contentType.toLowerCase().contains(this.requiredContentTypePart);
                }
            }
        }
        logger.finer(() -> "Consumed memory: " + (com.bc.util.Util.usedMemory(mb4)) + 
        ", checking content of: " + link);
        return output;
    }

    private boolean endsWith(String link, String extension) {
        boolean accept = false;
        if (link.endsWith(extension)) {
            accept = true;
        } else {
            int n = link.lastIndexOf('/');
            if (link.indexOf(extension + "?", n) != -1) {
                accept = true;
            }
        }
        return accept;
    }
}