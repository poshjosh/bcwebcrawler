package com.bc.webcrawler.util;

/**
 * @author USER
 */
public interface Buffer<E> extends Iterable<E>{
    
    boolean add(E element);
    
    boolean addAll(Buffer<E> buffer);
    
    int size();
    
    /**
     * <p>Confirms if this buffer's content has been deleted</p>
     * @return <tt>true</tt> if {@link #delete()} has been called, otherwise returns <tt>false</tt>
     *
     * @see #isDeleted() 
     */
    boolean isDeleted();
    
    /**
     * <p>Empties this buffer's contents.</p>
     * Once this method is called. All subsequent operations must throw
     * {@link java.lang.IllegalStateException}
     * 
     * @see #isDeleted() 
     */
    void delete();

    boolean contains(Object element);
    
    boolean containsAll(Buffer<?> buffer);
    
    boolean isEmpty();
}
