/*
 * Copyright 2017 NUROX Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class MultiListTest {
    
    private List alphabets;
    private List numbers;
    private List names;
    
    public MultiListTest() { }

    private void init() {
        this.alphabets = new ArrayList(Arrays.asList("A", "B", "C", "D", "E"));
        this.numbers = new ArrayList(Arrays.asList("1", "2", "3", "4"));
        this.names = new ArrayList(Arrays.asList("Ade", "Baba", "Chukwu", "Dadi", "Ejeh", "Nonso"));
    }
    
    private MultiList getDefaultInstance() {
        MultiList instance = new MultiList(alphabets, numbers, names);
        return instance;
    }
    /**
     * Test of getPageAt method, of class MultiList.
     */
    @Test
    public void testGetPageAt() {
        System.out.println("getPageAt");

        this.init();
        
        final MultiList instance = this.getDefaultInstance();
        
        int pageIndex = 1;
        List expResult = numbers;
        List result = instance.getPageAt(pageIndex);
        assertEquals(expResult, result);
        
        pageIndex = 0;
        expResult = alphabets;
        result = instance.getPageAt(pageIndex);
        assertEquals(expResult, result);

        pageIndex = 2;
        expResult = names;
        result = instance.getPageAt(pageIndex);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPageFor method, of class MultiList.
     */
    @Test
    public void testGetPageFor() {
        System.out.println("getPageFor");

        this.init();
        
        final MultiList instance = this.getDefaultInstance();
        
        int index = 5;
        List expResult = numbers;
        List result = instance.getPageFor(index);
        assertEquals(expResult, result);
        
        index = 4;
        expResult = alphabets;
        result = instance.getPageFor(index);
        assertEquals(expResult, result);

        index = 10;
        expResult = names;
        result = instance.getPageFor(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class MultiList.
     */
    @Test
    public void testGet() {
        System.out.println("get");

        this.init();
        
        final MultiList instance = this.getDefaultInstance();
        
        int index = 6;
        Object expResult = "2";
        Object result = instance.get(index);
        assertEquals(expResult, result);
        
        index = 1;
        expResult = "B";
        result = instance.get(index);
        assertEquals(expResult, result);

        index = 13;
        expResult = "Ejeh";
        result = instance.get(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of size method, of class MultiList.
     */
    @Test
    public void testSize() {
        System.out.println("size");

        this.init();
        
        MultiList instance;
        
        instance = new MultiList(this.alphabets, this.numbers, this.names);
        
        int expResult = this.alphabets.size() + this.numbers.size() + this.names.size();
        int result = instance.size();
        assertEquals(expResult, result);

        instance = new MultiList(this.alphabets, this.numbers);
        
        expResult = this.alphabets.size() + this.numbers.size();
        result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class MultiList.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        
        this.init();
        
        MultiList instance;
        instance = this.getDefaultInstance();
        
        final int previousSize = instance.size();
        int index = 6;
        Object expResult = "2";
        Object result = instance.remove(index);
        assertEquals(expResult, result);
        assertEquals(instance.size(), previousSize - 1);
        
        this.init();
        
        instance = this.getDefaultInstance();
        
        index = 13;
        expResult = "Ejeh";
        result = instance.remove(index);
        assertEquals(expResult, result);
        assertEquals(instance.size(), previousSize - 1);
    }

    /**
     * Test of add method, of class MultiList.
     */
    @Test
    public void testAdd() {
        System.out.println("add");

        this.init();
        
        MultiList instance;
        instance = this.getDefaultInstance();
        
        final int previousSize = instance.size();
        int index = 9;
        String element = "5";
        List expResult = new ArrayList(this.numbers);
        expResult.add(element);
        instance.add(index, element); 
//        assertEquals(expResult, this.numbers);
        assertEquals(instance.size(), previousSize + 1);
    }

    /**
     * Test of set method, of class MultiList.
     */
    @Test
    public void testSet() {
        System.out.println("set");

        this.init();
        
        MultiList instance;
        instance = this.getDefaultInstance();
        
        final int previousSize = instance.size();
        int index = 9;
        String element = "Alo";
        List expResult = new ArrayList(this.names);
        expResult.set(0, element);
        instance.set(index, element); 
        assertEquals(expResult, this.names);
        assertEquals(instance.size(), previousSize);
    }
}
