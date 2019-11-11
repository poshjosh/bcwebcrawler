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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 8, 2019 10:57:58 PM
 */
public class ContentTypeRequestOkHttp implements ContentTypeRequest {

    private transient static final Logger LOG = Logger.getLogger(ContentTypeRequestOkHttp.class.getName());

    @Override
    public String apply(String url, String resultIfNone) {
        
        final Request request = new Request.Builder()
                .url(url)
                .head()
                .build();
        
        try(final Response response = OkHttp.getDefaultClient().newCall(request).execute()){
            
            final ResponseBody body = response == null ? null : response.body();

            final MediaType mediaType = body == null ? null : body.contentType();

            final String contentType = mediaType == null ? null : mediaType.toString();

            return contentType == null ? resultIfNone : contentType;
            
        }catch(IOException e) {
            
            LOG.log(Level.WARNING, "Exception reading Content-Type for: " + url, e);
            
            return resultIfNone;
        }
    }
}
