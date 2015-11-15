package com.example.yuan.classes;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by yuan on 11/13/15.
 */
public class SoundMeter implements Runnable{

    private Context mContext;

    private boolean flag = true;
    private float db = 0;
    private double sum = 0;
    private double avg = 0;
    private int count = 0;
    private Handler mHandler;
    Recorder recorder;

    public SoundMeter(Context applicationContext, Handler handler){
        mContext = applicationContext;
        mHandler = handler;
        recorder = new Recorder(mContext);
        recorder.RecorderInit();
        new Timer().schedule(recordTimer, 10 * 1000);
    }


    TimerTask recordTimer = new TimerTask(){
        public void run() {
            flag = false;
        }
    };

    @Override
    public void run() {
        while (flag) {

            float sp = recorder.GetSoundPressure();
            sum = sum + sp;
            avg = (avg * count + sp) / (count + 1);
            count++;
        }
        recorder.RecorderRel();
        if (sum > 0.0F) {
            //db = (float)(20.0D * Math.log10(sum/count));
            db = 35 + (float)(20.0D * Math.log10(avg));
        } else {
            db = 35;
        }
//        if (db < 20) {
//            db = 20f;
//        } else if (db > 125){
//            db  = 125f;
//        }
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("DB", ((Float)db).toString());
        msg.setData(data);
        mHandler.sendMessage(msg);
    }


}
