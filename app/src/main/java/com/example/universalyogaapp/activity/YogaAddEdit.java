package com.example.universalyogaapp.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.database.YogaDatabase;
import com.example.universalyogaapp.model.YogaClass;
import com.example.universalyogaapp.adapter.InstanceAdapter;
import com.example.universalyogaapp.model.YogaClassInstance;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class YogaAddEdit extends AppCompatActivity implements InstanceAdapter.OnInstanceClickListener, ActionMode.Callback {
    private final String DATABASE_URL = "https://comp1786-8d6c2-default-rtdb.firebaseio.com/";
    private RecyclerView rvInstances;
    private InstanceAdapter instanceAdapter;
    private YogaDatabase db;
    private Button btnSave, btnAddInstance;
    private EditText etTime, etCapacity, etDuration, etPrice, etDescription;
    private Spinner spinnerDay;
    private RadioButton cbFlowYoga, cbAerialYoga, cbFamilyYoga;
    private int classId = -1;  // Default, used to identify if we're editing
    private ActionMode actionMode;  // Action mode for selection
    private boolean isSelectionMode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yoga_add_edit);

        // Initialize UI components
        spinnerDay = findViewById(R.id.spinnerDay);
        etTime = findViewById(R.id.etTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        cbFlowYoga = findViewById(R.id.cbFlowYoga);
        cbAerialYoga = findViewById(R.id.cbAerialYoga);
        cbFamilyYoga = findViewById(R.id.cbFamilyYoga);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        rvInstances = findViewById(R.id.rvInstances);
        btnAddInstance = findViewById(R.id.btnAddInstance);

        // Set RecyclerView layout manager
        rvInstances.setLayoutManager(new LinearLayoutManager(this));

        // Access the database
        db = YogaDatabase.getInstance(this);

        // Load class and instances if editing
        Intent intent = getIntent();
        if (intent.hasExtra("class_id")) {
            classId = intent.getIntExtra("class_id", -1);
            loadClassDetails(classId);  // Load details if editing
            loadClassInstances(classId);  // Load instances for the class
        } else {
            btnAddInstance.setVisibility(View.GONE);  // Hide add instance button for new classes
        }

        // Set listeners
        etTime.setOnClickListener(v -> showTimePickerDialog());
        btnAddInstance.setOnClickListener(v -> showAddInstanceDialog(null));
        btnSave.setOnClickListener(v -> saveYogaClass());
    }

    private void loadClassDetails(int classId) {
        YogaClass yogaClass = db.yogaClassDao().getClassById(classId);
        if (yogaClass != null) {
            // Set spinner to the correct day
            spinnerDay.setSelection(getIndexForDay(yogaClass.getDay()));
            etTime.setText(yogaClass.getTime());
            etCapacity.setText(String.valueOf(yogaClass.getCapacity()));
            etDuration.setText(String.valueOf(yogaClass.getDuration()));
            etPrice.setText(String.valueOf(yogaClass.getPrice()));

            // Check the appropriate class types
            String[] classTypes = yogaClass.getClassType().split(", ");
            for (String classType : classTypes) {
                switch (classType) {
                    case "Flow Yoga":
                        cbFlowYoga.setChecked(true);
                        break;
                    case "Aerial Yoga":
                        cbAerialYoga.setChecked(true);
                        break;
                    case "Family Yoga":
                        cbFamilyYoga.setChecked(true);
                        break;
                }
            }

            etDescription.setText(yogaClass.getDescription());
        }
    }

    private void loadClassInstances(int classId) {
        List<YogaClassInstance> instances = db.yogaClassInstanceDao().getInstancesByClassId(classId);
        instanceAdapter = new InstanceAdapter(this, instances, this); // Pass this as listener
        rvInstances.setAdapter(instanceAdapter);
    }

    @Override
    public void onInstanceClick(YogaClassInstance instance) {
        if (!isSelectionMode) {
            showAddInstanceDialog(instance);  // Show instance dialog on click
        }
    }

    // Handle long-click action
    @Override
    public void onInstanceLongClick(YogaClassInstance instance) {
        if (actionMode == null) {
            actionMode = startActionMode(this);  // Start action mode
        }
        instanceAdapter.toggleSelection(instance);  // Toggle selection state for this instance
        instanceAdapter.notifyDataSetChanged();  // Refresh the adapter
    }

    private void saveYogaClass() {
        if (validateInputs()) {
            // Collect selected class types
            ArrayList<String> selectedClassTypes = new ArrayList<>();
            if (cbFlowYoga.isChecked()) selectedClassTypes.add("Flow Yoga");
            if (cbAerialYoga.isChecked()) selectedClassTypes.add("Aerial Yoga");
            if (cbFamilyYoga.isChecked()) selectedClassTypes.add("Family Yoga");

            String classTypes = String.join(", ", selectedClassTypes);  // Join class types as string

            YogaClass yogaClass = new YogaClass(
                    spinnerDay.getSelectedItem().toString(),  // Get selected day
                    etTime.getText().toString(),
                    Integer.parseInt(etCapacity.getText().toString()),
                    Integer.parseInt(etDuration.getText().toString()),
                    Double.parseDouble(etPrice.getText().toString()),
                    classTypes,
                    etDescription.getText().toString()
            );

            if (classId == -1) {
                db.yogaClassDao().insert(yogaClass);  // Add new class
            } else {
                yogaClass.setId(classId);
                db.yogaClassDao().update(yogaClass);  // Update existing class
            }

            syncToFirebase();

            Toast.makeText(this, "Yoga class saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int getIndexForDay(String day) {
        String[] days = getResources().getStringArray(R.array.days_of_week);
        for (int i = 0; i < days.length; i++) {
            if (days[i].equalsIgnoreCase(day)) return i;
        }
        return 0;  // Default to first item
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int hourOfDay, int selectedMinute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, selectedMinute);
                    etTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showAddInstanceDialog(YogaClassInstance instance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(instance == null ? "Add New Instance" : "Edit Instance");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_instance, null);
        builder.setView(dialogView);

        EditText etInstanceDate = dialogView.findViewById(R.id.etInstanceDate);
        EditText etTeacher = dialogView.findViewById(R.id.etTeacher);
        EditText etComments = dialogView.findViewById(R.id.etComments);

        if (instance != null) {
            etInstanceDate.setText(instance.getDate());
            etTeacher.setText(instance.getTeacher());
            etComments.setText(instance.getComments());
        }

        etInstanceDate.setOnClickListener(v -> showDatePickerDialog(etInstanceDate));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String date = etInstanceDate.getText().toString();
            String teacher = etTeacher.getText().toString();
            String comments = etComments.getText().toString();

            if (!date.isEmpty() && !teacher.isEmpty()) {
                if (instance == null) {
                    YogaClassInstance newInstance = new YogaClassInstance(0, classId, date, teacher, comments);
                    db.yogaClassInstanceDao().insert(newInstance);
                } else {
                    instance.setDate(date);
                    instance.setTeacher(teacher);
                    instance.setComments(comments);
                    db.yogaClassInstanceDao().update(instance);
                }
                syncToFirebase();

                loadClassInstances(classId); // Refresh list
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private boolean validateInputs() {
        if (etTime.getText().toString().isEmpty() || etCapacity.getText().toString().isEmpty() ||
                etDuration.getText().toString().isEmpty() || etPrice.getText().toString().isEmpty()) {
            showValidationDialog("Please fill in all fields.");
            return false;
        }
        return true;
    }

    private void showValidationDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Validation Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        final int classDayOfWeek = getClassDayOfWeek();  // Get the class's day of the week (1=Monday, 7=Sunday)

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
            // Create Calendar object for the selected date
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);

            // Adjust the selected date if it's not the correct day of the week
            int selectedDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
            if (selectedDayOfWeek != classDayOfWeek) {
                // Calculate the difference in days (adjust to the correct class day)
                int diff = classDayOfWeek - selectedDayOfWeek;
                if (diff < 0) {
                    diff += 7;  // If the target day is earlier in the week, adjust by adding 7
                }
                selectedDate.add(Calendar.DAY_OF_YEAR, diff);

                // Update the EditText with the adjusted date
                String adjustedDate = String.format("%04d-%02d-%02d", selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
                editText.setText(adjustedDate);

                // Optionally show a Toast to inform the user that the date was automatically adjusted
                Toast.makeText(this, "Date adjusted to the correct class weekday: " +
                        getResources().getStringArray(R.array.days_of_week)[classDayOfWeek - 2], Toast.LENGTH_SHORT).show();
            } else {
                // If it matches, update the text field with the selected date
                String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                editText.setText(date);
            }
        }, year, month, day);

        // Optional: Limit the date range if needed, for example, disabling past dates:
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());  // Disable past dates

        datePickerDialog.show();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_all:
                instanceAdapter.selectAll();
                return true;
            case R.id.menu_delete_selected:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void deleteSelectedInstances() {
        List<YogaClassInstance> selectedClasses = instanceAdapter.getSelectedInstances();
        for (YogaClassInstance yogaClassInstance : selectedClasses) {
            db.yogaClassInstanceDao().delete(yogaClassInstance);
        }
        instanceAdapter.updateData(db.yogaClassInstanceDao().getInstancesByClassId(classId));
        syncToFirebase();
    }


    private void selectAllClasses() {
        instanceAdapter.selectAll();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensure instanceAdapter is initialized before updating data
        if (instanceAdapter == null) {
            loadClassInstances(classId);  // Initialize instanceAdapter if not already done
        } else {
            instanceAdapter.updateData(db.yogaClassInstanceDao().getInstancesByClassId(classId));  // Update data
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d("YogaAddEdit", "Action mode started");
        // Inflate the menu that should appear during action mode
        getMenuInflater().inflate(R.menu.selection_menu, menu); // Your menu resource (e.g., selection_menu.xml)
        isSelectionMode = true;
        return true;
    }

    public void startSelectionMode() {
        if (actionMode != null) return;  // Prevent starting another action mode

        actionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_instance_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.menu_delete_selected) {
                    deleteSelectedInstances();
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
                instanceAdapter.clearSelection();
                actionMode = null;
            }
        });
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteSelectedInstances();  // Implement your delete logic
                mode.finish();  // Finish action mode
                return true;

            case R.id.action_select_all:
                selectAllInstances();  // Implement your select all logic
                return true;

            default:
                return false;
        }
    }

    private void selectAllInstances() {
        instanceAdapter.selectAll();  // Select all instances
        instanceAdapter.notifyDataSetChanged();  // Refresh the adapter to reflect the selection
    }


    public void onDestroyActionMode(ActionMode mode) {
        // Clear selection when the action mode is destroyed
        instanceAdapter.clearSelection();
        isSelectionMode = false;
        actionMode = null;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete the selected instances?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteSelectedInstances();
                    actionMode.finish();  // Close the action mode
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private int getClassDayOfWeek() {
        String classDay = spinnerDay.getSelectedItem().toString();  // e.g., "Monday"
        switch (classDay) {
            case "Monday": return Calendar.MONDAY;
            case "Tuesday": return Calendar.TUESDAY;
            case "Wednesday": return Calendar.WEDNESDAY;
            case "Thursday": return Calendar.THURSDAY;
            case "Friday": return Calendar.FRIDAY;
            case "Saturday": return Calendar.SATURDAY;
            case "Sunday": return Calendar.SUNDAY;
            default: return Calendar.MONDAY;  // Default to Monday if not found
        }
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
