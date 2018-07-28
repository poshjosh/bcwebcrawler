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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 6:12:29 PM
 */
public class SameHostTest implements Predicate<String> {

    private static final Logger LOG = Logger.getLogger(SameHostTest.class.getName());

    private final String targetHost;

    public SameHostTest(String targetLink) throws MalformedURLException {
        this(new URL(targetLink));
    }

    public SameHostTest(URL targetUrl) {
        this.targetHost = this.getHost(targetUrl);
    }

    @Override
    public boolean test(String link) {
        try{
            final URL url = new URL(link);
            return Objects.equals(this.targetHost, this.getHost(url));
        }catch(MalformedURLException e) {
            LOG.fine(e.toString());
            return false;
        }
    }
    
    private String getHost(URL url) {
        final String host = url.getHost();
        if(host != null && !host.isEmpty()) {
            return host;
        }else{
            return url.getAuthority();
        }
    }
}
