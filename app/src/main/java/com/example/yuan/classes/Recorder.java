package com.example.yuan.classes;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import com.example.yuan.map4loud.R;

import java.io.IOException;

/**
 * Created by Admin on 13-9-10.
 */
public class Recorder {

    private MediaRecorder mRecorder = null;
    private Context mContext;

    public Recorder(Context applicationContext) {
        mContext = applicationContext;
    }

    public void RecorderInit()  {

        if (mRecorder != null)
            return;

        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(1);
            mRecorder.setOutputFormat(1);
            mRecorder.setAudioEncoder(1);
            mRecorder.setMaxDuration(12*1000);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            RecorderErr();
        }
        catch (IOException e) {
            e.printStackTrace();
            RecorderErr();
        }
        catch (Exception e) {
            e.printStackTrace();
            RecorderErr();
        }
        return;
    }

    private void RecorderErr() {
        mRecorder = null;
        Toast.makeText(mContext, mContext.getString(R.string.msg_mic_error), Toast.LENGTH_SHORT).show();
    }

    public void RecorderRel() {

        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public float GetSoundPressure() {
        float f1 = mRecorder.getMaxAmplitude();
        return f1;
    }


}
