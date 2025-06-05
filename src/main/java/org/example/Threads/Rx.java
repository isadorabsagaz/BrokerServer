package org.example.Threads;

import com.google.gson.Gson;
import org.example.Models.Message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Rx implements Runnable {
        private final InputStream in;
        private final ClientService service;

    public Rx(InputStream in, ClientService service) {
        this.in = in;
        this.service = service;
    }

    public void run() {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);

                String message = new String(data, StandardCharsets.UTF_8);
                //System.out.println("Received (raw)" + message);

                try {
                    Message msg = new Gson().fromJson(message, Message.class);
                    System.out.println("Client  : " + service.client.getId());
                    System.out.println("Type    : " + msg.type);
                    System.out.println("Topic   : " + msg.topic);
                    System.out.println("Message : " + msg.payload);
                    System.out.println("Date    : " + msg.date);
                    System.out.println("Time    : " + msg.time +"\n");

                }
                catch (Exception e) {
                    System.out.println("Failed to parse message: " + message);
                }

                Message msg = new Gson().fromJson(message, Message.class);
                service.processMessage(msg);
            }
        } catch (IOException e) {
            System.out.println("Error reading (Rx) client " + service.client.getId() + ": " + e.getMessage());
            e.fillInStackTrace();
        } finally {
            service.close();
        }
    }
}
