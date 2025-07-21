package com.nepaltourismmanagementapp.model;

public class Admin extends User {
    private String adminLevel;

    public Admin(String userId, String username, String password, String email, String fullName, String adminLevel) {
        // FIX: Added an empty string "" for the 'phone' argument to match the User constructor's 7 arguments.
        super(userId, username, password, email, fullName, "", "ADMIN");
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public String toString() {
        // User.toString() now includes phone, so this will correctly append adminLevel
        return super.toString() + "," + adminLevel;
    }
}
