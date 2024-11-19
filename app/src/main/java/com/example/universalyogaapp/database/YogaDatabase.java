package com.example.universalyogaapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.universalyogaapp.dao.YogaClassDao;
import com.example.universalyogaapp.dao.YogaClassInstanceDao;
import com.example.universalyogaapp.model.YogaClass;
import com.example.universalyogaapp.model.YogaClassInstance;

// Define the Room database class
@Database(entities = {YogaClass.class, YogaClassInstance.class}, version = 5)  // Increment version to 2
public abstract class YogaDatabase extends RoomDatabase {

    private static YogaDatabase instance;

    // Singleton instance to prevent multiple database instances
    public static synchronized YogaDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            YogaDatabase.class, "yoga_class_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract YogaClassDao yogaClassDao();

    public abstract YogaClassInstanceDao yogaClassInstanceDao();
}
