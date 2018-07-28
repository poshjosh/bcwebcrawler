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

import java.net.URL;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 6:15:58 PM
 */
public class PrintLinks {

    public static void main(String... args) throws Exception{
        for(String link : SampleValues.LINKS) {
            print(new URL(link));
        }
    }
    
    private static void print(URL url) {
        System.out.println("\nURL: " + url);
        System.out.println("URL.getAuthority(): " + url.getAuthority());
        System.out.println("URL.getFile(): " + url.getFile());
        System.out.println("URL.getHost(): " + url.getHost());
        System.out.println("URL.getPath(): " + url.getPath());
        System.out.println("URL.getRef(): " + url.getRef());
    }
}
