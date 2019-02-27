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
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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
    private float mBrightness;
    private RadioButton rBtn1, rBtn2, rBtn3, rBtn4, rBtn5, rBtnSystem, rBtnWindow;
    private TextView tvTitle, tvCurrent, tvSystemOrWindow;
    private RadioGroup rGroupBrightness, rGroupSysOrWin;
    private double brightMultiplier = 1;
    private float lastLightValue = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();


        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null && (isLightSensorPresent == false)) {
            mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_FASTEST);
            isLightSensorPresent = true;
        } else {
            isLightSensorPresent = false;
            Toast.makeText(this, "No light sensor available", Toast.LENGTH_SHORT).show();
        }
        initScreenBrightness();


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null && (isProximitySensorPresent == false)) {
            mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mSensorManager.registerListener(this, mSensorProximity, SensorManager.SENSOR_DELAY_FASTEST);
            isProximitySensorPresent = true;
        } else {
            isProximitySensorPresent = false;
            Toast.makeText(this, "No Proximity sensor available", Toast.LENGTH_SHORT).show();
        }

        initCameraFlashlight();


    }

    private void initScreenBrightness() {
        mContentResolver = getContentResolver();
        mWindow = getWindow();
    }

    private void initComponents() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCurrent = findViewById(R.id.tvCurrentSetting);
        tvSystemOrWindow = findViewById(R.id.tvSystemOrWindow);

        rGroupBrightness = findViewById(R.id.rGroupBrightness);

        rGroupBrightness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rBtn1 = findViewById(R.id.rBtn1);
                if (rBtn1.isChecked()) {
                    setBrightnessLevel(0.05);
                }
                rBtn2 = findViewById(R.id.rBtn2);
                if (rBtn2.isChecked()) {
                    setBrightnessLevel(0.2);
                }
                rBtn3 = findViewById(R.id.rBtn3);
                if (rBtn3.isChecked()) {
                    setBrightnessLevel(0.5);
                }
                rBtn4 = findViewById(R.id.rBtn4);
                if (rBtn4.isChecked()) {
                    setBrightnessLevel(0.7);
                }
                rBtn5 = findViewById(R.id.rBtn5);
                if (rBtn5.isChecked()) {
                    setBrightnessLevel(0.9);
                }
            }
        });

        rGroupSysOrWin = findViewById(R.id.rGroupSysOrWin);

        rGroupSysOrWin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rBtnSystem = findViewById(R.id.rBtnSystem);
                rBtnWindow = findViewById(R.id.rBtnWindow);
                if (rBtnSystem.isChecked()) {
                    setSystemBrightness(true);
                    buildAlert();
                } else {
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

        if (event.sensor.equals(mSensorLight)) {
            changeScreenBrightness(event.values[0]);
            lastLightValue = event.values[0];

        } else if (event.sensor.equals(mSensorProximity)) {

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

           float light = event.values[0];
            if (light > 0 && light < 100) {
                changeScreenBrightness((float) brightMultiplier / light);
            }

        }

    }


    private void changeScreenBrightness(float brightness) {

        float changedBrightness = (float) (brightMultiplier + ((1 / brightness) / 4));


        if (brightness > 0 && brightness < 100) {

            if (!changeSystemBrightness) {
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
                } else {
                    Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, (int) (255 * changedBrightness));
                }

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


