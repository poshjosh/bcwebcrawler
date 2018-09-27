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

package com.bc.webcrawler.predicates;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 27, 2018 4:09:49 PM
 */
public class LinkHasExtensionTest implements Predicate<String>, Serializable {

    @Override
    public boolean test(String link) {
        
        final int dot;
        
        final int slash = link.lastIndexOf('/');
//        System.out.println("lastIndexOf('/') = " + slash);
        
        if(slash == -1) {
            
            dot = -1;
            
        }else{    
        
            dot = link.substring(slash).lastIndexOf('.');
//            System.out.println("lastIndexOf('.') = " + dot);
        }
        
        return dot != -1;
    }
}
