package com.nepaltourismmanagementapp.model;

public class Attraction {
    private String attractionId;
    private String name;
    private String nameNepali;
    private String location;
    private String district;
    private String province;
    private double latitude;
    private double longitude;
    private String description;
    private String descriptionNepali;
    private String category;
    private double entryFee;
    private String imageUrl;
    private double rating;
    private boolean isActive;

    public Attraction(String attractionId, String name, String nameNepali, String location,
                      String description, String descriptionNepali, String category,
                      double entryFee, String imageUrl, double rating, boolean isActive) {
        this.attractionId = attractionId;
        this.name = name;
        this.nameNepali = nameNepali;
        this.location = location;
        this.description = description;
        this.descriptionNepali = descriptionNepali;
        this.category = category;
        this.entryFee = entryFee;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.isActive = isActive;
        
        // Parse district and province from location if in format "District, Province"
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            this.district = parts[0].trim();
            if (parts.length > 1) {
                this.province = parts[1].trim();
            }
        }
    }
    
    // Enhanced constructor with all location fields
    public Attraction(String attractionId, String name, String nameNepali, 
                      String district, String province, double latitude, double longitude,
                      String description, String descriptionNepali, String category,
                      double entryFee, String imageUrl, double rating, boolean isActive) {
        this.attractionId = attractionId;
        this.name = name;
        this.nameNepali = nameNepali;
        this.district = district;
        this.province = province;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = district + ", " + province; // Format location as "District, Province"
        this.description = description;
        this.descriptionNepali = descriptionNepali;
        this.category = category;
        this.entryFee = entryFee;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getAttractionId() { return attractionId; }
    public void setAttractionId(String attractionId) { this.attractionId = attractionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameNepali() { return nameNepali; }
    public void setNameNepali(String nameNepali) { this.nameNepali = nameNepali; }

    public String getLocation() { return location; }
    public void setLocation(String location) { 
        this.location = location; 
        // Parse district and province from location if in format "District, Province"
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            this.district = parts[0].trim();
            if (parts.length > 1) {
                this.province = parts[1].trim();
            }
        }
    }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { 
        this.district = district; 
        updateLocationString();
    }
    
    public String getProvince() { return province; }
    public void setProvince(String province) { 
        this.province = province; 
        updateLocationString();
    }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    // Helper method to update location string when district or province changes
    private void updateLocationString() {
        if (district != null && province != null) {
            this.location = district + ", " + province;
        } else if (district != null) {
            this.location = district;
        } else if (province != null) {
            this.location = province;
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionNepali() { return descriptionNepali; }
    public void setDescriptionNepali(String descriptionNepali) { this.descriptionNepali = descriptionNepali; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getEntryFee() { return entryFee; }
    public void setEntryFee(double entryFee) { this.entryFee = entryFee; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return attractionId + "," + name + "," + nameNepali + "," + location + "," +
                description + "," + descriptionNepali + "," + category + "," + entryFee + "," +
                imageUrl + "," + rating + "," + isActive + "," +
                (district != null ? district : "") + "," + (province != null ? province : "") + "," +
                latitude + "," + longitude;
    }
}
