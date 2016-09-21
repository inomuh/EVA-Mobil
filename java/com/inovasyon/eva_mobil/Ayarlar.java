/**
 * This page applies settings for "Eva Kontrol" screen.
 * These settings can be done in this page;
 * - Name of the robot
 * - IP adress of the robot
 * - Control method (Digital control with buttons or Accelerometer in IMU)
 * - Maximum linear speed in mm/s
 * - Maximum angular speed in mm/s
 * - Close range limit in mm
 * - Free range limit in mm
 */

package com.inovasyon.eva_mobil;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class Ayarlar extends AppCompatActivity {
    /**
     * Input area to get name of the robot.
     */
    EditText et_robot_name;

    /**
     * Input area to get ip address of the robot.
     */
    EditText et_ip_adresi;

    /**
     * Input area to get maximum linear speed.
     */
    EditText et_max_lin_speed;

    /**
     * Input area to get maximum angular speed.
     */
    EditText et_max_rot_speed;

    /**
     * Input area to get close range limit in mm.
     */
    EditText et_x1_range;

    /**
     * Input area to get free range limit in mm.
     */
    EditText et_x2_range;

    /**
     * Input area to select accelerometer control method.
     */
    RadioButton rb_imu;

    /**
     * Input area to select digital control method.
     */
    RadioButton rb_digital;

    /**
     * Information image to explain close range.
     */
    ImageView iv_tehlikeli_alan;

    /**
     * Information image to explain free range.
     */
    ImageView iv_guvenli_alan;

    /**
     * Toast object to show toasts on the screen.
     */
    Toast toast;

    /**
     * This method runs automatically when the page is loaded.
     * @param savedInstanceState State of the application or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        /**
         * UI items are loaded from layout file.
         */
        et_robot_name = (EditText) findViewById(R.id.et_robot_name);
        et_ip_adresi = (EditText) findViewById(R.id.et_ip_adresi);
        et_max_lin_speed = (EditText) findViewById(R.id.et_max_lin_speed);
        et_max_rot_speed = (EditText) findViewById(R.id.et_max_rot_speed);
        et_x1_range = (EditText) findViewById(R.id.et_x1_range);
        et_x2_range = (EditText) findViewById(R.id.et_x2_range);
        rb_imu = (RadioButton) findViewById(R.id.rb_imu);
        rb_digital = (RadioButton) findViewById(R.id.rb_digital);
        iv_guvenli_alan = (ImageView) findViewById(R.id.iv_guvenli_alan);
        iv_tehlikeli_alan = (ImageView) findViewById(R.id.iv_tehlikeli_alan);
        Button btn_save = (Button) findViewById(R.id.btn_save);

        /**
         * Toast is shown when the close range information image is clicked.
         * If range value of sonar/ir sensor smaller than close limit, its color will be red.
         */
        iv_tehlikeli_alan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!toast.getView().isShown())
                        toast = Toast.makeText(getApplicationContext(), "Girilen mm değerinden küçük sensör verileri EvaKontrol sayfasında kırmızı renkte gösterilir.", Toast.LENGTH_LONG);
                        toast.show();
                }catch(Exception e){
                    toast = Toast.makeText(getApplicationContext(), "Girilen mm değerinden küçük sensör verileri EvaKontrol sayfasında kırmızı renkte gösterilir.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        /**
         * Toast is shown when the free range information image is clicked.
         * If range value of sonar/ir sensor higher than free limit, its color will be green.
         */
        iv_guvenli_alan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!toast.getView().isShown())
                        toast = Toast.makeText(getApplicationContext(), "Girilen mm değerinden büyük sensör verileri EvaKontrol sayfasında yeşil renkte gösterilir.", Toast.LENGTH_LONG);
                    toast.show();
                }catch(Exception e){
                    toast = Toast.makeText(getApplicationContext(), "Girilen mm değerinden büyük sensör verileri EvaKontrol sayfasında yeşil renkte gösterilir.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        /**
         * "Kaydet" (Save) button operation is set.
         */
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();
            }
        });

        /**
         * Saved values are fetched and shown in the screen.
         */
        KayitliDegerleriAl();
    }

    /**
     * Saved values are fetched and shown in the screen.
     * If there is no saved data, default values are shown in the screen.
     */
    void KayitliDegerleriAl()
    {
        String str_robot_name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_robot_name", "evarobot");
        et_robot_name.setText(str_robot_name);
        String str_ip_adresi = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_ip_adresi", "192.168.3.37");
        et_ip_adresi.setText(str_ip_adresi);
        String str_max_lin_speed = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_max_lin_speed", "1000");
        et_max_lin_speed.setText(str_max_lin_speed);
        String str_max_rot_speed = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_max_rot_speed", "1000");
        et_max_rot_speed.setText(str_max_rot_speed);
        String str_x1_range = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_x1_range", "1000");
        et_x1_range.setText(str_x1_range);
        String str_x2_range = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_x2_range", "3000");
        et_x2_range.setText(str_x2_range);
        String str_control_method = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("str_control_method", "digital");
        if(str_control_method.contentEquals("digital"))
            rb_digital.setChecked(true);
        else
            rb_imu.setChecked(true);
    }

    /**
     * "Kaydet" (Save) button operations are set.
     * Values in the screen are saved to shared preferences.
     */
    void Save()
    {
        if(Float.parseFloat(et_x1_range.getText().toString()) < Float.parseFloat(et_x2_range.getText().toString())) {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_robot_name", et_robot_name.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_ip_adresi", et_ip_adresi.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_max_lin_speed", et_max_lin_speed.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_max_rot_speed", et_max_rot_speed.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_x1_range", et_x1_range.getText().toString()).commit();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_x2_range", et_x2_range.getText().toString()).commit();
            if (rb_digital.isChecked())
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_control_method", "digital").commit();
            else
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("str_control_method", "imu").commit();
            Toast.makeText(getApplicationContext(), "Ayarlarınız kaydedilmiştir.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "x1 range değeri x2 range değerinden küçük olmalıdır.", Toast.LENGTH_SHORT).show();
        }
    }

}
