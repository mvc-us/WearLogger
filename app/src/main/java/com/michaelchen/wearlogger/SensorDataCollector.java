package com.michaelchen.wearlogger;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by derek on 3/9/15.
 */
public class SensorDataCollector implements SensorEventListener {
    private FileOutputStream toUpdate;
    public static final String EXTERN_FILE_NAME = "gestureAuth.log";
    private File file;
    private int count = 0;
    private Activity activity;
    private BufferedWriter buf;

    public static final String TAG = "SensorDataCollector";

    private TextView update;

    public SensorDataCollector(Activity a, TextView tv) {
        this.activity = a;
        update = tv;
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), EXTERN_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e("SensorDataCollector", "failed to make file", e);
            }
        }

        try {
            buf = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Log.e("SensorDataCollector", "unable to create buffer", e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final String text = appendStorage(event.sensor.getName(), event.values, event.accuracy, event.timestamp);
        if (count % 21 == 0) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SensorDataCollector.this.update.setText(text);
                }
            });
        }
        count++;
    }

    public void flushBuffer() {
        if (buf != null) {
            try {
                buf.flush();
            } catch (IOException e) {
                Log.e(TAG, "buffer flush", e);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.v("SensorDataCollector", "Sensor " + sensor + " accuracy changed to " + accuracy);
    }

    private String appendStorage(String sensorName, float[] values, int accuracy, long timestamp) {
        String text = sensorName + " " + Arrays.toString(values) + " " + accuracy + " " + timestamp;
        try {
            buf.append(text);
            buf.newLine();
        } catch (IOException e) {
            Log.d("SensorDataCollector", "failed to append to log file");
        }
        return text;
    }
}
