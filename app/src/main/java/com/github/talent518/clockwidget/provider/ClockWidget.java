package com.github.talent518.clockwidget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.talent518.clockwidget.R;
import com.github.talent518.clockwidget.service.ClockService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class ClockWidget extends AppWidgetProvider {
    private static final String TAG = ClockWidget.class.getSimpleName();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");

    SharedPreferences pref = null;
    DisplayMetrics dm;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);

        Bitmap bitmap = Bitmap.createBitmap(getWidth(appWidgetId), getHeight(appWidgetId), Bitmap.Config.ARGB_8888);
        draw(new Canvas(bitmap));
        views.setImageViewBitmap(R.id.iv_clock, bitmap);


        views.setOnClickPendingIntent(R.id.iv_clock, PendingIntent.getService(context, 0, new Intent(context, ClockService.class), 0));

        try {
            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "ClockWidget-" + appWidgetId + ".png"));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.d(TAG, "updateAppWidget: appWidgetId = " + appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: appWidgetIds = " + Arrays.toString(appWidgetIds));

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.d(TAG, "onReceive: " + intent.getAction());

        pref = context.getSharedPreferences("widgetOptions", Context.MODE_PRIVATE);
        dm = context.getResources().getDisplayMetrics();
        super.onReceive(context, intent);
    }

    public int dip2px(float dpValue) {
        return (int) (dpValue * dm.density + 0.5f);
    }

    private int getWidth(int appWidgetId) {
        return dip2px(pref.getInt("w" + appWidgetId, 66));
    }

    private int getHeight(int appWidgetId) {
        return dip2px(pref.getInt("h" + appWidgetId, 66));
    }

    private void setSize(int appWidgetId, int width, int height) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("w" + appWidgetId, width);
        editor.putInt("h" + appWidgetId, height);
        editor.commit();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "onAppWidgetOptionsChanged: appWidgetId = " + appWidgetId + ", newOptions = " + toString(newOptions));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        setSize(appWidgetId, newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 66), newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 66));

        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted: appWidgetIds = " + Arrays.toString(appWidgetIds));

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(TAG, "onRestored: oldWidgetIds = " + Arrays.toString(oldWidgetIds) + ", newWidgetIds = " + Arrays.toString(newWidgetIds));

        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.d(TAG, "onEnabled");
        context.startService(new Intent(context, ClockService.class));
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.d(TAG, "onDisabled");
        context.stopService(new Intent(context, ClockService.class));
    }

    private String toString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        if (bundle.isEmpty()) {
            return "{}";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (String key : bundle.keySet()) {
            if (sb.length() > 2) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
            sb.append("    " + key + ": " + bundle.get(key));
        }
        sb.append("\n}");

        return sb.toString();
    }

    private void draw(Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        final float RADIUS = Math.min(width, height) / 2.0f;
        final float DIAL = RADIUS - 10.0f;
        final float DIAL2 = DIAL - RADIUS * 0.05f;
        final float TEXTSIZE = DIAL2 * 0.125f;
        final float TEXT = DIAL2 - TEXTSIZE / 2.0f - 5.0f;
        final float POINTER = DIAL2 - TEXTSIZE - 10.0f;
        final float DATETIME = POINTER / 2.0f;
        final float DTTextSize = POINTER * 0.2f;
        final float HOUR = POINTER * 0.5f;
        final float HOURWEIGTH = POINTER * 0.025f;
        final float MINUTE = POINTER * 0.75f;
        final float MINUTEWEIGHT = POINTER * 0.02f;
        final float SECOND = POINTER;
        final float SECONDWEIGHT = POINTER * 0.0125f;
        final float cx = width / 2.0f, cy = height / 2.0f;

        Log.i(TAG, "draw: width = " + width + ", height = " + height);

        Calendar calendar = Calendar.getInstance();

        float x, y, x2, y2;
        double angle;
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        Paint bgPainter = new Paint(paint);
        bgPainter.setColor(Color.WHITE);
        bgPainter.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, RADIUS, bgPainter);

        canvas.drawCircle(cx, cy, DIAL, paint);
        canvas.drawCircle(cx, cy, DIAL2, paint);

        textPaint.setTextSize(TEXTSIZE);
        int n = 1;
        for (angle = 60; angle > -300; angle -= 6) {
            x = cx + (float) (cos(angle) * DIAL);
            y = cy - (float) (sin(angle) * DIAL);
            x2 = cx + (float) (cos(angle) * DIAL2);
            y2 = cy - (float) (sin(angle) * DIAL2);

            if (angle % 90 == 0) {
                paint.setStrokeWidth(10.0f);
            } else if (angle % 30 == 0) {
                paint.setStrokeWidth(6.0f);
            } else {
                paint.setStrokeWidth(2.0f);
            }
            canvas.drawLine(x, y, x2, y2, paint);
            if (angle % 30 == 0) {
                x = cx + (float) (cos(angle) * TEXT);
                y = cy - (float) (sin(angle) * TEXT) + TEXTSIZE / 3.0f;
                canvas.drawText(Integer.toString(n++), x, y, textPaint);
            }
        }

        String[] dfs = dateFormat.format(new Date()).split("\n");
        textPaint.setAlpha(80);
        textPaint.setTextSize(DTTextSize);
        canvas.drawText(dfs[0], cx, cy - DATETIME, textPaint);
        canvas.drawText(dfs[1], cx, cy + DATETIME, textPaint);

        paint.setStyle(Paint.Style.FILL);

        // 时针
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(HOURWEIGTH);
        drawPointer(canvas, cx, cy, HOUR * 1.0f / 2.0f, 1.0f, 90 - 30 * hour - 30.0f / 60.0f * minute, paint);

        // 分针
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(MINUTEWEIGHT);
        drawPointer(canvas, cx, cy, MINUTE * 2.0f / 3.0f, 1.0f / 2.0f, 90 - 6 * minute - 6.0f / 60.0f * second, paint);

        // 秒针
        paint.setColor(Color.RED);
        paint.setStrokeWidth(SECONDWEIGHT);
        drawPointer(canvas, cx, cy, SECOND * 2.0f / 3.0f, 1.0f / 2.0f, 90 - 6 * second, paint);
    }

    /**
     * 绘制指针
     *
     * @param canvas
     * @param cx
     * @param cy
     * @param radius
     * @param angle
     * @param paint
     */
    protected void drawPointer(Canvas canvas, float cx, float cy, float radius, float scale, double angle, Paint paint) {
        float x = cx + (float) (cos(angle) * radius);
        float y = cy - (float) (sin(angle) * radius);
        float x2 = cx + (float) (cos(angle + 180) * 24.0f);
        float y2 = cy - (float) (sin(angle + 180) * 24.0f);
        float r = paint.getStrokeWidth() * 1.5f;
        float R = radius + radius * scale;
        double agl = (float) (Math.acos(r / radius * scale) * 180.0f / Math.PI);

        canvas.drawLine(x2, y2, x, y, paint);

        paint.setStrokeWidth(1.0f);
        canvas.drawCircle(cx, cy, r, paint);
        canvas.drawCircle(x, y, r, paint);

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + (float) (cos(angle + agl) * r), y - (float) (sin(angle + agl) * r));
        path.lineTo(cx + (float) (cos(angle) * R), cy - (float) (sin(angle) * R));
        path.lineTo(x + (float) (cos(angle - agl) * r), y - (float) (sin(angle - agl) * r));
        path.lineTo(x, y);
        path.close();
        canvas.drawPath(path, paint);
    }

    protected double cos(double a) {
        return Math.cos(a * Math.PI / 180.0f);
    }

    protected double sin(double a) {
        return Math.sin(a * Math.PI / 180.0f);
    }

}

