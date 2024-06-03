package com.mycompany.finalproject405;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

//Names:
//      Ahmed Anmar Matawi - 2036840
//      Abdulrahman Bagh
//CPCS 405 - CS1
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Web Crawler Application");
        showInitialMenu();
    }

    private void showInitialMenu() {
        VBox vbox = new VBox();
        Label label = new Label("Select Mode:");
        RadioButton masterRadio = new RadioButton("Master");
        RadioButton slaveRadio = new RadioButton("Slave");
        ToggleGroup toggleGroup = new ToggleGroup();
        masterRadio.setToggleGroup(toggleGroup);
        slaveRadio.setToggleGroup(toggleGroup);
        Button nextButton = new Button("Next");

        nextButton.setOnAction(e -> {
            if (masterRadio.isSelected()) {
                showMasterConfiguration();
            } else if (slaveRadio.isSelected()) {
                showSlaveConfiguration();
            }
        });

        vbox.getChildren().addAll(label, masterRadio, slaveRadio, nextButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMasterConfiguration() {
        VBox vbox = new VBox();
        Label slaveAddressLabel = new Label("Slave IP:Port (one per line):");
        TextArea slaveAddressArea = new TextArea();
        Label seedUrlLabel = new Label("Seed URLs (one per line):");
        TextArea seedUrlArea = new TextArea();
        Label allowDuplicatesLabel = new Label("Enable Duplicate URL Extraction:");
        CheckBox allowDuplicatesCheckBox = new CheckBox();
        Label maxLevelsLabel = new Label("Max Levels (0-2):");
        TextField maxLevelsField = new TextField();
        Button runButton = new Button("Run");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);

        runButton.setOnAction(e -> {
            List<String> slaveAddresses = List.of(slaveAddressArea.getText().split("\\s+"));
            List<String> seedUrls = List.of(seedUrlArea.getText().split("\\s+"));
            boolean allowDuplicates = allowDuplicatesCheckBox.isSelected();
            int maxLevels = Integer.parseInt(maxLevelsField.getText());

            // Create Master instance and send tasks
            Master master = new Master(slaveAddresses, seedUrls, allowDuplicates, maxLevels, logArea);
            new Thread(master::sendTasks).start();
        });

        vbox.getChildren().addAll(slaveAddressLabel, slaveAddressArea, seedUrlLabel, seedUrlArea, allowDuplicatesLabel, allowDuplicatesCheckBox, maxLevelsLabel, maxLevelsField, runButton, logArea);
        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showSlaveConfiguration() {
        VBox vbox = new VBox();
        Label portLabel = new Label("Enter Port Number:");
        TextField portField = new TextField();
        Button runButton = new Button("Run");

        runButton.setOnAction(e -> {
            int port = Integer.parseInt(portField.getText());
            try {
                Slave slave = new Slave(port);
                new Thread(slave::start).start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        vbox.getChildren().addAll(portLabel, portField, runButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
