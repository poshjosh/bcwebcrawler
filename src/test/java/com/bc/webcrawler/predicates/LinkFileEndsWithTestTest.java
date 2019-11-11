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

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class LinkFileEndsWithTestTest {
    
    public LinkFileEndsWithTestTest() { }

    /**
     * Test of test method, of class LinkFileEndsWithTest.
     */
    @Test
    public void testTest() {
        this.test("http://www.abc/a.jpg", ".jpg", true);
        this.test("http://www.sunnewsonline.com/wp-content/uploads/2018/10/Eko-Disco-Logo.jpg", ".jpg", true);
        this.test("http://www.sunnewsonline.com/wp-content/uploads/2018/10/Eko-Disco-Logo.jpg", ".pdf", false);
        this.test("https://www.dailytrust.com.ng/pdf/Advertise.pdf", ".pdf", true);
        this.test("https://www.dailytrust.com.ng/pdf/Advertise.pdf", ".jpg", false);
    }

    public void test(String link, String extension, boolean expResult) {
        System.out.println("test");
        final LinkFileEndsWithTest instance = new LinkFileEndsWithTest();
        final boolean result = instance.test(link, extension);
        System.out.println(LocalDateTime.now() + ". Passed: " + result + 
                ", extension: " + extension + ", link: " + link);
        assertEquals(expResult, result);
    }
}
