package org.example.Threads;

import com.google.gson.Gson;
import org.example.Models.Message;
import org.example.Models.Client;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientService {

    protected final Socket socket;
    protected final Map<String, List<ClientService>> topics;
    protected final Client client;
    protected final BlockingQueue<byte[]> sendingQueue;
    protected OutputStream out;
    protected InputStream in;

    public ClientService(Socket socket) throws IOException {
        this.socket = socket;
        this.topics = new ConcurrentHashMap<>();
        this.client = new Client(socket.getInetAddress());
        this.sendingQueue = new LinkedBlockingQueue<>();
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public void init() {
        new Thread(new Rx(in, this)).start();
        new Thread(new Tx(out, sendingQueue, this)).start();
    }

    protected void close(){
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Client " + client.getId() + " disconnected");
    }

    public void sendMessage(byte[] msg) {
        sendingQueue.add(msg);
    }

    public void processMessage(Message msg) {
        switch (msg.type.toUpperCase()) {

            //subscribe in the topic
            case "SUBSCRIBE" -> {
                topics.computeIfAbsent(msg.topic, k -> new CopyOnWriteArrayList<>()).add(this);
                client.getTopics().add(msg.topic);
                System.out.println("Client " + client.getId() + " subscribed to topic: " + msg.topic);
            }
            //publish message to the topic
            //TODO: cryptographic the messages
            case "PUBLISH" -> {
                if (client.getTopics().contains(msg.topic)) {
                    System.out.println("Client " + client.getId() + " PUBLISHED to topic: " + msg.topic);
                    System.out.println("Message: " + msg.payload);

                    //generate message to publish in topic
                    Message response = new Message();
                    response.type = "MESSAGE";
                    response.topic = msg.topic;
                    response.payload = msg.payload;
                    response.date = LocalDate.now();
                    response.time = LocalTime.now();

                    String json = new Gson().toJson(response);
                    byte[] data = json.getBytes();

                    //shows message to people subscribed in that topic
                    List<ClientService> subscribers = topics.get(msg.topic);
                    if (subscribers != null) {
                        for (ClientService subscriber : subscribers) {
                            subscriber.sendMessage(data);
                        }
                    }
                } else System.out.println("Client not subscribed to topic: " + msg.topic);
            }
            default -> System.out.println("Unknown command: " + msg.type);
        }
    }
}
