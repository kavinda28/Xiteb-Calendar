package com.news.calendarapp.Adapter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.news.calendarapp.AlarmReceiver;
import com.news.calendarapp.DB.DBOpenHlper;
import com.news.calendarapp.DB.DbStructure;
import com.news.calendarapp.Model.Events;
import com.news.calendarapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.MyViewHolder> {
    Context context;
    ArrayList<Events> arrayList;
    DBOpenHlper dbOpenHlper;

    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_rowlayout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = arrayList.get(position);

        holder.Event.setText(events.getEVENT());
        holder.DateTxt.setText(events.getDATE());
        holder.Time.setText(events.getTIME());
        holder.Note_show.setText(events.getNOTE());
        holder.Delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_calendar_event(events.getEVENT(), events.getDATE(), events.getTIME());
                arrayList.remove(position);
                notifyDataSetChanged();
            }
        });

        if (isAlarmed(events.getDATE(), events.getEVENT(), events.getTIME())) {
            holder.setAlarm.setImageResource(R.drawable.notification_on);
           // notifyDataSetChanged();
        } else {
            holder.setAlarm.setImageResource(R.drawable.notification_off);
           // notifyDataSetChanged();
        }

        Calendar datecalendar = Calendar.getInstance();
        datecalendar.setTime(ConvertStringToDate(events.getDATE()));
        int alarmYear = datecalendar.get(Calendar.YEAR);
        int alarmMonth = datecalendar.get(Calendar.MONTH);
        int alarmDay = datecalendar.get(Calendar.DAY_OF_MONTH);
        Calendar timecalendar = Calendar.getInstance();
        timecalendar.setTime(ConvertStringTome(events.getTIME()));
        int alarmHour = timecalendar.get(Calendar.HOUR_OF_DAY);
        int alarmMinuit = timecalendar.get(Calendar.MINUTE);



        holder.setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAlarmed(events.getDATE(), events.getEVENT(), events.getTIME())) {
                    holder.setAlarm.setImageResource(R.drawable.notification_off);
                    Cancel_Alarm(getRequestCode(events.getDATE(), events.getEVENT(), events.getTIME()));
                    updateEvent(events.getDATE(), events.getEVENT(), events.getTIME(), "off");
                    notifyDataSetChanged();

                } else {
                    holder.setAlarm.setImageResource(R.drawable.notification_on);
                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinuit);
                    set_Alarm(alarmCalendar, events.getEVENT(),events.getTIME(),
                            getRequestCode(events.getDATE(),events.getEVENT(),events.getTIME()));
                    updateEvent(events.getDATE(), events.getEVENT(), events.getTIME(), "on");
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView DateTxt, Event, Time,Note_show;
        ImageButton Delete_btn, setAlarm;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DateTxt = itemView.findViewById(R.id.event_date);
            Event = itemView.findViewById(R.id.event_name);
            Time = itemView.findViewById(R.id.event_Time_rowlayout);
            Delete_btn = itemView.findViewById(R.id.Delete_btn);
            setAlarm = itemView.findViewById(R.id.alarm_btn);
            Note_show = itemView.findViewById(R.id.Show_note);

        }
    }

    private Date ConvertStringToDate(String eventDate) {
        SimpleDateFormat format = new
                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    private Date ConvertStringTome(String eventDate) {
        SimpleDateFormat format = new
                SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    private void delete_calendar_event(String event, String date, String time) {
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getWritableDatabase();
        dbOpenHlper.deleteEvent(event, date, time, database);
        dbOpenHlper.close();
    }

    private boolean isAlarmed(String date, String event, String time) {
        boolean alarmed = false;
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        Cursor cursor = dbOpenHlper.ReadIDEvents(date, event, time, database);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String notify = cursor.getString(cursor.getColumnIndex(DbStructure.Notify));
            if (notify.equals("on")) {
                alarmed = true;
            } else {
                alarmed = false;
            }
        }
        cursor.close();
        dbOpenHlper.close();
        return alarmed;
    }

    private void set_Alarm(Calendar calendar, String event, String time, int RequestCode) {
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("event", event);
        intent.putExtra("time", time);
        intent.putExtra("id", RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void Cancel_Alarm(int RequestCode) {
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @SuppressLint("Range")
    private int getRequestCode(String date, String event, String time) {
        int code = 0;
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        Cursor cursor = dbOpenHlper.ReadIDEvents(date, event, time, database);
        while (cursor.moveToNext()) {
            code = cursor.getInt(cursor.getColumnIndex(DbStructure.ID));
        }
        cursor.close();
        dbOpenHlper.close();

        return code;
    }

    private void updateEvent(String date, String event, String time, String notify) {
        dbOpenHlper = new DBOpenHlper(context);
        SQLiteDatabase database = dbOpenHlper.getReadableDatabase();
        dbOpenHlper.updateEvent(date, event, time, notify, database);
        dbOpenHlper.close();
    }

}
