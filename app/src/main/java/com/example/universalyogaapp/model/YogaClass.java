package com.example.universalyogaapp.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "yoga_classes")
public class YogaClass {

    @PrimaryKey(autoGenerate = true)
    private int id;  // Primary key

    private String day;
    private String time;
    private int capacity;
    private int duration;
    private double price;
    private String classType;
    private String description;

    // Constructor
    public YogaClass(String day, String time, int capacity, int duration, double price, String classType, String description) {
        this.day = day;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.classType = classType;
        this.description = description;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) { this.day = day; }

    public String getTime() {
        return time;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }

    public String getClassType() {
        return classType;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("day", day);
        result.put("time", time);
        result.put("capacity", capacity);
        result.put("duration", duration);
        result.put("price", price);
        result.put("classType", classType);
        result.put("description", description);
        return result;
    }
}
