package com.flightcomputer.utilities;

import com.flightcomputer.R;
import com.flightcomputer.R.raw;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
public class BeepThread implements Runnable, SoundPool.OnLoadCompleteListener {
    private boolean running;
    private double base = 1000.0;
    private double increment = 100.0;
    private SoundPool soundPool;
    private int numSounds = 2;
    private int soundsLoaded = 0;
    private int tone_1000;
    private int sink;
    private int tone_1000_stream;
    private int sink_stream;
    private Thread thread;
    private boolean beepOn=true;
    private double avgvario,sinkalarm;
    private double sinkBase = 500.0;
    private double sinkIncrement = 100.0;
    private PiecewiseLinearFunction cadenceFunction;
    public BeepThread(Context context,double var,double newsinkalarm) {  
    	cadenceFunction = new PiecewiseLinearFunction(new Point2d(0, 0.4763));
        cadenceFunction.addNewPoint(new Point2d(0.135, 0.4755));
        cadenceFunction.addNewPoint(new Point2d(0.441, 0.3619));
        cadenceFunction.addNewPoint(new Point2d(1.029, 0.2238));
        cadenceFunction.addNewPoint(new Point2d(1.559, 0.1565));
        cadenceFunction.addNewPoint(new Point2d(2.471, 0.0985));
        cadenceFunction.addNewPoint(new Point2d(3.571, 0.0741));
        
        running = true;
        soundPool = new SoundPool(numSounds, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        tone_1000 = soundPool.load(context, R.raw.tone_1000mhz, 1);
        sink = soundPool.load(context, R.raw.sink_tone500mhz, 1);
        avgvario=var;
        sinkalarm=newsinkalarm;
    }
    public void setAvgVario(double newavgvario) {
    	this.avgvario = newavgvario;    	
    }
    public void stop() {
    	this.beepOn = false; 	
    }  
    public void start() {
    	this.beepOn = true; 	
    }  
    public void run() {
        while (running) {
            try {
                while (beepOn) {                 	
                	if(avgvario>0.2)
                	{
                		 tone_1000_stream = soundPool.play(tone_1000, 1.0f, 1.0f, 0, -1, getRateFromTone1000(avgvario));//need to set rate to something other than 1.0f to start with for Android 4.1 based Nexus 7. Perhaps a bug?
                         Thread.sleep((int) (cadenceFunction.getValue(avgvario) * 1250));
                         soundPool.setVolume(tone_1000_stream, 0.0f, 0.0f);
                         Thread.sleep((int) (cadenceFunction.getValue(avgvario) * 1000));
                	}else if(avgvario<=-1*sinkalarm)
                	{
                		sink_stream = soundPool.play(sink, 1.0f, 1.0f, 0, -1, 0);
                		Thread.sleep((int) (600));
                		soundPool.setVolume(sink_stream, 0.0f, 0.0f);
                		Thread.sleep((int) (200));
                	}
                }
            } catch (InterruptedException e) {
                soundPool.stop(tone_1000_stream);
            }            
        }
        soundPool.stop(tone_1000_stream);
        soundPool.stop(sink_stream);
        soundPool.release();
        soundPool = null;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
        if (beepOn) {
            thread.interrupt();
        }
    }   
    public float getRateFromTone1000(double var) {
        double hZ = base + increment * var;
        float rate = (float) hZ / 1000.0f;
        if (rate < 0.5f) {
            rate = 0.5f;
        } else if (rate > 2.0f) {
            rate = 2.0f;
        } else if (rate == 1.0f) {
            rate = 1.0f + Float.MIN_VALUE;
        }
        return rate;
    }
    public float getRateFromTone500(double var) {
        double hZ = sinkBase + sinkIncrement * var;

        float rate = (float) hZ / 500.0f;
        if (rate < 0.5f) {
            rate = 0.5f;
        } else if (rate > 2.0f) {
            rate = 2.0f;
        } else if (rate == 1.0f) {
            rate = 1.0f + Float.MIN_VALUE;
        }
        return rate;


    }
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        soundsLoaded++;
        if (soundsLoaded == numSounds) {
            thread = new Thread(this);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }
    public synchronized void setBase(double base) {
        this.base = base;
    }

    public synchronized void setIncrement(double increment) {
        this.increment = increment;
    }

    public void onDestroy() {
        this.setRunning(false);
        thread = null;
    }


}