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

import com.bc.util.Util;
import com.bc.webcrawler.SampleValues;
import java.net.MalformedURLException;
import java.util.function.Predicate;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class SameHostTestTest {
    
    public SameHostTestTest() { }
    
    /**
     * Test of test method, of class SameHostTest.
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testTest() throws MalformedURLException {
        System.out.println("test");
        
        final long mb4 = Util.availableMemory();
        final long tb4 = System.currentTimeMillis();
        
        final Predicate<String> instance = new SameHostTest(SampleValues.getRandomLink());
        
        for(int i=0; i<100; i++) {
            
            final String link = SampleValues.getRandomLink();
            
            instance.test(link);
        }

        System.out.println("Consumed. Memory: " + Util.usedMemory(mb4) +
                ", time: " + (System.currentTimeMillis() - tb4));
    }
}
