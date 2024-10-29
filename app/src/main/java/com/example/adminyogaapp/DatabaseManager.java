package com.example.adminyogaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private DatabaseReference databaseReference;

    // Columns for the Course table
    private static final String[] allCourseColumns = {
            DatabaseHelper.COLUMN_COURSE_ID,
            DatabaseHelper.COLUMN_DAY_OF_THE_WEEK,
            DatabaseHelper.COLUMN_TIME_OF_COURSE,
            DatabaseHelper.COLUMN_CAPACITY,
            DatabaseHelper.COLUMN_DURATION,
            DatabaseHelper.COLUMN_PRICE_PER_CLASS,
            DatabaseHelper.COLUMN_TYPE_OF_CLASS,
            DatabaseHelper.COLUMN_DESCRIPTION
    };

    // Columns for the Class table
    private static final String[] allClassColumns = {
            DatabaseHelper.COLUMN_CLASS_ID,
            DatabaseHelper.COLUMN_TEACHER,
            DatabaseHelper.COLUMN_DATE,
            DatabaseHelper.COLUMN_COMMENT,
            DatabaseHelper.COLUMN_COURSE_ID_FK
    };

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Open database connection
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    // Close database connection
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // Add a new course
    public long addCourse(String dayOfWeek, String timeOfCourse, int capacity, int duration, double price, String type, String description) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DAY_OF_THE_WEEK, dayOfWeek);
        values.put(DatabaseHelper.COLUMN_TIME_OF_COURSE, timeOfCourse);
        values.put(DatabaseHelper.COLUMN_CAPACITY, capacity);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_PRICE_PER_CLASS, price);
        values.put(DatabaseHelper.COLUMN_TYPE_OF_CLASS, type);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        long id = database.insert(DatabaseHelper.TABLE_COURSE, null, values);

        // Add Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference courseRef = firebaseDatabase.getReference("courses").child(String.valueOf(id));
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("dayOfTheWeek", dayOfWeek);
        courseData.put("timeOfCourse", timeOfCourse);
        courseData.put("capacity", capacity);
        courseData.put("duration", duration);
        courseData.put("pricePerClass", price);
        courseData.put("typeOfClass", type);
        courseData.put("description", description);

        courseRef.setValue(courseData);

        return id;
    }


    // Get course by ID
    public Cursor getCourseById(long id) {
        return database.query(DatabaseHelper.TABLE_COURSE, allCourseColumns,
                DatabaseHelper.COLUMN_COURSE_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    // Update course information
    public int updateCourse(long id, String dayOfWeek, String timeOfCourse, int capacity, int duration, double price, String type, String description) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DAY_OF_THE_WEEK, dayOfWeek);
        values.put(DatabaseHelper.COLUMN_TIME_OF_COURSE, timeOfCourse);
        values.put(DatabaseHelper.COLUMN_CAPACITY, capacity);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_PRICE_PER_CLASS, price);
        values.put(DatabaseHelper.COLUMN_TYPE_OF_CLASS, type);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);

        int rowsAffected = database.update(DatabaseHelper.TABLE_COURSE, values, DatabaseHelper.COLUMN_COURSE_ID + " = " + id, null);

        if (rowsAffected > 0) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference courseRef = firebaseDatabase.getReference("courses").child(String.valueOf(id));

            Map<String, Object> courseData = new HashMap<>();
            courseData.put("dayOfTheWeek", dayOfWeek);
            courseData.put("timeOfCourse", timeOfCourse);
            courseData.put("capacity", capacity);
            courseData.put("duration", duration);
            courseData.put("pricePerClass", price);
            courseData.put("typeOfClass", type);
            courseData.put("description", description);

            courseRef.setValue(courseData);
        }

        return rowsAffected;
    }

    // Delete course by ID
    public void deleteCourse(long id) {
        database.delete(DatabaseHelper.TABLE_COURSE, DatabaseHelper.COLUMN_COURSE_ID + "=" + id, null);
        // Get all classes related to the course
        Cursor cursor = getClassesByCourseId(id);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference classRef = firebaseDatabase.getReference("classes");

        // Delete all course related classes in SQLite and Firebase
        if (cursor.moveToFirst()) {
            do {
                long classId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID));
                database.delete(DatabaseHelper.TABLE_CLASS, DatabaseHelper.COLUMN_CLASS_ID + "=" + classId, null); // Xóa lớp trong SQLite
                classRef.child(String.valueOf(classId)).removeValue(); // Xóa lớp khỏi Firebase
            } while (cursor.moveToNext());
        }
        cursor.close();
        // Delete course from Firebase
        DatabaseReference courseRef = firebaseDatabase.getReference("courses");
        courseRef.child(String.valueOf(id)).removeValue();
    }

    // Add a new class
    public long addClass(String teacher, String date, String comment, long courseId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TEACHER, teacher);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_COMMENT, comment);
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId);

         long classId = database.insert(DatabaseHelper.TABLE_CLASS, null, values);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference classRef = firebaseDatabase.getReference("classes").child(String.valueOf(classId)); // Sử dụng ID vừa tạo
        Map<String, Object> classData = new HashMap<>();
        classData.put("courseId", courseId);
        classData.put("teacher", teacher);
        classData.put("date", date);
        classData.put("comment", comment);

        classRef.setValue(classData);
        return classId;
    }

    // Get classes by course ID
    public Cursor getClassesByCourseId(long courseId) {
        String selection = DatabaseHelper.COLUMN_COURSE_ID_FK + " = ?";
        String[] selectionArgs = {String.valueOf(courseId)};

        return database.query(
                DatabaseHelper.TABLE_CLASS,
                allClassColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    // Search classes by teacher's name
    public Cursor searchClassesByTeacher(long courseId, String teacherName) {
        String selection = DatabaseHelper.COLUMN_COURSE_ID_FK + " = ? AND " + DatabaseHelper.COLUMN_TEACHER + " LIKE ?";
        String[] selectionArgs = {String.valueOf(courseId), "%" + teacherName + "%"};

        return database.query(
                DatabaseHelper.TABLE_CLASS,
                allClassColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    // Update class information
    public int updateClass(long id, String teacher, String date, String comment, long courseId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TEACHER, teacher);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_COMMENT, comment);
        values.put(DatabaseHelper.COLUMN_COURSE_ID_FK, courseId);

        int rowsAffected = database.update(DatabaseHelper.TABLE_CLASS, values, DatabaseHelper.COLUMN_CLASS_ID + " = " + id, null);

        if (rowsAffected > 0) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference courseRef = firebaseDatabase.getReference("classes").child(String.valueOf(id));

            Map<String, Object> classData = new HashMap<>();
            classData.put("teacher", teacher);
            classData.put("date", date);
            classData.put("comment", comment);
            classData.put("courseId", courseId);

            courseRef.setValue(classData);
        }

        return rowsAffected;
    }

    // Delete class by ID
    public void deleteClass(long id) {
        database.delete(DatabaseHelper.TABLE_CLASS, DatabaseHelper.COLUMN_CLASS_ID + " = ?", new String[]{String.valueOf(id)});
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Delete course from Firebase
        DatabaseReference classRef = firebaseDatabase.getReference("classes");
        classRef.child(String.valueOf(id)).removeValue();

    }
}
