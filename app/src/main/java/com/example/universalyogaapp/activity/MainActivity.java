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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.adapter.InstanceAdapter;
import com.example.universalyogaapp.adapter.YogaClassAdapter;
import com.example.universalyogaapp.database.YogaDatabase;
import com.example.universalyogaapp.model.YogaClass;
import com.example.universalyogaapp.model.YogaClassInstance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final String DATABASE_URL = "https://comp1786-8d6c2-default-rtdb.firebaseio.com/";
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private YogaClassAdapter adapter;
    private YogaDatabase db;
    private ActionMode actionMode;
    private EditText searchBar;
    private ImageButton btnAdvancedSearch;
    private InstanceAdapter instanceAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Pagination variables
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 0;
    private List<YogaClass> allClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        searchBar = findViewById(R.id.searchBar);
        btnAdvancedSearch = findViewById(R.id.btnAdvancedSearch);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        db = YogaDatabase.getInstance(this);

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new YogaClassAdapter(this, db.yogaClassDao().getAllClasses());
        instanceAdapter = new InstanceAdapter(this, db.yogaClassInstanceDao().getAllInstances());
        recyclerView.setAdapter(adapter);

        // Navigate to YogaAddEditActivity activity to add a new class
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YogaAddEditActivity.class);
            startActivity(intent);
        });

        // Handle class item clicks (edit)
        adapter.setOnItemClickListener((yogaClass) -> {
            if (adapter.isSelectionMode()) {
                adapter.toggleSelection(yogaClass);
            } else {
                Intent intent = new Intent(MainActivity.this, YogaAddEditActivity.class);
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

        // Set up swipe refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        });

        // Set up pagination buttons
        findViewById(R.id.btnPreviousPage).setOnClickListener(v -> previousPage());
        findViewById(R.id.btnNextPage).setOnClickListener(v -> nextPage());
    }

    private void updatePageData() {
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allClasses.size());
        List<YogaClass> paginatedClasses = allClasses.subList(start, end);
        adapter.updateData(paginatedClasses);

        // Enable/disable pagination buttons
        findViewById(R.id.btnPreviousPage).setEnabled(currentPage > 0);
        findViewById(R.id.btnNextPage).setEnabled(end < allClasses.size());
    }

    private void nextPage() {
        if ((currentPage + 1) * ITEMS_PER_PAGE < allClasses.size()) {
            currentPage++;
            updatePageData();
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePageData();
        }
    }


    private void refreshData() {
        allClasses = db.yogaClassDao().getAllClasses();
        currentPage = 0; // Reset to the first page
        updatePageData();
        Toast.makeText(this, "Data refreshed", Toast.LENGTH_SHORT).show();
    }



    private void filterClassesByTeacher(String query) {
        // Retrieve all instances that match the teacher's name (case-insensitive)
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
        FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
        DatabaseReference classesRef = database.getReference("yoga_classes");

        for (YogaClass yogaClass : selectedClasses) {
            // Remove from local database
            db.yogaClassDao().delete(yogaClass);

            // Remove from Firebase
            classesRef.child(String.valueOf(yogaClass.getId()))
                    .removeValue()
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Class " + yogaClass.getId() + " deleted from Firebase."))
                    .addOnFailureListener(e -> Log.e("FirebaseSync", "Failed to delete class " + yogaClass.getId() + " from Firebase.", e));
        }

        // Update the adapter with the remaining classes
        adapter.updateData(db.yogaClassDao().getAllClasses());
    }



    private void selectAllClasses() {
        adapter.selectAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateData(db.yogaClassDao().getAllClasses());
        instanceAdapter.updateData(db.yogaClassInstanceDao().getAllInstances());
        refreshData();
    }
}
