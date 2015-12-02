package com.example.yuan.classes;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by yuan on 11/13/15.
 */
public class SoundMeter implements Runnable{

    public Handler mHandler;
    public double calibration = 4;

    private double decibel = 0;
    private File audioFile;
    private boolean isRecording=true;
    private int frequence = 36000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    public SoundMeter(Handler mHandler, double calibration){
        this.calibration = calibration;
        this.mHandler = mHandler;

        //在这里我们创建一个文件，用于保存录制内容
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.example.yuan.classes/cache/");
        fpath.mkdirs();//创建文件夹
        try {
            //创建临时文件,注意这里的格式为.pcm
            audioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        isRecording = true;

        try {
            //开通输出流到指定的文件
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
            //根据定义好的几个配置，来获取合适的缓冲大小
            int bufferSize = 32768;//AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
            Log.d("Record", "bufferSize = " + bufferSize);
            //实例化AudioRecord
            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, frequence, channelConfig, audioEncoding, bufferSize);
            //定义缓冲
            short[] buffer = new short[bufferSize];
            //开始录制
            record.startRecording();

            //10秒后停止录音
            new Timer().schedule(new TimerTask() {
                public void run() {
                    isRecording = false;
                }
            }, 10000);

            //定义循环，根据isRecording的值来判断是否继续录制
            while(isRecording){
                //从bufferSize中读取字节，返回读取的short个数
                //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                int bufferReadResult = record.read(buffer, 0, buffer.length);
                double[] toTransform = new double[bufferReadResult];
                //循环将buffer中的音频数据写入到OutputStream中
                Log.d("Record", "bufferReadResult = " + bufferReadResult);
                for(int i=0; i<bufferReadResult; i++){
                    toTransform[i] = calibration * (double) buffer[i] / 32768.0;
                    dos.writeShort(buffer[i]);
                }
                new Thread(new Meter(bufferReadResult, toTransform, frequence)).start();
                //
            }
            //录制结束
            record.stop();
            record.release();
            dos.close();
            Thread.sleep(100);
        } catch (Exception e) {
            // TODO: handle exception
        }

        Message msg = new Message();
        Bundle data = new Bundle();
        decibel = 10 * Math.log10(decibel);
        data.putDouble("dBA", decibel);
        msg.setData(data);
        mHandler.sendMessage(msg);

    }


    class Meter implements Runnable {

        int bufferSize;
        double[] audioBuffer;
        float sampleRate;

        Meter(int bufferSize, double[] audioBuffer, float sampleRate){
            this.bufferSize = bufferSize;
            this.audioBuffer = audioBuffer;
            this.sampleRate = sampleRate;
        }

        @Override
        public void run() {
            decibel  += Math.pow(10, 0.1 * calDecibel());
        }

        private double calDecibel() {

            final int nFreq = 30;
            double dBA = 0;
            double[] f0 = {20.0, 25.0, 31.5, 40.0, 50.0, 63.0, 80.0, 100.0, 125.0, 160.0,
                    200.0, 250.0, 315.0, 400.0, 500.0, 630.0, 800.0, 1000.0, 1250.0, 1600.0,
                    2000.0, 2500.0, 3150.0, 4000.0, 5000.0, 6300.0, 8000.0, 10000.0, 12500.0, 16000.0};
            double[] cf = {-50.5, -44.7, -39.4, -34.6, -30.2, -26.2, -22.5, -19.1, -16.1, -13.4,
                    -10.9, -8.6, -6.6, -4.8, -3.2, -1.9, -0.8, 0, 0.6, 1.0,
                    1.2, 1.3, 1.2, 1.0, 0.5, -0.1, -1.1, -2.5, -4.3, -6.6};
            double[] sp = new double[30];
            double[] dB = new double[30];
            double[] freqBuffer = new double[bufferSize];
            double window;
            double arg = 2.0 * Math.PI / (double) (bufferSize - 1);
            for (int i = 0; i < bufferSize; i++) {
                window = (0.5 - 0.5 * Math.cos(arg * (double) i));
                audioBuffer[i] = 1.633 * audioBuffer[i] * window;
            }


            int FFT_SIZE = bufferSize;
            DoubleFFT_1D mFFT = new DoubleFFT_1D(FFT_SIZE); //this is a jTransforms type
            mFFT.realForward(audioBuffer);

            for (int i = 0; i < nFreq; i++) {

                double freqMin = f0[i] / 1.1225;
                double freqMax = f0[i] * 1.1225;
                int nl = (int) Math.round(freqMin * FFT_SIZE / sampleRate);
                int nu = (int) Math.round(freqMax * FFT_SIZE / sampleRate);
                for (int j = nl; j <=nu; j++){
                    if(nu > FFT_SIZE / 2)
                        break;
                    freqBuffer[2*j] = audioBuffer[2*j];
                    freqBuffer[2*j+1] = audioBuffer[2*j+1];
                }
                //Take the inverse FFT to convert signal from frequency to time domain
                mFFT.realInverse(freqBuffer, true);
                sp[i] = Math.sqrt(var(freqBuffer));
                dB[i] = 20 * Math.log10(sp[i] / 0.00002d) + cf[i];
                //dB[i] = 20*Math.log10(sp[i] / 0.0000000001d);
            }
            for (int i = 0; i < nFreq; i++) {
                dBA += Math.pow(10, 0.1 * dB[i]);
            }
            dBA = 10 * Math.log10(dBA);
            return dBA;
        }

        private double var(double[] a) {
            double var = 0;
            double mean = 0;
            int n = a.length;
            for (int i = 0; i < n; i++) {
                mean += a[i];
            }
            mean = mean / n;
            for (int i = 0; i < n; i++) {
                var += (a[i] - mean) * (a[i] - mean);
            }
            return var / n;
        }
    }
}
