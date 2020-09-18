package com.bigphi.autoscreenlock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigphi.autoscreenlock.intent.CustomIntents;
import com.bigphi.autoscreenlock.receiver.MyAdmin;
import com.bigphi.autoscreenlock.service.ForegroundService;

import static com.bigphi.autoscreenlock.constants.Handlers.serviceToActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button lock, disable, enable, killTimer;
    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    public static final int LOCK_SCREEN = 101;
    private EditText minuteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        minuteEditText = findViewById(R.id.minute_edittext);
        lock = findViewById(R.id.lock_counter_start);
        killTimer = findViewById(R.id.kill_timer);
        enable = findViewById(R.id.enableBtn);
        disable = findViewById(R.id.disableBtn);
        lock.setOnClickListener(this);
        enable.setOnClickListener(this);
        disable.setOnClickListener(this);
        killTimer.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        enable.setVisibility(isActive ? View.GONE : View.VISIBLE);

        serviceToActivity = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
               if(LOCK_SCREEN == msg.what){
                   lockScreen();
               }
               return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == lock) {
            startTimer();

        } else if (view == enable) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
            startActivityForResult(intent, RESULT_ENABLE);

        } else if (view == disable) {
            devicePolicyManager.removeActiveAdmin(compName);
            disable.setVisibility(View.GONE);
            enable.setVisibility(View.VISIBLE);

        } else if(view == killTimer){
            Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
            intent.setAction(CustomIntents.STOP_TIMER);
            callService(intent);
        }
    }

    private void callService(Intent intent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void startTimer() {
        boolean active = devicePolicyManager.isAdminActive(compName);
        if (active) {
            String timeInMinute = minuteEditText.getText().toString();
            Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
            intent.setAction(CustomIntents.START_TIMER);
            intent.putExtra("time", Integer.parseInt(timeInMinute));
            callService(intent);
            finish();
        } else {
            Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
        }


    }

    private void lockScreen() {
        boolean active = devicePolicyManager.isAdminActive(compName);
        if (active) {
            devicePolicyManager.lockNow();
        } else {
            Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}