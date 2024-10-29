package com.example.adminyogaapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class ClassListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClassAdapter classAdapter;
    private DatabaseManager databaseManager;
    private Cursor cursor;
    private long courseId;
    private SearchView searchViewClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_class_list, container, false);

        // Get courseId from Bundle
        if (getArguments() != null) {
            courseId = getArguments().getLong("COURSE_ID", -1);
        }

        if (courseId == -1) {
            Toast.makeText(getContext(), "Invalid course ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize DatabaseManager and RecyclerView
        databaseManager = new DatabaseManager(getContext());
        databaseManager.open();
        recyclerView = view.findViewById(R.id.recyclerViewClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up the Add Class button
        Button btnAddClass = view.findViewById(R.id.btnAddClass);
        btnAddClass.setOnClickListener(v -> showAddDialog());
        // Set up the SearchView
        searchViewClass = view.findViewById(R.id.searchViewClass);
        searchViewClass.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterClasses(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterClasses(newText);
                return false;
            }
        });
        loadClasses();

        return view;
    }
    private void filterClasses(String query) {
        Cursor filteredCursor = databaseManager.searchClassesByTeacher(courseId, query);
        if (classAdapter != null) {
            classAdapter.updateCursor(filteredCursor);
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Class");

        // Create dialog interface from layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_class, null);
        builder.setView(dialogView);

        // Declare EditTexts in dialog
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etTeacher = dialogView.findViewById(R.id.etTeacher);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})EditText etDate = dialogView.findViewById(R.id.etDate);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})EditText etComment = dialogView.findViewById(R.id.etComment);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);

        // Event date selection
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        etDate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Set up the "Save" button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String teacher = etTeacher.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String comment = etComment.getText().toString().trim();

            if (teacher.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Teacher and date are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add class to database
            databaseManager.addClass(teacher, date, comment, courseId);
            loadClasses();
            Toast.makeText(getContext(), "Class added successfully", Toast.LENGTH_SHORT).show();
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void loadClasses() {
        cursor = databaseManager.getClassesByCourseId(courseId);
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No classes available for this course", Toast.LENGTH_SHORT).show();
        } else {
            classAdapter = new ClassAdapter(cursor);
            recyclerView.setAdapter(classAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        databaseManager.close();
    }

    private class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

        private Cursor cursor;

        public ClassAdapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @NonNull
        @Override
        public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
            return new ClassViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                long classId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENT));

                holder.tvTeacher.setText("Teacher: " + teacher);
                holder.tvDate.setText("Date: " + date);
                holder.tvComment.setText("Comment: " + comment);

                // Edit and delete button events
                holder.btnEdit.setOnClickListener(v -> showEditDialog(classId, teacher, date, comment));
                holder.btnDelete.setOnClickListener(v -> {
                    databaseManager.deleteClass(classId);
                    loadClasses(); // Refresh the list after deletion
                    Toast.makeText(getContext(), "Class deleted", Toast.LENGTH_SHORT).show();
                });
            }
        }
        public void updateCursor(Cursor newCursor) {
            if (cursor != null) {
                cursor.close();
            }
            cursor = newCursor;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return (cursor == null) ? 0 : cursor.getCount();
        }

        public class ClassViewHolder extends RecyclerView.ViewHolder {
            TextView tvTeacher, tvDate, tvComment;
            Button btnEdit, btnDelete;

            public ClassViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTeacher = itemView.findViewById(R.id.tvTeacher);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvComment = itemView.findViewById(R.id.tvComment);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
        private void showEditDialog(long classId, String teacher, String date, String comment) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext() to get context

            builder.setTitle("Edit Class");

            // Create dialog interface from layout
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_class, null);
            builder.setView(dialogView);

            // Declare EditTexts in dialog
            EditText etTeacher = dialogView.findViewById(R.id.etTeacher);
            EditText etDate = dialogView.findViewById(R.id.etDate);
            EditText etComment = dialogView.findViewById(R.id.etComment);

            // Set current values to fields
            etTeacher.setText(teacher);
            etDate.setText(date);
            etComment.setText(comment);


            builder.setPositiveButton("Save", (dialog, which) -> {
                String newTeacher = etTeacher.getText().toString();
                String newDate = etDate.getText().toString();
                String newComment = etComment.getText().toString();

                // Update class information in database
                databaseManager.updateClass(classId, newTeacher, newDate, newComment, courseId);
                loadClasses();
                Toast.makeText(getContext(), "Class updated", Toast.LENGTH_SHORT).show();
            });


            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        }
    }
}
