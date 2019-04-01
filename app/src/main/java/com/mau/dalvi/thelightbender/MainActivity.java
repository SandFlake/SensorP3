package com.mau.dalvi.thelightbender;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor mSensorProximity, mSensorLight;
    private boolean isProximitySensorPresent, isLightSensorPresent, changeSystemBrightness;
    private CameraManager mCameraManager;
    private String mCameraID;
    private CameraCharacteristics mParameters;
    private float distanceFromPhone;
    private boolean isFlashLightOn, hasChecked = false;
    private ContentResolver mContentResolver;
    private Window mWindow;
    private float brightness = 0;
    private Button btn1, btn2, btn3, btn4, btn5;
    private RadioButton rBtnSystem, rBtnWindow;
    private TextView tvTitle, tvCurrent, tvSystemOrWindow;
    private RadioGroup rGroupSysOrWin;
    private double brightMultiplier = 0;
    private float lastLightValue = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        initSensors();
        initScreenBrightness();
    }

    private void initSensors() {

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_FASTEST);
            isLightSensorPresent = true;
        } else {
            isLightSensorPresent = false;
            Toast.makeText(this, "No light sensor available", Toast.LENGTH_SHORT).show();
        }
        initCameraFlashlight();

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mSensorManager.registerListener(this, mSensorProximity, SensorManager.SENSOR_DELAY_FASTEST);
            isProximitySensorPresent = true;
        } else {
            isProximitySensorPresent = false;
            Toast.makeText(this, "No Proximity sensor available", Toast.LENGTH_SHORT).show();
        }

        initScreenBrightness();

    }

    private void initScreenBrightness() {
        mContentResolver = getContentResolver();
        mWindow = getWindow();
    }

    private void initComponents() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCurrent = findViewById(R.id.tvCurrentSetting);
        tvSystemOrWindow = findViewById(R.id.tvSystemOrWindow);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        rBtnSystem = findViewById(R.id.rBtnSystem);
        rBtnWindow = findViewById(R.id.rBtnWindow);
        rGroupSysOrWin = findViewById(R.id.rGroupSysOrWin);

        btn1.setOnClickListener(new ButtonListener());
        btn2.setOnClickListener(new ButtonListener());
        btn3.setOnClickListener(new ButtonListener());
        btn4.setOnClickListener(new ButtonListener());
        btn5.setOnClickListener(new ButtonListener());

        rGroupSysOrWin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rBtnSystem.isChecked()) {
                    setSystemBrightness(true);
                    buildAlert();
                } else if (rBtnWindow.isChecked()) {
                    setSystemBrightness(false);
                }
            }
        });

    }

    public void setSystemBrightness(boolean changeSystemBrightness) {
        this.changeSystemBrightness = changeSystemBrightness;
    }

    public void setBrightnessLevel(double brightMultiplier) {
        this.brightMultiplier = brightMultiplier;
    }

    private class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.equals(btn1)) {
                setBrightnessLevel(0.0);
                changeScreenBrightness((float)0.0);
            } else if (v.equals(btn2)) {
                setBrightnessLevel(0.2);
                changeScreenBrightness((float)0.2);
            } else if (v.equals(btn3)) {
                setBrightnessLevel(0.5);
                changeScreenBrightness((float)0.5);
            } else if (v.equals(btn4)) {
                setBrightnessLevel(0.7);
                changeScreenBrightness((float)0.7);
            } else if (v.equals(btn5)) {
                setBrightnessLevel(0.9);
                changeScreenBrightness((float)0.9);

            }
        }
    }

    public void buildAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Warning!");

        builder.setMessage("Changing the system brightness is permanent!!");

        builder.setPositiveButton("That's fine", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Oh lord no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    protected void onResume() {
        super.onResume();
        if (isProximitySensorPresent) {
            mSensorManager.registerListener(this, mSensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Proximity Sensor registered", Toast.LENGTH_SHORT).show();
        }
        if (isLightSensorPresent) {
            mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Light Sensor registered", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onPause() {
        super.onPause();
        if (isProximitySensorPresent) {
            mSensorManager.unregisterListener(this);
            Toast.makeText(this, "Proximity UNREGISTERED", Toast.LENGTH_SHORT).show();
        }
        if (isLightSensorPresent) {
            mSensorManager.unregisterListener(this);
            Toast.makeText(this, "Light UNREGISTERED", Toast.LENGTH_LONG).show();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

           distanceFromPhone = event.values[0];

           if (distanceFromPhone <= mSensorProximity.getMaximumRange()) {

               if (!isFlashLightOn) {
                   turnTorchLightOn();
               }
           } else {
               if (isFlashLightOn) {
                   turnTorchLightOff();


           }
       }

        if (event.sensor.equals(mSensorLight)) {

            changeScreenBrightness(event.values[0]);
            lastLightValue = event.values[0];

        }

    }


    private void changeScreenBrightness(float brightness) {

        float changedBrightness = (float) (brightMultiplier + ((1 / brightness) / 4));

        if (brightness > 0 && brightness < 100) {

            if (!changeSystemBrightness) {
                Log.d(TAG, "changeScreenBrightness: " + changedBrightness + " " + brightness + "multiplier: " + brightMultiplier);
                final WindowManager.LayoutParams mLayoutParams = mWindow.getAttributes();
                mLayoutParams.screenBrightness = changedBrightness;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWindow.setAttributes(mLayoutParams);
                    }
                });

            } else if (!Settings.System.canWrite(this)) {
                if (!hasChecked) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    startActivity(i);
                    hasChecked = true;
                }

            } else if (changeSystemBrightness) {
                Log.d(TAG, "changeSystemBrightness oranges " + changeSystemBrightness);
                Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, (int) (255 * changedBrightness));
                final WindowManager.LayoutParams mLayoutParams = mWindow.getAttributes();
                mLayoutParams.screenBrightness = changedBrightness;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWindow.setAttributes(mLayoutParams);
                    }
                });
            }
        }
    }


    private void initCameraFlashlight() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraID = mCameraManager.getCameraIdList()[0];
            mParameters = mCameraManager.getCameraCharacteristics(mCameraID);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void turnTorchLightOn() {
        if (mParameters.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) && !isFlashLightOn) {
            try {
                mCameraManager.setTorchMode(mCameraID, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            isFlashLightOn = true;
        }
    }

    public void turnTorchLightOff() {
        if (isFlashLightOn) {
            try {
                mCameraManager.setTorchMode(mCameraID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            isFlashLightOn = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}


