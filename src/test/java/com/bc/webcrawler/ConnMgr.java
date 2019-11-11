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

import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.http.ConnectionMonitor;
import org.htmlparser.http.HttpHeader;
import org.htmlparser.util.ParserException;

/**
 *
 * @author Josh
 */
public class ConnMgr {

    private transient static final Logger LOG = Logger.getLogger(ConnMgr.class.getName());
    
    public static final int CONNECT_TIMEOUT = 30_000;
    public static final int READ_TIMEOUT = 90_000;
    
    private ConnMgr() { }
    
    public static ConnectionManager getDefault() {
        return ConnectionManagerSingletonHolder.INSTANCE;
    }
    
    private static class ConnectionManagerSingletonHolder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
        static{
            INSTANCE.setMaxRedirects(3);
            INSTANCE.setRedirectionProcessingEnabled(true);
            INSTANCE.setCookieProcessingEnabled(false);
            INSTANCE.setMonitor(createConnectionMonitor());
        }
    }
    
    public static ConnectionMonitor createConnectionMonitor(){
        return new ConnectionMonitor(){
            @Override
            public void preConnect(HttpURLConnection connection) throws ParserException {
                LOG.log(Level.FINER, "Connection request header: {0}", HttpHeader.getRequestHeader(connection));
                if(CONNECT_TIMEOUT > -1) {
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                }
                if(READ_TIMEOUT > -1) {
                    connection.setReadTimeout(READ_TIMEOUT);
                }
            }
            @Override
            public void postConnect(HttpURLConnection connection) throws ParserException {
                LOG.log(Level.FINER, "Connection response header: {0}", HttpHeader.getResponseHeader(connection));
            }
        };
    }
}