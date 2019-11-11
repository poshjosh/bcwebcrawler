/*
 * Copyright 2019 NUROX Ltd.
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

import com.bc.net.util.UserAgents;
import okhttp3.Request;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 8, 2019 11:19:16 PM
 */
public class OkHttpBase {

    public Request head(String url) {
        final Request request = this.getRequestBuilder()
                .header("User-Agent", this.getRandomUserAgent(url))
                .url(url)
                .head()
                .build();
        return request;
    }

    public Request get(String url) {
        final Request request = this.getRequestBuilder()
                .header("User-Agent", this.getRandomUserAgent(url))
                .url(url)
                .get()
                .build();
        return request;
    }
    
    private Request.Builder _rb;
    private Request.Builder getRequestBuilder() {
        if(_rb == null) {
            _rb = new Request.Builder();
        }
        return _rb;
    }
    
    public String getRandomUserAgent(String url) {
        return this.getUserAgents().any(url, Math.random() > 0.5);
    }
    
    private UserAgents _ua;
    public UserAgents getUserAgents() {
        if(_ua == null) {
            _ua = new UserAgents();
        }
        return _ua;
    }
}
