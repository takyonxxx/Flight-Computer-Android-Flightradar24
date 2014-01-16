
package com.flightcomputer;
import com.flightcomputer.utilities.KalmanFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PressureAltimeterActivity extends Activity implements SensorEventListener {
// A reference to this activity, for use in anonymous classes.
	private PressureAltimeterActivity this_activity_;	
	// Handles for all of the moving parts of the altimeter.
	private ImageView altimeter_needle_100_;
	private ImageView altimeter_needle_1000_;
	private ImageView altimeter_needle_10000_;
	private ImageView altimeter_pressure_dial_;
	private Button Altdec,Altinc,Exit;
	// Handle for the debug text view.
	private TextView debugTextView_;
	
	// Pressure sensor and sensor manager.
	private Sensor sensor_pressure_;
	private SensorManager sensor_manager_;
	
	// Sea level pressure in inches of mercury.
	private double slp_inHg_;
	// Current measured pressure in millibars. It's aviation; you just have to
	// get used to ugly unit combos like this.
	private double pressure_hPa_;
	
	// Kalman filter for smoothing the measured pressure.
	private KalmanFilter pressure_hPa_filter_;
	// Time of the last measurement in seconds since boot; used to compute time
	// since last update so that the Kalman filter can estimate rate of change.
	private double last_measurement_time_;
	
	// Whether we've admonished the user not to use this app for flying planes.
	private boolean admonished_;
	// Whether we've told the user that they need a pressure sensor.
	private boolean pressured_;
	
	// Constants for the altitude calculation.
	// See http://psas.pdx.edu/RocketScience/PressureAltitude_Derived.pdf
	private static final double SLT_K = 288.15;  // Sea level temperature.
	private static final double TLAPSE_K_PER_M = -0.0065;  // Linear temperature atmospheric lapse rate.
	private static final double G_M_PER_S_PER_S = 9.80665;  // Acceleration from gravity.
	private static final double R_J_PER_KG_PER_K = 287.052;  // Specific gas constant for air, US Standard Atmosphere edition.
	
	// Constants for unit conversion.
	private static final double PA_PER_INHG = 3386;  // Pascals per inch of mercury.
	//private static final double FT_PER_M = 3.2808399;  // Feet per meter.
	private static final double FT_PER_M = 1;  // Feet per meter.
	// Constants for the Kalman filter's noise models. These values are bigger
	// than actual noise recovered from data, but that's because that noise
	// looks more Laplacian than anything.
	private static final double KF_VAR_ACCEL = 0.0075;  // Variance of pressure acceleration noise input.
	private static final double KF_VAR_MEASUREMENT = 0.05;  // Variance of pressure measurement noise.
	
	// Constants for identifying dialogs.
	private static final int DIALOG_ADMONITION = 0;  // Don't use this to fly!
	private static final int DIALOG_PRESSURE = 1;  // Need a pressure sensor.
	private boolean touchinc=false,touchdec=false;
    /** Create activity, init objects, and get handles to various Droidly bits. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        // Set the main layout as the content view.
        setContentView(R.layout.altimeter);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Create our Kalman filter.
        pressure_hPa_filter_ = new KalmanFilter(KF_VAR_ACCEL);       
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("slp_inHg_");
            if (value.length() != 0)				
			{
				slp_inHg_=Double.parseDouble(value);						
			}
        }  
        // Obtain handles for all of the moving parts of the altimeter.
        altimeter_needle_100_ = (ImageView) findViewById(R.id.altimeter_needle_100);
        altimeter_needle_1000_ = (ImageView) findViewById(R.id.altimeter_needle_1000);
        altimeter_needle_10000_ = (ImageView) findViewById(R.id.altimeter_needle_10000);
        altimeter_pressure_dial_ = (ImageView) findViewById(R.id.altimeter_pressure_dial);
        
        // Obtain handle for the debug text view.
        debugTextView_ = (TextView) findViewById(R.id.debugText);

        // Obtain sensor manager and pressure sensor.
        sensor_manager_ = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor_pressure_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_PRESSURE);
        debugTextView_.setVisibility(View.VISIBLE);
        
        Exit = (Button) findViewById(R.id.exit);
        Exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {	
				Intent intent = new Intent();
	     		intent.putExtra("slp_inHg_", String.valueOf(slp_inHg_));
	     		setResult(RESULT_OK, intent);
	     		finish();
			}
		 }        		
        );
        Altdec = (Button) findViewById(R.id.decalt);
        Altdec.setOnTouchListener(
        		new Button.OnTouchListener() {
        			public boolean onTouch(View v, MotionEvent m) {        				
        				if(m.getAction() == MotionEvent.ACTION_UP){
        					touchdec = false;
              		    }else if(m.getAction() == MotionEvent.ACTION_DOWN){
              		    	touchdec = true;
              		    } 
        			    return true;
        			}
        		}
        		
        );
        Altinc = (Button) findViewById(R.id.incalt);
        Altinc.setOnTouchListener(
        		new Button.OnTouchListener() {
        			public boolean onTouch(View v, MotionEvent m) {        				
        				if(m.getAction() == MotionEvent.ACTION_UP){
        					touchinc = false;
              		    }else if(m.getAction() == MotionEvent.ACTION_DOWN){
              		    	touchinc = true;
              		    } 
        			    return true;
        			}
        		}
        		
        );
    }  
    /** On resume, set up the display and start listening to the pressure sensor. */
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Update our self-reference.
    	this_activity_ = this;   	
	
    	// If we have started for the first time or have restored from corrupt
    	// state information, the altimeter setting will be bogus. Fall back
    	// on the standard day.
        if (slp_inHg_ < 28.1 || slp_inHg_ > 31.0) slp_inHg_ = 29.92;
        
        // Likewise, we set the pressure reading to standard day sea level. It
        // should get overwritten by the sensor right away, but if we don't
        // have a pressure sensor, a nice zero indication will show up instead.
        pressure_hPa_ = 1013.0912;
    	
        // We reset the Kalman filter with that same pressure value, then mark
        // now as the time of the last measurement.
        pressure_hPa_filter_.reset(pressure_hPa_);
        last_measurement_time_ = SystemClock.elapsedRealtime() / 1000.;
        
    	// Immediately update the hands and the pressure dial to reflect
    	// initial or saved state.
        updatePressureDial();
        updateNeedles();      
    	
    	// Start listening to the pressure sensor.
    	if (sensor_pressure_ != null) {
    		sensor_manager_.registerListener(this, sensor_pressure_, SensorManager.SENSOR_DELAY_GAME);
    	} else if (!pressured_) {
    		showDialog(DIALOG_PRESSURE);
    		pressured_ = true;
    	}    	
    }

    /** On pause, stop listening to sensors. */
    @Override
    public void onPause() {
    	super.onPause();
    	sensor_manager_.unregisterListener(this);
    }
    
    /** Save the current altimeter setting, etc. during interruptions. */
    protected void onSaveInstanceState(Bundle bundle) {
    	super.onSaveInstanceState(bundle);
    	bundle.putDouble("slp_inHg", slp_inHg_);
    	bundle.putBoolean("admonished", admonished_);
    	bundle.putBoolean("pressured", pressured_);
    }
    
    /** Retrieve the current altimeter setting, etc. after interruptions. */
    protected void onRestoreInstanceState(Bundle bundle) {
    	super.onRestoreInstanceState(bundle);
    	slp_inHg_ = bundle.getDouble("slp_inHg");
    	admonished_ = bundle.getBoolean("admonished");
    	pressured_ = bundle.getBoolean("pressured");
    }

   
    public boolean onKeyDown(int key_code, KeyEvent key_event) {
    	if (key_code == KeyEvent.KEYCODE_BACK) {
    		Intent intent = new Intent();
     		intent.putExtra("slp_inHg_", String.valueOf(slp_inHg_));
     		setResult(RESULT_OK, intent);
     		finish();
			return true;
		}    	
    	return true;
    }
   
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Does nothing, bummer.
	}

	public void onSensorChanged(SensorEvent event) {
		if(touchinc == true){
			long slp_inHg_long = Math.round(100.0 * slp_inHg_);
			if (slp_inHg_long < 3100) ++slp_inHg_long;	
	    	slp_inHg_ = slp_inHg_long / 100.0;
	    	updatePressureDial();
	    	updateNeedles();	
        }else if(touchdec == true)
        {
        	long slp_inHg_long = Math.round(100.0 * slp_inHg_);
			if (slp_inHg_long > 2810) --slp_inHg_long;	 
	    	slp_inHg_ = slp_inHg_long / 100.0;		        	
	    	updatePressureDial();
	    	updateNeedles();
        }
		// Update current measured pressure.
		if (event.sensor.getType() != Sensor.TYPE_PRESSURE) return;  // Should not occur.
		pressure_hPa_ = event.values[0];
		// Update the Kalman filter.
		final double curr_measurement_time = SystemClock.elapsedRealtime() / 1000.;
		final double dt = curr_measurement_time - last_measurement_time_;
        pressure_hPa_filter_.update(pressure_hPa_, KF_VAR_MEASUREMENT, dt);
		last_measurement_time_ = curr_measurement_time;
		// Update the needles.
		updateNeedles();
	}
   private static double hPaToFeet(double slp_inHg, double pressure_hPa) {
    	// Algebraically unoptimized computations---let the compiler sort it out.
    	double factor_m = SLT_K / TLAPSE_K_PER_M;
    	double exponent = -TLAPSE_K_PER_M * R_J_PER_KG_PER_K / G_M_PER_S_PER_S;    	
    	double current_sea_level_pressure_Pa = slp_inHg * PA_PER_INHG;
    	double altitude_m =
    			factor_m *
    			(Math.pow(100.0 * pressure_hPa / current_sea_level_pressure_Pa, exponent) - 1.0);
    	return FT_PER_M * altitude_m;
    }
	
	// Retrieve the fractional part of a double.
	private static double getFractional(double value) {
		return value - ((long) value);
	}
	
	/** Update the positions of the altimeter's three indicator needles to
	 *  the current altitude in feet. */
	private void updateNeedles() {
		// Compute current altitude in feet given Kalman-filtered pressure.
		double altitude_ft = hPaToFeet(slp_inHg_, pressure_hPa_filter_.getXAbs());

		// Determine angular orientation of the needles.
		double angle_needle_100 = 360.0 * getFractional(altitude_ft / 1000.0);
		double angle_needle_1000 = 360.0 * getFractional(altitude_ft / 10000.0);
		double angle_needle_10000 = 360.0 * getFractional(altitude_ft / 100000.0);
		
		// Set angular orientation of the needles.
		altimeter_needle_100_.setRotation((float) angle_needle_100);
		altimeter_needle_1000_.setRotation((float) angle_needle_1000);
		altimeter_needle_10000_.setRotation((float) angle_needle_10000);
		
		// Update debugging text.
		// TODO: only if visible
		if (debugTextView_.getVisibility() == View.VISIBLE) {
			debugTextView_.setText(String.format(
					"Altitude: %4.1f m\n" + "Raw baro: %4.3f hPa (mb)\n" + "Filtered: %4.3f hPa (mb)\n"  ,
					hPaToMeter(slp_inHg_,pressure_hPa_filter_.getXAbs()),
					pressure_hPa_,
					pressure_hPa_filter_.getXAbs()));
		}
	}
	 private static double hPaToMeter(double slp_inHg, double pressure_hPa) {
		  	// Algebraically unoptimized computations---let the compiler sort it out.
		  	double factor_m = SLT_K / TLAPSE_K_PER_M;
		  	double exponent = -TLAPSE_K_PER_M * R_J_PER_KG_PER_K / G_M_PER_S_PER_S;    	
		  	double current_sea_level_pressure_Pa = slp_inHg * PA_PER_INHG;
		  	double altitude_m =
		  			factor_m *
		  			(Math.pow(100.0 * pressure_hPa / current_sea_level_pressure_Pa, exponent) - 1.0);
		  	return altitude_m;
		  }
	/** Rotate the pressure setting dial to reflect the actual value of slp_inHg_. */
	private void updatePressureDial() {
	    double degrees = 100.0 * (31.0 - slp_inHg_);
	    altimeter_pressure_dial_.setRotation((float) degrees);
	}
	
	/** Create dialogs, mainly to admonish and pressure the user. */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_PRESSURE: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.alert_pressure)
			       .setCancelable(false)
			       .setPositiveButton(R.string.alert_pressure_ack, new DialogInterface.OnClickListener() {
			    	   public void onClick(DialogInterface dialog, int id) {
			    		   dialog.dismiss();
			    	   }
			       })
				   .setNegativeButton(R.string.alert_pressure_abort, new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				    	   this_activity_.finish();
				       }
			       });
			dialog = builder.create();
			break;
		}
	    default:
		    dialog = null;
		}
		return dialog;
	}
}