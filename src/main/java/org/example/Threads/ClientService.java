package org.example.Threads;

import com.google.gson.Gson;
import org.example.Managers.TopicManager;
import org.example.Models.Message;
import org.example.Models.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientService {

    protected final Map<String, List<byte[]>> pendingMessages = new ConcurrentHashMap<>();

    protected final Socket socket;
    protected final Client client;
    protected final BlockingQueue<byte[]> sendingQueue;
    protected OutputStream out;
    protected InputStream in;

    public ClientService(Socket socket) throws IOException {
        this.socket = socket;
        this.client = new Client(socket.getInetAddress());
        this.sendingQueue = new LinkedBlockingQueue<>();
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public void init() {
        new Thread(new Rx(in, this)).start();
        new Thread(new Tx(out, sendingQueue, this)).start();

        List<byte[]> pending = pendingMessages.remove(client.getId());
        if (pending != null) {
            for (byte[] data : pending) {
                sendMessage(data);
            }
        }
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

    private boolean isConnected(){
        return !socket.isClosed() && socket.isConnected();
    }

    public void processMessage(Message msg) {
        switch (msg.type.toUpperCase()) {

            //list all global topics
            case "LIST_ALL_TOPICS" -> {
                List<String> allTopics = new ArrayList<>(TopicManager.topics.keySet());
                Message response = new Message();
                response.type = "TOPICS_LIST";
                response.payload = new Gson().toJson(allTopics);
                sendMessage(new Gson().toJson(response).getBytes());
                System.out.println("Sending topics list to client: " + allTopics);
            }

            //list client's topics
            case "LIST_MY_TOPICS" -> {
                List<String> myTopics = new ArrayList<>(client.getTopics());
                Message response = new Message();
                response.type = "TOPICS_LIST";
                response.payload = new Gson().toJson(myTopics);
                sendMessage(new Gson().toJson(response).getBytes());
                System.out.println("Sending topics list to client: " + myTopics);
            }

            //subscribe in the topic
            case "SUBSCRIBE" -> {

                if (!client.getTopics().contains(msg.topic)){
                    TopicManager.topics.computeIfAbsent(msg.topic, k -> new CopyOnWriteArrayList<>()).add(this); //add client to global topic
                    client.getTopics().add(msg.topic); //add topic to client's list
                }
            }
            //publish message to the topic
            //TODO: cryptographic the messages
            case "PUBLISH" -> {
                    //generate message to publish in topic
                    Message response = new Message();
                    response.type = "MESSAGE";
                    response.topic = msg.topic;
                    response.payload = msg.payload;
                    response.date = msg.date;
                    response.time = msg.time;

                    String json = new Gson().toJson(response);
                    byte[] data = json.getBytes();

                    //sends message to people subscribed in that topic
                    List<ClientService> subscribers = TopicManager.topics.get(msg.topic);
                    if (subscribers != null) {
                        for (ClientService subscriber : subscribers) {
                            if (subscriber.isConnected()){
                                subscriber.sendMessage(data);
                            }
                            else {
                                pendingMessages
                                        .computeIfAbsent(subscriber.client.getId(), k -> new ArrayList<>())
                                        .add(data);
                            }
                        }
                    }
            }
            default -> System.out.println("Unknown command: " + msg.type);
        }
    }
}
