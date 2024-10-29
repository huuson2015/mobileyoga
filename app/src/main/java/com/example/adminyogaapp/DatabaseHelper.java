package com.example.adminyogaapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "yoga_new.db"; // Tên cơ sở dữ liệu mới
    public static final int DATABASE_VERSION = 1; // Phiên bản cơ sở dữ liệu

    // Table Course
    public static final String TABLE_COURSE = "Course";
    public static final String COLUMN_COURSE_ID = "id";
    public static final String COLUMN_DAY_OF_THE_WEEK = "dayOfTheWeek";
    public static final String COLUMN_TIME_OF_COURSE = "timeOfCourse";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE_PER_CLASS = "pricePerClass";
    public static final String COLUMN_TYPE_OF_CLASS = "typeOfClass";
    public static final String COLUMN_DESCRIPTION = "description";

    // Table Class
    public static final String TABLE_CLASS = "Class";
    public static final String COLUMN_CLASS_ID = "classId";
    public static final String COLUMN_TEACHER = "teacher";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_COURSE_ID_FK = "courseId";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COURSE_TABLE = "CREATE TABLE " + TABLE_COURSE + " ("
                + COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DAY_OF_THE_WEEK + " TEXT NOT NULL, "
                + COLUMN_TIME_OF_COURSE + " TEXT NOT NULL, "
                + COLUMN_CAPACITY + " INTEGER NOT NULL, "
                + COLUMN_DURATION + " INTEGER NOT NULL, "
                + COLUMN_PRICE_PER_CLASS + " REAL NOT NULL, "
                + COLUMN_TYPE_OF_CLASS + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT NOT NULL)";
        db.execSQL(CREATE_COURSE_TABLE);

        String CREATE_CLASS_TABLE = "CREATE TABLE " + TABLE_CLASS + " ("
                + COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TEACHER + " TEXT NOT NULL, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + COLUMN_COMMENT + " TEXT, "
                + COLUMN_COURSE_ID_FK + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_COURSE_ID_FK + ") REFERENCES "
                + TABLE_COURSE + "(" + COLUMN_COURSE_ID + "))";
        db.execSQL(CREATE_CLASS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete old tables if there is a change in structure
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No downgrade processing in this case
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        onCreate(db);
    }


}
