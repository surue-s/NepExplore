package com.nepaltourismmanagementapp.controller;

import com.nepaltourismmanagementapp.model.*;
import com.nepaltourismmanagementapp.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel, totalUsersLabel, totalAttractionsLabel, totalBookingsLabel, totalRevenueLabel;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userIdCol, usernameCol, emailCol, userTypeCol;

    @FXML
    private TableView<Attraction> attractionsTable;
    @FXML
    private TableColumn<Attraction, String> attractionIdCol, attractionNameCol, locationCol, categoryCol;
    @FXML
    private TableColumn<Attraction, Double> feeCol;
    @FXML
    private TableColumn<Attraction, Boolean> activeCol;

    @FXML
    private TableView<Booking> bookingsTable;
    @FXML
    private TableColumn<Booking, String> bookingIdCol, touristIdCol, guideIdCol, statusCol;
    @FXML
    private TableColumn<Booking, Double> amountCol;

    private Admin currentUser;
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private final ObservableList<Attraction> attractionsList = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadAllData();
        updateStatistics();
    }

    public void setCurrentUser(Admin user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome Admin, " + user.getFullName() + "!");
    }

    private void setupTables() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));

        attractionIdCol.setCellValueFactory(new PropertyValueFactory<>("attractionId"));
        attractionNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        feeCol.setCellValueFactory(new PropertyValueFactory<>("entryFee"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        touristIdCol.setCellValueFactory(new PropertyValueFactory<>("touristUsername"));
        guideIdCol.setCellValueFactory(new PropertyValueFactory<>("guideUsername"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        usersTable.setItems(usersList);
        attractionsTable.setItems(attractionsList);
        bookingsTable.setItems(bookingsList);
    }

    private void loadAllData() {
        try {
            usersList.setAll(DataManager.loadAllUsers());
            attractionsList.setAll(DataManager.loadAllAttractions());
            bookingsList.setAll(DataManager.loadAllBookings());
        } catch (IOException e) {
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            List<User> users = DataManager.loadAllUsers();
            List<Attraction> attractions = DataManager.loadAllAttractions();
            List<Booking> bookings = DataManager.loadAllBookings();

            totalUsersLabel.setText(String.valueOf(users.size()));
            totalAttractionsLabel.setText(String.valueOf(attractions.size()));
            totalBookingsLabel.setText(String.valueOf(bookings.size()));

            double totalRevenue = bookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            totalRevenueLabel.setText("NPR " + String.format("%.2f", totalRevenue));
        } catch (IOException e) {
            showAlert("Error", "Failed to update statistics: " + e.getMessage());
        }
    }

    // ==================== USER CRUD OPERATIONS ====================

    @FXML
    private void handleCreateUser() {
        showCreateUserDialog();
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "Please select a user to edit.");
            return;
        }
        showEditUserDialog(selectedUser);
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "Please select a user to delete.");
            return;
        }

        if (selectedUser.getUserId().equals(currentUser.getUserId())) {
            showAlert("Cannot Delete", "You cannot delete your own account.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser(selectedUser);
            }
        });
    }

    @FXML
    private void handleViewUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "Please select a user to view details.");
            return;
        }
        showUserDetailsDialog(selectedUser);
    }

    // ==================== ATTRACTION CRUD OPERATIONS ====================

    @FXML
    private void handleCreateAttraction() {
        showCreateAttractionDialog();
    }

    @FXML
    private void handleEditAttraction() {
        Attraction selectedAttraction = attractionsTable.getSelectionModel().getSelectedItem();
        if (selectedAttraction == null) {
            showAlert("No Selection", "Please select an attraction to edit.");
            return;
        }
        showEditAttractionDialog(selectedAttraction);
    }

    @FXML
    private void handleDeleteAttraction() {
        Attraction selectedAttraction = attractionsTable.getSelectionModel().getSelectedItem();
        if (selectedAttraction == null) {
            showAlert("No Selection", "Please select an attraction to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Attraction");
        confirmAlert
                .setContentText("Are you sure you want to delete attraction: " + selectedAttraction.getName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteAttraction(selectedAttraction);
            }
        });
    }

    @FXML
    private void handleViewAttraction() {
        Attraction selectedAttraction = attractionsTable.getSelectionModel().getSelectedItem();
        if (selectedAttraction == null) {
            showAlert("No Selection", "Please select an attraction to view details.");
            return;
        }
        showAttractionDetailsDialog(selectedAttraction);
    }

    // ==================== USER CRUD DIALOG METHODS ====================

    private void showCreateUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter user information");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField emailField = new TextField();
        TextField fullNameField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<String> userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("TOURIST", "GUIDE", "ADMIN");
        userTypeCombo.setValue("TOURIST");

        // Additional fields for specific user types
        TextField nationalityField = new TextField();
        Spinner<Integer> ageSpinner = new Spinner<>(1, 120, 25);
        TextField licenseField = new TextField();
        TextField languagesField = new TextField();
        TextField specializationsField = new TextField();
        Spinner<Integer> experienceSpinner = new Spinner<>(0, 50, 0);
        ComboBox<String> adminLevelCombo = new ComboBox<>();
        adminLevelCombo.getItems().addAll("STANDARD", "SUPER");
        adminLevelCombo.setValue("STANDARD");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Full Name:"), 0, 3);
        grid.add(fullNameField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("User Type:"), 0, 5);
        grid.add(userTypeCombo, 1, 5);

        // Dynamic fields based on user type
        Label nationalityLabel = new Label("Nationality:");
        Label ageLabel = new Label("Age:");
        Label licenseLabel = new Label("License:");
        Label languagesLabel = new Label("Languages:");
        Label specializationsLabel = new Label("Specializations:");
        Label experienceLabel = new Label("Experience (years):");
        Label adminLevelLabel = new Label("Admin Level:");

        userTypeCombo.setOnAction(e -> {
            // Clear previous type-specific fields
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 5);

            String selectedType = userTypeCombo.getValue();
            if ("TOURIST".equals(selectedType)) {
                grid.add(nationalityLabel, 0, 6);
                grid.add(nationalityField, 1, 6);
                grid.add(ageLabel, 0, 7);
                grid.add(ageSpinner, 1, 7);
            } else if ("GUIDE".equals(selectedType)) {
                grid.add(licenseLabel, 0, 6);
                grid.add(licenseField, 1, 6);
                grid.add(languagesLabel, 0, 7);
                grid.add(languagesField, 1, 7);
                grid.add(specializationsLabel, 0, 8);
                grid.add(specializationsField, 1, 8);
                grid.add(experienceLabel, 0, 9);
                grid.add(experienceSpinner, 1, 9);
            } else if ("ADMIN".equals(selectedType)) {
                grid.add(adminLevelLabel, 0, 6);
                grid.add(adminLevelCombo, 1, 6);
            }
        });

        // Trigger initial setup
        userTypeCombo.fireEvent(new ActionEvent());

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String userType = userTypeCombo.getValue();
                    String userId = generateUserId(userType);

                    User newUser;
                    if ("TOURIST".equals(userType)) {
                        newUser = new Tourist(userId, usernameField.getText(), passwordField.getText(),
                                emailField.getText(), fullNameField.getText(), nationalityField.getText(),
                                phoneField.getText(), ageSpinner.getValue());
                    } else if ("GUIDE".equals(userType)) {
                        List<String> languages = Arrays.asList(languagesField.getText().split(","));
                        List<String> specializations = Arrays.asList(specializationsField.getText().split(","));
                        newUser = new Guide(userId, usernameField.getText(), passwordField.getText(),
                                emailField.getText(), fullNameField.getText(), phoneField.getText(),
                                licenseField.getText(), languages, specializations, 0.0, experienceSpinner.getValue());
                    } else {
                        newUser = new Admin(userId, usernameField.getText(), passwordField.getText(),
                                emailField.getText(), fullNameField.getText(), adminLevelCombo.getValue());
                    }

                    return newUser;
                } catch (Exception e) {
                    showAlert("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            try {
                DataManager.saveUser(user);
                loadAllData();
                updateStatistics();
                showAlert("Success", "User created successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to create user: " + e.getMessage());
            }
        });
    }

    private void showEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user information for: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField emailField = new TextField(user.getEmail());
        TextField fullNameField = new TextField(user.getFullName());
        TextField phoneField = new TextField(user.getPhone());

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        // Add type-specific fields
        if (user instanceof Tourist tourist) {
            TextField nationalityField = new TextField(tourist.getNationality());
            Spinner<Integer> ageSpinner = new Spinner<>(1, 120, tourist.getAge());

            grid.add(new Label("Nationality:"), 0, 3);
            grid.add(nationalityField, 1, 3);
            grid.add(new Label("Age:"), 0, 4);
            grid.add(ageSpinner, 1, 4);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    tourist.setEmail(emailField.getText());
                    tourist.setFullName(fullNameField.getText());
                    tourist.setPhone(phoneField.getText());
                    tourist.setNationality(nationalityField.getText());
                    tourist.setAge(ageSpinner.getValue());
                    return tourist;
                }
                return null;
            });
        } else if (user instanceof Guide guide) {
            TextField languagesField = new TextField(String.join(", ", guide.getLanguages()));
            TextField specializationsField = new TextField(String.join(", ", guide.getSpecializations()));
            Spinner<Integer> experienceSpinner = new Spinner<>(0, 50, guide.getExperienceYears());

            grid.add(new Label("Languages:"), 0, 3);
            grid.add(languagesField, 1, 3);
            grid.add(new Label("Specializations:"), 0, 4);
            grid.add(specializationsField, 1, 4);
            grid.add(new Label("Experience (years):"), 0, 5);
            grid.add(experienceSpinner, 1, 5);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    guide.setEmail(emailField.getText());
                    guide.setFullName(fullNameField.getText());
                    guide.setPhone(phoneField.getText());
                    guide.setLanguages(Arrays.asList(languagesField.getText().split(", ")));
                    guide.setSpecializations(Arrays.asList(specializationsField.getText().split(", ")));
                    guide.setExperienceYears(experienceSpinner.getValue());
                    return guide;
                }
                return null;
            });
        } else if (user instanceof Admin admin) {
            ComboBox<String> adminLevelCombo = new ComboBox<>();
            adminLevelCombo.getItems().addAll("STANDARD", "SUPER");
            adminLevelCombo.setValue(admin.getAdminLevel());

            grid.add(new Label("Admin Level:"), 0, 3);
            grid.add(adminLevelCombo, 1, 3);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    admin.setEmail(emailField.getText());
                    admin.setFullName(fullNameField.getText());
                    admin.setPhone(phoneField.getText());
                    admin.setAdminLevel(adminLevelCombo.getValue());
                    return admin;
                }
                return null;
            });
        }

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(updatedUser -> {
            try {
                DataManager.updateUser(updatedUser);
                loadAllData();
                updateStatistics();
                showAlert("Success", "User updated successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to update user: " + e.getMessage());
            }
        });
    }

    private void deleteUser(User user) {
        try {
            List<User> allUsers = DataManager.loadAllUsers();
            allUsers.removeIf(u -> u.getUserId().equals(user.getUserId()));

            // Rewrite users file
            try (PrintWriter writer = new PrintWriter(new FileWriter("Data/users.txt"))) {
                for (User u : allUsers) {
                    writer.println(u.toString());
                }
            }

            loadAllData();
            updateStatistics();
            showAlert("Success", "User deleted successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to delete user: " + e.getMessage());
        }
    }

    // ==================== ATTRACTION CRUD DIALOG METHODS ====================

    private void showCreateAttractionDialog() {
        Dialog<Attraction> dialog = new Dialog<>();
        dialog.setTitle("Create New Attraction");
        dialog.setHeaderText("Enter attraction information");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField nameNepaliField = new TextField();
        TextField districtField = new TextField();
        TextField provinceField = new TextField();
        TextField descriptionField = new TextField();
        TextField descriptionNepaliField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Mountain", "Religious", "Wildlife", "Cultural", "Adventure", "Historical");
        TextField entryFeeField = new TextField("0.0");
        TextField imageUrlField = new TextField();
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, 4.0, 0.1);
        CheckBox activeCheckBox = new CheckBox();
        activeCheckBox.setSelected(true);
        TextField latitudeField = new TextField("0.0");
        TextField longitudeField = new TextField("0.0");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Name (Nepali):"), 0, 1);
        grid.add(nameNepaliField, 1, 1);
        grid.add(new Label("District:"), 0, 2);
        grid.add(districtField, 1, 2);
        grid.add(new Label("Province:"), 0, 3);
        grid.add(provinceField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("Description (Nepali):"), 0, 5);
        grid.add(descriptionNepaliField, 1, 5);
        grid.add(new Label("Category:"), 0, 6);
        grid.add(categoryCombo, 1, 6);
        grid.add(new Label("Entry Fee:"), 0, 7);
        grid.add(entryFeeField, 1, 7);
        grid.add(new Label("Image URL:"), 0, 8);
        grid.add(imageUrlField, 1, 8);
        grid.add(new Label("Rating:"), 0, 9);
        grid.add(ratingSpinner, 1, 9);
        grid.add(new Label("Active:"), 0, 10);
        grid.add(activeCheckBox, 1, 10);
        grid.add(new Label("Latitude:"), 0, 11);
        grid.add(latitudeField, 1, 11);
        grid.add(new Label("Longitude:"), 0, 12);
        grid.add(longitudeField, 1, 12);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    String attractionId = generateAttractionId();
                    double latitude = Double.parseDouble(latitudeField.getText());
                    double longitude = Double.parseDouble(longitudeField.getText());
                    double entryFee = Double.parseDouble(entryFeeField.getText());

                    return new Attraction(attractionId, nameField.getText(), nameNepaliField.getText(),
                            districtField.getText(), provinceField.getText(), latitude, longitude,
                            descriptionField.getText(), descriptionNepaliField.getText(),
                            categoryCombo.getValue(), entryFee, imageUrlField.getText(),
                            ratingSpinner.getValue(), activeCheckBox.isSelected());
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid number format in entry fee, latitude, or longitude");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(attraction -> {
            try {
                saveAttraction(attraction);
                loadAllData();
                updateStatistics();
                showAlert("Success", "Attraction created successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to create attraction: " + e.getMessage());
            }
        });
    }

    private void showEditAttractionDialog(Attraction attraction) {
        Dialog<Attraction> dialog = new Dialog<>();
        dialog.setTitle("Edit Attraction");
        dialog.setHeaderText("Edit attraction: " + attraction.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(attraction.getName());
        TextField nameNepaliField = new TextField(attraction.getNameNepali());
        TextField districtField = new TextField(attraction.getDistrict() != null ? attraction.getDistrict() : "");
        TextField provinceField = new TextField(attraction.getProvince() != null ? attraction.getProvince() : "");
        TextField descriptionField = new TextField(attraction.getDescription());
        TextField descriptionNepaliField = new TextField(attraction.getDescriptionNepali());
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Mountain", "Religious", "Wildlife", "Cultural", "Adventure", "Historical");
        categoryCombo.setValue(attraction.getCategory());
        TextField entryFeeField = new TextField(String.valueOf(attraction.getEntryFee()));
        TextField imageUrlField = new TextField(attraction.getImageUrl());
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, attraction.getRating(), 0.1);
        CheckBox activeCheckBox = new CheckBox();
        activeCheckBox.setSelected(attraction.isActive());
        TextField latitudeField = new TextField(String.valueOf(attraction.getLatitude()));
        TextField longitudeField = new TextField(String.valueOf(attraction.getLongitude()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Name (Nepali):"), 0, 1);
        grid.add(nameNepaliField, 1, 1);
        grid.add(new Label("District:"), 0, 2);
        grid.add(districtField, 1, 2);
        grid.add(new Label("Province:"), 0, 3);
        grid.add(provinceField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("Description (Nepali):"), 0, 5);
        grid.add(descriptionNepaliField, 1, 5);
        grid.add(new Label("Category:"), 0, 6);
        grid.add(categoryCombo, 1, 6);
        grid.add(new Label("Entry Fee:"), 0, 7);
        grid.add(entryFeeField, 1, 7);
        grid.add(new Label("Image URL:"), 0, 8);
        grid.add(imageUrlField, 1, 8);
        grid.add(new Label("Rating:"), 0, 9);
        grid.add(ratingSpinner, 1, 9);
        grid.add(new Label("Active:"), 0, 10);
        grid.add(activeCheckBox, 1, 10);
        grid.add(new Label("Latitude:"), 0, 11);
        grid.add(latitudeField, 1, 11);
        grid.add(new Label("Longitude:"), 0, 12);
        grid.add(longitudeField, 1, 12);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    attraction.setName(nameField.getText());
                    attraction.setNameNepali(nameNepaliField.getText());
                    attraction.setDistrict(districtField.getText());
                    attraction.setProvince(provinceField.getText());
                    attraction.setDescription(descriptionField.getText());
                    attraction.setDescriptionNepali(descriptionNepaliField.getText());
                    attraction.setCategory(categoryCombo.getValue());
                    attraction.setEntryFee(Double.parseDouble(entryFeeField.getText()));
                    attraction.setImageUrl(imageUrlField.getText());
                    attraction.setRating(ratingSpinner.getValue());
                    attraction.setActive(activeCheckBox.isSelected());
                    attraction.setLatitude(Double.parseDouble(latitudeField.getText()));
                    attraction.setLongitude(Double.parseDouble(longitudeField.getText()));
                    return attraction;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid number format in entry fee, latitude, or longitude");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedAttraction -> {
            try {
                updateAttraction(updatedAttraction);
                loadAllData();
                updateStatistics();
                showAlert("Success", "Attraction updated successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to update attraction: " + e.getMessage());
            }
        });
    }

    private void deleteAttraction(Attraction attraction) {
        try {
            List<Attraction> allAttractions = DataManager.loadAllAttractions();
            allAttractions.removeIf(a -> a.getAttractionId().equals(attraction.getAttractionId()));

            // Rewrite attractions file
            try (PrintWriter writer = new PrintWriter(new FileWriter("Data/attractions.txt"))) {
                for (Attraction a : allAttractions) {
                    writer.println(a.toString());
                }
            }

            loadAllData();
            updateStatistics();
            showAlert("Success", "Attraction deleted successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to delete attraction: " + e.getMessage());
        }
    }

    // ==================== OTHER METHODS ====================

    @FXML
    private void handleGenerateReport() {
        try {
            String report = generateSystemReport();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save System Report");
            fileChooser.setInitialFileName("system_report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println(report);
                }
                showAlert("Success", "System report generated successfully!");
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private String generateSystemReport() throws IOException {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        report.append("NEPAL TOURISM MANAGEMENT SYSTEM - SYSTEM REPORT\n")
                .append("=".repeat(60)).append("\n")
                .append("Generated on: ").append(LocalDateTime.now().format(formatter)).append("\n")
                .append("Generated by: ").append(currentUser.getFullName()).append("\n\n");

        // User Stats
        List<User> users = DataManager.loadAllUsers();
        Map<String, Long> userTypeCount = users.stream()
                .collect(Collectors.groupingBy(User::getUserType, Collectors.counting()));

        report.append("USER STATISTICS\n").append("-".repeat(30)).append("\n")
                .append("Total Users: ").append(users.size()).append("\n");
        userTypeCount.forEach((type, count) -> report.append(type).append(" Users: ").append(count).append("\n"));
        report.append("\n");

        // Attraction Stats
        List<Attraction> attractions = DataManager.loadAllAttractions();
        Map<String, Long> categoryCount = attractions.stream()
                .collect(Collectors.groupingBy(Attraction::getCategory, Collectors.counting()));

        report.append("ATTRACTION STATISTICS\n").append("-".repeat(30)).append("\n")
                .append("Total Attractions: ").append(attractions.size()).append("\n");
        categoryCount.forEach((cat, count) -> report.append(cat).append(" Attractions: ").append(count).append("\n"));
        report.append("\n");

        // Booking Stats
        List<Booking> bookings = DataManager.loadAllBookings();
        Map<String, Long> statusCount = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getStatus().name(), Collectors.counting()));

        double totalRevenue = bookings.stream().mapToDouble(Booking::getTotalPrice).sum();

        report.append("BOOKING STATISTICS\n").append("-".repeat(30)).append("\n")
                .append("Total Bookings: ").append(bookings.size()).append("\n");
        statusCount.forEach((status, count) -> report.append(status).append(" Bookings: ").append(count).append("\n"));
        report.append("Total Revenue: NPR ").append(String.format("%.2f", totalRevenue)).append("\n\n");

        return report.toString();
    }

    @FXML
    private void handleRefreshData() {
        loadAllData();
        updateStatistics();
        showAlert("Success", "Data refreshed successfully!");
    }

    @FXML
    private void handleViewStatistics() {
        try {
            List<User> users = DataManager.loadAllUsers();
            List<Booking> bookings = DataManager.loadAllBookings();

            long tourists = users.stream().filter(u -> "TOURIST".equals(u.getUserType())).count();
            long guides = users.stream().filter(u -> "GUIDE".equals(u.getUserType())).count();
            long admins = users.stream().filter(u -> "ADMIN".equals(u.getUserType())).count();
            double totalRevenue = bookings.stream().mapToDouble(Booking::getTotalPrice).sum();
            double avgBooking = bookings.isEmpty() ? 0 : totalRevenue / bookings.size();

            StringBuilder stats = new StringBuilder();
            stats.append("USER BREAKDOWN:\n")
                    .append("Tourists: ").append(tourists).append("\n")
                    .append("Guides: ").append(guides).append("\n")
                    .append("Admins: ").append(admins).append("\n\n")
                    .append("REVENUE STATISTICS:\n")
                    .append("Total Revenue: NPR ").append(String.format("%.2f", totalRevenue)).append("\n")
                    .append("Average Booking Value: NPR ").append(String.format("%.2f", avgBooking)).append("\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detailed Statistics");
            alert.setHeaderText("System Statistics");
            alert.setContentText(stats.toString());
            alert.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllBookings() {
        try {
            List<Booking> allBookings = DataManager.loadAllBookings();

            if (allBookings.isEmpty()) {
                showAlert("No Bookings", "No bookings found in the system.");
                return;
            }

            StringBuilder bookingDetails = new StringBuilder();
            bookingDetails.append("ALL SYSTEM BOOKINGS\n")
                    .append("=".repeat(50)).append("\n\n");

            for (Booking booking : allBookings) {
                bookingDetails.append("Booking ID: ").append(booking.getId()).append("\n")
                        .append("Tourist: ").append(booking.getTouristUsername()).append("\n")
                        .append("Guide: ").append(booking.getGuideUsername()).append("\n")
                        .append("Attraction: ").append(booking.getAttractionId()).append("\n")
                        .append("Trek Date: ").append(booking.getTrekDate()).append("\n")
                        .append("Status: ").append(booking.getStatus()).append("\n")
                        .append("Amount: NPR ").append(String.format("%.2f", booking.getTotalPrice())).append("\n")
                        .append("-".repeat(30)).append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("All Bookings");
            alert.setHeaderText("Complete Booking List (" + allBookings.size() + " bookings)");
            alert.setContentText(bookingDetails.toString());
            alert.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageUsers() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showUserManagementDialog();
        } else {
            showUserDetailsDialog(selectedUser);
        }
    }

    @FXML
    private void handleManageAttractions() {
        Attraction selectedAttraction = attractionsTable.getSelectionModel().getSelectedItem();
        if (selectedAttraction == null) {
            showAttractionManagementDialog();
        } else {
            showAttractionDetailsDialog(selectedAttraction);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            String fxmlFile = "/com/nepaltourismmanagementapp/fxml/Login.fxml";
            URL fxmlUrl = getClass().getResource(fxmlFile);

            if (fxmlUrl == null) {
                showAlert("Error", "FXML file not found: " + fxmlFile);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 1200, 800);

            URL cssUrl = getClass().getResource("/com/nepaltourismmanagementapp/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    // ==================== DIALOG METHODS ====================

    private void showUserManagementDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Management");
        alert.setHeaderText("User Management Options");
        alert.setContentText("Select a user from the table to view details, or use the following options:\n\n" +
                "• View user statistics\n" +
                "• Export user data\n" +
                "• Monitor user activity\n\n" +
                "For detailed user management, select a specific user from the table.");
        alert.showAndWait();
    }

    private void showUserDetailsDialog(User user) {
        StringBuilder details = new StringBuilder();
        details.append("USER DETAILS\n")
                .append("=".repeat(30)).append("\n")
                .append("User ID: ").append(user.getUserId()).append("\n")
                .append("Username: ").append(user.getUsername()).append("\n")
                .append("Email: ").append(user.getEmail()).append("\n")
                .append("Full Name: ").append(user.getFullName()).append("\n")
                .append("Phone: ").append(user.getPhone()).append("\n")
                .append("User Type: ").append(user.getUserType()).append("\n");

        // Add specific details based on user type
        if (user instanceof Tourist tourist) {
            details.append("Nationality: ").append(tourist.getNationality()).append("\n")
                    .append("Age: ").append(tourist.getAge()).append("\n");
        } else if (user instanceof Guide guide) {
            details.append("License: ").append(guide.getLicenseNumber()).append("\n")
                    .append("Languages: ").append(String.join(", ", guide.getLanguages())).append("\n")
                    .append("Specializations: ").append(String.join(", ", guide.getSpecializations())).append("\n")
                    .append("Rating: ").append(String.format("%.1f", guide.getRating())).append("\n")
                    .append("Experience: ").append(guide.getExperienceYears()).append(" years\n");
        } else if (user instanceof Admin admin) {
            details.append("Admin Level: ").append(admin.getAdminLevel()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("Details for " + user.getUsername());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void showAttractionManagementDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attraction Management");
        alert.setHeaderText("Attraction Management Options");
        alert.setContentText("Select an attraction from the table to view details, or use the following options:\n\n" +
                "• View attraction statistics\n" +
                "• Monitor booking trends\n" +
                "• Export attraction data\n" +
                "• Analyze popularity metrics\n\n" +
                "For detailed attraction management, select a specific attraction from the table.");
        alert.showAndWait();
    }

    private void showAttractionDetailsDialog(Attraction attraction) {
        StringBuilder details = new StringBuilder();
        details.append("ATTRACTION DETAILS\n")
                .append("=".repeat(30)).append("\n")
                .append("ID: ").append(attraction.getAttractionId()).append("\n")
                .append("Name: ").append(attraction.getName()).append("\n")
                .append("Name (Nepali): ").append(attraction.getNameNepali()).append("\n")
                .append("Location: ").append(attraction.getLocation()).append("\n")
                .append("District: ").append(attraction.getDistrict() != null ? attraction.getDistrict() : "N/A")
                .append("\n")
                .append("Province: ").append(attraction.getProvince() != null ? attraction.getProvince() : "N/A")
                .append("\n")
                .append("Category: ").append(attraction.getCategory()).append("\n")
                .append("Entry Fee: NPR ").append(String.format("%.2f", attraction.getEntryFee())).append("\n")
                .append("Rating: ").append(String.format("%.1f", attraction.getRating())).append("\n")
                .append("Status: ").append(attraction.isActive() ? "Active" : "Inactive").append("\n")
                .append("Description: ").append(attraction.getDescription()).append("\n");

        if (attraction.getLatitude() != 0 && attraction.getLongitude() != 0) {
            details.append("Coordinates: ").append(attraction.getLatitude())
                    .append(", ").append(attraction.getLongitude()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attraction Details");
        alert.setHeaderText("Details for " + attraction.getName());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    // ==================== UTILITY METHODS ====================

    private String generateUserId(String userType) {
        String prefix = switch (userType) {
            case "TOURIST" -> "TOU";
            case "GUIDE" -> "GUD";
            case "ADMIN" -> "ADM";
            default -> "USR";
        };
        return prefix + String.format("%03d", (int) (Math.random() * 1000));
    }

    private String generateAttractionId() {
        return "ATT" + String.format("%03d", (int) (Math.random() * 1000));
    }

    private void saveAttraction(Attraction attraction) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("Data/attractions.txt", true))) {
            writer.println(attraction.toString());
        }
    }

    private void updateAttraction(Attraction updatedAttraction) throws IOException {
        List<Attraction> allAttractions = DataManager.loadAllAttractions();
        for (int i = 0; i < allAttractions.size(); i++) {
            if (allAttractions.get(i).getAttractionId().equals(updatedAttraction.getAttractionId())) {
                allAttractions.set(i, updatedAttraction);
                break;
            }
        }

        // Rewrite attractions file
        try (PrintWriter writer = new PrintWriter(new FileWriter("Data/attractions.txt"))) {
            for (Attraction a : allAttractions) {
                writer.println(a.toString());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.contains("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}