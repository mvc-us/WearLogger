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

/**
 * Created by derek on 3/9/15.
 */
public class SensorDataCollector implements SensorEventListener {
    private FileOutputStream toUpdate;
    public static final String EXTERN_FILE_NAME_LINEAR = "gestureAuthLinear";
    public static final String EXTERN_FILE_NAME_GYRO = "gestureAuthGyro";
    public static final String EXTERN_FILE_NAME_ACCEL = "gestureAuthAccel";
    public static final String EXTERN_FILE_NAME_DEFAULT = "gestureAuthDefault";

    private File file;
    private int count = 0;
    private Activity activity;
    private BufferedWriter buf;

    public static final String TAG = "SensorDataCollector";

    private TextView update;
    private String fileName;
    private String baseFileName;
    private int counter;

    public SensorDataCollector(Activity a, int sensorType, int counter) {
        this.activity = a;
        switch(sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                fileName = EXTERN_FILE_NAME_ACCEL;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                fileName = EXTERN_FILE_NAME_LINEAR;
                break;
            case Sensor.TYPE_GYROSCOPE:
                fileName = EXTERN_FILE_NAME_GYRO;
                break;
            default:
                fileName = EXTERN_FILE_NAME_DEFAULT;
        }

        this.counter = counter;
        baseFileName = fileName;
        fileName = baseFileName + Integer.toString(counter);

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
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

    void endLog() {
        flushBuffer();
        if (buf != null) {
            try {
                buf.append(END + System.currentTimeMillis());
                buf.newLine();
                buf.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void startLog() {
        if (buf != null) {
            try {
                buf.append(START + System.currentTimeMillis());
                buf.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void clearFile() {
        try {
            file.delete();
            file.createNewFile();
            buf = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            Log.e("SensorDataCollector", "failed to make file", e);
        }
    }

    private static String floatArrToString(float[] values) {
        String ret = "";
        for (int i = 0; i < values.length; i++, ret += ",") {
            ret += Float.toString(values[i]);
        }
        return ret.substring(0, ret.length() - 1);
    }

    private String appendStorage(String sensorName, float[] values, int accuracy, long timestamp) {
        String text = timestamp + "," + floatArrToString(values);
        String ret = sensorName + " " + text;

        try {
            buf.append(text);
            buf.newLine();
        } catch (IOException e) {
            Log.d("SensorDataCollector", "failed to append to log file");
        } catch (NullPointerException e) {
            Log.d("SensorDataCollector", "unknown sensor");
        }
        return ret;
    }
}
