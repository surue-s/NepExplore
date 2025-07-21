package com.nepaltourismmanagementapp.controller;

import com.nepaltourismmanagementapp.model.*;
import com.nepaltourismmanagementapp.utils.*;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BookingsController implements Initializable {

    @FXML
    private Label titleLabel;
    @FXML
    private TableView<Booking> bookingsTable;
    @FXML
    private TableColumn<Booking, String> bookingIdCol;
    @FXML
    private TableColumn<Booking, String> attractionCol;
    @FXML
    private TableColumn<Booking, LocalDate> visitDateCol;
    @FXML
    private TableColumn<Booking, String> statusCol;
    @FXML
    private TableColumn<Booking, Double> amountCol;
    @FXML
    private TableColumn<Booking, Integer> peopleCol;
    @FXML
    private Button backButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button viewDetailsButton;
    @FXML
    private Button cancelBookingButton;
    @FXML
    private Button reportEmergencyButton;
    @FXML
    private Button rebookButton;
    @FXML
    private Button clearFiltersButton;

    // Search and Filter Components
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;

    // Summary Labels
    @FXML
    private Label totalBookingsLabel;
    @FXML
    private Label upcomingBookingsLabel;
    @FXML
    private Label completedBookingsLabel;
    @FXML
    private Label totalSpentLabel;
    @FXML
    private Label bookingCountLabel;

    private User currentUser;
    private final ObservableList<Booking> bookingsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBookingsTable();
        setupTableSelectionListener();
        setupSearchAndFilters();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        titleLabel.setText("My Bookings - " + user.getFullName());
        loadUserBookings();
    }

    private void setupBookingsTable() {
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        attractionCol.setCellValueFactory(new PropertyValueFactory<>("attractionId"));
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("trekDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        peopleCol.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));

        bookingsTable.setItems(bookingsList);
    }

    private void setupTableSelectionListener() {
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            viewDetailsButton.setDisable(!hasSelection);

            if (hasSelection) {
                String status = newSelection.getStatus().toString();
                cancelBookingButton.setDisable("COMPLETED".equals(status) || "CANCELLED".equals(status));
                rebookButton.setDisable("PENDING".equals(status) || "CONFIRMED".equals(status));
            } else {
                cancelBookingButton.setDisable(true);
                rebookButton.setDisable(true);
            }
        });
    }

    private void setupSearchAndFilters() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> filterBookings());
        }

        if (statusFilter != null) {
            statusFilter.getItems().addAll("All", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
            statusFilter.setValue("All");
            statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> filterBookings());
        }
    }

    private void loadUserBookings() {
        try {
            List<Booking> userBookings;
            if (currentUser instanceof Tourist) {
                userBookings = BookingManager.getBookingsByTourist(currentUser.getUserId());
            } else if (currentUser instanceof Guide) {
                userBookings = BookingManager.getBookingsByGuide(currentUser.getUserId());
            } else {
                userBookings = BookingManager.loadAllBookings();
            }
            bookingsList.setAll(userBookings);
            updateBookingSummary(userBookings);

            if (bookingCountLabel != null) {
                bookingCountLabel.setText("Showing " + userBookings.size() + " bookings");
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private void filterBookings() {
        if (currentUser == null)
            return;

        try {
            List<Booking> allBookings;
            if (currentUser instanceof Tourist) {
                allBookings = BookingManager.getBookingsByTourist(currentUser.getUserId());
            } else if (currentUser instanceof Guide) {
                allBookings = BookingManager.getBookingsByGuide(currentUser.getUserId());
            } else {
                allBookings = BookingManager.loadAllBookings();
            }

            List<Booking> filteredBookings = allBookings.stream()
                    .filter(this::matchesSearchCriteria)
                    .filter(this::matchesStatusFilter)
                    .toList();

            bookingsList.setAll(filteredBookings);
            updateBookingSummary(filteredBookings);

            if (bookingCountLabel != null) {
                bookingCountLabel.setText("Showing " + filteredBookings.size() + " bookings");
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to filter bookings: " + e.getMessage());
        }
    }

    private boolean matchesSearchCriteria(Booking booking) {
        if (searchField == null || searchField.getText().trim().isEmpty()) {
            return true;
        }

        String searchText = searchField.getText().toLowerCase().trim();
        return booking.getId().toLowerCase().contains(searchText) ||
                booking.getAttractionId().toLowerCase().contains(searchText);
    }

    private boolean matchesStatusFilter(Booking booking) {
        if (statusFilter == null || "All".equals(statusFilter.getValue())) {
            return true;
        }

        return booking.getStatus().toString().equals(statusFilter.getValue());
    }

    private void updateBookingSummary(List<Booking> bookings) {
        if (totalBookingsLabel != null) {
            totalBookingsLabel.setText(String.valueOf(bookings.size()));
        }

        if (upcomingBookingsLabel != null) {
            long upcoming = bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus().toString())
                            || "PENDING".equals(b.getStatus().toString()))
                    .filter(b -> b.getTrekDate().isAfter(LocalDate.now()))
                    .count();
            upcomingBookingsLabel.setText(String.valueOf(upcoming));
        }

        if (completedBookingsLabel != null) {
            long completed = bookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus().toString()))
                    .count();
            completedBookingsLabel.setText(String.valueOf(completed));
        }

        if (totalSpentLabel != null) {
            double totalSpent = bookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            totalSpentLabel.setText("NPR " + String.format("%.0f", totalSpent));
        }
    }

    @FXML
    private void handleBack() {
        try {
            String fxmlFile;
            if (currentUser instanceof Tourist) {
                fxmlFile = "/com/nepaltourismmanagementapp/fxml/TouristDashboard.fxml";
            } else if (currentUser instanceof Guide) {
                fxmlFile = "/com/nepaltourismmanagementapp/fxml/GuideDashboard.fxml";
            } else {
                fxmlFile = "/com/nepaltourismmanagementapp/fxml/AdminDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Set the current user in the controller
            if (currentUser instanceof Tourist && loader.getController() instanceof TouristDashboardController) {
                ((TouristDashboardController) loader.getController()).setCurrentUser((Tourist) currentUser);
            } else if (currentUser instanceof Guide && loader.getController() instanceof GuideDashboardController) {
                ((GuideDashboardController) loader.getController()).setCurrentUser((Guide) currentUser);
            } else if (currentUser instanceof Admin && loader.getController() instanceof AdminDashboardController) {
                ((AdminDashboardController) loader.getController()).setCurrentUser((Admin) currentUser);
            }

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets()
                    .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadUserBookings();
        showAlert("Success", "Bookings refreshed successfully!");
    }

    @FXML
    private void handleViewDetails() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            showBookingDetails(selectedBooking);
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            handleCancelBooking(selectedBooking);
        }
    }

    @FXML
    private void handleReportEmergency() {
        showEmergencyDialog();
    }

    @FXML
    private void handleClearFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (statusFilter != null) {
            statusFilter.setValue("All");
        }
        loadUserBookings();
    }

    @FXML
    private void handleRebook() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            try {
                // Navigate to attractions page for rebooking
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/nepaltourismmanagementapp/fxml/Attractions.fxml"));
                Parent root = loader.load();

                AttractionsController controller = loader.getController();
                if (currentUser instanceof Tourist) {
                    controller.setCurrentUser((Tourist) currentUser);
                }

                Scene scene = new Scene(root, 1200, 800);
                scene.getStylesheets()
                        .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to open attractions for rebooking: " + e.getMessage());
            }
        } else {
            showAlert("No Selection", "Please select a booking to rebook.");
        }
    }

    private void showBookingDetails(Booking booking) {
        try {
            // Get attraction details
            List<Attraction> attractions = DataManager.loadAllAttractions();
            Attraction attraction = attractions.stream()
                    .filter(a -> a.getAttractionId().equals(booking.getAttractionId()))
                    .findFirst()
                    .orElse(null);

            String attractionName = attraction != null ? attraction.getName() : "Unknown Attraction";

            // Create detailed information
            StringBuilder details = new StringBuilder();
            details.append("Booking ID: ").append(booking.getId()).append("\n");
            details.append("Attraction: ").append(attractionName).append("\n");
            details.append("Visit Date: ").append(booking.getTrekDate()).append("\n");
            details.append("Status: ").append(booking.getStatus()).append("\n");
            details.append("Number of People: ").append(booking.getNumberOfPeople()).append("\n");
            details.append("Total Price: NPR ").append(String.format("%.2f", booking.getTotalPrice())).append("\n");

            if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
                details.append("\nSpecial Requests:\n").append(booking.getNotes());
            }

            // Show the details in an alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Details");
            alert.setHeaderText("Details for Booking #" + booking.getId());
            alert.setContentText(details.toString());
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Failed to load booking details: " + e.getMessage());
        }
    }

    private void handleCancelBooking(Booking booking) {
        if ("COMPLETED".equals(booking.getStatus().toString())) {
            showAlert("Cannot Cancel", "This booking has already been completed.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Booking");
        confirmAlert.setHeaderText("Are you sure you want to cancel this booking?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update booking status
                booking.setStatus(Booking.BookingStatus.CANCELLED);

                // Save the updated booking
                List<Booking> allBookings = BookingManager.loadAllBookings();
                for (int i = 0; i < allBookings.size(); i++) {
                    if (allBookings.get(i).getId().equals(booking.getId())) {
                        allBookings.set(i, booking);
                        break;
                    }
                }
                BookingManager.saveAllBookings(allBookings);

                // Refresh the view
                loadUserBookings();

                showAlert("Success", "Booking cancelled successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to cancel booking: " + e.getMessage());
            }
        }
    }

    private void showEmergencyDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Emergency");
        dialog.setHeaderText("Emergency Reporting System");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> emergencyTypeCombo = new ComboBox<>();
        emergencyTypeCombo.getItems().addAll(
                "Medical Emergency",
                "Lost/Stranded",
                "Accident",
                "Natural Disaster",
                "Security Issue",
                "Equipment Failure",
                "Booking Related Emergency",
                "Other");
        emergencyTypeCombo.setValue("Medical Emergency");

        TextField locationField = new TextField();
        locationField.setPromptText("Current location or last known location");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the emergency situation in detail...");
        descriptionArea.setPrefRowCount(4);

        TextField contactField = new TextField(currentUser.getPhone());
        contactField.setPromptText("Emergency contact number");

        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("Related booking ID (if applicable)");

        CheckBox needsImmediateHelp = new CheckBox("This is a life-threatening emergency");

        grid.add(new Label("Emergency Type:"), 0, 0);
        grid.add(emergencyTypeCombo, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        grid.add(new Label("Contact Number:"), 0, 3);
        grid.add(contactField, 1, 3);
        grid.add(new Label("Related Booking ID:"), 0, 4);
        grid.add(bookingIdField, 1, 4);
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
                            currentUser.getFullName(),
                            emergencyTypeCombo.getValue(),
                            locationField.getText().trim(),
                            descriptionArea.getText().trim(),
                            contactField.getText().trim(),
                            bookingIdField.getText().trim(),
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
            successAlert.setHeaderText("Your emergency has been reported");
            successAlert.setContentText("Emergency services have been notified. Help is on the way!\n\n" +
                    "Emergency Hotlines:\n" +
                    "Police: 100\n" +
                    "Fire: 101\n" +
                    "Ambulance: 102\n" +
                    "Tourist Helpline: 1144");
            successAlert.showAndWait();
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