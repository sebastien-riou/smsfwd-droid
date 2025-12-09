package me.leansys.smsfwd;

import static me.leansys.smsfwd.MainActivity.PREFS_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SmsListener extends BroadcastReceiver {
    String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean forceDst = MainActivity.forceDst(context);

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String default_dst_num;
        if(forceDst){
            default_dst_num = context.getResources().getString(R.string.default_dst_num);
        }else{
            default_dst_num = "";
        }
        String dst = settings.getString("dst", default_dst_num);
        SmsManager smsManager = SmsManager.getDefault();

        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage m : smsMessages) {
            String body = m.getMessageBody();
            String sender = m.getOriginatingAddress();
            String toSend = sender + ":" + body;

            editor.putString("last_msg", toSend);
            editor.apply();

            if(forceDst) {
                try {
                    LongOperation runningTask = new LongOperation(context);
                    runningTask.execute(toSend);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                dst = dst.replace(" ","");
                Log.i(TAG,"dst = "+dst);
                smsManager.sendTextMessage(dst, null, toSend, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final class LongOperation extends AsyncTask<String, Void, String> {
        private Context mContext;

        public LongOperation (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            sendOverNetwork(mContext,params[0]);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    void sendOverNetwork(Context context, String msg){
        //should be executed in background
        try {
            //String send_url = "https://smsapi.free-mobile.fr/sendmsg?user=free-id&pass=api-key&msg=";
            String send_url =  context.getResources().getString(R.string.send_url);
            String escaped = send_url +
                    URLEncoder.encode(msg, "UTF8");
            URL url = new URL(escaped);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Log the server response code
            int responseCode = connection.getResponseCode();
            Log.i(TAG, "Server responded with: " + responseCode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}