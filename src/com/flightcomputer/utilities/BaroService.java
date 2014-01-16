package com.flightcomputer.utilities;
import com.flightcomputer.FCActivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
public class BaroService extends Service implements SensorEventListener {
	private static final String TAG = BaroService.class.getSimpleName();
	Sensor mSensor;
	SensorManager mSensorManager;
	FCActivity activity_fc;
	private SensorManager sensorManager = null;
	    private Sensor sensor = null;
	    @SuppressLint("ShowToast")
		@Override
	    public int onStartCommand(Intent intent, int flags, int startId) {	    	
	        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
	        sensorManager.registerListener(this, sensor,1000000);	  	       
	        return START_STICKY;
	    }
	    @Override
		public IBinder onBind(Intent intent) {
			// TODO: Return the communication channel to the service.
			throw new UnsupportedOperationException("Not yet implemented");
		}
	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        // do nothing
	    }
	    @Override
	    public void onSensorChanged(SensorEvent event) {
	    	if (event.sensor.getType() != Sensor.TYPE_PRESSURE) return; 
	    	 Intent i = new Intent("android.intent.action.MAIN").putExtra("some_msg", String.valueOf(event.values[0]));
		     this.sendBroadcast(i);	    	 	    	 
	    }		   
	    @Override
	    public void onDestroy() {	        
	        sensorManager.unregisterListener(this);	    
   		    stopSelf();
	    }
	}	