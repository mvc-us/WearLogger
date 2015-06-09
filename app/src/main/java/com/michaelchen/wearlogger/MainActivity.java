package com.michaelchen.wearlogger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends WearableActivity {

    static final String PREF_FILE_KEY = "stored_preferences";
    private static final String PREF_NUM_TRAINING_POINTS = "num_training_points";
    public static final String EXTERN_FILE_NAME_BOOLS = "gestureAuthBools";
    public static final String TAG = "main activity";


    static final int CLASS_LIST_SELECT_CODE = 1;

    public static final int[] TARGET_SENSORS = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
    };

    private SensorDataCollector[] sensorEventListeners = new SensorDataCollector[TARGET_SENSORS.length];
    private Switch loggingSwitch;
    private Switch classifySwitch;
    private boolean classify;
    private int numTrainingPoints;
    private File boolFile;
    private BufferedWriter boolBuf;
    private CheckBox isCorrectBox;
    private int gestureType = 0;

    private WearableListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        classify = false;
        initSwitches();
        initBoolBuf();
        isCorrectBox = (CheckBox) findViewById(R.id.checkBoxCorrect);

        for (int i = 0; i < TARGET_SENSORS.length; i++) {
            SensorDataCollector sensorEventListener = new SensorDataCollector(TARGET_SENSORS[i], numTrainingPoints);
            sensorEventListeners[i] = sensorEventListener;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (SensorDataCollector s : sensorEventListeners) {
            s.flushBuffer();
        }

        try {
            boolBuf.flush();
            boolBuf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updatePref(PREF_NUM_TRAINING_POINTS, numTrainingPoints);
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

    /**
     * Note: this can also change numTrainingPoints
     */
    private void initBoolBuf() {
        numTrainingPoints = getSharedPreferences(PREF_FILE_KEY, MODE_PRIVATE).getInt(PREF_NUM_TRAINING_POINTS, 0);
        boolFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), EXTERN_FILE_NAME_BOOLS);
        if (!boolFile.exists()) {
            numTrainingPoints = 0; // shortcut to quickly reset: just delete bool file, plus we have no info here
            updatePref(PREF_NUM_TRAINING_POINTS, numTrainingPoints);
            try {
                boolFile.createNewFile();
            } catch (IOException e) {
                Log.e("MainAct", "failed to make file", e);
            }
        }

        try {
            boolBuf = new BufferedWriter(new FileWriter(boolFile, true));
        } catch (IOException e) {
            Log.e("MainAct", "unable to create buffer", e);
        }
    }

    private void startLogging() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (!classify) {
            try {
                boolBuf.append(Boolean.toString(isCorrectBox.isChecked()));
                boolBuf.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            numTrainingPoints++;
        }

        for (int i = 0; i < TARGET_SENSORS.length; i++) {
            int type = TARGET_SENSORS[i];
            Sensor s = sensorManager.getDefaultSensor(type);
            sensorEventListeners[i].startLog(classify);
            sensorManager.registerListener(sensorEventListeners[i], s, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void stopLogging() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for (SensorDataCollector sensorEventListener : sensorEventListeners) {
            sensorEventListener.endLog();
            sensorManager.unregisterListener(sensorEventListener);
        }

        if (classify) {
            // prepare knn classifier on separate thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File[] trainingFiles = MainActivity.this.getLinearClassificationFiles();
                    boolean[] trainingBools = MainActivity.this.getClassificationBools();
                    File toClassify = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS),
                            SensorDataCollector.EXTERN_FILE_NAME_LINEAR + SensorDataCollector.EXTERN_FILE_SUFFIX_CLASSIFY);
                    GestureClassifier classifier = null;
                    switch(gestureType) {
                        case 0:
                            classifier = new KnnClassifier(trainingFiles, trainingBools);
                            break;
                        case 1:
                            classifier = new DistanceClassifier(trainingFiles, trainingBools);
                            break;
                        case 2:
                            classifier = new BufferedDistanceClassifier(trainingFiles, trainingBools);
                            break;
                    }

                    if (classifier == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unknown classifier", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    final boolean good = classifier.classify(toClassify);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Good signature: " + good, Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }).start();
        }
    }

    private File[] getLinearClassificationFiles() {
        File[] files = new File[numTrainingPoints];
        for (int i = 1; i <= files.length; i++) { //modified loop to deal with obo
            String fileName = SensorDataCollector.EXTERN_FILE_NAME_LINEAR + Integer.toString(i);
            files[i-1] = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        }
        return files;
    }

    private boolean[] getClassificationBools() {
        boolean[] bools = new boolean[numTrainingPoints];
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), EXTERN_FILE_NAME_BOOLS);
        try {
            BufferedReader buf = new BufferedReader(new FileReader(f));
            String line;
            int i = 0;
            while ((line = buf.readLine()) != null) {
                bools[i] = Boolean.parseBoolean(line);
                i++;
            }
        } catch (IOException e) {
            Log.e("getting bools", "bufferedReader error", e);
        } catch (IndexOutOfBoundsException e) {
            Log.e("getting bools", "too many lines in file", e);
        }
        return bools;
    }

    public void onResetClicked(View v) {
        loggingSwitch.setChecked(false);
        for (SensorDataCollector sensorEventListener : sensorEventListeners) {
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

    public void classifyTypeClicked(View v) {
        Intent intent = new Intent(this, ClassSelectorActivity.class);
        startActivityForResult(intent, CLASS_LIST_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "GestureType:" + ClassSelectorActivity.classMethods.get(resultCode));
    }
}

