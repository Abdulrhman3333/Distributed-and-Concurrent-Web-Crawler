package com.mycompany.finalproject405;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Master {

    private final List<String> slaveAddresses;
    private final List<String> seedUrls;
    private final boolean allowDuplicates;
    private final int maxLevels;
    private final TextArea logArea;

    public Master(List<String> slaveAddresses, List<String> seedUrls, boolean allowDuplicates, int maxLevels, TextArea logArea) {
        this.slaveAddresses = slaveAddresses;
        this.seedUrls = seedUrls;
        this.allowDuplicates = allowDuplicates;
        this.maxLevels = maxLevels;
        this.logArea = logArea;
    }

    public void sendTasks() {
        try {
            List<TaskInfo> taskInfos = splitTasks();
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < slaveAddresses.size(); i++) {
                String[] parts = slaveAddresses.get(i).split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                log("Connecting to slave at " + host + ":" + port);
                int finalI = i;
                Thread thread = new Thread(() -> {
                    try (Socket slaveSocket = new Socket(host, port); ObjectOutputStream output = new ObjectOutputStream(slaveSocket.getOutputStream()); ObjectInputStream input = new ObjectInputStream(slaveSocket.getInputStream())) {

                        log("Sending TaskInfo to slave: " + taskInfos.get(finalI));
                        output.writeObject(taskInfos.get(finalI));
                        output.flush();
                        log("TaskInfo sent to slave");

                        // response from slave
                        log("Waiting for processed URLs from slave...");
                        Object response = input.readObject();
                        log("Processed URLs received from slave: " + response);
                        if (response instanceof ConcurrentHashMap) {
                            ConcurrentHashMap<String, ArrayList<String>> result = (ConcurrentHashMap<String, ArrayList<String>>) response;
                            log("Processing result...");
                            synchronized (this) {
                                processResult(result);
                            }

                            // Insert result into database
                            DB db = new DB();
                            db.setMap(new HashMap<>(result));
                            db.insertData();
                        } else {
                            log("Unexpected response type: " + response.getClass().getName());
                        }

                        // status message from slave
                        Object messageResponse = input.readObject();
                        if (messageResponse instanceof Message) {
                            Message statusMessage = (Message) messageResponse;
                            log("Status message from slave: " + statusMessage.getText());
                        } else {
                            log("Unexpected message type: " + messageResponse.getClass().getName());
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<TaskInfo> splitTasks() {
        List<TaskInfo> taskInfos = new ArrayList<>();
        int numberOfSlaves = slaveAddresses.size();
        int chunkSize = (int) Math.ceil((double) seedUrls.size() / numberOfSlaves);

        for (int i = 0; i < numberOfSlaves; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, seedUrls.size());
            List<String> chunk = seedUrls.subList(start, end);
            taskInfos.add(new TaskInfo(new ArrayList<>(chunk), allowDuplicates, maxLevels));
        }
        return taskInfos;
    }

    private void processResult(ConcurrentHashMap<String, ArrayList<String>> result) {
        result.forEach((page, extractedUrls) -> {
            log("Page: " + page);
            extractedUrls.forEach(url -> log("Extracted URL: " + url));
        });
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    public static void main(String[] args) {
        List<String> slaveAddresses = new ArrayList<>();
        slaveAddresses.add("localhost:5000");
        slaveAddresses.add("localhost:5001");

        List<String> seedUrls = new ArrayList<>();
        seedUrls.add("http://example.com");

        boolean allowDuplicates = false;
        int maxLevels = 1;

    }
}
