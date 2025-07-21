package com.nepaltourismmanagementapp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Booking {
    private String id;
    private String touristUsername;
    private String guideUsername;
    private String attractionId;
    private LocalDate bookingDate;
    private LocalDate trekDate; // Field for trek date
    private BookingStatus status;
    private double totalPrice; // Field for total price
    private double discount;
    private String notes;
    private boolean emergencyReported;
    private int numberOfPeople; // Added field for number of people

    public enum BookingStatus {
        PENDING, CONFIRMED, COMPLETED, CANCELLED, EMERGENCY
    }

    public Booking() {
        this.bookingDate = LocalDate.now();
        this.status = BookingStatus.PENDING;
        this.discount = 0.0;
        this.emergencyReported = false;
        this.numberOfPeople = 1; // Default to 1 person
    }

    public Booking(String id, String touristUsername, String guideUsername, String attractionId,
            LocalDate trekDate, double totalPrice) {
        this(); // Call default constructor to set bookingDate, status, discount,
                // emergencyReported
        this.id = id;
        this.touristUsername = touristUsername;
        this.guideUsername = guideUsername;
        this.attractionId = attractionId;
        this.trekDate = trekDate;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTouristUsername() {
        return touristUsername;
    }

    public void setTouristUsername(String touristUsername) {
        this.touristUsername = touristUsername;
    }

    public String getGuideUsername() {
        return guideUsername;
    }

    public void setGuideUsername(String guideUsername) {
        this.guideUsername = guideUsername;
    }

    public String getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(String attractionId) {
        this.attractionId = attractionId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getTrekDate() {
        return trekDate;
    } // ADDED/CONFIRMED: getTrekDate()

    public void setTrekDate(LocalDate trekDate) {
        this.trekDate = trekDate;
    } // ADDED/CONFIRMED: setTrekDate()

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    } // ADDED/CONFIRMED: getTotalPrice()

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    } // ADDED/CONFIRMED: setTotalPrice()

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isEmergencyReported() {
        return emergencyReported;
    }

    public void setEmergencyReported(boolean emergencyReported) {
        this.emergencyReported = emergencyReported;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public double getFinalPrice() {
        return totalPrice - discount;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return id + "," + touristUsername + "," + guideUsername + "," + attractionId + "," +
                bookingDate.format(formatter) + "," + trekDate.format(formatter) + "," +
                status.name() + "," + totalPrice + "," + discount + "," + // Use status.name() for string representation
                (notes != null ? notes.replace(",", ";") : "") + "," + emergencyReported + "," + numberOfPeople;
    }

    public static Booking fromString(String data) {
        String[] parts = data.split(",", -1); // Use -1 to keep trailing empty strings
        if (parts.length >= 10) { // Minimum parts for a booking string
            Booking booking = new Booking();
            booking.setId(parts[0]);
            booking.setTouristUsername(parts[1]);
            booking.setGuideUsername(parts[2]);
            booking.setAttractionId(parts[3]);
            booking.setBookingDate(LocalDate.parse(parts[4]));
            booking.setTrekDate(LocalDate.parse(parts[5]));
            booking.setStatus(BookingStatus.valueOf(parts[6])); // Parse enum from string
            booking.setTotalPrice(Double.parseDouble(parts[7]));
            booking.setDiscount(Double.parseDouble(parts[8]));
            if (parts.length > 9 && !parts[9].isEmpty()) { // Check if notes part exists and is not empty
                booking.setNotes(parts[9].replace(";", ","));
            }
            if (parts.length > 10) { // Check if emergencyReported part exists
                booking.setEmergencyReported(Boolean.parseBoolean(parts[10]));
            }
            if (parts.length > 11) { // Check if numberOfPeople part exists
                try {
                    booking.setNumberOfPeople(Integer.parseInt(parts[11]));
                } catch (NumberFormatException e) {
                    // Default to 1 if parsing fails
                    booking.setNumberOfPeople(1);
                }
            }
            return booking;
        }
        return null;
    }
}
