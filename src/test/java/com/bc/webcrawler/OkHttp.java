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

import java.util.concurrent.TimeUnit;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 *
 * @author Josh
 */
public class OkHttp {
    
    public static final int CONNECT_TIMEOUT = ConnMgr.CONNECT_TIMEOUT;
    public static final int READ_TIMEOUT = ConnMgr.READ_TIMEOUT;
    
    private OkHttp() { }
    
    public static OkHttpClient getDefaultClient() {
        return OkHttpClientSingletonHolder.INSTANCE;
    }
    
    private static class OkHttpClientSingletonHolder {
        private static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true) 
                .cookieJar(CookieJar.NO_COOKIES).build();
    }
}
