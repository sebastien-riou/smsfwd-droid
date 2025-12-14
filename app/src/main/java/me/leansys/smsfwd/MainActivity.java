package me.leansys.smsfwd;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    static String REFRESH_UI_INTENT = "refreshUi";
    static String PREFS_NAME = "prefs";
    static HashMap<Integer, String> prefDefaultValues;
    private View mLayout;
    private String[] permissions = new String[]{
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            //Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET,
            //Manifest.permission.READ_PHONE_STATE,
            //Manifest.permission.READ_PHONE_NUMBERS
    };
    BroadcastReceiver refreshUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshDstView();
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refreshUiReceiver);
    }
    static String getPrefDefaultValue(@StringRes int id){
        if(null == prefDefaultValues){
            prefDefaultValues = new HashMap<Integer, String>();
            prefDefaultValues.put(R.string.pref_rap,"A82FDA52B9C708BB");
            prefDefaultValues.put(R.string.pref_rao,"false");
            prefDefaultValues.put(R.string.pref_su_txt,"[text]");
        }
        return prefDefaultValues.getOrDefault(id,"");
    }
    String getPrefName(@StringRes int id){
        return getResources().getString(id);
    }
    String getPref(@StringRes int id){
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(getPrefName(id),getPrefDefaultValue(id));
    }
    void createPrefIfDontExist(@StringRes int id){
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        String name = getPrefName(id);
        if(!settings.contains(name)){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(getPrefName(id), getPrefDefaultValue(id));
            editor.apply();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPrefIfDontExist(R.string.pref_dst);
        createPrefIfDontExist(R.string.pref_rap);
        createPrefIfDontExist(R.string.pref_rao);
        createPrefIfDontExist(R.string.pref_su);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        mLayout = findViewById(R.id.main);
        TextView editTextDst = findViewById(R.id.editDstPhone);
        editTextDst.addTextChangedListener(new TextValidator(editTextDst) {
            @Override public void validate(TextView textView, String text) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getPrefName(R.string.pref_dst), text);
                editor.apply();
            }
        });
        TextView remoteAdminPwd = findViewById(R.id.remoteAdminPwd);
        remoteAdminPwd.setText(getPref(R.string.pref_rap));
        remoteAdminPwd.addTextChangedListener(new TextValidator(remoteAdminPwd) {
            @Override public void validate(TextView textView, String text) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getPrefName(R.string.pref_rap), text);
                editor.apply();
            }
        });
        TextView sendUrl = findViewById(R.id.sendUrl);
        sendUrl.setText(getPref(R.string.pref_su));
        sendUrl.addTextChangedListener(new TextValidator(sendUrl) {
            @Override public void validate(TextView textView, String text) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getPrefName(R.string.pref_su), text);
                editor.apply();
            }
        });
        TextView sendUrlTxt = findViewById(R.id.sendUrlTxt);
        sendUrlTxt.setText(getPref(R.string.pref_su_txt));
        sendUrl.addTextChangedListener(new TextValidator(sendUrlTxt) {
            @Override public void validate(TextView textView, String text) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getPrefName(R.string.pref_su_txt), text);
                editor.apply();
            }
        });
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        versionName += " ("+versionCode+")";
        TextView versionInfoView = findViewById(R.id.versionInfo);
        versionInfoView.setText(versionName);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(!hasPermissions()){
            requestPermissions();
        }
        refreshDstView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(refreshUiReceiver, new IntentFilter(REFRESH_UI_INTENT), RECEIVER_EXPORTED);
        } else {
            registerReceiver(refreshUiReceiver, new IntentFilter(REFRESH_UI_INTENT));
        }
    }

    private void refreshDstView(){
        TextView editTextDst = findViewById(R.id.editDstPhone);
        TextView mainTextView = findViewById(R.id.mainTextView);
        TextView sendUrl = findViewById(R.id.sendUrl);
        TextView sendUrlTxt = findViewById(R.id.sendUrlTxt);
        TextView remoteAdminPwd = findViewById(R.id.remoteAdminPwd);

        String dst = getPref(R.string.pref_dst);
        String su = getPref(R.string.pref_su);
        String su_txt = getPref(R.string.pref_su_txt);
        String rap = getPref(R.string.pref_rap);

        if(remoteAdminOnly()) {
            mainTextView.setText("'Remote Admin Only' mode ON\nForward to: ");
            editTextDst.setEnabled(false);
            sendUrl.setEnabled(false);
            sendUrlTxt.setEnabled(false);
            remoteAdminPwd.setEnabled(false);
        }else{
            mainTextView.setText("Forward to:");
            editTextDst.setEnabled(true);
            sendUrl.setEnabled(true);
            sendUrlTxt.setEnabled(true);
            remoteAdminPwd.setEnabled(true);

        }
        editTextDst.setText(dst);
        sendUrl.setText(su);
        sendUrlTxt.setText(su_txt);
        remoteAdminPwd.setText(rap);
    }

    private Boolean hasPermissions(){
        for(String permission:permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                permissions,
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Boolean granted = Boolean.TRUE;
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    // Permission request was denied.
                    Snackbar.make(mLayout, "permissions denied!",
                                    Snackbar.LENGTH_SHORT)
                            .show();
                    granted = Boolean.FALSE;
                    break;
                }
            }
            if(granted){
                Log.i(TAG,"Permissions granted");
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    Boolean remoteAdminOnly(){
        return getPref(R.string.pref_rao).equalsIgnoreCase("true");
    }
}