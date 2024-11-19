package com.example.universalyogaapp.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.universalyogaapp.model.YogaClass;

import java.util.List;

@Dao
public interface YogaClassDao {

    @Insert
    void insert(YogaClass yogaClass);

    @Update
    void update(YogaClass yogaClass);

    @Delete
    void delete(YogaClass yogaClass);

    @Query("SELECT * FROM yoga_classes")
    List<YogaClass> getAllClasses();

    @Query("SELECT * FROM yoga_classes WHERE id = :classId LIMIT 1")
    YogaClass getClassById(int classId);
}

