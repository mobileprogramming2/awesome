package com.awesome.awesome.sql;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.awesome.awesome.Priority;
import com.awesome.awesome.Status;
import com.awesome.awesome.entity.Assignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "awesome_db";
    public SQLiteHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table Assignments (ID integer primary key autoincrement, Name char(15), EndDate DateTime, Status Integer, Priority Integer, Subject char(15));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table Assignments;");
        onCreate(sqLiteDatabase);
    }

    public void insertNewAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = assignment.getEndDateTime().format(formatter);
        int priority = Priority.PriorityToInt(assignment.getPriority());

        db.execSQL("insert into Assignments(Name, EndDate, Status, Priority, Subject) values(?, ?, ?, ?, ?)",
                new Object[]{assignment.getName(), formattedDateTime, Status.WAITING, priority, assignment.getSubject()});

        db.close();
    }

    public ArrayList<Assignment> getIncompleteAssignments() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Assignments where Status != ?;",
                new String[]{String.format("%d", Status.StatusToInt(Status.COMPLETE))});
        ArrayList<Assignment> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            String dateTimeString = cursor.getString(2); // SQLite DATETIME 값
            LocalDateTime endDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            /// 0 : id, 1 : name, 2 : end date, 3: status, 4 : priority, 5: subject
            Assignment assignment = new Assignment(cursor.getInt(0), cursor.getString(1), endDateTime, Status.intToStatus(cursor.getInt(3)),
                    Priority.intToPriority(cursor.getInt(4)), cursor.getString(5));
            result.add(assignment);
        }

        cursor.close();
        db.close();

        return result;
    }

    public ArrayList<Assignment> getCompleteAssignments() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Assignments where Status = ?;",
                new String[]{String.format("%d", Status.StatusToInt(Status.COMPLETE))});
        ArrayList<Assignment> result = new ArrayList<>();


        while (cursor.moveToNext()) {
            String dateTimeString = cursor.getString(2); // SQLite DATETIME 값
            LocalDateTime endDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            // 0 : id, 1 : name, 2 : end date, 3: status
            Assignment assignment = new Assignment(cursor.getInt(0), cursor.getString(1), endDateTime,
                    Status.intToStatus(cursor.getInt(3)), Priority.intToPriority(cursor.getInt(4)), cursor.getString(5));
            result.add(assignment);
        }

        cursor.close();
        db.close();

        return result;
    }

    public Assignment getAssignment(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Assignments where ID = ?;", new String[]{Integer.toString(id)});
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        cursor.moveToNext();

        // 0 : id, 1 : name, 2 : end date, 3: status
        Assignment assignment = new Assignment(cursor.getInt(0), cursor.getString(1), LocalDateTime.parse(cursor.getString(2), formatter),
                Status.intToStatus(cursor.getInt(3)), Priority.intToPriority(cursor.getInt(4)), cursor.getString(5));
        Assignment result = assignment;

        cursor.close();
        db.close();

        return result;
    }

    public void modifyAssignments(int id, Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDateTime = assignment.getEndDateTime().format(formatter);

            db.execSQL("update Assignments set Name = ?, EndDate = ?, Status = ?, Priority = ?, Subject = ? where id = ?;",
                    new Object[]{assignment.getName(), formattedDateTime, Status.StatusToInt(assignment.getStatus()), Priority.PriorityToInt(assignment.getPriority()), assignment.getSubject(), id});
        }
        catch (Exception e) {

        }

        db.close();
    }

    public void updateAssignmentStatus(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();  // 데이터베이스 객체 가져오기

        ContentValues values = new ContentValues();
        values.put("status", Status.StatusToInt(assignment.getStatus()));  // 상태를 정수로 저장

        // ID를 기준으로 데이터를 업데이트
        db.update("Assignments", values, "ID = ?", new String[]{String.valueOf(assignment.getID())});

        db.close();  // 데이터베이스 연결 종료
    }

}