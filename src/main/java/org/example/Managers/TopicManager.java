package org.example.Managers;

import org.example.Threads.ClientService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManager {
    public static final Map<String, List<ClientService>> topics = new ConcurrentHashMap<>();
}
