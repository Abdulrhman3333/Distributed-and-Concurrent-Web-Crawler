package com.mycompany.finalproject405;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Slave {

    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final Map<String, List<String>> urlMap;

    public Slave(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(2);  // Example with 2 threads
        this.urlMap = new ConcurrentHashMap<>();
        System.out.println("Slave running on port: " + port);
    }

    public void start() {
        System.out.println("Slave is ready to accept tasks...");
        try (Socket masterSocket = serverSocket.accept(); ObjectInputStream input = new ObjectInputStream(masterSocket.getInputStream()); ObjectOutputStream output = new ObjectOutputStream(masterSocket.getOutputStream())) {

            System.out.println("Receiving TaskInfo from master...");
            TaskInfo taskInfo = (TaskInfo) input.readObject();
            System.out.println("TaskInfo received: " + taskInfo);

            LinkedList<String> urlQueue = new LinkedList<>(taskInfo.getSeedUrls());

            for (int i = 0; i < 2; i++) {
                threadPool.submit(new TaskProcessor(urlQueue, urlMap, taskInfo.isAllowDuplicates(), taskInfo.getMaxLevels()));
            }

            threadPool.shutdown();
            while (!threadPool.isTerminated()) {
                // Wait for all threads to finish
            }

            System.out.println("Sending processed URLs back to master...");
            output.writeObject(urlMap);
            output.flush();
            System.out.println("Processed URLs sent to master: " + urlMap);

            // Send a status message back to the master
            Message statusMessage = new Message("Processing completed successfully.");
            output.writeObject(statusMessage);
            output.flush();
            System.out.println("Status message sent to master: " + statusMessage.getText());

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling master request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class TaskProcessor implements Runnable {

        private final LinkedList<String> urlQueue;
        private final Map<String, List<String>> urlMap;
        private final boolean allowDuplicates;
        private final int maxLevels;
        private int currentLevel = 0;

        public TaskProcessor(LinkedList<String> urlQueue, Map<String, List<String>> urlMap, boolean allowDuplicates, int maxLevels) {
            this.urlQueue = urlQueue;
            this.urlMap = urlMap;
            this.allowDuplicates = allowDuplicates;
            this.maxLevels = maxLevels;
        }

        @Override
        public void run() {
            while (currentLevel <= maxLevels) {
                String url;
                synchronized (urlQueue) {
                    if (urlQueue.isEmpty()) {
                        break;
                    }
                    url = urlQueue.poll();
                }

                if (url != null) {
                    processUrl(url);
                    currentLevel++;
                }
            }
        }

        private void processUrl(String url) {
            System.out.println("Processing URL: " + url);
            // Simulating URL processing
            ArrayList<String> extractedUrls = new ArrayList<>();
            extractedUrls.add(url + "/dummy1");
            extractedUrls.add(url + "/dummy2");

            synchronized (urlMap) {
                urlMap.computeIfAbsent(url, k -> new ArrayList<>());
                if (allowDuplicates) {
                    urlMap.get(url).addAll(extractedUrls);
                } else {
                    for (String extractedUrl : extractedUrls) {
                        if (!urlMap.get(url).contains(extractedUrl)) {
                            urlMap.get(url).add(extractedUrl);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            int port = 5000;  // Example port number
            Slave slave = new Slave(port);
            slave.start();
        } catch (IOException e) {
            System.err.println("Failed to start the slave: " + e.getMessage());
        }
    }
}
