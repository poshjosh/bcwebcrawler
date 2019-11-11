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

import com.bc.net.util.UrlProbeImpl;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 9, 2019 1:57:10 PM
 */
public class ContentTypeRequestImpl extends UrlProbeImpl implements ContentTypeRequest {

    private transient static final Logger LOG = Logger.getLogger(ContentTypeRequestImpl.class.getName());

    public ContentTypeRequestImpl() { 
        super(true);
    }

    public ContentTypeRequestImpl(boolean followRedirects) {
        super(followRedirects);
    }

    @Override
    public String apply(String url, String resultIfNone) {
        try{
            
            final String contentType = this.getContentType(new URL(url));
            
            return contentType == null ? resultIfNone : contentType;
            
        }catch(IOException e) {
            
            LOG.log(Level.WARNING, "Exception reading Content-Type from: " + url, e);
            
            return resultIfNone;
        }
    }
}
