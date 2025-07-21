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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AttractionsController implements Initializable {

    // UI Components
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Button backButton, prevButton, nextButton, bookButton, safetyWarningsButton;
    @FXML
    private Label featuredAttractionName, featuredAttractionDescription;
    @FXML
    private VBox featuredAttractionCard, bookingModal;
    @FXML
    private HBox attractionCardsContainer;
    @FXML
    private ScrollPane attractionScrollPane;

    // Booking Modal Components
    @FXML
    private DatePicker visitDatePicker;
    @FXML
    private Spinner<Integer> peopleSpinner;
    @FXML
    private TextArea specialRequestsArea;
    @FXML
    private Label totalAmountLabel, festivalDiscountLabel;

    // Data and State
    private Tourist currentUser;
    private final ObservableList<Attraction> attractionsList = FXCollections.observableArrayList();
    private int currentAttractionIndex = 0;
    private Attraction selectedAttraction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBookingControls();
        loadAttractions();
    }

    public void setCurrentUser(Tourist user) {
        this.currentUser = user;
        // If user is null, redirect back to login
        if (user == null && backButton != null) {
            showAlert("Error", "User session expired. Please log in again.");
            handleBackToLogin();
        }
    }

    private void handleBackToLogin() {
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

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to login: " + e.getMessage());
        }
    }

    private void setupBookingControls() {
        // Initialize spinner
        if (peopleSpinner != null) {
            peopleSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
            peopleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotalAmount());
        }

        // Initialize date picker
        if (visitDatePicker != null) {
            visitDatePicker.setValue(LocalDate.now().plusDays(1));
            visitDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotalAmount());
        }

        // Set up safety warnings button
        if (safetyWarningsButton != null) {
            safetyWarningsButton.setOnAction(e -> showSafetyWarnings());
        }
    }

    private void loadAttractions() {
        try {
            List<Attraction> attractions = DataManager.loadAllAttractions();
            attractionsList.setAll(attractions.stream().filter(Attraction::isActive).toList());

            if (!attractionsList.isEmpty()) {
                displayFeaturedAttraction();
                createAttractionCards();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load attractions: " + e.getMessage());
        }
    }

    private void displayFeaturedAttraction() {
        if (!attractionsList.isEmpty() && currentAttractionIndex < attractionsList.size()) {
            Attraction featured = attractionsList.get(currentAttractionIndex);
            selectedAttraction = featured;

            // Update UI elements
            featuredAttractionName.setText(featured.getName());
            featuredAttractionDescription.setText(featured.getDescription());

            // Update background image with enhanced fallback logic
            updateBackgroundImage(featured);
            calculateTotalAmount();
        }
    }

    private void updateBackgroundImage(Attraction attraction) {
        try {
            // Try to load the specific attraction image
            String imagePath = "/com/nepaltourismmanagementapp/images/attractions/" + attraction.getImageUrl();
            Image attractionImage = new Image(imagePath);

            // Check if image loaded successfully
            if (!attractionImage.isError()) {
                backgroundImage.setImage(attractionImage);
                System.out.println("Loaded attraction image: " + imagePath);
            } else {
                throw new Exception("Image loading failed");
            }
        } catch (Exception e) {
            // Fallback to default nepal background
            try {
                Image fallbackImage = new Image("/com/nepaltourismmanagementapp/images/nepal-background.jpg");
                backgroundImage.setImage(fallbackImage);
                System.out.println("Using fallback image for: " + attraction.getName());
            } catch (Exception fallbackError) {
                System.err.println("Failed to load both attraction and fallback images: " + fallbackError.getMessage());
            }
        }
    }

    private void createAttractionCards() {
        attractionCardsContainer.getChildren().clear();

        for (int i = 0; i < attractionsList.size(); i++) {
            Attraction attraction = attractionsList.get(i);
            VBox card = createAttractionCard(attraction, i);
            attractionCardsContainer.getChildren().add(card);
        }
    }

    private VBox createAttractionCard(Attraction attraction, int index) {
        VBox card = new VBox(8);
        card.getStyleClass().add("attraction-card");

        // Add selection styling if this is the current attraction
        if (index == currentAttractionIndex) {
            card.getStyleClass().add("attraction-card-selected");
        }

        // Attraction name
        Label nameLabel = new Label(attraction.getName());
        nameLabel.getStyleClass().add("attraction-card-title");

        // Location
        Label locationLabel = new Label(attraction.getLocation());
        locationLabel.getStyleClass().add("attraction-card-location");

        // Price
        Label priceLabel = new Label("NPR " + String.format("%.0f", attraction.getEntryFee()));
        priceLabel.getStyleClass().add("attraction-card-price");

        card.getChildren().addAll(nameLabel, locationLabel, priceLabel);

        // Add click handler
        card.setOnMouseClicked(e -> {
            currentAttractionIndex = index;
            displayFeaturedAttraction();
            updateAttractionCardSelection();
        });

        return card;
    }

    private void updateAttractionCardSelection() {
        for (int i = 0; i < attractionCardsContainer.getChildren().size(); i++) {
            VBox card = (VBox) attractionCardsContainer.getChildren().get(i);
            card.getStyleClass().remove("attraction-card-selected");

            if (i == currentAttractionIndex) {
                card.getStyleClass().add("attraction-card-selected");
            }
        }
    }

    @FXML
    private void handlePreviousAttraction() {
        if (!attractionsList.isEmpty()) {
            currentAttractionIndex = (currentAttractionIndex - 1 + attractionsList.size()) % attractionsList.size();
            displayFeaturedAttraction();
            updateAttractionCardSelection();
        }
    }

    @FXML
    private void handleNextAttraction() {
        if (!attractionsList.isEmpty()) {
            currentAttractionIndex = (currentAttractionIndex + 1) % attractionsList.size();
            displayFeaturedAttraction();
            updateAttractionCardSelection();
        }
    }

    @FXML
    private void handleBooking() {
        if (selectedAttraction == null) {
            showAlert("No Selection", "Please select an attraction to book.");
            return;
        }

        bookingModal.setVisible(true);
        calculateTotalAmount();
    }

    @FXML
    private void handleConfirmBooking() {
        if (selectedAttraction == null) {
            showAlert("No Selection", "Please select an attraction to book.");
            return;
        }

        LocalDate visitDate = visitDatePicker.getValue();
        if (visitDate == null || visitDate.isBefore(LocalDate.now())) {
            showAlert("Invalid Date", "Please select a valid future date.");
            return;
        }

        try {
            List<User> allUsers = DataManager.loadAllUsers();
            List<Guide> guides = allUsers.stream()
                    .filter(u -> u instanceof Guide)
                    .map(u -> (Guide) u)
                    .toList();

            if (guides.isEmpty()) {
                showAlert("No Guides", "No guides available for booking.");
                return;
            }

            Guide selectedGuide = guides.stream().findFirst().orElse(null);
            if (selectedGuide == null) {
                showAlert("No Guides", "No guides available for booking.");
                return;
            }

            Booking booking = BookingManager.createBooking(
                    currentUser.getUserId(),
                    selectedGuide.getUserId(),
                    selectedAttraction.getAttractionId(),
                    visitDate,
                    peopleSpinner.getValue(),
                    selectedAttraction.getEntryFee(),
                    specialRequestsArea.getText());

            showAlert("Booking Confirmed",
                    "Your booking has been confirmed!\nBooking ID: " + booking.getId() +
                            "\nTotal Amount: NPR " + String.format("%.2f", booking.getTotalPrice()));

            handleCancelBooking(); // Close the modal
        } catch (IOException e) {
            showAlert("Error", "Failed to create booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBooking() {
        bookingModal.setVisible(false);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nepaltourismmanagementapp/fxml/TouristDashboard.fxml"));
            Parent root = loader.load();

            TouristDashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets()
                    .add(getClass().getResource("/com/nepaltourismmanagementapp/css/style.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void calculateTotalAmount() {
        if (selectedAttraction != null && peopleSpinner != null && totalAmountLabel != null) {
            int people = peopleSpinner.getValue();
            double base = selectedAttraction.getEntryFee() * people;
            double discount = FestivalManager.calculateFestivalDiscount(base);
            double finalAmount = base - discount;

            totalAmountLabel.setText("Total: NPR " + String.format("%.2f", finalAmount));

            if (festivalDiscountLabel != null) {
                festivalDiscountLabel.setVisible(discount > 0);
            }
        }
    }

    private void showSafetyWarnings() {
        if (selectedAttraction == null)
            return;

        StringBuilder warnings = new StringBuilder();
        warnings.append("Safety Information for ").append(selectedAttraction.getName()).append("\n\n");

        // Add generic safety warnings
        warnings.append("• Check weather conditions before visiting\n");
        warnings.append("• Carry sufficient water and snacks\n");
        warnings.append("• Inform someone about your travel plans\n");
        warnings.append("• Keep emergency contacts handy\n");
        warnings.append("• Respect local customs and traditions\n");

        // Add specific warnings based on attraction category
        if ("Mountain".equalsIgnoreCase(selectedAttraction.getCategory())) {
            warnings.append("\nMountain Safety:\n");
            warnings.append("• Be aware of altitude sickness symptoms\n");
            warnings.append("• Carry appropriate gear for cold weather\n");
            warnings.append("• Use proper hiking equipment\n");
        } else if ("Religious".equalsIgnoreCase(selectedAttraction.getCategory())) {
            warnings.append("\nReligious Site Etiquette:\n");
            warnings.append("• Dress modestly covering shoulders and knees\n");
            warnings.append("• Remove shoes when required\n");
            warnings.append("• Ask permission before taking photographs\n");
        }

        showAlert("Safety Information", warnings.toString());
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