package com.example.hlkhjk_ok.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView compass = null;
    private SensorManager manager;
    private TextView orientation_text;
    private final int DEVIATION_DEGREE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initFullView();

        setContentView(R.layout.activity_main);
        compass = (ImageView) findViewById(R.id.compass);
        orientation_text = (TextView) findViewById(R.id.orientation);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor geomgSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        manager.registerListener(sensorListener, accerSensor, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(sensorListener, geomgSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void initFullView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void forceStopAPK(String pkgName){
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(sh.getOutputStream());
            final String Command = "am force-stop "+pkgName+ "\n";
            os.writeBytes(Command);
            os.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            sh.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (manager != null) {
            manager.unregisterListener(sensorListener);
        }
    }

    private String getStrByID(int id) {
        return this.getResources().getString(id);
    }

    private String getRetriveInfo(float degree) {
        StringBuilder result = new StringBuilder();
        float tmp = degree;
        tmp = Math.abs(Math.round(degree));
        String degree_other = " " + (int)(Math.abs(90-tmp)) + " °";

        if (degree > 0 && degree < 90) {
            result = result.append(getStrByID(R.string.loc_en)).append(degree_other);
        } else if (degree == 0) {
            result = result.append(getStrByID(R.string.loc_n));
        } else if (degree == 90) {
            result = result.append(getStrByID(R.string.loc_e));
        } else if (degree == 180 || degree == -180) {
            result = result.append(getStrByID(R.string.loc_s));
        } else if (degree == -90) {
            result = result.append(getStrByID(R.string.loc_w));
        } else if (degree > 90 && degree <180) {
            result = result.append(getStrByID(R.string.loc_es)).append(degree_other);
        } else if (degree < -90 && degree > -180) {
            result = result.append(getStrByID(R.string.loc_ws)).append(degree_other);
        } else if (degree > -90 && degree < 0) {
            result = result.append(getStrByID(R.string.loc_wn)).append(degree_other);
        }

        return result.toString();
    }

    private SensorEventListener sensorListener = new SensorEventListener() {
        private float[] accerValues = new float[3];
        private float[] geomeValues = new float[3];
        private float lastRotataDegree = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomeValues = event.values.clone();
            }

            float[] R = new float[9];
            float[] values = new float[3];

            manager.getRotationMatrix(R, null, accerValues, geomeValues);
            manager.getOrientation(R, values);

            float rotateDegree = -(float)Math.toDegrees(values[0]);

            if (Math.abs(rotateDegree - lastRotataDegree) > DEVIATION_DEGREE) {
                RotateAnimation animation = new RotateAnimation(lastRotataDegree, rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setFillAfter(true);

//                LinearInterpolator in = new LinearInterpolator(); //线性
//                animation.setInterpolator(in);

                AccelerateInterpolator acceranim = new AccelerateInterpolator();
                animation.setInterpolator(acceranim);

//                DecelerateInterpolator decelanim = new DecelerateInterpolator();
//                animation.setInterpolator(decelanim);

                compass.startAnimation(animation);
                lastRotataDegree = rotateDegree;
                String result = getRetriveInfo(-rotateDegree);
                orientation_text.setText(result);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
