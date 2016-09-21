/**
 * First screen of the application. It contains three buttons.
 * "Eva Kontrol" button launches digital/accelerometer driving screen.
 * "Ayarlar" button opens settings like ip address, max linear velocity, etc.
 * "Çıkış" button makes a pop-up to close application.
 */

package com.inovasyon.eva_mobil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AnaSayfa extends AppCompatActivity {

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
        setContentView(R.layout.activity_ana_sayfa);

        /**
         * Buttons are loaded from layout file.
         * Operations are set for button clicks.
         */
        Button btn_eva_kontrol = (Button) findViewById(R.id.btn_eva_kontrol);
        Button btn_cikis = (Button) findViewById(R.id.btn_cikis);
        Button btn_ayarlar = (Button) findViewById(R.id.btn_ayarlar);

        btn_eva_kontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EvaKontrol();
            }
        });

        btn_cikis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cikis();
            }
        });

        btn_ayarlar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ayarlar();
            }
        });
    }

    /**
     * Launches digital/accelerometer driving screen.
     */
    void EvaKontrol()
    {
        Intent intent = new Intent(this, EvaKontrol.class);
        startActivity(intent);
    }

    /**
     * Makes a pop-up to close application.
     */
    void Cikis()
    {
        /**
         * Pop-up dialog is shown to close application.
         * If user clicks to "Evet" (Yes) stop the activity
         * If user clicks to "Hayır" (No) do nothing.
         */
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Çıkış")
                .setMessage("Çıkmak İstediğinize Emin Misiniz?")
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AnaSayfa.this.finish();
                    }

                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    /**
     * Opens settings like ip address, max linear velocity, etc.
     */
    void Ayarlar()
    {
        Intent intent = new Intent(this, Ayarlar.class);
        startActivity(intent);
    }
}
