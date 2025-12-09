package me.leansys.smsfwd;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    static String PREFS_NAME = "prefs";
    static TextView textView;
    EditText editTextDst;
    static SharedPreferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_PHONE_STATE
                },
                PackageManager.PERMISSION_GRANTED);
        settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = findViewById(R.id.thisPhone);
        String last_msg = settings.getString("last_msg", "none");
        Context context = getApplicationContext();
        textView.setText(getPhoneNumber(context));
        editTextDst = findViewById(R.id.editDstPhone);
        if(forceDst(context)) {
            editTextDst.setFocusable(false);
            editTextDst.setText("Dst forced");
        } else {
            String dst = settings.getString("dst", "");
            editTextDst.setText(dst);
            editTextDst.addTextChangedListener(new TextValidator(editTextDst) {
                @Override public void validate(TextView textView, String text) {
                    editor.putString("dst", text);
                    editor.apply();
                }
            });
        }
    }

    static String getPhoneNumber(Context context){
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    static Boolean forceDst(Context context){
        String thisPhoneNumber = getPhoneNumber(context);
        String num_forced_dst = context.getResources().getString(R.string.num_with_forced_dst);
        Boolean out = thisPhoneNumber.equals(num_forced_dst);
        return out;
    }
}