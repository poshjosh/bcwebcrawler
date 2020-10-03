package com.bc.webcrawler.links;

import java.util.Set;
import java.util.function.Function;

/**
 * @author hp
 */
public interface LinksExtractor<E> extends Function<E, Set<String>>{
    
}
