package com.nepaltourismmanagementapp.controller;

import com.nepaltourismmanagementapp.model.*;
import com.nepaltourismmanagementapp.utils.BookingManager;
import com.nepaltourismmanagementapp.utils.DataManager;
import com.nepaltourismmanagementapp.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class GuideDashboardController implements Initializable {

    @FXML
    private Button languageButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalBookingsLabel;
    @FXML
    private Label totalEarningsLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label upcomingBookingsLabel;
    @FXML
    private TableView<Booking> bookingsTable;
    @FXML
    private TableColumn<Booking, String> bookingIdCol;
    @FXML
    private TableColumn<Booking, String> touristIdCol;
    @FXML
    private TableColumn<Booking, String> attractionCol;
    @FXML
    private TableColumn<Booking, LocalDate> visitDateCol;
    @FXML
    private TableColumn<Booking, String> statusCol;
    @FXML
    private TableColumn<Booking, Double> amountCol;

    // Profile fields (NEWLY ADDED FXML elements)
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField; // NEW: For phone input
    @FXML
    private TextField licenseField;
    @FXML
    private TextField languagesField;
    @FXML
    private TextField specializationsField;
    @FXML
    private Spinner<Integer> experienceSpinner;
    @FXML
    private Label profileRatingLabel; // To display rating in profile tab

    @FXML
    private Button editProfileButton;
    @FXML
    private Button saveChangesButton;
    @FXML
    private Button cancelEditButton;

    private Guide currentUser;
    private final ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    private final LanguageManager languageManager = LanguageManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBookingsTable();
        setupSpinners();
        setProfileFieldsEditable(false); // Start with fields not editable
        // languageButton.setText(languageManager.getText("language")); // Uncomment if
        // language button is used
    }

    private void setupSpinners() {
        if (experienceSpinner != null) {
            experienceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));
        }
    }

    public void setCurrentUser(Guide user) {
        this.currentUser = user;
        updateWelcomeMessage();
        loadGuideData();
        updateProfileInfo(); // Populate profile fields
    }

    private void updateWelcomeMessage() {
        welcomeLabel.setText(languageManager.getText("welcome") + ", " + currentUser.getFullName() + "!");
    }

    private void setupBookingsTable() {
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        touristIdCol.setCellValueFactory(new PropertyValueFactory<>("touristUsername"));
        attractionCol.setCellValueFactory(new PropertyValueFactory<>("attractionId"));
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("trekDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        bookingsTable.setItems(bookingsList);
    }

    private void loadGuideData() {
        try {
            List<Booking> guideBookings = BookingManager.getBookingsByGuide(currentUser.getUserId());
            bookingsList.setAll(guideBookings);
            updateStatistics(guideBookings);
        } catch (IOException e) {
            showAlert("Error", "Failed to load guide data: " + e.getMessage());
        }
    }

    private void updateStatistics(List<Booking> bookings) {
        totalBookingsLabel.setText("Total Bookings: " + bookings.size());
        double earnings = bookings.stream().mapToDouble(Booking::getTotalPrice).sum();
        totalEarningsLabel.setText("Total Earnings: NPR " + String.format("%.2f", earnings));
        ratingLabel.setText("Rating: " + String.format("%.1f", currentUser.getRating()));
        long upcoming = bookings.stream().filter(b -> b.getTrekDate().isAfter(LocalDate.now())).count();
        upcomingBookingsLabel.setText("Upcoming Bookings: " + upcoming);
    }

    private void updateProfileInfo() {
        // Populate the individual TextFields and Spinner
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone()); // Now correctly uses getPhone() from User
        licenseField.setText(currentUser.getLicenseNumber());
        languagesField.setText(String.join(";", currentUser.getLanguages()));
        specializationsField.setText(String.join(";", currentUser.getSpecializations()));

        // Initialize spinner value factory if not already set
        if (experienceSpinner.getValueFactory() == null) {
            experienceSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, currentUser.getExperienceYears()));
        } else {
            experienceSpinner.getValueFactory().setValue(currentUser.getExperienceYears());
        }

        profileRatingLabel.setText(String.format("%.1f", currentUser.getRating()));

        setProfileFieldsEditable(false); // Ensure fields are not editable initially
        saveChangesButton.setVisible(false);
        cancelEditButton.setVisible(false);
        editProfileButton.setVisible(true);
    }

    private void setProfileFieldsEditable(boolean editable) {
        fullNameField.setEditable(editable);
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        licenseField.setEditable(false); // License should probably not be editable
        languagesField.setEditable(editable);
        specializationsField.setEditable(editable);
        experienceSpinner.setEditable(editable);
        // Rating label is not editable
    }

    @FXML
    private void handleEditProfile() {
        setProfileFieldsEditable(true);
        editProfileButton.setVisible(false);
        saveChangesButton.setVisible(true);
        cancelEditButton.setVisible(true);
    }

    @FXML
    private void handleSaveChanges() {
        try {
            // Update currentUser object with new values from fields
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim()); // Update phone
            // licenseNumber is final, cannot be changed after creation
            currentUser.setLanguages(Arrays.asList(languagesField.getText().split(";")));
            currentUser.setSpecializations(Arrays.asList(specializationsField.getText().split(";")));
            currentUser.setExperienceYears(experienceSpinner.getValue());

            // Save the updated user data
            DataManager.updateUser(currentUser); // This method needs to be implemented in DataManager

            showAlert("Success", "Profile updated successfully!");
            updateProfileInfo(); // Refresh displayed info and reset buttons
        } catch (IOException e) {
            showAlert("Error", "Failed to save profile changes: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelEdit() {
        updateProfileInfo(); // Revert changes by reloading original data
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

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.contains("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRefreshData() {
        loadGuideData();
        updateProfileInfo(); // Also refresh profile data
        showAlert("Success", "Data refreshed successfully!");
    }

    @FXML
    public void handleViewAllBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nepaltourismmanagementapp/fxml/Bookings.fxml"));
            Parent root = loader.load();

            BookingsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets()
                    .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open bookings page: " + e.getMessage());
        }
    }

    @FXML
    public void handleStatistics() {
        showGuideStatisticsDialog();
    }

    private void showGuideStatisticsDialog() {
        try {
            // Load guide bookings for statistics
            List<Booking> guideBookings = BookingManager.getBookingsByGuide(currentUser.getUserId());
            List<Attraction> allAttractions = DataManager.loadAllAttractions();

            // Calculate statistics
            int totalBookings = guideBookings.size();
            int completedBookings = (int) guideBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus().toString()))
                    .count();
            int upcomingBookings = (int) guideBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus().toString())
                            || "PENDING".equals(b.getStatus().toString()))
                    .count();

            double totalEarnings = guideBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();

            double averageEarningsPerTrip = totalBookings > 0 ? totalEarnings / totalBookings : 0;

            // Find most guided category
            java.util.Map<String, Long> categoryCount = guideBookings.stream()
                    .map(booking -> allAttractions.stream()
                            .filter(attr -> attr.getAttractionId().equals(booking.getAttractionId()))
                            .findFirst()
                            .map(Attraction::getCategory)
                            .orElse("Unknown"))
                    .collect(java.util.stream.Collectors.groupingBy(
                            category -> category,
                            java.util.stream.Collectors.counting()));

            String specialtyCategory = categoryCount.entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .map(java.util.Map.Entry::getKey)
                    .orElse("None");

            // Calculate unique tourists served
            long uniqueTourists = guideBookings.stream()
                    .map(Booking::getTouristUsername)
                    .distinct()
                    .count();

            // Create statistics dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Guide Performance Statistics");
            dialog.setHeaderText("Your Professional Journey Overview");

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            // Add statistics
            int row = 0;
            grid.add(new Label("📊 Booking Statistics"), 0, row++, 2, 1);
            grid.add(new Label("Total Tours Guided:"), 0, row);
            grid.add(new Label(String.valueOf(totalBookings)), 1, row++);

            grid.add(new Label("Completed Tours:"), 0, row);
            grid.add(new Label(String.valueOf(completedBookings)), 1, row++);

            grid.add(new Label("Upcoming Tours:"), 0, row);
            grid.add(new Label(String.valueOf(upcomingBookings)), 1, row++);

            grid.add(new Label("Unique Tourists Served:"), 0, row);
            grid.add(new Label(String.valueOf(uniqueTourists)), 1, row++);

            grid.add(new Label(""), 0, row++); // Spacer

            grid.add(new Label("💰 Earnings Statistics"), 0, row++, 2, 1);
            grid.add(new Label("Total Earnings:"), 0, row);
            grid.add(new Label("NPR " + String.format("%.2f", totalEarnings)), 1, row++);

            grid.add(new Label("Average per Tour:"), 0, row);
            grid.add(new Label("NPR " + String.format("%.2f", averageEarningsPerTrip)), 1, row++);

            grid.add(new Label(""), 0, row++); // Spacer

            grid.add(new Label("🎯 Professional Profile"), 0, row++, 2, 1);
            grid.add(new Label("Specialty Category:"), 0, row);
            grid.add(new Label(specialtyCategory), 1, row++);

            grid.add(new Label("Experience Level:"), 0, row);
            grid.add(new Label(currentUser.getExperienceYears() + " years"), 1, row++);

            grid.add(new Label("Current Rating:"), 0, row);
            grid.add(new Label(String.format("%.1f/5.0", currentUser.getRating())), 1, row++);

            grid.add(new Label("Languages:"), 0, row);
            grid.add(new Label(String.join(", ", currentUser.getLanguages())), 1, row++);

            // Style the labels
            grid.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().startsWith("📊") || label.getText().startsWith("💰")
                            || label.getText().startsWith("🎯")) {
                        label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #6c5ce7;");
                    } else if (GridPane.getColumnIndex(node) == 1) {
                        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436;");
                    } else {
                        label.setStyle("-fx-text-fill: #636e72;");
                    }
                }
            });

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateStatus() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            // Example: Prompt for new status
            ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedBooking.getStatus().name(), "PENDING", "CONFIRMED",
                    "COMPLETED", "CANCELLED", "EMERGENCY");
            dialog.setTitle("Update Booking Status");
            dialog.setHeaderText("Change status for Booking ID: " + selectedBooking.getId());
            dialog.setContentText("Select new status:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newStatus -> {
                try {
                    BookingManager.updateBookingStatus(selectedBooking.getId(), newStatus);
                    loadGuideData(); // Refresh table
                    showAlert("Success", "Booking status updated to " + newStatus + "!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to update status: " + e.getMessage());
                }
            });
        } else {
            showAlert("Info", "Please select a booking to update its status.");
        }
    }

    @FXML
    public void handleViewTouristInfo() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            try {
                // Find the tourist by username
                User touristUser = DataManager.loadAllUsers().stream()
                        .filter(u -> u.getUsername().equals(selectedBooking.getTouristUsername())
                                && u instanceof Tourist)
                        .findFirst()
                        .orElse(null);

                if (touristUser instanceof Tourist tourist) {
                    showAlert("Tourist Information",
                            "Name: " + tourist.getFullName() + "\n" +
                                    "Email: " + tourist.getEmail() + "\n" +
                                    "Phone: " + tourist.getPhone() + "\n" + // Uses getPhone() from User
                                    "Nationality: " + tourist.getNationality() + "\n" +
                                    "Age: " + tourist.getAge());
                } else {
                    showAlert("Error", "Tourist information not found.");
                }
            } catch (IOException e) {
                showAlert("Error", "Failed to load tourist information: " + e.getMessage());
            }
        } else {
            showAlert("Info", "Please select a booking to view tourist information.");
        }
    }

    @FXML
    public void handleReportEmergency() {
        showEmergencyDialog();
    }

    private void showEmergencyDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Emergency");
        dialog.setHeaderText("Emergency Reporting System - Guide");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> emergencyTypeCombo = new ComboBox<>();
        emergencyTypeCombo.getItems().addAll(
                "Tourist Medical Emergency",
                "Tourist Lost/Stranded",
                "Accident During Tour",
                "Natural Disaster",
                "Security Issue",
                "Equipment Failure",
                "Group Emergency",
                "Other");
        emergencyTypeCombo.setValue("Tourist Medical Emergency");

        TextField locationField = new TextField();
        locationField.setPromptText("Current location or last known location");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the emergency situation in detail...");
        descriptionArea.setPrefRowCount(4);

        TextField contactField = new TextField(currentUser.getPhone());
        contactField.setPromptText("Emergency contact number");

        TextField touristInvolvedField = new TextField();
        touristInvolvedField.setPromptText("Tourist(s) involved (if applicable)");

        CheckBox needsImmediateHelp = new CheckBox("This is a life-threatening emergency");

        grid.add(new Label("Emergency Type:"), 0, 0);
        grid.add(emergencyTypeCombo, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        grid.add(new Label("Contact Number:"), 0, 3);
        grid.add(contactField, 1, 3);
        grid.add(new Label("Tourist(s) Involved:"), 0, 4);
        grid.add(touristInvolvedField, 1, 4);
        grid.add(needsImmediateHelp, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType reportButtonType = new ButtonType("Report Emergency", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                if (locationField.getText().trim().isEmpty() || descriptionArea.getText().trim().isEmpty()) {
                    showAlert("Error", "Please fill in all required fields.");
                    return null;
                }

                try {
                    // Create emergency record
                    String emergencyId = "EMG" + System.currentTimeMillis();
                    String emergencyRecord = String.format(
                            "%s|%s|%s|%s|%s|%s|%s|%s|%s|%s\n",
                            emergencyId,
                            currentUser.getUserId(),
                            currentUser.getFullName() + " (Guide)",
                            emergencyTypeCombo.getValue(),
                            locationField.getText().trim(),
                            descriptionArea.getText().trim(),
                            contactField.getText().trim(),
                            touristInvolvedField.getText().trim(),
                            needsImmediateHelp.isSelected() ? "CRITICAL" : "NORMAL",
                            LocalDate.now().toString());

                    // Save to emergencies file
                    java.nio.file.Files.write(
                            java.nio.file.Paths.get("Data/emergencies.txt"),
                            emergencyRecord.getBytes(),
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.APPEND);

                    return "Emergency reported successfully";
                } catch (Exception e) {
                    showAlert("Error", "Failed to report emergency: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Emergency Reported");
            successAlert.setHeaderText("Emergency has been reported");
            successAlert.setContentText("Emergency services and tourism authorities have been notified.\n\n" +
                    "Emergency Hotlines:\n" +
                    "Police: 100\n" +
                    "Fire: 101\n" +
                    "Ambulance: 102\n" +
                    "Tourist Police: 1144\n" +
                    "Guide Emergency Line: 1155");
            successAlert.showAndWait();
        }
    }

}
