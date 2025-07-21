package com.nepaltourismmanagementapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class TourismApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("fxml/Login.fxml")));
        Scene scene = new Scene(root, 1200, 800);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/style.css")).toExternalForm());

        primaryStage.setTitle("Nepal Tourism Management");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
