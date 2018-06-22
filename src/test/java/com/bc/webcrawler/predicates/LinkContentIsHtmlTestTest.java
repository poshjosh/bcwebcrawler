/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bc.webcrawler.predicates;

import com.bc.webcrawler.ConnectionProviderImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Predicate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.webcrawler.ConnectionProvider;

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
        
        final Predicate<String> instance = new LinkContentIsHtmlTest(
                urlConnProvider, 10_000, 30_000
        );
        
        String link = "http://www.looseboxes.com";
        boolean passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
        
        link = "http://www.looseboxes.com/legal/licenses/software.html";
        passed = instance.test(link);
        System.out.println(LocalDateTime.now() + ". Passed: " + passed + ", link: " + link);
    }
}
