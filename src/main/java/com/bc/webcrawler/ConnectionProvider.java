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
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 6, 2017 12:22:09 PM
 */
public interface ConnectionProvider extends Function<String, Optional<URLConnection>> {

    Logger logger = Logger.getLogger(ConnectionProvider.class.getName());
    
    @Override
    default Optional<URLConnection> apply(String url) {
        return Optional.ofNullable(this.of(url, false, null));
    }

    default URLConnection ofGet(String url) throws MalformedURLException, IOException {
        return of(url, false);
    }
    
    default URLConnection ofPost(String url) throws MalformedURLException, IOException {
        return of(url, true);
    }

    default URLConnection of(String url, boolean post, URLConnection outputIfNone) {
        URLConnection output;
        try{
            output = this.of(url, post);
        }catch(Exception e) {
            logger.warning(e.toString());
            output = null;
        }
        return output == null ? outputIfNone : output;
    }
    
    URLConnection of(String url, boolean post) throws MalformedURLException, IOException;
}
