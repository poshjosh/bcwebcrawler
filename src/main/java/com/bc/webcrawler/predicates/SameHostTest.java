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
            final String otherHost = this.getHost(new URL(link));
            if(Objects.equals(this.targetHost, otherHost)) {
                return true;
            }else if(this.targetHost == null){
                return false;
            }else if(otherHost == null){
                return false;
            }else{    
                final String regex = "\\.";
                final String [] lhs = this.targetHost.split(regex);
                final String [] rhs = otherHost.split(regex);
                if(lhs.length < 2) {
                    throw new MalformedURLException(this.targetHost);
                }
                if(rhs.length < 2) {
                    throw new MalformedURLException(otherHost);
                }
                final boolean lastEquals = Objects.equals(lhs[lhs.length - 1], rhs[rhs.length - 1]);
                if(!lastEquals) {
                    return false;
                }
                return Objects.equals(lhs[lhs.length - 2], rhs[rhs.length - 2]);
            }
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
