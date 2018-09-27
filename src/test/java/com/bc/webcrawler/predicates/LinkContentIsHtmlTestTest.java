/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bc.webcrawler.predicates;

import com.bc.webcrawler.ConnectionProviderImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.webcrawler.ConnectionProvider;
import java.util.function.BiPredicate;

/**
 *
 * @author Josh
 */
public class LinkContentIsHtmlTestTest {
    
    public LinkContentIsHtmlTestTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testTest() {
        
        System.out.println("test()");
        
        final ConnectionProvider urlConnProvider = 
                new ConnectionProviderImpl(() -> Collections.EMPTY_LIST);
        
        final BiPredicate<String, Boolean> instance = new LinkContentIsHtmlTest(
                urlConnProvider, 10_000, 30_000
        );
        
        String link = "http://www.looseboxes.com";
        boolean passed = instance.test(link, Boolean.TRUE);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        
        link = "http://www.looseboxes.com/legal/licenses/software.html";
        passed = instance.test(link, Boolean.TRUE);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);

        link = "http://www.looseboxes.com/idisc/images/appicon.png";
        passed = instance.test(link, Boolean.TRUE);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        
        link = "http://www.looseboxes.com/legal/licenses/software.jpg";
        passed = instance.test(link, Boolean.TRUE);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
    }
}
