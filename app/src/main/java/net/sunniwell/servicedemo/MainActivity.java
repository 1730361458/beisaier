package net.sunniwell.servicedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import net.sunniwell.servicedemo.MyService.AudioService;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private AudioService mAudioService;
    private boolean mIsBound = false;

    private SeekBar mSeekBar;
    private WaveProgressView progressBar;
    private Button mPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar= findViewById(R.id.customBezierProgressBar);
        mSeekBar = findViewById(R.id.value);
        //是否绘制第二层波浪
        //progressBar.isSetCanvasSecondWave(true);
        //将TextView设置进度条里
//        progressBar.setTextViewVaule(mTextValue);
//        //设置字体数值显示监听
//        progressBar.setUpdateTextListener(new BezierProgressBar.UpdateTextListener() {
//            @Override
//            public String updateText(float interpolatedTime, float currentProgress, float maxProgress) {
//                //取一位整和并且保留两位小数
//                DecimalFormat decimalFormat=new DecimalFormat("0.00");
//                Log.d("123",interpolatedTime+"=+"+currentProgress+"=="+maxProgress);
//                String text_value = decimalFormat.format(interpolatedTime * currentProgress / maxProgress * 100)+"%";
//                //最终把格式好的内容(数值带进进度条)
//                return text_value ;
//            }
//        });
//        //设置进度和时间
//        progressBar.setProgress(30,3000);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressBar.setProgress(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
//        Intent intent = new Intent(this, AudioService.class);
//        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mIsBound) {
//            unbindService(musicConnection);
//            mIsBound = false;
//        }
    }

    // 可以在活动中调用 musicService 的方法来控制音乐的播放
}