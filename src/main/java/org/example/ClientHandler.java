package org.example;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, List<PrintWriter>> topics;
    private final Client client;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.topics = new ConcurrentHashMap<>();
        this.client = new Client(socket.getInetAddress());
    }

    @Override
    public void run() {
        try (
                //creates buffer to read message and writer to send
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
        ) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String line;

            while ((line = in.readLine()) != null) {
                Message msg = new Gson().fromJson(line, Message.class);

                switch (msg.type.toUpperCase()) {

                    //subscribe in the topic
                    case "SUBSCRIBE" -> {
                        topics.computeIfAbsent(msg.topic, k -> new CopyOnWriteArrayList<>())
                                .add(out);
                        client.getTopics().add(msg.topic);  //adding topic to client's list
                        System.out.println("Client " + client.getId() + " SUBSCRIBED to topic: " + msg.topic);
                    }

                    //publish message to the topic
                    case "PUBLISH" -> {
                        if (client.getTopics().contains(msg.topic)) {
                            System.out.println("Client " + client.getId() + " PUBLISHED to topic: " + msg.topic);
                            System.out.println("Message: " + msg.payload);
                            Message response = new Message();

                            //generate message to publish in topic
                            response.type = "MESSAGE";
                            response.topic = msg.topic;
                            response.payload = msg.payload;

                            String json = new Gson().toJson(response);
                            List<PrintWriter> registered = topics.get(msg.topic);

                            //shows message to people subscribed in that topic
                            if (registered != null) {
                                for (PrintWriter register : registered) {
                                    register.println(json);
                                }
                            }
                        } else System.out.println("Client not subscribed to topic: " + msg.topic);
                    }
                    default -> System.out.println("Unknown command: " + msg.type);
                }
            }
            System.out.println("Client " + client.getId() + " disconnected");
            socket.close();

        } catch (IOException e) {
            e.fillInStackTrace();
            throw new RuntimeException();
        }
    }
}
