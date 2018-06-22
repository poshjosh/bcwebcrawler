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

import com.bc.net.impl.RequestBuilderImpl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import com.bc.net.RequestBuilder;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 6, 2017 12:10:33 PM
 */
public class ConnectionProviderImpl implements ConnectionProvider {

    private final Supplier<Collection<String>> cookiesSupplier;
    
    private final RequestBuilder requestBuilder;
    
    public ConnectionProviderImpl(Supplier<Collection<String>> cookiesSupplier) {
        this.cookiesSupplier = Objects.requireNonNull(cookiesSupplier);
        this.requestBuilder = new RequestBuilderImpl();
        this.requestBuilder
                .connectTimeout(10_000)
                .readTimeout(30_000)
                .randomUserAgent(true);
    }

    @Override
    public URLConnection of(String url, boolean post) throws MalformedURLException, IOException {
        return this.requestBuilder.addCookies(cookiesSupplier.get())
                .url(new URL(null, url, new com.bc.net.util.HttpStreamHandlerForBadStatusLine()))
                .method(post ? "POST" : "GET")
                .build();
    }
}
