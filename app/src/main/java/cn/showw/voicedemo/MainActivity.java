package cn.showw.voicedemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    EventManager eventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyPermission(this, getPermission(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
        }));

        eventManager = EventManagerFactory.create(this, "asr");
        eventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String s1, byte[] bytes, int i, int i1) {
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                    handler.sendMessage(getMsg("请说话"));
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                    handler.sendMessage(getMsg("finish"));
                }
                if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                    handler.sendMessage(getMsg(getMatcher(s1, MatcherRegex.RESULT, 1)));
                }
            }
        });

        findViewById(R.id.button).setOnClickListener(this);

    }

    static class MatcherRegex {
        public static final String RESULT = ".*\\{\"word\":\\[\"(.*?)\"\\]\\}.*";
    }

    private String getMatcher(String info,String regex, int index) {
        Matcher m = Pattern.compile(regex).matcher(info);
        m.find();
        try {
            return m.group(index);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Message getMsg(Object obj) {
        Message msg = handler.obtainMessage();
        msg.obj = obj;
        return msg;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                ((TextView) findViewById(R.id.textView2)).append(msg.obj.toString() + "\n\n");
            }

        }
    };

    static boolean b = true;

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button: {
                if (b) {
                    String json = "{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":true,\"pid\":1536}";
                    eventManager.send(SpeechConstant.ASR_START, json, null, 0, 0);
                    b = false;
                } else {
                    eventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
                    b = true;
                }
            }
            break;
        }
    }

    public static void applyPermission(Activity activitie, String[] permissions) {
        if (permissions != null) {
            ActivityCompat.requestPermissions(activitie, permissions, 123);
        }
    }

    public static String[] getPermission(Activity activity, String[] permissions) {
        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            return toApplyList.toArray(tmpList);
        }
        return null;
    }

}
