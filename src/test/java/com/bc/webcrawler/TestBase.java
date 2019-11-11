/*
 * Copyright 2018 NUROX Ltd.
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

import com.bc.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 28, 2018 10:20:41 AM
 */
public class TestBase {

    public static final String [] LINKS = {
        "http://www.looseboxes.com/idisc/feed.jsp?feedid=23098",
        "https://www.google.com/",
        "http://www.looseboxes.com/idisc/feed.jsp#comment",
        "http://google.com",
        "https://www.bellanaija.com",
        "https://mail.google.com"
    };

    public TestBase() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(InputStream ins = loader.getResourceAsStream("META-INF/logging.properties")) {
            LogManager.getLogManager().readConfiguration(ins);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static final String getRandomLink() {
        return LINKS[Util.randomInt(LINKS.length)];
    }
}
