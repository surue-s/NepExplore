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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TouristDashboardController implements Initializable {

    // Dashboard UI Components
    @FXML
    private Label welcomeLabel, totalAttractionsLabel, myBookingsLabel, totalSpentLabel, festivalDiscountLabel;
    @FXML
    private HBox latestAttractionsContainer;
    @FXML
    private TableView<Booking> recentBookingsTable;
    @FXML
    private TableColumn<Booking, String> bookingIdCol, attractionCol, statusCol;
    @FXML
    private TableColumn<Booking, LocalDate> visitDateCol;
    @FXML
    private TableColumn<Booking, Double> amountCol;
    @FXML
    private VBox festivalDiscountCard;

    // Data
    private Tourist currentUser;
    private final ObservableList<Booking> bookingsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBookingsTable();
        checkFestivalDiscount();
    }

    public void setCurrentUser(Tourist user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
        loadUserData();
    }

    private void setupBookingsTable() {
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        attractionCol.setCellValueFactory(new PropertyValueFactory<>("attractionId"));
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("trekDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        recentBookingsTable.setItems(bookingsList);
    }

    private void loadUserData() {
        try {
            // Load user bookings
            List<Booking> userBookings = BookingManager.getBookingsByTourist(currentUser.getUserId());
            bookingsList.setAll(userBookings);

            // Update statistics
            myBookingsLabel.setText(String.valueOf(userBookings.size()));

            double totalSpent = userBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            totalSpentLabel.setText("NPR " + String.format("%.0f", totalSpent));

            // Load attractions count
            List<Attraction> attractions = DataManager.loadAllAttractions();
            totalAttractionsLabel.setText(String.valueOf(attractions.size()));

            // Load latest attractions preview
            loadLatestAttractions(attractions);

        } catch (IOException e) {
            showAlert("Error", "Failed to load user data: " + e.getMessage());
        }
    }

    private void loadLatestAttractions(List<Attraction> allAttractions) {
        latestAttractionsContainer.getChildren().clear();

        // Get up to 5 latest attractions
        List<Attraction> latestAttractions = allAttractions.stream()
                .filter(Attraction::isActive)
                .limit(5)
                .toList();

        for (Attraction attraction : latestAttractions) {
            VBox card = createAttractionPreviewCard(attraction);
            latestAttractionsContainer.getChildren().add(card);
        }
    }

    private VBox createAttractionPreviewCard(Attraction attraction) {
        VBox card = new VBox(10);
        card.getStyleClass().add("attraction-preview-card");
        card.setPrefWidth(200);

        // Image (placeholder for now)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Try to load image or use placeholder
        try {
            imageView.setImage(new Image("/com/nepaltourismmanagementapp/images/attractions/" +
                    attraction.getImageUrl()));
        } catch (Exception e) {
            // Use placeholder image
            try {
                imageView.setImage(new Image("/com/nepaltourismmanagementapp/images/placeholder.png"));
            } catch (Exception ex) {
                // If even placeholder fails, just continue without image
            }
        }

        // Name
        Label nameLabel = new Label(attraction.getName());
        nameLabel.getStyleClass().add("attraction-name");

        // Location
        Label locationLabel = new Label(attraction.getLocation());
        locationLabel.getStyleClass().add("attraction-location");

        // Price
        Label priceLabel = new Label("NPR " + String.format("%.0f", attraction.getEntryFee()));
        priceLabel.getStyleClass().add("attraction-price");

        card.getChildren().addAll(imageView, nameLabel, locationLabel, priceLabel);

        // Add click handler
        card.setOnMouseClicked(e -> handleViewAttractionDetails(attraction));

        return card;
    }

    private void handleViewAttractionDetails(Attraction attraction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nepaltourismmanagementapp/fxml/Attractions.fxml"));
            Parent root = loader.load();

            AttractionsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets()
                    .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open attraction details: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAttractions() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nepaltourismmanagementapp/fxml/Attractions.fxml"));
            Parent root = loader.load();

            AttractionsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets()
                    .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open attractions page: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        if (currentUser == null) {
            showAlert("Error", "User information not available. Please log in again.");
            return;
        }

        showProfileEditDialog();
    }

    private void showProfileEditDialog() {
        Dialog<Tourist> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField fullNameField = new TextField(currentUser.getFullName());
        TextField emailField = new TextField(currentUser.getEmail());
        TextField phoneField = new TextField(currentUser.getPhone());
        TextField nationalityField = new TextField(currentUser.getNationality());
        Spinner<Integer> ageSpinner = new Spinner<>(1, 120, currentUser.getAge());

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Nationality:"), 0, 3);
        grid.add(nationalityField, 1, 3);
        grid.add(new Label("Age:"), 0, 4);
        grid.add(ageSpinner, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Update user object
                    currentUser.setFullName(fullNameField.getText().trim());
                    currentUser.setEmail(emailField.getText().trim());
                    currentUser.setPhone(phoneField.getText().trim());
                    currentUser.setNationality(nationalityField.getText().trim());
                    currentUser.setAge(ageSpinner.getValue());

                    // Save to file
                    DataManager.updateUser(currentUser);
                    return currentUser;
                } catch (Exception e) {
                    showAlert("Error", "Failed to save profile: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Tourist> result = dialog.showAndWait();
        if (result.isPresent()) {
            showAlert("Success", "Profile updated successfully!");
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");
        }
    }

    @FXML
    private void handleEmergency() {
        showEmergencyDialog();
    }

    @FXML
    private void handleStatistics() {
        showStatisticsDialog();
    }

    @FXML
    private void handleQuickBooking() {
        showQuickBookingDialog();
    }

    private void showQuickBookingDialog() {
        try {
            List<Attraction> attractions = DataManager.loadAllAttractions();
            List<Attraction> activeAttractions = attractions.stream()
                    .filter(Attraction::isActive)
                    .toList();

            if (activeAttractions.isEmpty()) {
                showAlert("No Attractions", "No attractions available for booking at the moment.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Quick Booking");
            dialog.setHeaderText("Book your next adventure quickly!");

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            // Attraction selection
            ComboBox<Attraction> attractionCombo = new ComboBox<>();
            attractionCombo.getItems().addAll(activeAttractions);
            attractionCombo.setConverter(new javafx.util.StringConverter<Attraction>() {
                @Override
                public String toString(Attraction attraction) {
                    return attraction != null
                            ? attraction.getName() + " - NPR " + String.format("%.0f", attraction.getEntryFee())
                            : "";
                }

                @Override
                public Attraction fromString(String string) {
                    return null;
                }
            });
            attractionCombo.setValue(activeAttractions.get(0));

            // Date selection
            DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));

            // People count
            Spinner<Integer> peopleSpinner = new Spinner<>(1, 50, 1);

            // Price calculation
            Label priceLabel = new Label();
            Runnable updatePrice = () -> {
                Attraction selected = attractionCombo.getValue();
                if (selected != null) {
                    double totalPrice = selected.getEntryFee() * peopleSpinner.getValue();
                    double discount = FestivalManager.calculateFestivalDiscount(totalPrice);
                    double finalPrice = totalPrice - discount;
                    priceLabel.setText("Total: NPR " + String.format("%.2f", finalPrice));
                    priceLabel.setStyle("-fx-text-fill: #00b894; -fx-font-weight: bold;");
                }
            };

            attractionCombo.setOnAction(e -> updatePrice.run());
            peopleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice.run());
            updatePrice.run();

            grid.add(new Label("Select Attraction:"), 0, 0);
            grid.add(attractionCombo, 1, 0);
            grid.add(new Label("Visit Date:"), 0, 1);
            grid.add(datePicker, 1, 1);
            grid.add(new Label("Number of People:"), 0, 2);
            grid.add(peopleSpinner, 1, 2);
            grid.add(new Label("Price:"), 0, 3);
            grid.add(priceLabel, 1, 3);

            dialog.getDialogPane().setContent(grid);

            ButtonType bookButtonType = new ButtonType("Book Now", ButtonBar.ButtonData.OK_DONE);
            ButtonType viewDetailsButtonType = new ButtonType("View Details", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, viewDetailsButtonType, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get() == bookButtonType) {
                    // Proceed with booking
                    Attraction selectedAttraction = attractionCombo.getValue();
                    LocalDate selectedDate = datePicker.getValue();

                    if (selectedDate == null || selectedDate.isBefore(LocalDate.now())) {
                        showAlert("Invalid Date", "Please select a valid future date.");
                        return;
                    }

                    try {
                        // Find a guide
                        List<User> allUsers = DataManager.loadAllUsers();
                        Guide selectedGuide = allUsers.stream()
                                .filter(u -> u instanceof Guide)
                                .map(u -> (Guide) u)
                                .findFirst()
                                .orElse(null);

                        if (selectedGuide == null) {
                            showAlert("No Guides", "No guides available for booking.");
                            return;
                        }

                        Booking booking = BookingManager.createBooking(
                                currentUser.getUserId(),
                                selectedGuide.getUserId(),
                                selectedAttraction.getAttractionId(),
                                selectedDate,
                                peopleSpinner.getValue(),
                                selectedAttraction.getEntryFee(),
                                "Quick booking from dashboard");

                        showAlert("Booking Confirmed",
                                "Your quick booking has been confirmed!\n" +
                                        "Booking ID: " + booking.getId() + "\n" +
                                        "Attraction: " + selectedAttraction.getName() + "\n" +
                                        "Date: " + selectedDate + "\n" +
                                        "Total: NPR " + String.format("%.2f", booking.getTotalPrice()));

                        // Refresh dashboard data
                        loadUserData();

                    } catch (IOException e) {
                        showAlert("Error", "Failed to create booking: " + e.getMessage());
                    }
                } else if (result.get() == viewDetailsButtonType) {
                    // Navigate to attractions page
                    handleViewAttractions();
                }
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to load attractions: " + e.getMessage());
        }
    }

    private void showStatisticsDialog() {
        try {
            // Load user bookings for statistics
            List<Booking> userBookings = BookingManager.getBookingsByTourist(currentUser.getUserId());
            List<Attraction> allAttractions = DataManager.loadAllAttractions();

            // Calculate statistics
            int totalBookings = userBookings.size();
            int completedBookings = (int) userBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus().toString()))
                    .count();
            int pendingBookings = (int) userBookings.stream()
                    .filter(b -> "PENDING".equals(b.getStatus().toString())
                            || "CONFIRMED".equals(b.getStatus().toString()))
                    .count();

            double totalSpent = userBookings.stream()
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();

            double averageSpending = totalBookings > 0 ? totalSpent / totalBookings : 0;

            // Find most visited category
            java.util.Map<String, Long> categoryCount = userBookings.stream()
                    .map(booking -> allAttractions.stream()
                            .filter(attr -> attr.getAttractionId().equals(booking.getAttractionId()))
                            .findFirst()
                            .map(Attraction::getCategory)
                            .orElse("Unknown"))
                    .collect(java.util.stream.Collectors.groupingBy(
                            category -> category,
                            java.util.stream.Collectors.counting()));

            String favoriteCategory = categoryCount.entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .map(java.util.Map.Entry::getKey)
                    .orElse("None");

            // Create statistics dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Travel Statistics");
            dialog.setHeaderText("Your Tourism Journey Overview");

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(15);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            // Add statistics
            int row = 0;
            grid.add(new Label("📊 Booking Statistics"), 0, row++, 2, 1);

            grid.add(new Label(String.valueOf(totalBookings)), 1, row++);

            grid.add(new Label(String.valueOf(completedBookings)), 1, row++);

            grid.add(new Label("Upcoming/Pending:"), 0, row);
            grid.add(new Label(String.valueOf(pendingBookings)), 1, row++);

            grid.add(new Label(""), 0, row++); // Spacer

            grid.add(new Label("💰 Spending Statistics"), 0, row++, 2, 1);
            grid.add(new Label("Total Spent:"), 0, row);
            grid.add(new Label("NPR " + String.format("%.2f", totalSpent)), 1, row++);

            grid.add(new Label("Average per Trip:"), 0, row);
            grid.add(new Label("NPR " + String.format("%.2f", averageSpending)), 1, row++);

            grid.add(new Label(""), 0, row++); // Spacer

            grid.add(new Label("🎯 Preferences"), 0, row++, 2, 1);
            grid.add(new Label("Favorite Category:"), 0, row);
            grid.add(new Label(favoriteCategory), 1, row++);

            grid.add(new Label("Member Since:"), 0, row);
            grid.add(new Label("2024"), 1, row++); // Could be enhanced with actual registration date

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
                "Other");
        emergencyTypeCombo.setValue("Medical Emergency");

        TextField locationField = new TextField();
        locationField.setPromptText("Current location or last known location");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the emergency situation in detail...");
        descriptionArea.setPrefRowCount(4);

        TextField contactField = new TextField(currentUser.getPhone());
        contactField.setPromptText("Emergency contact number");

        CheckBox needsImmediateHelp = new CheckBox("This is a life-threatening emergency");

        grid.add(new Label("Emergency Type:"), 0, 0);
        grid.add(emergencyTypeCombo, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        grid.add(new Label("Contact Number:"), 0, 3);
        grid.add(contactField, 1, 3);
        grid.add(needsImmediateHelp, 1, 4);

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
                            "%s|%s|%s|%s|%s|%s|%s|%s|%s\n",
                            emergencyId,
                            currentUser.getUserId(),
                            currentUser.getFullName(),
                            emergencyTypeCombo.getValue(),
                            locationField.getText().trim(),
                            descriptionArea.getText().trim(),
                            contactField.getText().trim(),
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

    private void checkFestivalDiscount() {
        String festival = FestivalManager.getCurrentFestival();
        if (festival != null) {
            festivalDiscountCard.setVisible(true);
            festivalDiscountLabel.setText("Enjoy special discounts during " + festival + "!");
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
    private void handleViewBookings() {
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

    private void setupBookingContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewDetails = new MenuItem("View Details");
        viewDetails.setOnAction(e -> {
            Booking selectedBooking = recentBookingsTable.getSelectionModel().getSelectedItem();
            if (selectedBooking != null) {
                showBookingDetails(selectedBooking);
            }
        });

        MenuItem cancelBooking = new MenuItem("Cancel Booking");
        cancelBooking.setOnAction(e -> {
            Booking selectedBooking = recentBookingsTable.getSelectionModel().getSelectedItem();
            if (selectedBooking != null) {
                handleCancelBooking(selectedBooking);
            }
        });

        contextMenu.getItems().addAll(viewDetails, cancelBooking);
        recentBookingsTable.setContextMenu(contextMenu);

        // Double-click to view details
        recentBookingsTable.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showBookingDetails(row.getItem());
                }
            });
            return row;
        });
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
        if ("COMPLETED".equals(booking.getStatus())) {
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
                loadUserData();

                showAlert("Success", "Booking cancelled successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to cancel booking: " + e.getMessage());
            }
        }
    }
}