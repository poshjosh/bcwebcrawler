package com.bc.webcrawler.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author hp
 */
public class QueueWithBackingStore implements com.bc.webcrawler.util.Queue<String> {

    private static final Logger LOG = Logger.getLogger(QueueWithBackingStore.class.getName());
    
    private final int bufferSize;
    private final BlockingQueue<String> queue;
    private final Store<String> store;

    public QueueWithBackingStore(int bufferSize, BlockingQueue<String> queue, Store<String> repository) {
        this.bufferSize = bufferSize;
        this.queue = Objects.requireNonNull(queue);
        this.store = Objects.requireNonNull(repository);
    }

    @Override
    public Iterator<String> iterator() {
        final int size = this.size();
        final List<String> all = new ArrayList<>(size);
        all.addAll(queue);
        final int toAdd = size - queue.size();
        if(toAdd > 0) {
            store.streamAll(0, toAdd)
                    .collect(Collectors.toCollection(() -> all));
        }
        return all.iterator();
    }
    
    @Override
    public boolean addAll(Collection<String> c) {
        boolean added = false;
        if( ! c.isEmpty()) {
            try{
                for(String link : c) {
                    if(this.add(link, false)) {
                        added = true;
                    }
                }
            }finally{
                if(added) {
                    store.flush();
                }
            }
        }
        return added;
    }
    
    @Override
    public boolean add(String elem) {
        return this.add(elem, true);
    }

    private boolean add(String elem, boolean flush) {
        final boolean addToQueue = queue.size() < bufferSize;
        LOG.finest(() -> "Adding to " + (addToQueue?"queue":"store") + ", link: " + elem);
        boolean ret;
        if(addToQueue) {
            final boolean queueContains = queue.contains(elem);
            if( ! queueContains) {
                ret = queue.add(elem);
            }else{
                LOG.finest(() -> "Queue already contains: " + elem);
                ret = false;
            }
        }else{
            final boolean storeContains = store.contains(elem);
            if( ! storeContains) {
                store.save(elem);
                ret = true;
                if(flush) {
                    store.flush();
                }
            }else{
                LOG.finest(() -> "Store already contains: " + elem);
                ret = false;
            }
        }
        return ret;
    }
    
    @Override
    public String poll() {
        final String ret = queue.poll();
        this.update();
        return ret;
    }
    
    @Override
    public int size() {
        final long size = queue.size() + store.count();
        return (int)size;
    }
    
    @Override
    public String poll(long timeout, TimeUnit timeUnit) throws InterruptedException{
        final String ret = queue.poll(timeout, timeUnit);
        this.update();
        return ret;
    }
    
    private void update() {
        final int toAdd = bufferSize - queue.size();
//        LOG.finest(() -> "To add to queue: " + toAdd + ", buffer size: " + 
//                bufferSize + ", queue size: " + queue.size());
        if(toAdd > 0) {
            final List<String> addToQueue = store.streamAll(0, toAdd)
                    .map(url -> store.delete(url))
                    .collect(Collectors.toList());
            LOG.log(Level.FINEST, "To add to queue: {}", addToQueue);
            queue.addAll(addToQueue);
        }
    }
}
