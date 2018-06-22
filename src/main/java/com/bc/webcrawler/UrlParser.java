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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 8:15:45 AM
 */
public interface UrlParser<E> {

    E parse(String url) throws IOException;
    
    default List<String> getCookieList() {
        final Map<String, String> cookies = this.getCookies();
        final List<String> cookieList = new ArrayList(cookies.size());
        cookies.forEach((key, val) -> {
            cookieList.add(key + '=' + val + ';');
        });
        return cookieList;
    }
    
    Map<String, String> getCookies();
}
