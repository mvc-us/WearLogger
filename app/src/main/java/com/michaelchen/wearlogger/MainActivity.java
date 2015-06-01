package com.michaelchen.wearlogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileOutputStream;

public class MainActivity extends Activity {

    static final String PREF_FILE_KEY = "stored_preferences";

    public static final int[] TARGET_SENSORS = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
    };

    private SensorDataCollector[] sensorEventListeners = new SensorDataCollector[TARGET_SENSORS.length];
    private TextView textSensor;
    private FileOutputStream fos;
    private Switch inChairSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textSensor = (TextView) findViewById(R.id.textViewSensor);
        for (int i = 0; i < TARGET_SENSORS.length; i++) {
            SensorDataCollector sensorEventListener = new SensorDataCollector(this, textSensor, TARGET_SENSORS[i]);
            sensorEventListeners[i] = sensorEventListener;
        }

        initSwitch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(SensorDataCollector s : sensorEventListeners) {
            s.flushBuffer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLogging();
    }

    private void startLogging() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        for (int i=0; i < TARGET_SENSORS.length; i++) {
            int type = TARGET_SENSORS[i];
            Sensor s = sensorManager.getDefaultSensor(type);
            sensorEventListeners[i].startLog();
            sensorManager.registerListener(sensorEventListeners[i], s, SensorManager.SENSOR_DELAY_FASTEST);
//            sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void stopLogging() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for(SensorDataCollector sensorEventListener : sensorEventListeners) {
            sensorEventListener.endLog();
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    public void onResetClicked(View v) {
        inChairSwitch.setChecked(false);
        for(SensorDataCollector sensorEventListener : sensorEventListeners) {
            if (sensorEventListener != null) stopLogging();
            sensorEventListener.clearFile();
        }
    }

    private void updateLoggingStatus(boolean logging) {
        if (logging) {
            startLogging();
        } else {
            stopLogging();
        }
    }

    protected boolean updatePref(String key, boolean value) {
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(
                PREF_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPref.edit();
        e.putBoolean(key, value);
        e.apply();
        return e.commit();
    }

    protected void initSwitch() {
//        SharedPreferences sharedPref = this.getSharedPreferences(
//                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//        boolean logging = sharedPref.getBoolean(getString(R.string.logging), false);
        inChairSwitch = (Switch) findViewById(R.id.switch1);
        boolean logging = false;
        inChairSwitch.setChecked(logging);
        inChairSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.updateLoggingStatus(isChecked);
            }
        });

    }
}

