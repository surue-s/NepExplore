package com.nepaltourismmanagementapp.controller;

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
import java.util.List;

import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private VBox loginContainer;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private RadioButton touristRadio;
    @FXML
    private RadioButton guideRadio;
    @FXML
    private RadioButton adminRadio;
    @FXML
    private ToggleGroup userTypeGroup;
    @FXML
    private ComboBox<String> languageCombo;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink registerLink;

    private final LanguageManager languageManager = LanguageManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DataManager.initializeDataFiles();
        setupRadioButtons();
        setupLanguageCombo();
        updateLanguage();
    }

    private void setupRadioButtons() {
        // Ensure radio buttons are in a toggle group
        if (userTypeGroup == null) {
            userTypeGroup = new ToggleGroup();
        }
        touristRadio.setToggleGroup(userTypeGroup);
        guideRadio.setToggleGroup(userTypeGroup);
        adminRadio.setToggleGroup(userTypeGroup);

        // Set default selection to Tourist
        touristRadio.setSelected(true);
    }

    private void setupLanguageCombo() {
        languageCombo.getItems().addAll("English", "नेपाली");
        languageCombo.setValue("English");
        languageCombo.setOnAction(e -> toggleLanguage());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String userType = getUserTypeFromRadio();

        // Debug output
        System.out.println("Login attempt - Username: " + username + ", UserType: " + userType);

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (userType == null) {
            showAlert("Error", "Please select a user type (Tourist, Guide, or Admin).");
            return;
        }

        try {
            User user = authenticateUser(username, password, userType);
            if (user != null) {
                System.out.println("Login successful for user: " + user.getUsername() + " as " + user.getRole());
                openDashboard(user);
            } else {
                showAlert("Login Failed",
                        """
                                Invalid username, password, or user type.
                                
                                Please check:
                                • Username and password are correct
                                • Selected user type matches your account
                                • Account exists in the system""");
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load user data: " + e.getMessage());
        }
    }

    private String getUserTypeFromRadio() {
        if (userTypeGroup.getSelectedToggle() == null) {
            return null; // No selection
        }

        if (touristRadio.isSelected()) {
            return "TOURIST";
        } else if (guideRadio.isSelected()) {
            return "GUIDE";
        } else if (adminRadio.isSelected()) {
            return "ADMIN";
        }
        return "TOURIST"; // Default fallback
    }

    private User authenticateUser(String username, String password, String userType) throws IOException {
        List<User> users = DataManager.loadAllUsers();

        // Debug: Print loaded users for troubleshooting
        System.out.println("Loaded " + users.size() + " users");
        for (User user : users) {
            System.out.println(
                    "User: " + user.getUsername() + ", Role: " + user.getRole() + ", Type: " + user.getUserType());
        }

        return users.stream()
                .filter(user -> {
                    boolean usernameMatch = user.getUsername().equals(username);
                    boolean passwordMatch = user.getPassword().equals(password);
                    // Use getRole() instead of getUserType() for authentication
                    boolean roleMatch = user.getRole().equalsIgnoreCase(userType);

                    // Debug output for failed matches
                    if (usernameMatch) {
                        System.out.println("Username match for: " + username);
                        if (passwordMatch) {
                            System.out.println("Password match for: " + username);
                            System.out.println("Role check - User role: " + user.getRole() + ", Expected: " + userType
                                    + ", Match: " + roleMatch);
                        } else {
                            System.out.println("Password mismatch for: " + username);
                        }
                    }

                    return usernameMatch && passwordMatch && roleMatch;
                })
                .findFirst()
                .orElse(null);
    }

    private void openDashboard(User user) {
        try {
            String fxmlFile = getDashboardFxmlPath(user.getRole());
            if (fxmlFile == null) {
                showAlert("Error", "Unknown user role: " + user.getRole());
                return;
            }

            URL fxmlUrl = getClass().getResource(fxmlFile);
            if (fxmlUrl == null) {
                showAlert("Error", "FXML file not found: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Pass user data to dashboard controller
            setUserInController(loader.getController(), user);

            Scene scene = new Scene(root, 1200, 800);
            addStylesheet(scene);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open dashboard: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    private String getDashboardFxmlPath(String role) {
        switch (role.toUpperCase()) {
            case "TOURIST":
                return "/com/nepaltourismmanagementapp/fxml/TouristDashboard.fxml";
            case "GUIDE":
                return "/com/nepaltourismmanagementapp/fxml/GuideDashboard.fxml";
            case "ADMIN":
                return "/com/nepaltourismmanagementapp/fxml/AdminDashboard.fxml";
            default:
                return null;
        }
    }

    private void setUserInController(Object controller, User user) {
        if (controller instanceof TouristDashboardController && user instanceof Tourist) {
            ((TouristDashboardController) controller).setCurrentUser((Tourist) user);
        } else if (controller instanceof GuideDashboardController && user instanceof Guide) {
            ((GuideDashboardController) controller).setCurrentUser((Guide) user);
        } else if (controller instanceof AdminDashboardController && user instanceof Admin) {
            ((AdminDashboardController) controller).setCurrentUser((Admin) user);
        }
    }

    private void addStylesheet(Scene scene) {
        URL cssUrl = getClass().getResource("/com/nepaltourismmanagementapp/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Warning: CSS file not found. UI may not display correctly.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            String fxmlFile = "/com/nepaltourismmanagementapp/fxml/Register.fxml";
            URL fxmlUrl = getClass().getResource(fxmlFile);

            if (fxmlUrl == null) {
                showAlert("Error", "FXML file not found: " + fxmlFile);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 1200, 800);
            addStylesheet(scene);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration form: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void toggleLanguage() {
        String selectedLanguage = languageCombo.getValue();
        String newLang = "नेपाली".equals(selectedLanguage) ? "ne" : "en";
        languageManager.setCurrentLanguage(newLang);
        updateLanguage();
    }

    private void updateLanguage() {
        // Update UI text based on selected language
        if (languageManager.isNepali()) {
            loginButton.setText("लग-इन");
            registerLink.setText("दर्ता गर्नुहोस्");
            usernameField.setPromptText("प्रयोगकर्ता नाम");
            passwordField.setPromptText("पासवर्ड");
            touristRadio.setText("पर्यटक");
            guideRadio.setText("गाइड");
            adminRadio.setText("प्रशासक");
            languageCombo.setValue("नेपाली");
        } else {
            loginButton.setText("login");
            registerLink.setText("Register");
            usernameField.setPromptText("username");
            passwordField.setPromptText("password");
            touristRadio.setText("tourist");
            guideRadio.setText("guide");
            adminRadio.setText("admin");
            languageCombo.setValue("English");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}