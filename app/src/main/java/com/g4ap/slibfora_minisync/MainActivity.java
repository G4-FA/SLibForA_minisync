package com.g4ap.slibfora_minisync;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    TextView tvMySysState = null;

    void initUI() {

        tvMySysState = findViewById(R.id.tvSysState);

        Button btn = findViewById(R.id.btnGetSysState);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long nCallhis = SLibMiniSync.getSyncStateCallhis( MainActivity.this );
                long nSms = SLibMiniSync.getSyncStateSms( MainActivity.this );
                String str = String.format( "sys state: callhis=%d sms=%d", nCallhis, nSms );
                tvMySysState.setText( str );
            }
        });

        btn = findViewById(R.id.btnSyncCallhis);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLibMiniSync.syncAndroidCallhisDB( MainActivity.this );
            }
        });

        btn = findViewById(R.id.btnSyncSms);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLibMiniSync.syncAndroidSMSDB( MainActivity.this );
            }
        });

    }


}
