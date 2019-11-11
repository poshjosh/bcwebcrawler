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
import com.bc.webcrawler.TestBase;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Josh
 */
public class SameHostTestTest extends TestBase {
    
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
        
        final String lhs = "http://www.google.com"; 
        final Map<String, Boolean> params = new HashMap<>();
        params.put("https://www.google.com", Boolean.TRUE);
        params.put("https://www.google.com/", Boolean.TRUE);
        params.put("https://mail.google.com/", Boolean.TRUE);
        params.put("https://google.com/", Boolean.TRUE);
        params.put("https://auth.mail.google.com/", Boolean.TRUE);
        params.put("https://googlemail.com/", Boolean.FALSE);
        params.put("https://com.google", Boolean.FALSE);
        params.put("https://www.google", Boolean.FALSE);
        
        this.test(lhs, params);

        System.out.println("Consumed. Memory: " + Util.usedMemory(mb4) +
                ", time: " + (System.currentTimeMillis() - tb4));
        
        final String left = getRandomLink();
        final Predicate<String> test = this.newPredicate(lhs);
        for(int i=0; i<10; i++) {
            final String right = getRandomLink();
            System.out.println("Result: " + test.test(right) + ", LHS: " + left + ", RHS: " + right);
        }
    }
    
    public void test(String lhs, Map<String, Boolean> params) throws MalformedURLException {
        
        final Predicate<String> instance = this.newPredicate(lhs);
        
        for(String key : params.keySet()) {
            
            final Boolean expResult = params.get(key);
            
            final Boolean result = instance.test(key);

            final String msg = "\nLHS: " + lhs + "\nRHS: " + key + "\nExpected: " + expResult + ". Found: " + result;
            
            System.out.println(msg);
            
            assertEquals(msg, expResult, result);
        }
    }
    
    public Predicate<String> newPredicate(String str) throws MalformedURLException {
        return new SameHostTest(str);
    }
}
