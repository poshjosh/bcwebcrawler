package com.bc.webcrawler;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author hp
 */
public interface RetryOnExceptionTestSupplier extends Supplier<Predicate<Throwable>>{
    
}
