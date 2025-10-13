package com.motorph.inventory;

import java.util.Date;

public class InventoryData {
    private String brand;
    private String engineNumber;
    private Date dateEntered;
    private String status;
    private String level;

    public InventoryData(String brand, String engineNumber, Date dateEntered, String status, String level) {
        this.brand = brand;
        this.engineNumber = engineNumber;
        this.dateEntered = dateEntered;
        this.status = status;
        this.level = level;
    }

    // Getters
    public String getBrand() {
        return brand;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public Date getDateEntered() {
        return dateEntered;
    }

    public String getStatus() {
        return status;
    }

    public String getLevel() {
        return level;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "InventoryData{" +
                "brand='" + brand + '\'' +
                ", engineNumber='" + engineNumber + '\'' +
                ", dateEntered=" + dateEntered +
                ", status='" + status + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}