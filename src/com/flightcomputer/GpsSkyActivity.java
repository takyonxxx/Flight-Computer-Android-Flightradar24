package com.flightcomputer;
import java.util.Iterator;
import com.flightcomputer.utilities.GpsSkyView;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
public class GpsSkyActivity extends Activity  implements LocationListener, GpsStatus.Listener {
	private Resources mRes;
    private GpsSkyView mSkyView;   
    private LocationManager locationManager = null;
    private GridView GpsInfo=null;
    private SvGridAdapter mAdapter;
    
    private static final int PRN_COLUMN = 0;
    private static final int SNR_COLUMN = 1;
    private static final int ELEVATION_COLUMN = 2;
    private static final int AZIMUTH_COLUMN = 3;
    private static final int COLUMN_COUNT = 4;

    private int mSvCount;
    private int mPrns[];
    private float mSnrs[];
    private float mSvElevations[];
    private float mSvAzimuths[];
       
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mRes = getResources();
        setContentView(R.layout.activity_gps_status);
        mSkyView = (GpsSkyView)findViewById(R.id.gpsview);     
        GpsInfo = (GridView)findViewById(R.id.gpsinfo);
        mAdapter = new SvGridAdapter(this);
        GpsInfo.setAdapter(mAdapter);
        GpsInfo.setFocusable(false);
        GpsInfo.setFocusableInTouchMode(false);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);	                   
    }
    @Override
    protected void onResume()
    {
        super.onResume();       
        Criteria myCriteria = new Criteria();
		myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationManager.requestLocationUpdates(0L, // minTime
				0.0f, // minDistance
				myCriteria, // criteria
				this, // listener
				null); // looper
        locationManager.addGpsStatusListener(this);	      
        setStarted(true);    
    }
    
    private void setStarted(boolean navigating) {       
            if (navigating) {
            	 mSvCount = 0;
                 mAdapter.notifyDataSetChanged();
            } 
   }

    @Override
    protected void onPause()
    {
    	locationManager.removeGpsStatusListener(this);
    	locationManager.removeUpdates(this);
        super.onStop();
    }

    @Override
	public void onGpsStatusChanged(int arg0) {
		// TODO Auto-generated method stub
		GpsStatus gpsstatus = locationManager.getGpsStatus(null);    
    	 switch (arg0) {
         case GpsStatus.GPS_EVENT_STARTED:
            mSkyView.setStarted();
            setStarted(true);   
             break;
         case GpsStatus.GPS_EVENT_STOPPED:
            mSkyView.setStopped();
            setStarted(false);   
             break;

         case GpsStatus.GPS_EVENT_SATELLITE_STATUS:        	 
             mSkyView.setSats(gpsstatus);
             updateStatus(gpsstatus);
             break;
         case GpsStatus.GPS_EVENT_FIRST_FIX:           	        
            break;
     }
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub	
		mSkyView.setRotate(location.getBearing());
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	  private void updateStatus(GpsStatus status) {
	        setStarted(true);	        
	        Iterator<GpsSatellite> satellites = status.getSatellites().iterator();
	        if (mPrns == null) {
	            int length = status.getMaxSatellites();
	            mPrns = new int[length];
	            mSnrs = new float[length];
	            mSvElevations = new float[length];
	            mSvAzimuths = new float[length];
	        }
	        mSvCount = 0;	       
	        while (satellites.hasNext()) {
	            GpsSatellite satellite = satellites.next();
	            int prn = satellite.getPrn();
	            mPrns[mSvCount] = prn;
	            mSnrs[mSvCount] = satellite.getSnr();
	            mSvElevations[mSvCount] = satellite.getElevation();
	            mSvAzimuths[mSvCount] = satellite.getAzimuth();	            
	            mSvCount++;
	        }
	        mAdapter.notifyDataSetChanged();
	    }
	private class SvGridAdapter extends BaseAdapter {
        public SvGridAdapter(Context c) {
            mContext = c;
        }
        public int getCount() {
            // add 1 for header row
            return (mSvCount + 1) * COLUMN_COUNT;
        }

        public Object getItem(int position) {           
            return "foo";
        }

        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(mContext);
            } else {
                textView = (TextView) convertView;
            }
            int row = position / COLUMN_COUNT;
            int column = position % COLUMN_COUNT;
            CharSequence text = null;

            if (row == 0) {
                switch (column) {
                    case PRN_COLUMN:
                        text = mRes.getString(R.string.gps_prn_column_label);
                        break;
                    case SNR_COLUMN:
                        text = mRes.getString(R.string.gps_snr_column_label);
                        break;
                    case ELEVATION_COLUMN:
                        text = mRes.getString(R.string.gps_elevation_column_label);
                        break;
                    case AZIMUTH_COLUMN:
                        text = mRes.getString(R.string.gps_azimuth_column_label);
                        break;                   
                }
            } else {
                row--;
                switch (column) {
                    case PRN_COLUMN:
                        text = Integer.toString(mPrns[row]);
                        break;
                    case SNR_COLUMN:
                        text = Float.toString(mSnrs[row]);
                        break;
                    case ELEVATION_COLUMN:
                        text = Float.toString(mSvElevations[row]);
                        break;
                    case AZIMUTH_COLUMN:
                        text = Float.toString(mSvAzimuths[row]);
                        break;                   
                }
           }
           textView.setText(text);
            return textView;
        }
        private Context mContext;
    }
}
