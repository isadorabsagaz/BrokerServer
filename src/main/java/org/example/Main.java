package org.example;

import org.example.Threads.ClientService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int SERVER_PORT = 5000;
    private static final  ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args)   {
        //begins connection
        try (
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Broker is listening on port " + SERVER_PORT);

            //listens to clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection from " + clientSocket.getInetAddress());

                //socket to threads pool
                try {
                    ClientService handler = new ClientService(clientSocket);
                    pool.execute(handler::init);
                } catch (IOException e){
                    System.out.println("Error creating ClientHandler " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        } finally {
            pool.shutdown(); //closes pool when server is closed
        }
    }
}
