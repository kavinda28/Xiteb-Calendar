package com.news.calendarapp.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHlper extends SQLiteOpenHelper {

    private final static String CREATE_EVENTS_TABLE = "create table " + DbStructure.EVENT_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbStructure.EVENT + " TEXT, " + DbStructure.NOTE + " TEXT, " + DbStructure.TIME + " TEXT, " + DbStructure.DATE + " TEXT, " + DbStructure.MONTH + " TEXT, "
            + DbStructure.YEAR + " TEXT,"+ DbStructure.Notify +" TEXT)";

    private static final String DROP_EVENTS_TABLE = "DROP TABLE IF EXISTS " + DbStructure.EVENT_TABLE_NAME;


    public DBOpenHlper(@Nullable Context context) {
        super(context, DbStructure.DB_NAME, null, DbStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_EVENTS_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void SaveEvent(String event, String note,String time, String date, String month, String year,String notify, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbStructure.EVENT, event);
        contentValues.put(DbStructure.NOTE, note);

        System.out.println("::::::::::::db"+time);
        contentValues.put(DbStructure.TIME, time);
        contentValues.put(DbStructure.DATE, date);
        contentValues.put(DbStructure.MONTH, month);
        contentValues.put(DbStructure.YEAR, year);
        contentValues.put(DbStructure.Notify, notify);

        database.insert(DbStructure.EVENT_TABLE_NAME,null,contentValues);
    }

    public Cursor ReadEvents(String date,SQLiteDatabase database){
        String [] Projections = {DbStructure.EVENT,DbStructure.TIME,DbStructure.DATE,DbStructure.MONTH,DbStructure.YEAR,DbStructure.NOTE};
        String Selection = DbStructure.DATE +"=?";
        String [] SelectionArgs = {date};

        return  database.query(DbStructure.EVENT_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);

    }
    public Cursor ReadIDEvents(String date,String event,String time,SQLiteDatabase database){
        String [] Projections = {DbStructure.ID,DbStructure.Notify};
        String Selection = DbStructure.DATE +"=? and " +DbStructure.EVENT+"=? and " +DbStructure.TIME+"=?";
        String [] SelectionArgs = {date,event,time};

        return  database.query(DbStructure.EVENT_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);

    }

    public Cursor ReadEventsPerMonth(String month,String year,SQLiteDatabase database){
        String [] Projections = {DbStructure.EVENT,DbStructure.TIME,DbStructure.DATE,DbStructure.MONTH,DbStructure.YEAR,DbStructure.NOTE};
        String Selection = DbStructure.MONTH +"=? and "+DbStructure.YEAR+"=?";
        String [] SelectionArgs = {month,year};

        return  database.query(DbStructure.EVENT_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);

    }

    public  void deleteEvent(String event,String date,String time,SQLiteDatabase database){
        String selection = DbStructure.EVENT+"=? and "+DbStructure.DATE+"=? and "+DbStructure.TIME+"=?";
        String [] selectionArg = {event,date,time};
        database.delete(DbStructure.EVENT_TABLE_NAME,selection,selectionArg);
    }

    public void updateEvent(String date,String event,String time,String notify ,SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbStructure.Notify,notify);
        String Selection = DbStructure.DATE +"=? and " +DbStructure.EVENT+"=? and " +DbStructure.TIME+"=?";
        String [] SelectionArgs = {date,event,time};
        database.update(DbStructure.EVENT_TABLE_NAME,contentValues,Selection,SelectionArgs);
    }
}
