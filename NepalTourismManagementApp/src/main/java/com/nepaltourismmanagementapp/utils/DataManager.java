package com.nepaltourismmanagementapp.utils;

import com.nepaltourismmanagementapp.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManager {
    private static final String DATA_DIR = "Data/";
    private static final String USERS_FILE = DATA_DIR + "users.txt";
    private static final String ATTRACTIONS_FILE = DATA_DIR + "attractions.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.txt";

    public static void initializeDataFiles() {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // Initialize with default admin
            if (!new File(USERS_FILE).exists()) {
                createDefaultAdmin();
            }

            // Initialize with sample attractions
            if (!new File(ATTRACTIONS_FILE).exists()) {
                createDefaultAttractions();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultAdmin() throws IOException {
        // UPDATED: Added phone number (empty string) to Admin constructor
        Admin admin = new Admin("ADM001", "admin", "admin123", "admin@tourism.np", "System Administrator", "SUPER");
        saveUser(admin);
    }

    private static void createDefaultAttractions() throws IOException {
        List<Attraction> attractions = Arrays.asList(
                // Using the enhanced constructor with district, province, latitude, longitude
                new Attraction("ATT001", "Mount Everest", "सगरमाथा",
                        "Khumbu", "Solukhumbu", 27.9881, 86.9250,
                        "World's highest mountain peak", "संसारको सबैभन्दा अग्लो हिमाल",
                        "Mountain", 5000.0, "everest.jpg", 4.9, true),
                new Attraction("ATT002", "Pashupatinath Temple", "पशुपतिनाथ मन्दिर",
                        "Kathmandu", "Bagmati", 27.7109, 85.3484,
                        "Sacred Hindu temple dedicated to Lord Shiva", "भगवान शिवलाई समर्पित पवित्र हिन्दू मन्दिर",
                        "Religious", 1000.0, "pashupatinath.jpg", 4.8, true),
                new Attraction("ATT003", "Boudhanath Stupa", "बौधनाथ स्तुप",
                        "Kathmandu", "Bagmati", 27.7215, 85.3620,
                        "Ancient Buddhist stupa", "पुरानो बौद्ध स्तुप",
                        "Religious", 200.0, "boudhanath.jpg", 4.7, true),
                new Attraction("ATT004", "Chitwan National Park", "चितवन राष्ट्रिय निकुञ्ज",
                        "Chitwan", "Narayani", 27.5291, 84.3542,
                        "Wildlife sanctuary with rhinos and tigers", "गैंडा र बाघसहितको वन्यजन्तु अभयारण्य",
                        "Wildlife", 2000.0, "chitwan.jpg", 4.6, true));

        try (PrintWriter writer = new PrintWriter(new FileWriter(ATTRACTIONS_FILE))) {
            for (Attraction attraction : attractions) {
                writer.println(attraction.toString());
            }
        }
    }

    public static void saveUser(User user) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            writer.println(user.toString());
        }
    }

    // NEW METHOD: To update an existing user
    public static void updateUser(User updatedUser) throws IOException {
        List<User> allUsers = loadAllUsers();
        boolean found = false;
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserId().equals(updatedUser.getUserId())) {
                allUsers.set(i, updatedUser); // Replace the old user with the updated one
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IOException("User with ID " + updatedUser.getUserId() + " not found for update.");
        }

        // Rewrite the entire users file
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, false))) { // false to overwrite
            for (User user : allUsers) {
                writer.println(user.toString());
            }
        }
    }

    public static List<User> loadAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists())
            return users;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = parseUser(line);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    private static User parseUser(String line) {
        String[] parts = line.split(",", -1); // Use -1 to keep trailing empty strings
        // User.toString() now produces 7 parts:
        // userId,username,password,email,fullName,phone,userType
        if (parts.length < 7)
            return null; // Minimum parts for a User

        String userType = parts[6]; // userType is now at index 6
        String userId = parts[0];
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];
        String fullName = parts[4];
        String phone = parts[5]; // phone is now at index 5

        switch (userType) {
            case "ADMIN":
                if (parts.length >= 8) { // Admin adds 1 more field (adminLevel)
                    return new Admin(userId, username, password, email, fullName, parts[7]);
                }
                break;
            case "TOURIST":
                // Tourist.toString() is super.toString() + "," + nationality + "," + age;
                // So, 7 (from User) + 2 = 9 parts total
                if (parts.length >= 9) {
                    return new Tourist(userId, username, password, email, fullName,
                            parts[7], phone, Integer.parseInt(parts[8])); // nationality, phone (from User), age
                }
                break;
            case "GUIDE":
                // Guide.toString() is super.toString() + "," + licenseNumber + "," + languages
                // + "," + specializations + "," + rating + "," + experienceYears;
                // So, 7 (from User) + 5 = 12 parts total
                if (parts.length >= 12) {
                    List<String> languages = parts[8].isEmpty() ? new ArrayList<>()
                            : Arrays.asList(parts[8].split(";"));
                    List<String> specializations = parts[9].isEmpty() ? new ArrayList<>()
                            : Arrays.asList(parts[9].split(";"));
                    return new Guide(userId, username, password, email, fullName, phone, // Pass phone
                            parts[7], languages, specializations, Double.parseDouble(parts[10]),
                            Integer.parseInt(parts[11]));
                }
                break;
        }
        return null;
    }

    public static List<Attraction> loadAllAttractions() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        File file = new File(ATTRACTIONS_FILE);
        if (!file.exists())
            return attractions;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Attraction attraction = parseAttraction(line);
                if (attraction != null) {
                    attractions.add(attraction);
                }
            }
        }
        return attractions;
    }

    private static Attraction parseAttraction(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length >= 11) {
                // Create attraction with basic constructor first
                Attraction attraction = new Attraction(
                        parts[0], // attractionId
                        parts[1], // name
                        parts[2], // nameNepali
                        parts[3], // location
                        parts[4], // description
                        parts[5], // descriptionNepali
                        parts[6], // category
                        Double.parseDouble(parts[7]), // entryFee
                        parts[8], // imageUrl
                        Double.parseDouble(parts[9]), // rating
                        Boolean.parseBoolean(parts[10]) // isActive
                );

                // Handle additional location fields if they exist
                if (parts.length >= 15) {
                    if (parts.length > 11 && !parts[11].isEmpty())
                        attraction.setDistrict(parts[11]);
                    if (parts.length > 12 && !parts[12].isEmpty())
                        attraction.setProvince(parts[12]);
                    if (parts.length > 13 && !parts[13].isEmpty()) {
                        try {
                            attraction.setLatitude(Double.parseDouble(parts[13]));
                        } catch (NumberFormatException e) {
                            System.out.println("Warning: Invalid latitude format for " + parts[1] + ": " + parts[13]);
                        }
                    }
                    if (parts.length > 14 && !parts[14].isEmpty()) {
                        try {
                            attraction.setLongitude(Double.parseDouble(parts[14]));
                        } catch (NumberFormatException e) {
                            System.out.println("Warning: Invalid longitude format for " + parts[1] + ": " + parts[14]);
                        }
                    }
                }
                return attraction;
            }
            return null;
        } catch (NumberFormatException e) {
            System.out.println("Error parsing attraction: " + e.getMessage() + " in line: " + line);
            // Create a dummy attraction with default values for testing
            try {
                String[] parts = line.split(",");
                if (parts.length >= 11) {
                    return new Attraction(
                            parts[0], // id
                            parts[1], // name
                            parts[2], // nameNepali
                            parts[3], // location
                            parts[4], // description
                            parts[5], // descriptionNepali
                            parts[6], // category
                            1000.0, // default entryFee
                            parts[8], // imageUrl
                            4.5, // default rating
                            true // default isActive
                    );
                }
            } catch (Exception ex) {
                System.out.println("Failed to create dummy attraction: " + ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error parsing attraction: " + e.getMessage());
            return null;
        }
    }

    public static void saveBooking(Booking booking) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, true))) {
            writer.println(booking.toString());
        }
    }

    public static List<Booking> loadAllBookings() throws IOException {
        List<Booking> bookings = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists())
            return bookings;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Booking booking = Booking.fromString(line); // Use Booking's static fromString
                if (booking != null) {
                    bookings.add(booking);
                }
            }
        }
        return bookings;
    }
}