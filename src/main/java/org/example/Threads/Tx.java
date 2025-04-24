package org.example.Threads;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class Tx implements Runnable {
    private final OutputStream out;
    private final BlockingQueue<byte[]> sendingQueue;
    private final ClientService service;

    public Tx(OutputStream out, BlockingQueue<byte[]> sendingQueue, ClientService service) {
        this.out = out;
        this.sendingQueue = sendingQueue;
        this.service = service;
    }

    public void run() {
        try {
            while (true) {
                byte[] msg = sendingQueue.take();
                out.write(msg);
                out.flush();
            }
        } catch (InterruptedException | IOException  e) {
            System.out.println("Tx interrupted to client "+ service.client.getId());
            throw new RuntimeException(e);
        } finally {
            service.close();
        }
    }


}
