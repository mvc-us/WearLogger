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

public class MainActivity extends Activity {

    static final String PREF_FILE_KEY = "stored_preferences";
    private static final String PREF_NUM_TRAINING_POINTS = "num_training_points";

    public static final int[] TARGET_SENSORS = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
    };

    private SensorDataCollector[] sensorEventListeners = new SensorDataCollector[TARGET_SENSORS.length];
    private TextView textSensor;
    private Switch loggingSwitch;
    private Switch classifySwitch;
    private boolean classify;
    private int numTrainingPoints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textSensor = (TextView) findViewById(R.id.textViewSensor);
        for (int i = 0; i < TARGET_SENSORS.length; i++) {
            SensorDataCollector sensorEventListener = new SensorDataCollector(this, textSensor, TARGET_SENSORS[i]);
            sensorEventListeners[i] = sensorEventListener;
        }
        classify = false;
        initSwitches();
        numTrainingPoints = getSharedPreferences(PREF_FILE_KEY, MODE_PRIVATE).getInt(PREF_NUM_TRAINING_POINTS, 0);
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
        loggingSwitch.setChecked(false);
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

    protected boolean updatePref(String key, int value) {
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(
                PREF_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPref.edit();
        e.putInt(key, value);
        e.apply();
        return e.commit();
    }

    protected void initSwitches() {
        loggingSwitch = (Switch) findViewById(R.id.switch1);
        loggingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.updateLoggingStatus(isChecked);
            }
        });

        classifySwitch = (Switch) findViewById(R.id.classifySwitch);
        classifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.classify = isChecked;
            }
        });
    }
}

