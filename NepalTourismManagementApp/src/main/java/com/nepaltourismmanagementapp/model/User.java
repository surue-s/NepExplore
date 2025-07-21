// FileName: /com/tourismapp/model/User.java
package com.nepaltourismmanagementapp.model;

public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String email;
    protected String fullName;
    protected String phone; // ADDED: phone field
    protected String userType;

    public User() {} // Default constructor added for flexibility

    // UPDATED: Constructor to include 'phone'
    public User(String userId, String username, String password, String email, String fullName, String phone, String userType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone; // Initialize phone
        this.userType = userType;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; } // ADDED: getPhone() method
    public void setPhone(String phone) { this.phone = phone; } // ADDED: setPhone() method

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public abstract String getRole();

    @Override
    public String toString() {
        // UPDATED: toString to include 'phone'
        return userId + "," + username + "," + password + "," + email + "," + fullName + "," + phone + "," + userType;
    }

    // Optional: fromString method for User (if you need to parse generic User strings)
    // public static User fromString(String data) {
    //     String[] parts = data.split(",");
    //     if (parts.length >= 7) { // Now 7 parts including phone
    //         String type = parts[6]; // userType is at index 6
    //         // You'd need to determine the specific subclass (Tourist, Guide, Admin) here
    //         // For now, this is just a placeholder if needed.
    //         return null; // Or throw an exception, or return a basic User if possible
    //     }
    //     return null;
    // }
}
