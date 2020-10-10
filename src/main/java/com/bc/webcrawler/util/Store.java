package com.bc.webcrawler.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author hp
 */
public interface Store<E> {
    
    default boolean isEmpty() {
        return count() < 1;
    }
    
    default Stream<E> streamAll(int offset, int limit) {
        return StreamSupport.stream(getAll(offset, limit).spliterator(), false);
    }
    
    boolean contains(E elem);

    Iterable<E> getAll(int offset, int limit);
    
    void flush();
    
    E save(E elem);
    
    E delete(E elem);
    
    long count();
}
