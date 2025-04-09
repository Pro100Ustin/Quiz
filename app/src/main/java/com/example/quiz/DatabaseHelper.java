package com.example.quiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "QuizDB";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Таблица результатов
    private static final String TABLE_RESULTS = "results";
    private static final String COLUMN_RESULT_ID = "id";
    private static final String COLUMN_USER_ID_FK = "user_id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Создание таблицы пользователей
            String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT UNIQUE,"
                    + COLUMN_PASSWORD + " TEXT"
                    + ")";
            db.execSQL(createUsersTable);

            // Создание таблицы результатов
            String createResultsTable = "CREATE TABLE " + TABLE_RESULTS + "("
                    + COLUMN_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USER_ID_FK + " INTEGER,"
                    + COLUMN_SCORE + " INTEGER,"
                    + COLUMN_DIFFICULTY + " TEXT,"
                    + COLUMN_DATE + " TEXT,"
                    + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                    + ")";
            db.execSQL(createResultsTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        try {
            db.insertOrThrow(TABLE_USERS, null, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getUserId(String username) {
        if (username == null || username.isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    public void saveQuizResult(int userId, int score, String difficulty) {
        if (userId < 0 || difficulty == null || difficulty.isEmpty()) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_DATE, java.time.LocalDateTime.now().toString());
        try {
            db.insert(TABLE_RESULTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<QuizResult> getUserHistory(int userId) {
        List<QuizResult> results = new ArrayList<>();
        if (userId < 0) {
            return results;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
            COLUMN_SCORE,
            COLUMN_DIFFICULTY,
            COLUMN_DATE
        };
        String selection = COLUMN_USER_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = COLUMN_DATE + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_RESULTS, columns, selection, selectionArgs, null, null, orderBy);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int score = cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE));
                    String difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_DIFFICULTY));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    results.add(new QuizResult(score, difficulty, date));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return results;
    }

    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
