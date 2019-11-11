package com.bc.webcrawler;

import java.util.Set;
import java.util.function.Function;

/**
 * @author USER
 */

public interface ExtractLinks<E> extends Function<E, Set<String>>{
    
    int countLinksExtracted();
}
