package com.nepaltourismmanagementapp.utils;

import com.nepaltourismmanagementapp.model.Booking;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookingManager {

    public static String generateBookingId() {
        return "BK" + System.currentTimeMillis();
    }

    public static double calculateTotalAmount(double baseAmount, int numberOfPeople) {
        double total = baseAmount * numberOfPeople;
        return FestivalManager.applyFestivalDiscount(total);
    }

    // UPDATED: createBooking method to align with the Booking model's fields and
    // constructor logic
    public static Booking createBooking(String touristId, String guideId, String attractionId,
            LocalDate visitDate, int numberOfPeople, double baseAmount,
            String specialRequests) throws IOException {
        String bookingId = generateBookingId();
        double totalAmount = calculateTotalAmount(baseAmount, numberOfPeople);

        // The Booking model's constructor is:
        // public Booking(String id, String touristUsername, String guideUsername,
        // String attractionId, LocalDate trekDate, double totalPrice)
        // The default constructor sets bookingDate, status, discount,
        // emergencyReported.
        // So, we use the main constructor and then set additional fields if needed.

        Booking booking = new Booking(
                bookingId,
                touristId,
                guideId,
                attractionId,
                visitDate, // This maps to trekDate
                totalAmount // This maps to totalPrice
        );
        // Set additional fields that are not in the main constructor but are in the
        // model
        booking.setNotes(specialRequests);
        booking.setStatus(Booking.BookingStatus.CONFIRMED); // Default status upon creation
        booking.setNumberOfPeople(numberOfPeople); // Set the number of people

        DataManager.saveBooking(booking);
        return booking;
    }

    public static List<Booking> getBookingsByTourist(String touristId) throws IOException {
        return DataManager.loadAllBookings().stream()
                .filter(booking -> booking.getTouristUsername().equals(touristId)) // Changed getTouristId to
                                                                                   // getTouristUsername
                .collect(Collectors.toList());
    }

    public static List<Booking> getBookingsByGuide(String guideId) throws IOException {
        return DataManager.loadAllBookings().stream()
                .filter(booking -> booking.getGuideUsername().equals(guideId)) // Changed getGuideId to getGuideUsername
                .collect(Collectors.toList());
    }

    public static List<Booking> getUpcomingBookings() throws IOException {
        LocalDate today = LocalDate.now();
        return DataManager.loadAllBookings().stream()
                .filter(booking -> booking.getTrekDate().isAfter(today)) // Changed getVisitDate to getTrekDate
                .collect(Collectors.toList());
    }

    public static List<Booking> loadAllBookings() throws IOException {
        return DataManager.loadAllBookings();
    }

    public static void updateBookingStatus(String bookingId, String newStatus) throws IOException {
        List<Booking> bookings = DataManager.loadAllBookings();

        // Update booking status in memory
        bookings.stream()
                .filter(booking -> booking.getId().equals(bookingId)) // Changed getBookingId to getId
                .findFirst()
                .ifPresent(booking -> booking.setStatus(Booking.BookingStatus.valueOf(newStatus))); // Use enum

        // Save all bookings
        saveAllBookings(bookings);
    }

    public static void saveAllBookings(List<Booking> bookings) throws IOException {
        // Rewrite the entire file with updated data
        try (PrintWriter writer = new PrintWriter(new FileWriter("Data/bookings.txt"))) {
            for (Booking booking : bookings) {
                writer.println(booking.toString());
            }
        }
    }
}
