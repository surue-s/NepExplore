package com.nepaltourismmanagementapp.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class FestivalManager {
    private static final Map<String, FestivalDiscount> festivals = new HashMap<>();

    static {
        // Nepali festivals with approximate dates
        festivals.put("DASHAIN", new FestivalDiscount("Dashain", Month.OCTOBER, 1, 15, 0.20)); // 20% discount
        festivals.put("TIHAR", new FestivalDiscount("Tihar", Month.NOVEMBER, 1, 5, 0.15)); // 15% discount
        festivals.put("HOLI", new FestivalDiscount("Holi", Month.MARCH, 15, 16, 0.10)); // 10% discount
        festivals.put("BUDDHA_JAYANTI", new FestivalDiscount("Buddha Jayanti", Month.MAY, 15, 16, 0.12)); // 12% discount
        festivals.put("GAHWA_PUNHI", new FestivalDiscount("Gahwa Punhi", Month.AUGUST, 15, 16, 0.08)); // 8% discount
        festivals.put("MAGHE_SANKRANTI", new FestivalDiscount("Maghe Sankranti", Month.JANUARY, 14, 15, 0.10)); // 10% discount
    }

    public static double calculateFestivalDiscount(double originalAmount) {
        LocalDate today = LocalDate.now();

        for (FestivalDiscount festival : festivals.values()) {
            if (isFestivalActive(today, festival)) {
                return originalAmount * festival.discountPercentage;
            }
        }

        return 0.0; // No discount
    }

    public static double applyFestivalDiscount(double originalAmount) {
        double discount = calculateFestivalDiscount(originalAmount);
        return originalAmount - discount;
    }

    public static String getCurrentFestival() {
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, FestivalDiscount> entry : festivals.entrySet()) {
            if (isFestivalActive(today, entry.getValue())) {
                return entry.getValue().name;
            }
        }

        return null;
    }

    private static boolean isFestivalActive(LocalDate date, FestivalDiscount festival) {
        int year = date.getYear();
        LocalDate startDate = LocalDate.of(year, festival.month, festival.startDay);
        LocalDate endDate = LocalDate.of(year, festival.month, festival.endDay);

        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private static class FestivalDiscount {
        String name;
        Month month;
        int startDay;
        int endDay;
        double discountPercentage;

        FestivalDiscount(String name, Month month, int startDay, int endDay, double discountPercentage) {
            this.name = name;
            this.month = month;
            this.startDay = startDay;
            this.endDay = endDay;
            this.discountPercentage = discountPercentage;
        }
    }
}
