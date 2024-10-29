package com.example.adminyogaapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddCourseFragment extends Fragment {

    private EditText etDayOfWeek, etTimeOfCourse, etCapacity, etDuration, etPrice, etDescription;
    private RadioGroup rgTypeOfClass;
    private Button btnAddCourse;
    private DatabaseManager databaseManager;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_add_course, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DatabaseManager
        databaseManager = new DatabaseManager(requireContext());
        databaseManager.open();

        // Reference input fields
        etDayOfWeek = view.findViewById(R.id.etDayOfWeek);
        etTimeOfCourse = view.findViewById(R.id.etTimeOfCourse);
        etCapacity = view.findViewById(R.id.etCapacity);
        etDuration = view.findViewById(R.id.etDuration);
        etPrice = view.findViewById(R.id.etPrice);
        rgTypeOfClass = view.findViewById(R.id.rgTypeOfClass);
        etDescription = view.findViewById(R.id.etDescription);
        btnAddCourse = view.findViewById(R.id.btnAddCourse);

        // Set click listener for adding a course
        btnAddCourse.setOnClickListener(v -> addCourse());
    }

    // Method to add a course
    private void addCourse() {
        String dayOfWeek = etDayOfWeek.getText().toString().trim();
        String timeOfCourse = etTimeOfCourse.getText().toString().trim();
        String capacityText = etCapacity.getText().toString().trim();
        String durationText = etDuration.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate required fields
        if (dayOfWeek.isEmpty() || timeOfCourse.isEmpty() || capacityText.isEmpty() || durationText.isEmpty() || priceText.isEmpty()) {
            showErrorDialog("Error", "All required fields must be filled.");
            return;
        }

        // Check if a class type has been selected
        int selectedTypeId = rgTypeOfClass.getCheckedRadioButtonId();
        if (selectedTypeId == -1) {
            showErrorDialog("Error", "Please select a type of class.");
            return;
        }

        RadioButton selectedTypeRadioButton = view.findViewById(selectedTypeId);
        String typeOfClass = selectedTypeRadioButton.getText().toString();

        int capacity = Integer.parseInt(capacityText);
        int duration = Integer.parseInt(durationText);
        double price = Double.parseDouble(priceText);

        // Add the course to the database
        long result = databaseManager.addCourse(dayOfWeek, timeOfCourse, capacity, duration, price, typeOfClass, description);

        if (result != -1) {
            Toast.makeText(requireContext(), "Course added successfully!", Toast.LENGTH_SHORT).show();

            // Go back to the Home fragment using the BottomNavigationView
            requireActivity().runOnUiThread(() -> {
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            });

        } else {
            Toast.makeText(requireContext(), "Failed to add class", Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        databaseManager.close();
    }
}
