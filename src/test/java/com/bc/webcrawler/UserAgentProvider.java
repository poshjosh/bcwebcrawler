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

import com.bc.net.util.UserAgents;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2017 9:36:46 PM
 */
public class UserAgentProvider implements Function<String, String>, Serializable {

    private final UserAgents userAgents;
    
    private final boolean mobile;

    public UserAgentProvider() {
        this(new UserAgents(), false);
    }
    
    public UserAgentProvider(UserAgents userAgents, boolean mobile) {
        this.userAgents = userAgents;
        this.mobile = mobile;
    }
    
    @Override
    public String apply(String link) {
        try{
            return userAgents.getAny(link, mobile);
        }catch(MalformedURLException e) {
            return userAgents.getAny(mobile);
        }
    }
}
