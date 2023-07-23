package net.sunniwell.servicedemo.MyService;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.sunniwell.servicedemo.MyInterface.MusicInterface;
import net.sunniwell.servicedemo.R;

public class AudioService extends Service implements MusicInterface, MediaPlayer.OnCompletionListener {
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 MediaPlayer，设置音乐资源等
        if (mPlayer ==null){
            mPlayer = new MediaPlayer();
            mPlayer =MediaPlayer.create(this,R.raw.slowdown);
            mPlayer.setOnCompletionListener(this);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    public class MusicBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void play() {
        mPlayer.start();
    }

    @Override
    public void pause() {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }
}
