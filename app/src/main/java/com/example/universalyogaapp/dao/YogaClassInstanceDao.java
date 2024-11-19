package com.example.universalyogaapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.universalyogaapp.model.YogaClassInstance;

import java.util.List;

@Dao
public interface YogaClassInstanceDao {

    @Insert
    void insert(YogaClassInstance instance);

    @Update
    void update(YogaClassInstance instance);

    @Query("SELECT * FROM yoga_class_instances WHERE classId = :classId")
    List<YogaClassInstance> getInstancesByClassId(int classId);

    @Delete
    void delete(YogaClassInstance instance);

    @Query("SELECT * FROM yoga_class_instances")
    List<YogaClassInstance> getAllInstances();

    @Query("SELECT * FROM yoga_class_instances WHERE teacher LIKE :teacherName")
    List<YogaClassInstance> searchByTeacherName(String teacherName);

    @Query("SELECT * FROM yoga_class_instances WHERE date = :date")
    List<YogaClassInstance> searchByDate(String date);

    @Query("SELECT * FROM yoga_class_instances WHERE strftime('%w', date) = :dayOfWeek")
    List<YogaClassInstance> searchByDayOfWeek(String dayOfWeek);
}
