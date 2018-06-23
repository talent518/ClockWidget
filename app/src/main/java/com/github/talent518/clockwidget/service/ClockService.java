package com.github.talent518.clockwidget.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.talent518.clockwidget.provider.ClockWidget;

import java.time.Clock;
import java.util.Date;

/**
 * Created by john on 26.11.2016.
 */

public class ClockService extends Service {
    private static final String TAG = ClockService.class.getSimpleName();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage");

            // Push update for this widget to the Clock screen
            ComponentName thisWidget = new ComponentName(ClockService.this, ClockWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(ClockService.this);
            int[] ids = manager.getAppWidgetIds(thisWidget);
            if (ids == null || ids.length == 0) {
                stopService(new Intent(getApplicationContext(), ClockService.class));
                return;
            }

            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);

            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mHandler.sendEmptyMessage(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
