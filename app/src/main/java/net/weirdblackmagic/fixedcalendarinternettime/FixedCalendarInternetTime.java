package net.weirdblackmagic.fixedcalendarinternettime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class FixedCalendarInternetTime extends AppWidgetProvider {

    public static final String CLOCK_CALENDAR_WIDGET_UPDATE = "CLOCK_CALENDAR_WIDGET_UPDATE";

    public static final String[] monthNames = {"Unesamber", "Dutesamber", "Trisesamber", "Tetresamber", "Pentesamber", "Hexesamber", "Sevesamber", "Octesamber", "Novesamber", "Desamber", "Undesamber", "Dodesamber", "Tridesamber"};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            context.startService(new Intent(context, UpdateService.class));
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Enter relevant functionality for when the first widget is created

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);

        // update in 1 swatch .beat (86.4 sec = 86400 ms)
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 86400, createUpdateIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        // Enter relevant functionality for when the last widget is disabled

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createUpdateIntent(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(CLOCK_CALENDAR_WIDGET_UPDATE)) {
            context.startService(new Intent(context, UpdateService.class));
        }
    }

    public static class UpdateService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            RemoteViews updateViews = buildUpdate(this);
            ComponentName widget = new ComponentName(this, FixedCalendarInternetTime.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(widget, updateViews);

            return START_STICKY;
        }

        private RemoteViews buildUpdate(Context context) {
            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.fixed_calendar_internet_time);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("UTC+1"));
            calendar.setTime(new Date());

            int current_dot_beats = (int) Math.round((calendar.get(Calendar.SECOND) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.HOUR_OF_DAY) * 3600)) / 86.4);

            updateViews.setTextViewText(R.id.dot_beat_text, "@" + String.valueOf(current_dot_beats));

            int span = calendar.get(Calendar.DAY_OF_YEAR);
            int fixedYear = calendar.get(Calendar.YEAR);
            int fixedMonth = span >= 365 ? 0 : (int) Math.ceil(span / 28.0);
            int fixedDay = span == 365 ? 1 : span == 366 ? 1 : ((span % 28) == 0 ? 28 : span % 28);
            String fixedDayName = dayToDayName(fixedDay);

            updateViews.setTextViewText(R.id.fixed_calendar_year, String.valueOf(fixedYear));
            updateViews.setTextViewText(R.id.fixed_calendar_month, monthNames[fixedMonth-1]);
            updateViews.setTextViewText(R.id.fixed_calendar_day_name, fixedDayName);
            updateViews.setTextViewText(R.id.fixed_calendar_day,  fixedDay == 1 ? String.valueOf(fixedDay) + "st" : fixedDay == 2 ? String.valueOf(fixedDay) + "nd" : fixedDay == 3 ? String.valueOf(fixedDay) + "rd" : String.valueOf(fixedDay) + "th");

            return updateViews;
        }

        private String dayToDayName(int day) {
            if (day == 1 || day == 8 || day == 15 || day == 22) {
                return "Sun";
            }
            if (day == 2 || day == 9 || day == 16 || day == 23) {
                return "Mon";
            }
            if (day == 3 || day == 10 || day == 17 || day == 24) {
                return "Tue";
            }
            if (day == 4 || day == 11 || day == 18 || day == 25) {
                return "Wed";
            }
            if (day == 5 || day == 12 || day == 19 || day == 26) {
                return "Thu";
            }
            if (day == 6 || day == 13 || day == 20 || day == 27) {
                return "Fri";
            }
            if (day == 7 || day == 14 || day == 21 || day == 28) {
                return "Sat";
            }
            return "";
        }
    }

    private PendingIntent createUpdateIntent(Context context) {
        Intent intent = new Intent(CLOCK_CALENDAR_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
}

