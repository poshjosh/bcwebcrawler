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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 8:15:45 AM
 * @todo The getCookie methods return cookies from across all domains
 * but collapses them as a single list or map. This will result in conflict
 * where multiple cookies have the same name? To avert this conflict,
 * cookies should be returned as per domain
 */
public interface UrlParser<E> {

    E parse(String url) throws IOException;
    
    List<String> getCookieNameValueList();
    
    Map<String, String> getCookieNameValueMap();
}
