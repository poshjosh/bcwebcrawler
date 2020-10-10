package com.bc.webcrawler;

import com.bc.webcrawler.util.ComparatorForPredicate;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author hp
 */
public class ComparatorForPredicateTest {
    
    public ComparatorForPredicateTest() { }

    /**
     * Test of compare method, of class ComparatorForPredicate.
     */
    @Test
    public void testCompare() {
        Predicate<String> test = Pattern.compile("\\d{1,}").asPredicate();
        ComparatorForPredicate instance = new ComparatorForPredicate(test);
        PriorityQueue queue = new PriorityQueue(instance);
        queue.add("ABC");
        queue.add("123");
        assertThat(queue.peek(), is("123"));
    }
}
