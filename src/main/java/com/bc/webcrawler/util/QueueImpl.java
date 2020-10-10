package com.bc.webcrawler.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author hp
 */
public class QueueImpl<E> implements com.bc.webcrawler.util.Queue<E>{
    
    private final BlockingQueue<E> delegate;

    public QueueImpl(BlockingQueue<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean add(E elem) {
        return delegate.add(elem);
    }

    @Override
    public boolean addAll(Collection<E> elem) {
        return delegate.addAll(elem);
    }

    @Override
    public E poll() {
        return delegate.poll();
    }

    @Override
    public E poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return delegate.poll(timeout, timeUnit);
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
