package com.github.talent518.clockwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.github.talent518.clockwidget.provider.ClockWidget;

/**
 * Created by john on 26.11.2016.
 */

public class ClockService extends Service {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // Push update for this widget to the Clock screen
            ComponentName thisWidget = new ComponentName(ClockService.this, ClockWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(ClockService.this);
            int[] ids = manager.getAppWidgetIds(thisWidget);
            if (ids == null || ids.length == 0) {
                return;
            }

            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);

            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
