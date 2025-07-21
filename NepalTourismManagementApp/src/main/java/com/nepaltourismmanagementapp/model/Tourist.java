package com.nepaltourismmanagementapp.model;

public class Tourist extends User {
    private String nationality;
    // private String phoneNumber; // REMOVED: Now in User class
    private int age;

    public Tourist(String userId, String username, String password, String email, String fullName,
                   String nationality, String phoneNumber, int age) {
        super(userId, username, password, email, fullName, phoneNumber, "TOURIST"); // UPDATED: Pass phoneNumber to super
        this.nationality = nationality;
        // this.phoneNumber = phoneNumber; // REMOVED
        this.age = age;
    }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    // REMOVED: getPhoneNumber() and setPhoneNumber() as they are now in User
    // public String getPhoneNumber() { return phoneNumber; }
    // public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String getRole() { return "TOURIST"; }

    @Override
    public String toString() {
        // UPDATED: phoneNumber is now part of super.toString()
        return super.toString() + "," + nationality + "," + age;
    }
}
