package com.news.calendarapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.news.calendarapp.Adapter.EventRecyclerAdapter;
import com.news.calendarapp.Adapter.Grid_Adapter;
import com.news.calendarapp.DB.DBOpenHlper;
import com.news.calendarapp.DB.DbStructure;
import com.news.calendarapp.Model.Events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomCalendarView extends LinearLayout {
    ImageButton NextButton, PreviousButton;
    TextView CurrentDate;
    GridView gridView;
    private static final int MAX_CALENDAR_DAYS = 42;
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;
    AlertDialog alertDialog;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormate = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    SimpleDateFormat eventDateFormate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    List<Date> dates = new ArrayList<>();
    List<Events> eventsList = new ArrayList<>();
    DBOpenHlper dbOpenHlper;
    int alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinuit;

    public CustomCalendarView(Context context) {
        super(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Intialize_Layout();
        setUpCalendar();
        PreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH, -1);
                setUpCalendar();
            }
        });

        NextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH, 1);
                setUpCalendar();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                System.out.println("Gride view::::::::::::::::::::");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View addView = LayoutInflater.from(adapterView.getContext()).inflate(R.layout.add_newsevent_latout, null);

                EditText Event_name = addView.findViewById(R.id.event_note);
                EditText Note = addView.findViewById(R.id.note);

                TextView Event_time = addView.findViewById(R.id.event_time);
                ImageButton Set_Time = addView.findViewById(R.id.setEvent_time);
                Button add_Event = addView.findViewById(R.id.addEvent);

                CheckBox alarm_check = addView.findViewById(R.id.alarm_check);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(dates.get(i));
                alarmYear = dateCalendar.get(Calendar.YEAR);
                alarmMonth = dateCalendar.get(Calendar.MONTH);
                alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
                Button addEvent = addView.findViewById(R.id.addEvent);


                Set_Time.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(addView.getContext(), R.style.Theme_AppCompat_Dialog
                                , new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY, i);
                                c.set(Calendar.MINUTE, i1);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat hf = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                                String event_Time = hf.format(c.getTime());
                                Event_time.setText(event_Time);

                                alarmHour = c.get(Calendar.HOUR_OF_DAY);
                                alarmMinuit = c.get(Calendar.MINUTE);
                            }
                        }, hours, minutes, false);
                        timePickerDialog.show();
                    }
                });
                final String date = eventDateFormate.format(dates.get(i));
                final String month = monthFormat.format(dates.get(i));
                final String year = yearFormate.format(dates.get(i));

                add_Event.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (alarm_check.isChecked()) {
                            SaveEvent(Event_name.getText().toString(),Note.getText().toString(), Event_time.getText().toString(), date, month, year,"on");
                            setUpCalendar();

                            Calendar calendar =Calendar.getInstance();
                            calendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinuit);

                            SetAlarm(calendar,Event_name.getText().toString(),Event_time.getText().toString(),getRequestCode(date,Event_name.getText().toString(),Event_time.getText().toString()));
                            alertDialog.dismiss();
                        } else {
                            SaveEvent(Event_name.getText().toString(), Note.getText().toString(),Event_time.getText().toString(), date, month, year,"off");
                            setUpCalendar();
                            alertDialog.dismiss();
                        }

                    }
                });
                builder.setView(addView);
                alertDialog = builder.create();
                alertDialog.show();
                eventsList.clear();
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String date = eventDateFormate.format(dates.get(i));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View showView = LayoutInflater.from(adapterView.getContext()).inflate(R.layout.show_events_layout, null);

                RecyclerView recyclerView = showView.findViewById(R.id.Events_recycler_view);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(showView.getContext(),
                        CollectEventByDate(date));
                recyclerView.setAdapter(eventRecyclerAdapter);
                eventRecyclerAdapter.notifyDataSetChanged();

                builder.setView(showView);
                alertDialog = builder.create();
                alertDialog.show();
                eventsList.clear();
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        setUpCalendar();

                    }
                });
                return true;
            }
        });
    }

    @SuppressLint("Range")
    private  int getRequestCode(String date, String event, String time){
        int code =0;
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        Cursor cursor = dbOpenHlper.ReadIDEvents(date, event,time,database);
        while (cursor.moveToNext()) {
            code = cursor.getInt(cursor.getColumnIndex(DbStructure.ID));
        }
        cursor.close();
        dbOpenHlper.close();

        return  code;
    }


    private void SetAlarm(Calendar calendar,String event,String time,int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    private ArrayList<Events> CollectEventByDate(String date) {
        ArrayList<Events> arrayList = new ArrayList<>();
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        Cursor cursor = dbOpenHlper.ReadEvents(date, database);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String event = cursor.getString(cursor.getColumnIndex(DbStructure.EVENT));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(DbStructure.TIME));
            @SuppressLint("Range") String Date = cursor.getString(cursor.getColumnIndex(DbStructure.DATE));
            @SuppressLint("Range") String month = cursor.getString(cursor.getColumnIndex(DbStructure.MONTH));
            @SuppressLint("Range") String Year = cursor.getString(cursor.getColumnIndex(DbStructure.YEAR));
            @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex(DbStructure.NOTE));

            Events events = new Events(event, time, Date, month, Year, note);
            arrayList.add(events);
        }
        cursor.close();
        dbOpenHlper.close();

        return arrayList;
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void SaveEvent(String event,String note, String time, String date, String month, String year,String notify) {
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getWritableDatabase();
        System.out.println("::::::::::::::::::::::::::::::::::::" + time);
        dbOpenHlper.SaveEvent(event,note, time, date, month, year,notify,database);
        dbOpenHlper.close();
        Toast.makeText(context, "Event Saved", Toast.LENGTH_SHORT).show();
    }

    private void Intialize_Layout() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar_layout, this);
        NextButton = view.findViewById(R.id.nextbtn);
        PreviousButton = view.findViewById(R.id.previousBtn);
        CurrentDate = view.findViewById(R.id.Current_date);
        gridView = view.findViewById(R.id.gridView);

    }

    private void setUpCalendar() {
        String currentDate = dateFormat.format(calendar.getTime());
        CurrentDate.setText(currentDate);
        dates.clear();
        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayofMonth = monthCalendar.get(calendar.DAY_OF_WEEK) - 1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayofMonth);
        CollectEventsPerMonth(monthFormat.format(calendar.getTime()), yearFormate.format(calendar.getTime()));

        while (dates.size() < MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Grid_Adapter grid_adapter = new Grid_Adapter(context, dates, calendar, eventsList);
        gridView.setAdapter(grid_adapter);

    }

    private void CollectEventsPerMonth(String Month, String year) {
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        Cursor cursor = dbOpenHlper.ReadEventsPerMonth(Month, year, database);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String event = cursor.getString(cursor.getColumnIndex(DbStructure.EVENT));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(DbStructure.TIME));
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(DbStructure.DATE));
            @SuppressLint("Range") String month = cursor.getString(cursor.getColumnIndex(DbStructure.MONTH));
            @SuppressLint("Range") String Year = cursor.getString(cursor.getColumnIndex(DbStructure.YEAR));
            @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex(DbStructure.NOTE));

            Events events = new Events(event, time, date, month, Year, note);
            eventsList.add(events);
        }
        cursor.close();
        dbOpenHlper.close();
    }
}
