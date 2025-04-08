package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final int SERVER_PORT = 5000;

    public static void main(String[] args)   {

        ExecutorService pool = Executors.newCachedThreadPool();

        try (
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Broker is listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection from " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
