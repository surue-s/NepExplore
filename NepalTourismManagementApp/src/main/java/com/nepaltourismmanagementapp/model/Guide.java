// FileName: /com/tourismapp/model/Guide.java
package com.nepaltourismmanagementapp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Guide extends User {
    private final String licenseNumber;
    private List<String> languages;
    private List<String> specializations;
    private double rating;
    private int experienceYears;
    private boolean isAvailable;

    // UPDATED: Constructor to include 'phone' and pass it to super
    public Guide(String userId, String username, String password, String email, String fullName, String phone, // 'phone' parameter re-added
                 String licenseNumber, List<String> languages, List<String> specializations,
                 double rating, int experienceYears) {
        super(userId, username, password, email, fullName, phone, "GUIDE"); // Pass 'phone' to super
        this.licenseNumber = licenseNumber;
        this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>();
        this.specializations = specializations != null ? new ArrayList<>(specializations) : new ArrayList<>();
        this.rating = rating;
        this.experienceYears = experienceYears;
        this.isAvailable = true;
    }

    // Getters and Setters (no changes here)
    public String getLicenseNumber() { return licenseNumber; }
    public List<String> getLanguages() { return new ArrayList<>(languages); }
    public void setLanguages(List<String> languages) { this.languages = languages != null ? new ArrayList<>(languages) : new ArrayList<>(); }
    public List<String> getSpecializations() { return new ArrayList<>(specializations); }
    public void setSpecializations(List<String> specializations) { this.specializations = specializations != null ? new ArrayList<>(specializations) : new ArrayList<>(); }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String getRole() { return userType; }

    @Override
    public String toString() {
        // User.toString() now produces 7 parts: userId,username,password,email,fullName,phone,userType
        // Guide adds 5 more parts: licenseNumber,languages,specializations,rating,experienceYears
        // Total parts in Guide.toString() will be 7 + 5 = 12
        return super.toString() + "," +
                licenseNumber + "," +
                String.join(";", languages) + "," +
                String.join(";", specializations) + "," +
                rating + "," +
                experienceYears;
    }

    public static Guide fromString(String data) {
        String[] parts = data.split(",", -1);

        // User.toString() now produces 7 parts.
        // Guide.toString() adds 5 more parts after the User parts.
        // So, a complete Guide string should have at least 12 parts.
        // The userType ("GUIDE") is expected at index 6 (the 7th part from User.toString()).
        if (parts.length < 12 || !parts[6].equals("GUIDE")) {
            return null; // Not enough parts or not a GUIDE type
        }

        // Extract User-specific fields (first 7 parts from User.toString())
        String userId = parts[0];
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];
        String fullName = parts[4];
        String phone = parts[5]; // 'phone' is now at index 5
        // parts[6] is the userType, which we've already checked to be "GUIDE"

        // Extract Guide-specific fields (starting from index 7)
        String license = parts[7]; // licenseNumber is the 8th part overall (index 7)
        List<String> langs = parts.length > 8 && !parts[8].isEmpty() ? Arrays.asList(parts[8].split(";")) : new ArrayList<>();
        List<String> specs = parts.length > 9 && !parts[9].isEmpty() ? Arrays.asList(parts[9].split(";")) : new ArrayList<>();
        double rating = parts.length > 10 ? Double.parseDouble(parts[10]) : 0.0;
        int expYears = parts.length > 11 ? Integer.parseInt(parts[11]) : 0;

        // Construct and return a new Guide object using the extracted data.
        return new Guide(userId, username, password, email, fullName, phone, license, langs, specs, rating, expYears);
    }
}
