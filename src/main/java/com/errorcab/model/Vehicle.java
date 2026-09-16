package com.errorcab.model;

/**
 * Represents a registered commercial cab vehicle in India.
 * Demonstrates Encapsulation.
 */
public class Vehicle {
    private int id;
    private String model;
    private String plateNumber;
    private CabType cabType;
    private String color;

    public Vehicle(int id, String model, String plateNumber, CabType cabType, String color) {
        this.id = id;
        this.model = model;
        this.plateNumber = plateNumber;
        this.cabType = cabType;
        this.color = color;
    }

    public Vehicle(String model, String plateNumber, CabType cabType, String color) {
        this(0, model, plateNumber, cabType, color);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public CabType getCabType() {
        return cabType;
    }

    public void setCabType(CabType cabType) {
        this.cabType = cabType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", model, plateNumber, cabType.getDisplayName());
    }
}
