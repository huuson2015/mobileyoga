package com.example.adminyogaapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class CourseListFragment extends Fragment {
    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private Cursor cursor;
    private static final int REQUEST_CODE_MODIFY_COURSE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_course_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Initialize database and RecyclerView
        databaseHelper = new DatabaseHelper(getContext());
        database = databaseHelper.getWritableDatabase();


        loadCourses();

        return view;
    }

    private void loadCourses() {
        cursor = getAllCourses();
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No courses available", Toast.LENGTH_SHORT).show();
        } else {
            courseAdapter = new CourseAdapter(getContext(), cursor, getChildFragmentManager()); // Truyền FragmentManager
            recyclerView.setAdapter(courseAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        databaseHelper.close();
    }

    private Cursor getAllCourses() {
        return database.query(DatabaseHelper.TABLE_COURSE, null, null, null, null, null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MODIFY_COURSE && resultCode == getActivity().RESULT_OK) {
            refreshCourseList();
        }
    }

    private void refreshCourseList() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        cursor = getAllCourses();

        if (cursor != null && cursor.getCount() > 0) {
            courseAdapter.swapCursor(cursor);
        } else {
            Toast.makeText(getContext(), "No classes available", Toast.LENGTH_SHORT).show();
        }
    }

    private class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

        private Context context;
        private Cursor cursor;
        private FragmentManager fragmentManager; // Thêm FragmentManager

        public CourseAdapter(Context context, Cursor cursor, FragmentManager fragmentManager) {
            this.context = context;
            this.cursor = cursor;
            this.fragmentManager = fragmentManager; // Khởi tạo FragmentManager

        }

        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
            return new CourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                holder.tvDayOfWeek.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY_OF_THE_WEEK)));
                holder.tvTimeOfCourse.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME_OF_COURSE)));
                holder.tvTypeOfClass.setText("Type: " + cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE_OF_CLASS)));
                holder.tvDescription.setText("Description: " + cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));

                long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));

                // When starting the ModifyCourseFragment
                holder.itemView.setOnClickListener(v -> {
                    ModifyCourseFragment modifyFragment = new ModifyCourseFragment();
                    Bundle args = new Bundle();
                    args.putLong("COURSE_ID", courseId);
                    modifyFragment.setArguments(args);

                    // Use getActivity() or requireActivity() to get the FragmentManager
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, modifyFragment); // Đảm bảo ID đúng
                    transaction.addToBackStack(null); // Thêm vào back stack
                    transaction.commit();
                });


                // Set up the "More" button click listener
                holder.btnMore.setOnClickListener(v -> {
                    ClassListFragment classListFragment = new ClassListFragment();
                    Bundle args = new Bundle();
                    args.putLong("COURSE_ID", courseId);
                    classListFragment.setArguments(args);

                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, classListFragment); // Đảm bảo ID đúng
                    transaction.addToBackStack(null); // Thêm vào back stack
                    transaction.commit();
                });

            }
        }

        @Override
        public int getItemCount() {
            return (cursor == null) ? 0 : cursor.getCount();
        }

        public void swapCursor(Cursor newCursor) {
            if (cursor != null) {
                cursor.close();
            }
            cursor = newCursor;
            notifyDataSetChanged();
        }

        public class CourseViewHolder extends RecyclerView.ViewHolder {
            TextView tvDayOfWeek, tvTimeOfCourse, tvTypeOfClass, tvDescription;
            Button btnMore;

            public CourseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
                tvTimeOfCourse = itemView.findViewById(R.id.tvTimeOfCourse);
                tvTypeOfClass = itemView.findViewById(R.id.tvTypeOfClass);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                btnMore = itemView.findViewById(R.id.btnMore);
            }
        }
    }
}
