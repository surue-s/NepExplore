import com.nepaltourismmanagementapp.utils.DataManager;
import com.nepaltourismmanagementapp.model.User;
import java.util.List;

public class TestLogin {
    public static void main(String[] args) {
        try {
            System.out.println("Testing login system...");

            // Initialize data files
            DataManager.initializeDataFiles();

            // Load all users
            List<User> users = DataManager.loadAllUsers();
            System.out.println("Loaded " + users.size() + " users:");

            for (User user : users) {
                System.out.println("- Username: " + user.getUsername() +
                        ", Role: " + user.getRole() +
                        ", UserType: " + user.getUserType());
            }

            // Test authentication for each known user
            String[][] testCredentials = {
                    { "admin", "admin123", "ADMIN" },
                    { "tourist1", "pass123", "TOURIST" },
                    { "guide1", "guide123", "GUIDE" },
                    { "sachet", "sachet123", "TOURIST" }
            };

            System.out.println("\nTesting authentication:");
            for (String[] cred : testCredentials) {
                String username = cred[0];
                String password = cred[1];
                String expectedRole = cred[2];

                User foundUser = users.stream()
                        .filter(user -> user.getUsername().equals(username) &&
                                user.getPassword().equals(password) &&
                                user.getRole().equalsIgnoreCase(expectedRole))
                        .findFirst()
                        .orElse(null);

                if (foundUser != null) {
                    System.out.println("✓ " + username + " (" + expectedRole + ") - Authentication SUCCESS");
                } else {
                    System.out.println("✗ " + username + " (" + expectedRole + ") - Authentication FAILED");
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}