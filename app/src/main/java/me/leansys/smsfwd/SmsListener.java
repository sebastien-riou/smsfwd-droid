package me.leansys.smsfwd;

import static me.leansys.smsfwd.MainActivity.PREFS_NAME;
import static me.leansys.smsfwd.MainActivity.getPrefDefaultValue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.StringRes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SmsListener extends BroadcastReceiver {
    String TAG = getClass().getSimpleName();
    String getPrefName(Context context, @StringRes int id){
        return context.getResources().getString(id);
    }
    String getPref(Context context, @StringRes int id){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(getPrefName(context,id),getPrefDefaultValue(id));
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String dst = getPref(context,R.string.pref_dst);
        dst = dst.replace(" ","");
        Log.i(TAG,"dst = "+dst);
        boolean hasSendUrl = !getPref(context,R.string.pref_su).isEmpty();
        SmsManager smsManager = SmsManager.getDefault();
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        StringBuilder wholeMessageBuilder = new StringBuilder();
        String sender="";
        for (SmsMessage m : smsMessages) {
            String body = m.getMessageBody();
            sender = m.getOriginatingAddress();
            String toSend = sender + ":" + body;
            wholeMessageBuilder.append(body);
            try {
                if(!dst.isEmpty()) {
                    smsManager.sendTextMessage(dst, null, toSend, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String wholeMessage = wholeMessageBuilder.toString();
        String remoteAdminPwd = settings.getString("remoteAdminPwd","");
        boolean fromAdmin=false;
        if (!remoteAdminPwd.isEmpty()) {
            if (wholeMessage.startsWith(remoteAdminPwd)) {
                Log.i(TAG,"remoteAdmin message = "+wholeMessage);
                fromAdmin=true;
                String[] lines = wholeMessage.split("\n");

                for(int i=1; i<lines.length; i++){
                    String line = lines[i];
                    String[] kv = line.split("=",2);
                    if(settings.contains(kv[0])) {
                        editor.putString(kv[0], kv[1]);
                    }else{
                        Log.e(TAG,"Invalid preference key: '"+kv[0]+"'");
                    }
                    editor.apply();
                }
            }
        }
        if(hasSendUrl && !fromAdmin) {
            try {
                OverNetworkSender runningTask = new OverNetworkSender(context);
                runningTask.execute(sender + ":" +wholeMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.sendBroadcast(new Intent(MainActivity.REFRESH_UI_INTENT));
    }

    private final class OverNetworkSender extends AsyncTask<String, Void, String> {
        private Context mContext;

        public OverNetworkSender(Context context){
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
            String sendUrl = getPref(context,R.string.pref_su);
            if(sendUrl.isEmpty()) return;
            String txtPattern =  getPref(context,R.string.pref_su_txt);
            if(!sendUrl.contains(txtPattern)){
                Log.e(TAG, getPrefName(context,R.string.pref_su)+" '"+sendUrl+"' does not contain "+getPrefName(context,R.string.pref_su_txt)+" '" + txtPattern+"'");
                return;
            }
            String escaped = sendUrl.replace(txtPattern, URLEncoder.encode(msg, "UTF8"));
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