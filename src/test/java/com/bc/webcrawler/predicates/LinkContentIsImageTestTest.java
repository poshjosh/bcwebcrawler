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

import com.bc.webcrawler.TestBase;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Predicate;
import org.junit.Test;
import static org.junit.Assert.*;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ContentTypeRequestOkHttp;

/**
 *
 * @author Josh
 */
public class LinkContentIsImageTestTest extends TestBase {
    
    public LinkContentIsImageTestTest() { }
    
    @Test
    public void testAll() {
        
        System.out.println("testAll()");
        
        final ContentTypeRequest urlConnProvider = new ContentTypeRequestOkHttp();
        
        Predicate<String> instance = new LinkContentIsImageTest(
                urlConnProvider, 10_000, 30_000, true
        );
        
        String link = "http://www.looseboxes.com";
        boolean passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
        
        if(true) {
            return;
        }
        
        link = "http://www.looseboxes.com/legal/licenses/software.html";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);

        link = "https://www.dailytrust.com.ng/pdf/Advertise.pdf";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
        
        instance = new LinkContentIsImageTest(
                urlConnProvider, 10_000, 30_000, false
        );

        link = "http://www.looseboxes.com/idisc/images/appicon.png";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertTrue(passed);
      
        link = "http://www.looseboxes.com/legal/licenses/software.jpg";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertTrue(passed);

        link = "http://www.sunnewsonline.com/wp-content/uploads/2018/10/Eko-Disco-Logo.jpg";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertTrue(passed);
        
        final Extensions extMgr = new Extensions();
        final Collection<String> c = extMgr.forMulti(Extensions.HTML, Extensions.SERVER_PAGE, Extensions.IMAGE, Extensions.AUDIO, Extensions.VIDEO);
        for(String ext : c) {
            passed = instance.test(ext);
//            System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + ext);
        }
    }
}
