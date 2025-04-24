package org.example.Models;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    private final int id;
    private final List<String> topics;

    public Client(InetAddress address) {
        this.id = new Random().nextInt(100);
        this.topics = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public List<String> getTopics() {
        return topics;
    }
}
