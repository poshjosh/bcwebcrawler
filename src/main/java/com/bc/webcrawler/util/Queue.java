package com.bc.webcrawler.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author hp
 */
public interface Queue<E> extends Iterable<E>{
    
    default Stream<E> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
    
    default boolean isEmpty() {
        return size() < 1;
    }

    boolean add(E elem);

    boolean addAll(Collection<E> elem);

    E poll();

    E poll(long timeout, TimeUnit timeUnit) throws InterruptedException;

    int size();
}
