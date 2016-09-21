/**
 * This page applies velocities to the robot using selected method.
 */

package com.inovasyon.eva_mobil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EvaKontrol extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    /**
     * These variables are used to make page fullscreen.
     */
    private View mContentView;
    private View mControlsView;
    private AlphaAnimation button_click_anim = new AlphaAnimation(1F, 0.2F);
    Handler handler_hide;


    /**
     * These variables are used by RobotCanvas class.
     */
    static LinearLayout lin_lay_canvas;
    static String str_robot_name;
    static int i_x1_range;
    static int i_x2_range;
    static String str_topic_msg = "0;0;0;0;0;0;0;0;0;0;0;0;0";

    /**
     * Buttons to apply velocities in directions left, right, forward, backward.
     */
    ImageButton btn_left;
    ImageButton btn_right;
    ImageButton btn_forward;
    ImageButton btn_backward;

    /**
     * Buttons to reset left-right and forward-backward velocities.
     */
    ImageButton btn_left_right_reset;

    ImageButton btn_forward_backward_reset;

    /**
     * ImageView to show battery status of the robot.
     */
    ImageView iv_battery_status;

    /**
     * ImageView to show connection power.
     */
    ImageView iv_wifi_status;

    /**
     * TextViews to show applies linear and angular velocities.
     */
    TextView tv_dogrusal_hiz;
    TextView tv_acisal_hiz;

    /**
     * This is used to get wifi signal strength.
     */
    WifiManager wifiManager;

    /**
     * This is used to update wifi signal strength every 5 seconds.
     */
    Handler handler_wifi;

    /**
     * This is used to send velocities to cmd_vel topic every 100 ms.
     */
    Handler handler_cmd_vel;

    /**
     * This handler perform required operations when data is taken from evamobil topic.
     */
    Handler handler_topic;

    /**
     * This handler links device to the mobile robot.
     */
    Handler handler_connect;

    /**
     * Applied velocities.
     */
    static float f_applied_lin_speed = 0;
    static float f_applied_rot_speed = 0;

    /**
     * Setting from shared preerences.
     */
    static float f_max_lin_speed;
    static float f_max_rot_speed;
    static String str_control_method;
    String str_ip_adresi;

    /**
     *  These variables hold percentage of progress bars.
     */
    public static int i_pb_left = 0;
    public static int i_pb_right = 0;
    public static int i_pb_forward = 0;
    public static int i_pb_backward = 0;

    /**
     * WebView to run roslibjs.
     */
    private WebView Wv;

    /**
     * Values from accelerometer sensor in x and y directions.
     */
    float f_acc_x;
    float f_acc_y;

    /**
     * Gravity.
     */
    float f_yer_cekimi = 9.8f;

    /**
     * SensorManager to take values from accelerometer.
     */
    private SensorManager sensorManager;

    /**
     * Rounded buttons are clicked or not with accelerometer controller method.
     */
    boolean b_left_right = false;
    boolean b_forward_backward = false;

    /**
     * This method runs automatically when the page is loaded.
     * @param savedInstanceState State of the application or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * Set state of the application and page layout.
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eva_kontrol);

        /**
         * Handlers are initialized.
         */
        handler_hide = new Handler();
        handler_wifi = new Handler();
        handler_cmd_vel = new Handler();
        handler_topic = new Handler();
        handler_connect = new Handler();

        /**
         * SensorManager is created and regşstered to accelerometer changes.
         */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        /**
         * Fullscreen contents.
         */
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        /**
         * This is used to get wifi signal strength.
         */
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        /**
         * Settings are get from shared preferences.
         */
        SetPreferences();

        /**
         * UI elements are initialized with saved values.
         */
        InitInterfaceViews();

        /**
         * Switch to fullscreen.
         */
        hide();

        /**
         * Robot is created on the screen.
         */
        lin_lay_canvas = (LinearLayout) findViewById(R.id.lin_lay_canvas);
        RobotCanvas robot_canvas = new RobotCanvas(this);
        lin_lay_canvas.addView(robot_canvas);

        /**
         * Roslibjs is run in webview.
         */
        JavaScriptInterface myJavaScriptInterface = new JavaScriptInterface(this);
        Wv.getSettings().setJavaScriptEnabled(true);
        Wv.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
        Wv.loadUrl("file:///android_asset/index.html");
        Wv.getSettings().setUserAgentString("Mozilla/5.0 (Windows; MSIE 6.0; Android 1.6; en-US) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2 myKMB/1.0");


        /**
         * This is used to send velocities to cmd_vel topic every 100 ms.
         */
        SendCmdVelData();

        /**
         * This is used to update wifi signal strength every 5 seconds.
         */
        SetWifiStatus();

        /**
         * Connects to the robot after 1 second.
         */
        ConnectToRobot();
    }


    /**
     * Background processes are stopped when the activity is stopped and robot is stopped by applying 0 for both linear and angular velocities.
     */
    @Override
    public void onStop() {
        super.onStop();
        Wv.loadUrl("javascript:publish_cmd_vel(" + 0 + ", " + 0 + ")");
        handler_hide.removeCallbacksAndMessages(null);
        handler_topic.removeCallbacksAndMessages (null);
        handler_cmd_vel.removeCallbacksAndMessages(null);
        handler_wifi.removeCallbacksAndMessages(null);
        sensorManager.unregisterListener(this);

    }


    /**
     * This class is used to take values from javascript.
     */
    public class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /**
         * This method is called by javascript when the new data is taken.
         * @param str_msg Content of the message.
         */
        @JavascriptInterface
        public void getData(String str_msg) {
            str_topic_msg = str_msg;
            handler_topic.post(new Runnable() {
                @Override
                public void run() {
                    String[] separated = EvaKontrol.str_topic_msg.split(";");
                    SetBatteryStatus(Integer.valueOf(separated[separated.length-1]));
                }
            });
        }
    }

    /**
     * Connect to the robot after 1 second using javascript function.
     */
    public void ConnectToRobot() {
        handler_connect.postDelayed(new Runnable() {
            public void run() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String str_ip = "ws://" + str_ip_adresi + ":9090";
                        Wv.loadUrl("javascript:connectSelectedRobot('"+ str_ip + "')");
                    }
                };
                handler_connect.post(runnable);
            }
        }, 1000);
    }

    /**
     * Send velocities to cmd_vel topic every 100 ms using javascript function.
     */
    public void SendCmdVelData() {
        handler_cmd_vel.postDelayed(new Runnable() {
            public void run() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Wv.loadUrl("javascript:publish_cmd_vel(" + f_applied_lin_speed + ", " + f_applied_rot_speed + ")");
                        tv_dogrusal_hiz.setText(String.valueOf((int)(f_applied_lin_speed*1000)));
                        tv_acisal_hiz.setText(String.valueOf((int)(f_applied_rot_speed*1000)));
                        handler_cmd_vel.postDelayed(this, 100);
                    }
                };
                handler_cmd_vel.post(runnable);
            }
        }, 100);
    }

    /**
     * Update wifi signal strength every 5 seconds.
     */
    void SetWifiStatus() {
        handler_wifi.postDelayed(new Runnable() {
            public void run() {
                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 5);
                if (level <= 0)
                    iv_wifi_status.setImageResource(R.drawable.wifi_level_0);
                else if (level == 1)
                    iv_wifi_status.setImageResource(R.drawable.wifi_level_1);
                else if (level == 2)
                    iv_wifi_status.setImageResource(R.drawable.wifi_level_2);
                else if (level == 3)
                    iv_wifi_status.setImageResource(R.drawable.wifi_level_3);
                else if (level >= 4)
                    iv_wifi_status.setImageResource(R.drawable.wifi_level_4);
                handler_wifi.postDelayed(this, 5000);
            }
        }, 5000);
    }

    /**
     * Updates battery level image.
     * @param i_level Battery level (<1 for low, 1 for half, >1 for full)
     */
    void SetBatteryStatus(int i_level)
    {
        if(i_level<1)
            iv_battery_status.setImageResource(R.drawable.battery_low);
        else if (i_level==1)
            iv_battery_status.setImageResource(R.drawable.battery_half);
        else if(i_level>1)
            iv_battery_status.setImageResource(R.drawable.battery_full);
    }

    /**
     * UI elements are initialized with saved values.
     */
    void InitInterfaceViews()
    {
        Wv = (WebView) findViewById(R.id.web_view);
        btn_left = (ImageButton) findViewById(R.id.btn_left);
        btn_right = (ImageButton) findViewById(R.id.btn_right);
        btn_left_right_reset = (ImageButton) findViewById(R.id.btn_left_right_reset);
        btn_forward = (ImageButton) findViewById(R.id.btn_forward);
        btn_backward = (ImageButton) findViewById(R.id.btn_backward);
        btn_forward_backward_reset = (ImageButton) findViewById(R.id.btn_forward_backward_reset);
        iv_battery_status = (ImageView) findViewById(R.id.iv_battery_status);
        iv_wifi_status = (ImageView) findViewById(R.id.iv_wifi_status);
        tv_dogrusal_hiz = (TextView) findViewById(R.id.tv_dogrusal_hiz);
        tv_acisal_hiz = (TextView) findViewById(R.id.tv_acisal_hiz);

        btn_left.setOnClickListener(null);
        btn_right.setOnClickListener(null);
        btn_left_right_reset.setOnClickListener(null);
        btn_forward.setOnClickListener(null);
        btn_backward.setOnClickListener(null);
        btn_forward_backward_reset.setOnClickListener(null);
        btn_left_right_reset.setOnTouchListener(null);
        btn_forward_backward_reset.setOnTouchListener(null);

        /**
         * If IMU is selected as control method, buttons are hidden.
         * Else, buttons are shown and button operations are set.
         */
        if(!str_control_method.contentEquals("digital")) {
            btn_left.setVisibility(View.GONE);
            btn_right.setVisibility(View.GONE);
            btn_forward.setVisibility(View.GONE);
            btn_backward.setVisibility(View.GONE);

            btn_left_right_reset.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        b_left_right = true;
                        return false;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        b_left_right = false;
                        return false;
                    } else
                        return false;
                }
            });

            btn_forward_backward_reset.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        b_forward_backward = true;
                        return false;
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        b_forward_backward = false;
                        return false;
                    }
                    else
                        return false;
                }
            });
        }else{
            btn_left.setOnClickListener(this);
            btn_right.setOnClickListener(this);
            btn_left_right_reset.setOnClickListener(this);
            btn_forward.setOnClickListener(this);
            btn_backward.setOnClickListener(this);
            btn_forward_backward_reset.setOnClickListener(this);
        }
    }

    /**
     * Button operations are set.
     * @param v Button view.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_left:
                if(i_pb_right==0){
                    if(i_pb_left<100) {
                        i_pb_left += 10;
                    }
                }else{
                    i_pb_right -= 10;
                }
                break;

            case R.id.btn_right:
                if(i_pb_left==0){
                    if(i_pb_right<100) {
                        i_pb_right += 10;
                    }
                }else{
                    i_pb_left -= 10;
                }
                break;

            case R.id.btn_left_right_reset:
                i_pb_left = 0;
                i_pb_right = 0;
                break;

            case R.id.btn_forward:
                if(i_pb_backward==0){
                    if(i_pb_forward<100) {
                        i_pb_forward += 10;
                    }
                }else{
                    i_pb_backward -= 10;
                }
                break;

            case R.id.btn_backward:
                if(i_pb_forward==0){
                    if(i_pb_backward<100) {
                        i_pb_backward += 10;
                    }
                }else{
                    i_pb_forward -= 10;
                }
                break;

            case R.id.btn_forward_backward_reset:
                i_pb_forward = 0;
                i_pb_backward = 0;
                break;
        }

        if(i_pb_forward>0)
            f_applied_lin_speed = ((float)i_pb_forward/100.0f) * f_max_lin_speed;
        else if (i_pb_backward>0)
            f_applied_lin_speed = -((float)i_pb_backward/100.0f) * f_max_lin_speed;
        else
            f_applied_lin_speed = 0;

        if(i_pb_right>0)
            f_applied_rot_speed = -((float)i_pb_right/100.0f) * f_max_rot_speed;
        else if (i_pb_left>0)
            f_applied_rot_speed = ((float)i_pb_left/100.0f) * f_max_rot_speed;
        else
            f_applied_rot_speed = 0;

        v.startAnimation(button_click_anim);
    }

    /**
     * Settings are taken from shared preferences.
     */
    void SetPreferences()
    {
        str_robot_name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_robot_name", "evarobot");
        str_ip_adresi = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_ip_adresi", "192.168.3.37");
        f_max_lin_speed = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_max_lin_speed", "1000"))/1000.0f;
        f_max_rot_speed = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_max_rot_speed", "1000"))/1000.0f;
        i_x1_range = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_x1_range", "1000"));
        i_x2_range = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_x2_range", "3000"));
        str_control_method = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_control_method", "digital");
    }

    /**
     * This method runs automatically when the accelerometer data available.
     * @param event Contains accelerometer data.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (str_control_method.contentEquals("imu")) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                f_acc_x = 2*(event.values[2] - 4.9f);
                f_acc_y = event.values[1];

                /**
                 * Calculate velocities using accelerometer data.
                 */

                if(b_left_right)
                    f_applied_rot_speed = -((float) f_acc_y / f_yer_cekimi) * f_max_rot_speed;
                else
                    f_applied_rot_speed = 0;

                if(b_forward_backward)
                    f_applied_lin_speed = ((float) f_acc_x / f_yer_cekimi) * f_max_lin_speed;
                else
                    f_applied_lin_speed = 0;

                if(b_left_right){
                    if(f_applied_rot_speed<0){
                        i_pb_left = 0;
                        i_pb_right = Math.abs(Math.round(f_applied_rot_speed*100000)/Math.round(f_max_rot_speed*1000));
                    }else if(f_applied_rot_speed>0){
                        i_pb_right = 0;
                        i_pb_left = Math.round(f_applied_rot_speed*100000)/Math.round(f_max_rot_speed*1000);
                    }
                }else{
                    i_pb_left = 0;
                    i_pb_right = 0;
                }

                if(b_forward_backward){
                    if(f_applied_lin_speed>0){
                        i_pb_backward = 0;
                        i_pb_forward = Math.round(f_applied_lin_speed*100000)/Math.round(f_max_lin_speed*1000);
                    }else if(f_applied_lin_speed<0){
                        i_pb_forward = 0;
                        i_pb_backward = Math.abs(Math.round(f_applied_lin_speed*100000)/Math.round(f_max_lin_speed*1000));
                    }
                }else{
                    i_pb_backward = 0;
                    i_pb_forward = 0;
                }
            }
        }
    }


    /**
     * This method runs automatically when the accelerometer accuracy changed.
     * @param arg0 Not used.
     * @param arg1 Not used.
     */
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }


    /**
     * Switch to fullscreen.
     */
    private void hide() {
        /**
         * Hide UI first
         */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        /**
         * Schedule a runnable to remove the status and navigation bar after a delay
         */

        handler_hide.removeCallbacks(mShowPart2Runnable);
        handler_hide.postDelayed(mHidePart2Runnable, 300);
    }


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            /**
             * Delayed removal of status and navigation bar
             *
             * Note that some of these constants are new as of API 16 (Jelly Bean)
             * and API 19 (KitKat). It is safe to use them, as they are inlined
             * at compile-time and do nothing on earlier devices.
             */
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            /**
             * Delayed display of UI elements
             */
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };




}
