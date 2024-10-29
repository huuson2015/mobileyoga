package com.example.adminyogaapp;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ModifyCourseFragment extends Fragment {

    private DatabaseManager databaseManager;
    private long courseId;
    private EditText etDayOfWeek, etTimeOfCourse, etCapacity, etDuration, etPrice, etDescription;
    private RadioGroup rgTypeOfClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_modify_course, container, false);

        // Initialize views
        etDayOfWeek = view.findViewById(R.id.etDayOfWeek);
        etTimeOfCourse = view.findViewById(R.id.etTimeOfCourse);
        etCapacity = view.findViewById(R.id.etCapacity);
        etDuration = view.findViewById(R.id.etDuration);
        etPrice = view.findViewById(R.id.etPrice);
        rgTypeOfClass = view.findViewById(R.id.rgTypeOfClass);
        etDescription = view.findViewById(R.id.etDescription);
        Button btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        Button btnDeleteCourse = view.findViewById(R.id.btnDeleteCourse);

        // Retrieve the course ID from arguments
        if (getArguments() != null) {
            courseId = getArguments().getLong("COURSE_ID", -1);
        }

        if (courseId == -1) {
            Toast.makeText(getActivity(), "Invalid course ID", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return view;
        }

        // Fetch the course details
        databaseManager = new DatabaseManager(getActivity());
        databaseManager.open();
        Cursor cursor = databaseManager.getCourseById(courseId);

        if (cursor != null && cursor.moveToFirst()) {
            // Populate fields with course details
            populateFields(cursor);
        } else {
            Log.d("DatabaseInfo", "No data found for course ID: " + courseId);
        }

        if (cursor != null) {
            cursor.close();
        }

        // Set up save button click listener
        btnSaveChanges.setOnClickListener(v -> updateCourse());

        // Set up delete button click listener
        btnDeleteCourse.setOnClickListener(v -> confirmDeletion());

        return view;
    }

    private void populateFields(Cursor cursor) {
        String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY_OF_THE_WEEK));
        String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME_OF_COURSE));
        int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAPACITY));
        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE_PER_CLASS));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE_OF_CLASS));

        // Populate EditTexts
        etDayOfWeek.setText(dayOfWeek);
        etTimeOfCourse.setText(timeOfCourse);
        etCapacity.setText(String.valueOf(capacity));
        etDuration.setText(String.valueOf(duration));
        etPrice.setText(String.valueOf(price));
        etDescription.setText(description);

        // Set RadioGroup based on type
        if (type.equals("Flow Yoga")) {
            rgTypeOfClass.check(R.id.rbFlowYoga);
        } else if (type.equals("Aerial Yoga")) {
            rgTypeOfClass.check(R.id.rbAerialYoga);
        } else if (type.equals("Family Yoga")) {
            rgTypeOfClass.check(R.id.rbFamilyYoga);
        }
    }

    private void updateCourse() {
        String dayOfWeek = etDayOfWeek.getText().toString();
        String timeOfCourse = etTimeOfCourse.getText().toString();
        int capacity = Integer.parseInt(etCapacity.getText().toString());
        int duration = Integer.parseInt(etDuration.getText().toString());
        double price = Double.parseDouble(etPrice.getText().toString());
        String description = etDescription.getText().toString();

        int selectedTypeId = rgTypeOfClass.getCheckedRadioButtonId();
        String type = "";
        if (selectedTypeId == R.id.rbFlowYoga) {
            type = "Flow Yoga";
        } else if (selectedTypeId == R.id.rbAerialYoga) {
            type = "Aerial Yoga";
        } else if (selectedTypeId == R.id.rbFamilyYoga) {
            type = "Family Yoga";
        }

        // Update the course
        int rowsUpdated = databaseManager.updateCourse(courseId, dayOfWeek, timeOfCourse, capacity, duration, price, type, description);
        if (rowsUpdated > 0) {
            Toast.makeText(getActivity(), "Course updated successfully", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed(); // Return to the previous fragment
        } else {
            Toast.makeText(getActivity(), "Failed to update course", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCourse())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCourse() {
        databaseManager.deleteCourse(courseId);
        Toast.makeText(getActivity(), "Course deleted successfully", Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed(); // Return to the previous fragment
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseManager.close();
    }
}
