package com.bc.webcrawler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author USER
 */
public class BufferInMemoryStore<E> implements Buffer<E>{
    
    private final Collection<E> store;
    
    private boolean deleted;

    public BufferInMemoryStore() {
        this(new HashSet());
    }
    
    public BufferInMemoryStore(Collection<E> store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
    
    @Override
    public void delete() {
        deleted = true;
        store.clear();
    }
    
    @Override
    public int size() {
        requireNotDeleted();
        return store.size();
    }

    @Override
    public boolean isEmpty() {
        requireNotDeleted();
        return store.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        requireNotDeleted();
        return store.contains((E)o);
    }

    @Override
    public boolean add(E e) {
        requireNotDeleted();
        return store.add(e);
    }

    @Override
    public boolean addAll(Buffer<E> buffer) {
        int added = 0;
        for(E element : buffer) {
            if(add(element)) {
                ++added;
            }else{
                throw new IllegalStateException("Only " + added + " elements were successfully added");
            }
        }
        return added == buffer.size();
    }

    @Override
    public boolean containsAll(Buffer<?> buffer) {
        int contains = 0;
        for(Object element : buffer) {
            if(contains(element)) {
                ++contains;
            }else{
                break;
            }
        }
        return contains == buffer.size();
    }

    @Override
    public Iterator<E> iterator() {
        requireNotDeleted();
        return new BufferIterator();
    }
    
    private void requireNotDeleted() {
        if(this.isDeleted()) {
            throw new java.lang.IllegalStateException("Deleted");
        }
    }

    private class BufferIterator implements Iterator<E>{
        private final Iterator<E> iter;
        public BufferIterator() {
            this.iter = store.iterator();
        }
        @Override
        public boolean hasNext() {
            return this.iter.hasNext();
        }
        @Override
        public E next() {
            return this.iter.next();
        }
    }
}
