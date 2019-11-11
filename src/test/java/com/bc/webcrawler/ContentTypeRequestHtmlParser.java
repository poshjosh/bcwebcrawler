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

import java.net.URLConnection;
import com.bc.net.util.UserAgents;
import org.htmlparser.http.ConnectionManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 6, 2017 12:10:33 PM
 */
public class ContentTypeRequestHtmlParser implements ContentTypeRequest {
    
    @Override
    public String apply(String url, String resultIfNone) {
        final ConnectionManager connMgr = ConnMgr.getDefault();
        connMgr.getRequestProperties().put("User-Agent", this.getUserAgents().any(url));
        final URLConnection conn = connMgr.apply(url);
        final String contentType = conn == null ? null : conn.getContentType();
        return contentType == null ? resultIfNone : contentType;
    }
    
    private UserAgents _ua;
    public UserAgents getUserAgents() {
        if(_ua == null) {
            _ua = new UserAgents();
        }
        return _ua;
    }
}
