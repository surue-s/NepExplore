package com.nepaltourismmanagementapp.controller;

import com.nepaltourismmanagementapp.exception.RegistrationException;
import com.nepaltourismmanagementapp.model.*;
import com.nepaltourismmanagementapp.utils.DataManager;
import com.nepaltourismmanagementapp.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField fullNameField;
    @FXML
    private RadioButton touristRadio;
    @FXML
    private RadioButton guideRadio;
    @FXML
    private RadioButton adminRadio;
    @FXML
    private ToggleGroup userTypeGroup;
    @FXML
    private TextField nationalityField;
    @FXML
    private TextField phoneField;
    @FXML
    private Spinner<Integer> ageSpinner;
    @FXML
    private TextField licenseField;
    @FXML
    private TextField languagesField;
    @FXML
    private TextField specializationsField;
    @FXML
    private Spinner<Integer> experienceSpinner;
    @FXML
    private VBox touristFields;
    @FXML
    private VBox guideFields;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink loginLink;
    @FXML
    private ComboBox<String> languageCombo;

    private final LanguageManager languageManager = LanguageManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUserTypeRadios();
        setupSpinners();
        setupLanguageCombo();
        updateLanguage();
    }

    private void setupUserTypeRadios() {
        touristRadio.setSelected(true);

        // Add listeners to toggle fields visibility
        touristRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                touristFields.setVisible(true);
                guideFields.setVisible(false);
            }
        });

        guideRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                touristFields.setVisible(false);
                guideFields.setVisible(true);
            }
        });

        adminRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                touristFields.setVisible(false);
                guideFields.setVisible(false);
            }
        });
    }

    private void setupSpinners() {
        try {
            ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 25));
            experienceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));
        } catch (Exception e) {
            System.err.println("Error setting up spinners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupLanguageCombo() {
        try {
            languageCombo.getItems().addAll("English", "नेपाली");
            languageCombo.setValue("English");
            languageCombo.setOnAction(e -> toggleLanguage());
        } catch (Exception e) {
            System.err.println("Error setting up language combo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            validateFields();

            String userId = generateUserId();
            String userType = getUserTypeFromRadio();

            User newUser;
            if ("TOURIST".equals(userType)) {
                newUser = new Tourist(
                        userId,
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        emailField.getText().trim(),
                        fullNameField.getText().trim(),
                        nationalityField.getText().trim(),
                        phoneField.getText().trim(),
                        ageSpinner.getValue());
            } else if ("GUIDE".equals(userType)) {
                // Ensure languages and specializations are not null if input is empty
                List<String> languages = languagesField.getText().trim().isEmpty() ? new ArrayList<>()
                        : Arrays.asList(languagesField.getText().split(","));
                List<String> specializations = specializationsField.getText().trim().isEmpty() ? new ArrayList<>()
                        : Arrays.asList(specializationsField.getText().split(","));

                newUser = new Guide(
                        userId,
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        emailField.getText().trim(),
                        fullNameField.getText().trim(),
                        phoneField.getText().trim(),
                        licenseField.getText().trim(),
                        languages,
                        specializations,
                        0.0, // Initial rating
                        experienceSpinner.getValue());
            } else {
                // Admin registration (typically would be restricted)
                newUser = new Admin(
                        userId,
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        emailField.getText().trim(),
                        fullNameField.getText().trim(),
                        "STANDARD" // Default admin level
                );
            }

            DataManager.saveUser(newUser);

            showAlert("Success", "Registration successful! You can now login with your credentials.");
            handleBack();

        } catch (RegistrationException e) {
            showAlert("Registration Failed", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Registration Failed", "Error saving user data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Registration Failed", "Unexpected error: " + e.getMessage());
        }
    }

    private String getUserTypeFromRadio() {
        if (touristRadio.isSelected()) {
            return "TOURIST";
        } else if (guideRadio.isSelected()) {
            return "GUIDE";
        } else if (adminRadio.isSelected()) {
            return "ADMIN";
        }
        return "TOURIST"; // Default
    }

    private void validateFields() throws RegistrationException {
        if (usernameField.getText().trim().isEmpty()) {
            throw new RegistrationException("Username is required.");
        }

        if (passwordField.getText().isEmpty()) {
            throw new RegistrationException("Password is required.");
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            throw new RegistrationException("Passwords do not match.");
        }

        if (emailField.getText().trim().isEmpty()) {
            throw new RegistrationException("Email is required.");
        }

        if (fullNameField.getText().trim().isEmpty()) {
            throw new RegistrationException("Full name is required.");
        }

        String userType = getUserTypeFromRadio();
        if ("TOURIST".equals(userType)) {
            if (nationalityField.getText().trim().isEmpty()) {
                throw new RegistrationException("Nationality is required for tourists.");
            }
            if (phoneField.getText().trim().isEmpty()) {
                throw new RegistrationException("Phone number is required for tourists.");
            }
        } else if ("GUIDE".equals(userType)) {
            if (phoneField.getText().trim().isEmpty()) {
                throw new RegistrationException("Phone number is required for guides.");
            }
            if (licenseField.getText().trim().isEmpty()) {
                throw new RegistrationException("License number is required for guides.");
            }
            if (languagesField.getText().trim().isEmpty()) {
                throw new RegistrationException("Languages are required for guides.");
            }
        }

        // Check if username already exists
        try {
            List<User> existingUsers = DataManager.loadAllUsers();
            boolean usernameExists = existingUsers.stream()
                    .anyMatch(user -> user.getUsername().equals(usernameField.getText().trim()));

            if (usernameExists) {
                throw new RegistrationException("Username already exists. Please choose a different one.");
            }
        } catch (IOException e) {
            throw new RegistrationException("Failed to check existing users: " + e.getMessage());
        }
    }

    private String generateUserId() {
        String userType = getUserTypeFromRadio();
        String prefix = "TOURIST".equals(userType) ? "TOU" : "GUIDE".equals(userType) ? "GUD" : "ADM";
        return prefix + String.format("%03d", (int) (Math.random() * 1000));
    }

    @FXML
    private void handleBack() {
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

            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go back: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void toggleLanguage() {
        try {
            String selectedLanguage = languageCombo.getValue();
            String newLang = "नेपाली".equals(selectedLanguage) ? "ne" : "en";
            languageManager.setCurrentLanguage(newLang);
            updateLanguage();
        } catch (Exception e) {
            System.err.println("Error toggling language: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLanguage() {
        try {
            // Update UI text based on selected language
            if (languageManager.isNepali()) {
                registerButton.setText("दर्ता गर्नुहोस्");
                loginLink.setText("लग-इन");
                fullNameField.setPromptText("पूरा नाम");
                usernameField.setPromptText("प्रयोगकर्ता नाम");
                emailField.setPromptText("इमेल");
                phoneField.setPromptText("फोन नम्बर");
                nationalityField.setPromptText("राष्ट्रियता");
                passwordField.setPromptText("पासवर्ड");
                confirmPasswordField.setPromptText("पासवर्ड पुष्टि गर्नुहोस्");
                touristRadio.setText("पर्यटक");
                guideRadio.setText("गाइड");
                adminRadio.setText("प्रशासक");
                languageCombo.setValue("नेपाली");
            } else {
                registerButton.setText("Sign Up");
                loginLink.setText("Login");
                fullNameField.setPromptText("Full Name");
                usernameField.setPromptText("Username");
                emailField.setPromptText("Email");
                phoneField.setPromptText("Phone Number");
                nationalityField.setPromptText("Nationality");
                passwordField.setPromptText("Password");
                confirmPasswordField.setPromptText("Confirm Password");
                touristRadio.setText("tourist");
                guideRadio.setText("guide");
                adminRadio.setText("admin");
                languageCombo.setValue("English");
            }
        } catch (Exception e) {
            System.err.println("Error updating language: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.contains("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}