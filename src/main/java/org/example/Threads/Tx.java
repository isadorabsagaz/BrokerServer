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
                byte[] msg = sendingQueue.take(); //blocks till gets a message
                out.write(msg);
                out.write('\n');
                out.flush();
            }
        } catch (InterruptedException | IOException  e) {
            System.out.println("Tx interrupted to client "+ service.client.getId()+ ": " + e.getMessage());
        } finally {
            service.close();
        }
    }
}
