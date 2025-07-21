package com.nepaltourismmanagementapp.utils;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage = "en";
    private Map<String, Map<String, String>> translations;

    private LanguageManager() {
        initializeTranslations();
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private void initializeTranslations() {
        translations = new HashMap<>();

        // English translations
        Map<String, String> en = new HashMap<>();
        en.put("login", "Login");
        en.put("username", "Username");
        en.put("password", "Password");
        en.put("register", "Register");
        en.put("dashboard", "Dashboard");
        en.put("attractions", "Attractions");
        en.put("bookings", "Bookings");
        en.put("profile", "Profile");
        en.put("logout", "Logout");
        en.put("welcome", "Welcome to Nepal Tourism");
        en.put("tourist", "Tourist");
        en.put("guide", "Guide");
        en.put("admin", "Admin");
        en.put("statistics", "Statistics");
        en.put("reports", "Reports");
        en.put("users", "Users");
        en.put("settings", "Settings");

        // Nepali translations
        Map<String, String> ne = new HashMap<>();
        ne.put("login", "लग इन");
        ne.put("username", "प्रयोगकर्ता नाम");
        ne.put("password", "पासवर्ड");
        ne.put("register", "दर्ता गर्नुहोस्");
        ne.put("dashboard", "ड्यासबोर्ड");
        ne.put("attractions", "आकर्षणहरू");
        ne.put("bookings", "बुकिङहरू");
        ne.put("profile", "प्रोफाइल");
        ne.put("logout", "लग आउट");
        ne.put("welcome", "नेपाल पर्यटनमा स्वागत छ");
        ne.put("tourist", "पर्यटक");
        ne.put("guide", "गाइड");
        ne.put("admin", "प्रशासक");
        ne.put("statistics", "तथ्याङ्क");
        ne.put("reports", "रिपोर्टहरू");
        ne.put("users", "प्रयोगकर्ताहरू");
        ne.put("settings", "सेटिङहरू");

        translations.put("en", en);
        translations.put("ne", ne);
    }

    public String getText(String key) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, key);
    }

    public void setCurrentLanguage(String language) {
        if (translations.containsKey(language)) {
            this.currentLanguage = language;
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isNepali() {
        return "ne".equals(currentLanguage);
    }
}
