package com.michaelchen.wearlogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    static final String PREF_FILE_KEY = "stored_preferences";

    public static final int[] TARGET_SENSORS = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
    };

    private TextView mTextView;
    private SensorDataCollector sensorEventListener;
    private TextView textSensor;
    private FileOutputStream fos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textSensor = (TextView) findViewById(R.id.textViewSensor);
        sensorEventListener = new SensorDataCollector(this, textSensor);
        initSwitch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorEventListener.flushBuffer();
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

        for (int type: TARGET_SENSORS) {
            Sensor s = sensorManager.getDefaultSensor(type);
            sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_FASTEST);
//            sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void stopLogging() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void updateLoggingStatus(boolean logging) {
        if (logging) {
            startLogging();
        } else {
            stopLogging();
        }
    }

    protected boolean updatePref(String key, boolean value) {
        // update heating or cooling
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
        Switch inChairSwitch = (Switch) findViewById(R.id.switch1);
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

