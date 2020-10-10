package com.bc.webcrawler.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Comparator such that elements accepted by the Predicate will come before
 * elements not accepted.
 * @author hp
 */
public class ComparatorForPredicate<T> implements Comparator<T>{
    
    private final Predicate<T> test;

    public ComparatorForPredicate(Predicate<T> test) {
        this.test = Objects.requireNonNull(test);
    }

    @Override
    public int compare(T o1, T o2) {
        final boolean lhs = test.test(o1);
        final boolean rhs = test.test(o2);
        final int result = Boolean.compare(rhs, lhs);
//        System.out.println("LHS: " + o1 + ", RHS: " + o2 + ", result: " + result);
        return result;
    }
}
