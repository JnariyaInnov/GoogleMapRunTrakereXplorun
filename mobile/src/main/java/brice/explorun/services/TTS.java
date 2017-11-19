package brice.explorun.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by germain on 11/19/17.
 */

public class TTS extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    private TextToSpeech tts;
    private BroadcastReceiver ttsReceiver;
    public static boolean isStarted = false;

    @Override
    public void onCreate() {
        isStarted = true;
        tts = new TextToSpeech(this, this);
        ttsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                speak(intent.getStringExtra("text"));
            }
        };
        IntentFilter filter = new IntentFilter("ex_tts");
        this.registerReceiver(ttsReceiver, filter);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("eX_TTS", "This Language is not supported");
            }
            else{
                Log.i("eX_TTS", "Service successfully started");
            }

        } else {
            Log.e("eX_TTS", "Initialization Failed!");
        }
    }

    public void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onUtteranceCompleted(String uttId) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        this.unregisterReceiver(this.ttsReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}