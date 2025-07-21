module com.nepaltourismmanagementapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens com.nepaltourismmanagementapp to javafx.fxml;

    exports com.nepaltourismmanagementapp;
    exports com.nepaltourismmanagementapp.model;

    opens com.nepaltourismmanagementapp.model to javafx.fxml;

    exports com.nepaltourismmanagementapp.controller;

    opens com.nepaltourismmanagementapp.controller to javafx.fxml;
}