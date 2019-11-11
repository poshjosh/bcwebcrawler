/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bc.webcrawler.predicates;

import java.time.LocalDateTime;
import org.junit.Test;
import com.bc.webcrawler.TestBase;
import java.util.Collection;
import java.util.function.Predicate;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ContentTypeRequestOkHttp;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Josh
 */
public class LinkContentIsHtmlTestTest extends TestBase {
    
    public LinkContentIsHtmlTestTest() { }

    @Test
    public void testAll() {
        
        System.out.println("testAll()");
        
        final ContentTypeRequest contentTypeReq = new ContentTypeRequestOkHttp();
        
        Predicate<String> instance = new LinkContentIsHtmlTest(
                contentTypeReq, 10_000, 30_000, true
        );
        
        String link;
        boolean passed;
        
        link = "http://www.all9janews.com/images/appicon.png";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
      
        link = "http://www.looseboxes.com/legal/licenses/software.jpg";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
        
        link = "http://www.sunnewsonline.com/wp-content/uploads/2018/10/Eko-Disco-Logo.jpg";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
        
        link = "https://www.dailytrust.com.ng/pdf/Advertise.pdf";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertFalse(passed);
        
        instance = new LinkContentIsHtmlTest(
                contentTypeReq, 10_000, 30_000, false
        );

        link = "http://www.looseboxes.com";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        assertTrue(passed);
        
        link = "http://www.looseboxes.com/legal/licenses/software.html";
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
