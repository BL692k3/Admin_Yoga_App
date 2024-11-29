package com.example.universalyogaapp.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity(
        indices = {@Index("classId")},
        tableName = "yoga_class_instances",
        foreignKeys = @ForeignKey(
                entity = YogaClass.class,
                parentColumns = "id",
                childColumns = "classId",
                onDelete = ForeignKey.CASCADE
        )
)
public class YogaClassInstance {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int classId;  // FK: YogaClass id
    private String date;
    private String teacher;
    private String comments;

    // Constructor
    public YogaClassInstance(int id, int classId, String date, String teacher, String comments) {
        this.id = id;
        this.classId = classId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassId() {
        return classId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();
        result.put("id", String.valueOf(id));  // Convert int to String
        result.put("classId", String.valueOf(classId));  // Convert int to String
        result.put("date", date);  // date is already a String
        result.put("teacher", teacher);  // teacher is already a String
        result.put("comments", comments);  // comments is already a String
        return result;
    }
}
