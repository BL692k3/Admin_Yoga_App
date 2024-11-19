package com.example.universalyogaapp.activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.adapter.InstanceAdapter;
import com.example.universalyogaapp.adapter.YogaClassAdapter;
import com.example.universalyogaapp.database.YogaDatabase;
import com.example.universalyogaapp.model.YogaClass;
import com.example.universalyogaapp.model.YogaClassInstance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final String DATABASE_URL = "https://comp1786-8d6c2-default-rtdb.firebaseio.com/";
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private YogaClassAdapter adapter;  // Adapter for the RecyclerView
    private YogaDatabase db;  // SQLite database
    private ActionMode actionMode;
    private EditText searchBar;
    private ImageButton btnAdvancedSearch;
    private InstanceAdapter instanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Corresponds to main_activity.xml

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        searchBar = findViewById(R.id.searchBar);
        btnAdvancedSearch = findViewById(R.id.btnAdvancedSearch);

        db = YogaDatabase.getInstance(this);

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new YogaClassAdapter(this, db.yogaClassDao().getAllClasses());
        instanceAdapter = new InstanceAdapter(this, db.yogaClassInstanceDao().getAllInstances());
        recyclerView.setAdapter(adapter);

        // Navigate to YogaAddEdit activity to add a new class
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YogaAddEdit.class);
            startActivity(intent);  // No data passed, as it's for a new class
        });

        // Handle class item clicks (edit)
        adapter.setOnItemClickListener((yogaClass) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(yogaClass);
            } else {
                Intent intent = new Intent(MainActivity.this, YogaAddEdit.class);
                intent.putExtra("class_id", yogaClass.getId());
                startActivity(intent);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClassesByTeacher(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Show advanced search options dialog
        btnAdvancedSearch.setOnClickListener(v -> showAdvancedSearchDialog());
    }

    private void filterClassesByTeacher(String query) {
        // Retrieve all instances that match the teacher's name (case-insensitive search)
        List<YogaClassInstance> filteredInstances = db.yogaClassInstanceDao().searchByTeacherName("%" + query + "%");

        // Get the IDs of all classes that have matching instances
        Set<Integer> filteredClassIds = new HashSet<>();
        for (YogaClassInstance instance : filteredInstances) {
            filteredClassIds.add(instance.getClassId());
        }

        // Retrieve all classes from the database
        List<YogaClass> allClasses = db.yogaClassDao().getAllClasses();

        // Filter out the classes that contain instances with the filtered teacher
        List<YogaClass> filteredClasses = new ArrayList<>();
        for (YogaClass yogaClass : allClasses) {
            if (filteredClassIds.contains(yogaClass.getId())) {
                filteredClasses.add(yogaClass);
            }
        }

        // Update the adapter with the filtered classes
        adapter.updateData(filteredClasses);
    }

    private void showAdvancedSearchDialog() {
        // Create an AlertDialog with options for advanced search
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Advanced Search");

        // Inflate a custom layout for the dialog with options for date and day of the week
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_advanced_search, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        RadioButton radioDate = dialogView.findViewById(R.id.radioDate);
        RadioButton radioDayOfWeek = dialogView.findViewById(R.id.radioDayOfWeek);

        builder.setPositiveButton("Search", (dialog, which) -> {
            if (radioDate.isChecked()) {
                // Show a DatePicker dialog for selecting a date
                showDatePicker();
            } else if (radioDayOfWeek.isChecked()) {
                // Show a day-of-the-week selection dialog
                showDayOfWeekDialog();
            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePicker() {
        // Initialize a Calendar instance for today's date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Set the minimum date for the DatePicker to today (no past dates allowed)
        calendar.set(year, month, day);
        long todayInMillis = calendar.getTimeInMillis();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            filterClassesByDate(selectedDate);
        }, year, month, day);

        // Set the minimum date for the DatePicker
        datePickerDialog.getDatePicker().setMinDate(todayInMillis);

        datePickerDialog.show();
    }


    private void showDayOfWeekDialog() {
        // Show a list of days for user to select
        final String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        AlertDialog.Builder dayDialog = new AlertDialog.Builder(this);
        dayDialog.setTitle("Select Day of the Week");

        dayDialog.setItems(daysOfWeek, (dialog, which) -> {
            filterClassesByDayOfWeek(String.valueOf(which));  // Day of week as a string (0 for Sunday, 6 for Saturday)
        });

        dayDialog.show();
    }

    private void filterClassesByDate(String date) {
        // Retrieve instances that match the selected date
        List<YogaClassInstance> filteredInstances = db.yogaClassInstanceDao().searchByDate(date);

        // Get the IDs of the classes that have instances on the selected date
        Set<Integer> filteredClassIds = new HashSet<>();
        for (YogaClassInstance instance : filteredInstances) {
            filteredClassIds.add(instance.getClassId());
        }

        // Retrieve all yoga classes
        List<YogaClass> allClasses = db.yogaClassDao().getAllClasses();

        // Filter classes based on the filtered instance IDs
        List<YogaClass> filteredClasses = new ArrayList<>();
        for (YogaClass yogaClass : allClasses) {
            if (filteredClassIds.contains(yogaClass.getId())) {
                filteredClasses.add(yogaClass);
            }
        }

        // Update the adapter with the filtered classes
        adapter.updateData(filteredClasses);
    }


    private void filterClassesByDayOfWeek(String dayOfWeek) {
        // Retrieve instances that match the selected day of the week (0 for Sunday, 6 for Saturday)
        List<YogaClassInstance> filteredInstances = db.yogaClassInstanceDao().searchByDayOfWeek(dayOfWeek);

        // Get the IDs of the classes that have instances on the selected day of the week
        Set<Integer> filteredClassIds = new HashSet<>();
        for (YogaClassInstance instance : filteredInstances) {
            filteredClassIds.add(instance.getClassId());
        }

        // Retrieve all yoga classes
        List<YogaClass> allClasses = db.yogaClassDao().getAllClasses();

        // Filter classes based on the filtered instance IDs
        List<YogaClass> filteredClasses = new ArrayList<>();
        for (YogaClass yogaClass : allClasses) {
            if (filteredClassIds.contains(yogaClass.getId())) {
                filteredClasses.add(yogaClass);
            }
        }

        // Update the adapter with the filtered classes
        adapter.updateData(filteredClasses);
    }


    public void startSelectionMode() {
        if (actionMode != null) return;  // Prevent starting another action mode

        actionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.selection_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    deleteSelectedClasses();
                    mode.finish();
                    return true;
                } else if (item.getItemId() == R.id.action_select_all) {
                    selectAllClasses();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;
            }
        });
    }

    private void deleteSelectedClasses() {
        List<YogaClass> selectedClasses = adapter.getSelectedClasses();
        for (YogaClass yogaClass : selectedClasses) {
            db.yogaClassDao().delete(yogaClass);
        }
        adapter.updateData(db.yogaClassDao().getAllClasses());

        // Sync to Firebase after deletion
        syncToFirebase();
    }


    private void selectAllClasses() {
        adapter.selectAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateData(db.yogaClassDao().getAllClasses());
        instanceAdapter.updateData(db.yogaClassInstanceDao().getAllInstances());
    }

    private void syncToFirebase() {
        // Initialize the Firebase Realtime Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
        DatabaseReference classesRef = database.getReference("yoga_classes");

        // Retrieve all yoga classes and instances from the local database
        List<YogaClass> allClasses = db.yogaClassDao().getAllClasses();
        List<YogaClassInstance> allInstances = db.yogaClassInstanceDao().getAllInstances();

        // Clear the entire Firebase database path before re-uploading
        classesRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseSync", "Firebase database cleared successfully.");
            } else {
                Log.e("FirebaseSync", "Failed to clear Firebase database.");
            }

            // Re-upload all yoga classes and their instances to Firebase
            for (YogaClass yogaClass : allClasses) {
                DatabaseReference classRef = classesRef.child(String.valueOf(yogaClass.getId()));
                classRef.setValue(yogaClass.toMap())
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Class " + yogaClass.getId() + " synced successfully."))
                        .addOnFailureListener(e -> Log.e("FirebaseSync", "Failed to sync class " + yogaClass.getId(), e));

                // Upload each instance associated with the current yoga class
                for (YogaClassInstance instance : allInstances) {
                    if (instance.getClassId() == yogaClass.getId()) {
                        DatabaseReference instanceRef = classRef.child("instances").child(String.valueOf(instance.getId()));
                        instanceRef.setValue(instance.toMap())
                                .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Instance " + instance.getId() + " synced successfully."))
                                .addOnFailureListener(e -> Log.e("FirebaseSync", "Failed to sync instance " + instance.getId(), e));
                    }
                }
            }

            Toast.makeText(this, "Data synchronized with Firebase", Toast.LENGTH_SHORT).show();
        });
    }

}
