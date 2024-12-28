package com.sd56.common.connectors;

import com.sd56.common.util.logger.Logger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {
    private TaggedConnection connection;
    private Map<Integer, Entry> map = new HashMap<>();
    private final Lock l = new ReentrantLock();
    private IOException exception = null;

    private class Entry{
        ArrayDeque<byte[]> queue = new ArrayDeque<>();
        Condition cond = l.newCondition();
    }

    private Entry get(int tag){
        Entry entry = map.get(tag);
        if(entry == null){
            entry = new Entry();
            map.put(tag, entry);
        }
        return entry;
    }

    public Demultiplexer(TaggedConnection connection) {
        this.connection = connection;
    }

    public void start(){
        Logger logger = Logger.getGlobalLogger();

        new Thread(() -> {
            try{
                for(;;){
                    TaggedConnection.Frame f = connection.receive();
                    l.lock();
                    try{
                        Entry e = get(f.tag);
                        e.queue.add(f.data);
                        logger.debug("[DEMUX] Attempting to enqueue a message.");
                        e.cond.signal();
                    } finally{
                        l.unlock();
                    }
                }
            } catch (IOException e) {
                l.lock();
                try{
                    this.exception = e;
                    for(Entry entry: map.values()){
                        entry.cond.signal();
                    }
                } finally {
                    l.unlock();
                }

                // if (!Thread.currentThread().isInterrupted()) throw new RuntimeException(e);
            }
        }).start();
    }

    public void send(TaggedConnection.Frame frame) throws IOException {
        connection.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        connection.send(tag, data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        Logger logger = Logger.getGlobalLogger();

        l.lock();
        try {
            Entry e = get(tag);
            while(e.queue.isEmpty() && exception == null) e.cond.await();
            logger.debug("[DEMUX] Attempting to dequeue a message.");

            byte[] b = e.queue.poll();
            if(b != null){
                return b;
            } else {
                throw exception;
            }
        } finally {
            l.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }
}
