/* Türkay Biliyor turkaybiliyor@hotmail.com*/
package com.flightcomputer;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.core.GeoPoint;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.flightcomputer.filefilter.FilterByFileExtension;
import com.flightcomputer.filepicker.FilePicker;
import com.flightcomputer.utilities.BeepThread;
import com.flightcomputer.utilities.KalmanFilter;
import com.flightcomputer.utilities.VerticalProgressBar;
import com.flightcomputer.utilities.VerticalProgressBar_Reverse;
import com.nutiteq.MapView;
import com.nutiteq.components.Components;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.Options;
import com.nutiteq.geometry.Line;
import com.nutiteq.geometry.Marker;
import com.nutiteq.layers.Layer;
import com.nutiteq.log.Log;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.rasterlayers.TMSMapLayer;
import com.nutiteq.style.LabelStyle;
import com.nutiteq.style.LineStyle;
import com.nutiteq.style.MarkerStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.ui.DefaultLabel;
import com.nutiteq.ui.Label;
import com.nutiteq.utils.MapsforgeMapLayer;
import com.nutiteq.utils.UnscaledBitmapLoader;
import com.nutiteq.vectorlayers.GeometryLayer;
import com.nutiteq.vectorlayers.MarkerLayer;
public class FCActivity extends Activity {
	public MapView mapView;
	private RouteLine routeline=new RouteLine();
	private TrckLine trckline=new TrckLine();
	private Circle circle=new Circle();
	private int radarrange=0,radaralt=0,soundtype=2;;
	private LocationManager locationManager=null;
	private LocationListener locationListener=null;	
	private View textEntryView=null;
	ArrayList<String> igclog = new ArrayList<String>();
	ArrayList<String> trcktime= new ArrayList<String>();
	ArrayList<String> taskline = new ArrayList<String>();
	ArrayList<MapPos> routePoints =  new ArrayList<MapPos>();
	ArrayList<MapPos> trackPoints =  new ArrayList<MapPos>();
	ArrayList<MapPos> circlePoints =  new ArrayList<MapPos>();
	ArrayList<Integer> colorTrck = new ArrayList<Integer>();
	ArrayList<Integer> colorRoute = new ArrayList<Integer>();	
	ArrayList<Integer> colorTask = new ArrayList<Integer>();
	ArrayList<Double> diameter= new ArrayList<Double>();	 
	private MapPos routepos=null;   
	private static int ActiveTaskPoint = 0;	
	private final static int SETALT = 2;
	private final static int IGCFILE = 1;
	private final static int MAPFILE=0;
	private TextView AltTxt, SpeedTxt,LatTxt,LonTxt,DTkf,AvgVarTxt,TrackTxt,TimeStartTxt,TimeGpsTxt,DistEdgeTxt;
	private VerticalProgressBar_Reverse avgprogbar_reverse;
	private VerticalProgressBar avgprogbar;
	private ToggleButton varioOnOff;
	private AudioManager audio;
	private static boolean gps=false,gpsrunning=false,barometer=false,getfirstfix=false,drawmultitrack=false,
			getlastposition=false,gps_enabled=false,
			igcstart=false,logheader=false,enablelog=false,highress=false,
			logfooter=false,vario=false,threeaxis=false,drawgpstrack=false,existtask=false,radarready=false,createigcrunning=false;
	
	private int speed = 0, bearing = 0, acc = 0, gpsaltitude = 0,intalt=0;
	int satellites=0;
	private int currentdump=25;
	private int inttrackfactor=5,intdrawfactor=1;
	private int intlogtime=3000;
	private int trackcount=0;
	private long gpstime = 0;
	private double baroaltitude = 0,avgvario=0,altdiff=0,sinkalarm=2.5, 
			latitude=0, longitude=0, startlatitude=0, startlongitude=0,oldaltbaro=0,oldaltgps=0;
	private String pilotname, pilotid,logfilename = null,mapPath=null;
	private float zoomlevel=14,linewidth=0.05f,gllinewidth=5.0f,tiltlevel=0.0f,trckvario=0;
	private MarkerLayer trckmarkerLayer=null;
	private MarkerLayer trckclrmarkerLayer=null;
	private MarkerLayer routemarkerLayer=null;
	private MarkerLayer pointmarkerLayer=null;	
	private MarkerLayer taskmarkerLayer=null;	
	private MarkerLayer radarmarkerLayer=null;	
	private GeoPoint lastcoord=null;
	private Layer baselayer=null;
	private double slp_inHg_=29.92;	
	private double pressure_hPa_= 1013.0912;		
	private KalmanFilter pressure_hPa_filter_;
	private double last_measurement_time_;	
	private static final double SLT_K = 288.15;  // Sea level temperature.
	private static final double TLAPSE_K_PER_M = -0.0065;  // Linear temperature atmospheric lapse rate.
	private static final double G_M_PER_S_PER_S = 9.80665;  // Acceleration from gravity.
	private static final double R_J_PER_KG_PER_K = 287.052;  // Specific gas constant for air, US Standard Atmosphere edition.	
	private static final double PA_PER_INHG = 3386;  // Pascals per inch of mercury.
	private static final double FT_PER_M = 3.2808399;  // Feet per meter.
	private static final double KF_VAR_ACCEL = 0.0075;  // Variance of pressure acceleration noise input.
	private static final double KF_VAR_MEASUREMENT = 0.05;  // Variance of pressure measurement noise.		
	MapEventListener mapListener = null;	
	private Projection proj=new EPSG3857();
	private BeepThread beeps=null;
	private Handler loghandler = new Handler();	
	private Handler radarhandler = new Handler();	
	private float startalttrck=0,markersize=0.5f;
	private MarkerLayer searchMarkerLayer;
	private static Marker searchResult;
	private Sensor sensor_pressure_;
	private SensorManager sensor_manager_;
	private SensorEventListener mSensorListener;
	private String startTime="00:00",gpsTime="00:00",endTime="00:00",radarregion="europe";
	private int active=0;
	private boolean startok=false,wasincircle=false,wasoutcircle=false,gpscheck=false;
	private PowerManager.WakeLock wl;
	GeometryLayer geomLayer_route = new GeometryLayer(proj);
	BufferedReader bufferedReader;
	DefaultHttpClient httpclient;    
	String jstring;  
	static InputStream is = null;
	static JSONObject jObj = null;	
	JSONArray nameArray;
	public List<Marker> radarMarkerlist =new ArrayList<Marker>();
	LabelStyle labelStyle;
	ArrayList<Double> radardist= new ArrayList<Double>();
	static PositionWriter liveWriter;
	static String username,password,serverUrl,stime,errorinfo,wingmodel;
	static boolean loginLW=false,error=false,livetrackenabled=false;
	static int vechiletype=1,LWcount=0,type=0;
	static Context basecontext;
	@SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		wl.acquire();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);       
        setContentView(R.layout.activity_main); 
       
        PackageManager PM = getPackageManager();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		gps = PM.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);		
		barometer = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
		try { if( Build.VERSION.SDK_INT >= 11) getWindow().setFlags( 16777216, 16777216); } catch( Exception e) {} 
		   // ZoomInfo = (TextView) findViewById(R.id.zoominfo);
			AltTxt = (TextView) findViewById(R.id.altitude);
			SpeedTxt = (TextView) findViewById(R.id.speed);
			LatTxt = (TextView) findViewById(R.id.lat);
			LonTxt = (TextView) findViewById(R.id.lon);
			DTkf = (TextView) findViewById(R.id.disttakeoff);	
			AvgVarTxt=(TextView) findViewById(R.id.avgvario);
			TrackTxt=(TextView) findViewById(R.id.trackcount);
			DistEdgeTxt=(TextView) findViewById(R.id.textDistEdge);
			TimeStartTxt=(TextView) findViewById(R.id.textStartTime);
			TimeGpsTxt=(TextView) findViewById(R.id.textGpsTime);
			varioOnOff = (ToggleButton) findViewById(R.id.toggle_vario);			
			avgprogbar_reverse=(VerticalProgressBar_Reverse) findViewById(R.id.avgprogbar_reverse);
			avgprogbar=(VerticalProgressBar) findViewById(R.id.avgprogbar);	
			
			varioOnOff.setVisibility(View.INVISIBLE);			
			AvgVarTxt.setVisibility(View.INVISIBLE);
			avgprogbar.setVisibility(View.INVISIBLE);			
			avgprogbar_reverse.setVisibility(View.INVISIBLE);			
			
			trckmarkerLayer = new MarkerLayer(proj);   
			trckclrmarkerLayer= new MarkerLayer(proj); 
			routemarkerLayer = new MarkerLayer(proj); 
			pointmarkerLayer = new MarkerLayer(proj); 	
			taskmarkerLayer=new MarkerLayer(proj); 	
			radarmarkerLayer=new MarkerLayer(proj); 	
			initMap();	
			if(gps)	
			{				
				initGps();			
			}
			if(barometer){	
				varioOnOff.setVisibility(View.VISIBLE);				
				startSensor();
			}	
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());	
			String strlogtime=preferences.getString("logtime", "3000");		
			intlogtime=Integer.parseInt(strlogtime);				
			loghandler.postDelayed(logrunnable, intlogtime);	
			 
		varioOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {				
				if(!vario && barometer)
				{							        	          
					vario=true;	
					variohandler.postDelayed(variorunnable, 1000);					
					if(beeps==null)
			        	beeps = new BeepThread(FCActivity.this);
					if(beeps!=null)
					   beeps.start(getApplicationContext(),soundtype,sinkalarm);					
					 if (sensor_pressure_ != null) {
				    		sensor_manager_.registerListener(mSensorListener, sensor_pressure_, 1000000);
				    	
					AvgVarTxt.setVisibility(View.VISIBLE);
					avgprogbar.setVisibility(View.VISIBLE);
					avgprogbar_reverse.setVisibility(View.VISIBLE);
					Toast.makeText(getApplicationContext(),
							"Vario Enabled", Toast.LENGTH_SHORT).show();}
				}else
				{	
					vario=false;
					variohandler.removeCallbacks(variorunnable);					
					beeps.stop();					
					 if (sensor_pressure_ != null) {
				    		sensor_manager_.unregisterListener(mSensorListener);
				    	}	
					AvgVarTxt.setVisibility(View.INVISIBLE);
					avgprogbar.setVisibility(View.INVISIBLE);
					avgprogbar_reverse.setVisibility(View.INVISIBLE);
					Toast.makeText(getApplicationContext(),
							"Vario Disabled", Toast.LENGTH_SHORT).show();
				}		
			}
		});				
		 Display display = getWindowManager().getDefaultDisplay();
	       Point size = new Point();
	       display.getSize(size);
	       int width = size.x;
	       int height = size.y;
	       if(width>1300 || height>1300)
	       {
	    	   highress=true;
			   labelStyle = LabelStyle.builder()
	   			.setEdgePadding(15)
	   			.setLinePadding(15)
	   			.setTitleFont(Typeface.create("Arial", Typeface.BOLD), 30)
	   			.setDescriptionFont(Typeface.create("Arial", Typeface.BOLD), 28)
	   			.setBackgroundColor(Color.BLACK)
	   			.setTitleColor(Color.WHITE)
	   			.setDescriptionColor(Color.WHITE)
	   			.setBorderColor(Color.LTGRAY)
	   			.build();
			   markersize=0.9f;
	       }else
		   {
	    	   highress=false;
			   labelStyle = LabelStyle.builder()
	   			.setEdgePadding(15)
	   			.setLinePadding(15)
	   			.setTitleFont(Typeface.create("Arial", Typeface.BOLD), 15)
	   			.setDescriptionFont(Typeface.create("Arial", Typeface.BOLD), 14)
	   			.setBackgroundColor(Color.BLACK)
	   			.setTitleColor(Color.WHITE)
	   			.setDescriptionColor(Color.WHITE)
	   			.setBorderColor(Color.LTGRAY)
	   			.build();      
			   markersize=0.4f;
		   }
    }	
    @Override
	protected void onResume() {        	
		super.onResume();			
		startProperties();		
		 if(!enablelog)
		  {			  
			  if(igcstart)
			  {
				  callIgcFunc("normal");
			  }	
		  }			
		  if(!livetrackenabled && loginLW)
			{				  
				setLivePos emitPos = new setLivePos();
				emitPos.execute(3);								 
				}	
		existtask=existsTask();			
		if (searchResult != null && searchResult.getMapPos().x != 0) {
	            searchMarkerLayer.add(searchResult);
	            mapView.setFocusPoint(searchResult.getMapPos());
	            //searchResult.setVisible(true);
	            mapView.selectVectorElement(searchResult);
	            }		
		if(!getlastposition && gps)
		{
			gotoLastKnownPosition();		
			getlastposition=true;
		}	
		DistEdgeTxt.setText("DisEdg: 0 Km");
		if(existtask)
		{			
			gettask();	
			gettasktime();		
			drawTaskMarkers();
			ActiveTaskPoint=getActivePoint(null);
			mapView.selectVectorElement(taskMarkerlist.get(ActiveTaskPoint));
			drawTask(ActiveTaskPoint);				
		}else
		{	
			taskMarkerlist.clear();
			taskline.clear();
			circlePoints.clear();	
			taskmarkerLayer.clear();
		}		
		if(gps & !gpscheck)
		checkLocationProviders();		
	} 
    public void gettask()
    {	
    	taskline.clear();
		String sFileName="task.txt";
		String line = null;
		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
		File taskfile = new File(root, sFileName);
		if (taskfile.exists()) 
		{				
    		   BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(taskfile));				
					 while ((line = br.readLine()) != null) {							
						 taskline.add(line); 						 
					 }		
    		    		 br.close();        		
    		    	}
				 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();}
		} 
    }  
	private void getFlightData(String url)
	   {		
		getFlightRadar task1 = new getFlightRadar(this);
		task1.execute(url);			
	   }
   
    public void startSensor()
    {
    	pressure_hPa_filter_ = new KalmanFilter(KF_VAR_ACCEL);   				
		sensor_manager_ = (SensorManager) getSystemService(SENSOR_SERVICE);
	    sensor_pressure_ = sensor_manager_.getDefaultSensor(Sensor.TYPE_PRESSURE);
	        pressure_hPa_ = 1013.0912;		
	        pressure_hPa_filter_.reset(pressure_hPa_);
	        last_measurement_time_ = SystemClock.elapsedRealtime() / 1000.;	 
	    	// Start listening to the pressure sensor.		    	  			 	
			mSensorListener = new SensorEventListener() {
			    	@Override
			        public void onSensorChanged(SensorEvent event) {		
			    		pressure_hPa_=event.values[0];
			        }
			        public void onAccuracyChanged(Sensor sensor, int accuracy) {
			        }				   
			    };	 
    }
    public void startProperties() {	
    	basecontext=getBaseContext();
		//serverUrl="http://test.livetrack24.com/";
		serverUrl="http://www.livetrack24.com/";
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());		
		threeaxis = preferences.getBoolean("checkbox3d", false);
		drawgpstrack = preferences.getBoolean("checkboxgpstrack", false);
		drawmultitrack = preferences.getBoolean("checkboxmulti", false);		
		enablelog = preferences.getBoolean("checkboxigcrecord", false);
		livetrackenabled = preferences.getBoolean("livetrackenabled", false);			 
		radarregion= preferences.getString("radarregion", "full");			
		String radarRangeStr = preferences.getString("radarrange", "200");
		radarrange = Integer.parseInt(radarRangeStr);
		String soundfreqstr=preferences.getString("soundfreq", "2"); 	        
        soundtype=Integer.parseInt(soundfreqstr); 
		String radarAltStr = preferences.getString("radaralt", "15000");
		radaralt = Integer.parseInt(radarAltStr);
		radarready = preferences.getBoolean("checkradar", false);
		if(radarready)
		    	  radarhandler.postDelayed(radarrunnable, intlogtime);	
		    else
		    {		
		    	 radarmarkerLayer.clear();
		    	 radarMarkerlist.clear();
		    	 radarhandler.removeCallbacks(radarrunnable);			  		 
		    }
				
		String dumpLevelStr = preferences.getString("dumpLevel", "25");
		currentdump = Integer.parseInt(dumpLevelStr);
		
		String sinkLevelStr = preferences.getString("sinkLevel", "2.5");
		sinkalarm = Double.parseDouble(sinkLevelStr);
		String strlogtime=preferences.getString("logtime", "3000");		
		intlogtime=Integer.parseInt(strlogtime);	
		String strLineWidth=preferences.getString("linewidth", "0.05");	
		if(!strLineWidth.equals(""))
			linewidth=Float.parseFloat(strLineWidth);
		else
			linewidth=0.05f;		
		gllinewidth=linewidth*100;
		
		String strTrackfactor=preferences.getString("trackFactor", "5");	
		if(!strTrackfactor.equals(""))
			inttrackfactor=Integer.parseInt(strTrackfactor);
		else
			inttrackfactor=5;	
		String strDrawfactor=preferences.getString("drawFactor", "15");	
		if(!strTrackfactor.equals(""))
			intdrawfactor=Integer.parseInt(strDrawfactor);
		else
			intdrawfactor=15;	
		username= preferences.getString("liveusername", "").trim();			
		password = preferences.getString("livepassword", "").trim();
		pilotname = preferences.getString("pilotname", "n/a");
		wingmodel = preferences.getString("wingmodel", "n/a");
		pilotid = preferences.getString("pilotid", "n/a");		
		String strwechiletype=preferences.getString("vehicletype", "1");		
		vechiletype=Integer.parseInt(strwechiletype);
		avgprogbar_reverse.setMax(80);
		avgprogbar.setMax(80);
		SetProgressColor("#0000FF",avgprogbar);
		SetProgressColor("#ff0000",avgprogbar_reverse);		
		avgprogbar_reverse.setProgress(0);
		avgprogbar.setProgress(0);	
		if(vario)
		{			
			if(beeps==null)
	        	beeps = new BeepThread(FCActivity.this);
			if(beeps!=null)
			   beeps.start(getApplicationContext(),soundtype,sinkalarm);				
		}
	}
    protected void onPause(){
        super.onPause();       
	   	radarhandler.removeCallbacks(radarrunnable);	
	    radarmarkerLayer.clear();
	   	radarMarkerlist.clear();
    }
    protected void initMap() {  
    	
   	 // 1. Get the MapView from the Layout xml - mandatory
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setComponents(new Components());
        // add event listener
        mapListener = new MapEventListener(this, mapView);
        mapView.getOptions().setMapListener(mapListener);     

        getvalues();                
   		if(mapPath.contains(".map"))
   		{ 	
   			setMapsForgeDatabase(mapPath);
   		}
   		else if(mapPath.contains("http:"))
   			setOnlineMap(mapPath,0,18,6);
   		else
   		{
   			this.mapPath ="http://a.tile.thunderforest.com/outdoors/"; 
    		setOnlineMap(mapPath,0,18,6); 
   		}
        
        mapView.setZoom(zoomlevel);
		mapView.setTilt(tiltlevel);
     // preload map tiles outside current view. Makes map panning faster, with cost of extra bandwidth usage
        mapView.getOptions().setPreloading(true);
        // repeating world view if you are panning out of map to east or west. Usually important for most general zooms only.
        mapView.getOptions().setSeamlessHorizontalPan(true);
        // blending animation for tile replacement
        mapView.getOptions().setTileFading(true);
        // pan/scroll map dynamically
        mapView.getOptions().setKineticPanning(true);
        // enable doubleclick gesture for zoom in
        mapView.getOptions().setDoubleClickZoomIn(true);
        // enable two-finger touch gesture to zoom out
        mapView.getOptions().setDualClickZoomOut(true);
        adjustMapDpi(); 
     
        // set sky bitmap - optional, default - white
        mapView.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
        mapView.getOptions().setSkyOffset(4.86f);
        // see sample image for good size ideas
        mapView.getOptions().setSkyBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.sky_small));

        // Map background, visible if no map tiles loaded - optional, default - white
        mapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
        mapView.getOptions().setBackgroundPlaneBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.background_plane));
        mapView.getOptions().setClearColor(Color.WHITE);
       
        // configure texture caching. Following are usually good values
        mapView.getOptions().setTextureMemoryCacheSize(40 * 1024 * 1024);
        mapView.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);    
        // define online map persistent caching. 
        mapView.getOptions().setPersistentCachePath(
                 this.getDatabasePath("mapcache").getPath());
        // set persistent raster cache limit to 100MB. Use any value you feel appropriate
        mapView.getOptions().setPersistentCacheSize(100 * 1024 * 1024);
        mapView.getOptions().setPersistentCachePath ("/sdcard/nmlcache.db");
        mapView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        
        mapView.getOptions().setMapListener(mapListener);     
            
        mapListener = (MapEventListener) mapView.getOptions().getMapListener();
	   	mapListener.setRouteLine(routeline);
	    mapListener.setTrckLine(trckline);		    
	    mapListener.setCircle(circle);	 
        // add GPS My Location functionality    
    }    
	public void savevalues()
    {
    	String sFileName="flight_computer_settings.txt";
		try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"VarioLog");
			if (!root.exists()) {
				root.mkdirs();
			}
			File settingsfile = new File(root, sFileName);
			FileWriter writer = new FileWriter(settingsfile);
			writer.write(mapPath +";");	
			writer.write(String.valueOf(mapView.getZoom())+";");	
			writer.write(String.valueOf(mapView.getTilt())+";");
			writer.write(String.valueOf(slp_inHg_)+";");	
			writer.flush();
			writer.close();			
		}catch(Exception e){}
    }
	
    public void getvalues()
    {
    	String sFileName="flight_computer_settings.txt";
		try {
			File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
			File settingsfile = new File(root, sFileName);
			if (settingsfile.exists()) 
			{				
	    		    BufferedReader br = new BufferedReader(new FileReader(settingsfile));
	    		    String line;
	    		    line = br.readLine();
	    		    String[] parts = line.split(";");
	    		    this.mapPath = parts[0]; 
	    		    this.zoomlevel = Float.parseFloat(parts[1]); 
	    		    this.tiltlevel = Float.parseFloat(parts[2]); 
	    		    this.slp_inHg_ = Double.parseDouble(parts[3]);
	    		    br.close();
	    	}else
	    	{
	    		this.mapPath ="http://a.tile.thunderforest.com/outdoors/"; 
	    		setOnlineMap(mapPath,0,18,6); 
	    		this.zoomlevel = 13; 
	    		this.tiltlevel = 0.0f; 
	    		this.slp_inHg_ = 29.92;
	    	}
		}catch(Exception e){}
    }
    
    public void drawTaskCircle(ArrayList<MapPos> circlePoints,ArrayList<Double> diameter,ArrayList<Integer> colorTask)
    { 
		if (circle != null) {
			circle.setCircle(circlePoints,diameter,colorTask);
			circle.setVisible(true); 
	   			}		
    }  
    public String getTurnPoint(String name)
    {
		String value = null;
		String sFileName="turnpoints.txt";
		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");		
		try{
		      // Open the file that is the first 
		      // command line parameter
		      FileInputStream fstream = new FileInputStream(root+"/"+sFileName);
		      // Get the object of DataInputStream
		      DataInputStream in = new DataInputStream(fstream);
		      BufferedReader br = new BufferedReader(new InputStreamReader(in));
		      String strLine;
		      //Read File Line By Line
		      while ((strLine = br.readLine()) != null)   {
		    	  String[] parts = strLine.split(";");					
				    if (parts[0].equals(name)) {				    	
				    	value=parts[0]+";"+parts[1]+";"+parts[2]+";"+parts[3];
				    }
		      }
		      //Close the input stream
		      br.close();
		      in.close();
		        }catch (Exception e){//Catch exception if any
		        	
		      }	
		return value;
    }  
   
    @SuppressLint("SimpleDateFormat")
	public int getActivePoint(MapPos start)
    {    	
    	double lat=0,lon=0;    
    	double radius=0;
    	double distance=0;    	   	
    	Date date = new Date(gpstime);		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		gpsTime = sdf.format(date);		
		TimeGpsTxt.setText("Time: " + gpsTime.substring(0,5));
		TimeStartTxt.setText("Start: " + startTime.substring(0,5));				
		long tdiff=getTimeDiff(startTime,gpsTime);
		long endtdiff=getTimeDiff(gpsTime,endTime);		
		if(endtdiff<=0)
		{			
			DistEdgeTxt.setText("Task time is over!");				
		}else
		{
		for (String temp : taskline) {
		      String[] parts = temp.split(";");	
		       	if(start!=null)    	
					  {
						String tpline=getTurnPoint(parts[1]);		    	 
				    	String[] tpparts = tpline.split(";");	
				    	   lat=Double.parseDouble(tpparts[1]);
				    	   lon=Double.parseDouble(tpparts[2]);
				    	MapPos end = proj.fromWgs84(lon,lat);
				    	radius=Double.parseDouble(parts[3]);
				    	GeoPoint spoint=new GeoPoint(proj.toWgs84(start.x, start.y).y,proj.toWgs84(start.x, start.y).x);
						GeoPoint epoint=new GeoPoint(proj.toWgs84(end.x, end.y).y,proj.toWgs84(end.x, end.y).x);
						distance=getDistance(spoint,epoint)/1000 - radius;						
					  }				   
	    		    if(parts[2].trim().equals("StartOut-Enter") && tdiff>0 && !startok)    		    	
					  {	
	    		    	DistEdgeTxt.setText(String.format("DisEdg: %.1f Km",distance) + " - " + parts[2].trim());
		    	        active=Integer.parseInt(parts[0]);
			  			if(distance>0 && !wasoutcircle)
			  			{
			  				
			  				wasoutcircle=true;			  								  				
			  			}
			  			else if(distance<0 && wasoutcircle)
			  			{				  				
			  				startok=true;
			  				active++;				  				
			  			}  			  			 
			  			 mapView.selectVectorElement(taskMarkerlist.get(active));	
			  			
					  }    			 
	    			 else if(parts[2].trim().equals("StartIn-Exit") && tdiff>0 && !startok)
					  {	
	    			   DistEdgeTxt.setText(String.format("DisEdg: %.1f Km",distance) + " - " + parts[2].trim());
				  	   active=Integer.parseInt(parts[0]);
    				   if(distance<0 && !wasincircle)
			  			{    					  
			  				wasincircle=true;			  				 				  				 
			  			}
			  			else if(distance>0 && wasincircle)
			  			{				  				
			  				startok=true;	
			  				active++;					  				
			  			}  
    				   mapView.selectVectorElement(taskMarkerlist.get(active));		    				   
					  }
		    		   else if(parts[2].trim().equals("Cylinder") && startok==true && active==Integer.parseInt(parts[0]))
		    		   { 		    			   
		    			   if(distance>0)
		    			   {
		    				   active=Integer.parseInt(parts[0]);	
		    				   DistEdgeTxt.setText(String.format("DisEdg: %.1f Km",distance) + " - " + parts[2].trim());
		    			   }
		    			   else if(distance<0)
		    			   {
		    				  active++;  		    				  
		    			   } 
		    			   mapView.selectVectorElement(taskMarkerlist.get(active));			    			 
		    		   } 		 
		    		   else if(parts[2].trim().equals("Goal") && startok==true && active==Integer.parseInt(parts[0]))
		    		   { 		    			   
		    			   if(distance>0)
		    			   {		    				   
		    				   active=Integer.parseInt(parts[0]);	
		    				   DistEdgeTxt.setText(String.format("DisEdg: %.1f Km",distance) + " - " + parts[2].trim());
		    				   mapView.selectVectorElement(taskMarkerlist.get(active));		 
		    			   }
		    			   else 
		    			   {		    				   
		    				   DistEdgeTxt.setText(String.format("Goal!"));
		    				   startok=false;
		    				   active=0;
		    			   } 
		    			    			   
		    		   } 
		    		   	    		   
	    	 }		
		}
		return active;     	
		     	
    }
    public void gettasktime()
    {   if(taskline.size()>0)
		{     		    		 
			 for (String temp : taskline) {
					 String[] parts = temp.split(";");
					 startTime=parts[5];
					 endTime=parts[6]; 
				}		
		}
    }	
    public Boolean existsTask()
    {  
    	boolean empty=true;
		String sFileName="task.txt";
		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");	
		File file = new File(root+"/"+sFileName);
		if(!file.exists() || file.length() == 0)
			empty=false;
		return empty;	    	
    }
    public List<Marker> taskMarkerlist =new ArrayList<Marker>();
    public void drawTaskMarkers()
    { 
    	if(taskmarkerLayer!=null)
    		taskmarkerLayer.clear();
    	taskMarkerlist.clear();
		if(taskline.size()>0)
    	{    	    	
    	double lat,lon; 	
    	for (String temp : taskline) {					
	    	  String[] parts = temp.split(";");		    	  
	    	  String tpline=getTurnPoint(parts[1]);		    	 
		      String[] tpparts = tpline.split(";");	
		        lat=Double.parseDouble(tpparts[1]);
		    	lon=Double.parseDouble(tpparts[2]);
		    	 MapPos mapPos = proj.fromWgs84(lon,lat);  		    	
			    	
				  Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.marker);
			      MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
			      Label markerLabel = new DefaultLabel(parts[1],parts[2],labelStyle);  
			      Marker marker=new Marker(mapPos, markerLabel, markerStyle, null);
			      taskMarkerlist.add(marker);
			      taskmarkerLayer.add(marker);
			      mapView.getLayers().addLayer(taskmarkerLayer);	
	      }	 	 
    	}
    } 
    public void drawTask(int active)
    {    
    	circlePoints.clear();
    	diameter.clear();
    	colorTask.clear();
    	if(taskline.size()>0)
    	{
    	//List<String> duplicateList= new ArrayList<String>();
    	double lat,lon;
    	for (String temp : taskline) {					
	    	  String[] parts = temp.split(";");	
	    	 //if(!containsCaseInsensitive(parts[1],duplicateList))
	    	 //{
	    		  String tpline=getTurnPoint(parts[1]);		    	 
		    	  String[] tpparts = tpline.split(";");	
		    	    lat=Double.parseDouble(tpparts[1]);
			    	lon=Double.parseDouble(tpparts[2]);
			    	MapPos mapPos = proj.fromWgs84(lon,lat);  			    	
			        circlePoints.add(mapPos);			       
			        diameter.add(Double.parseDouble(parts[3])*1000);	
		    		   if(String.valueOf(active).equals(parts[0].trim()))			
		    		   {
		    			   colorTask.add(Color.RED);	
		    		   }
				       else
				       {
				    	   colorTask.add(Color.BLUE);	
				       }
	    	 // }	    		  
    		 // duplicateList.add(parts[1]);
	      }	 
	      drawTaskCircle(circlePoints,diameter,colorTask);    	
    	}
    } 
    public boolean containsCaseInsensitive(String s, List<String> l){
    	 for (String string : l){
    	   if (string.equalsIgnoreCase(s)){
    	    return true;
    	    }
    	   }
    	  return false;
    	  }
   
    protected void initGps() {	    	
   	  Criteria myCriteria = new Criteria();
   	  myCriteria.setAccuracy(Criteria.ACCURACY_FINE);      
   	     locationListener = new LocationListener() 
         {     
   		public void onLocationChanged(Location location) {   			
         	try
				{           		
 				GeoPoint point = new GeoPoint(
 					       (int) (location.getLatitude() * 1E6),
 					       (int) (location.getLongitude() * 1E6));
 				latitude=point.getLatitude();
 				longitude=point.getLongitude();
 				routepos=proj.fromWgs84(longitude,latitude);  	
     			acc = (int) (location.getAccuracy());	    				
   				speed = (int) (location.getSpeed() * 3600 / 1000);  					
   				gpsaltitude = (int) (location.getAltitude());
   				gpstime=location.getTime();
   				LatTxt.setText(String.valueOf(latitude) + " " + getHemisphereLat(latitude));
   				LonTxt.setText(String.valueOf(longitude) + " " + getHemisphereLon(longitude));
   				SpeedTxt.setText("Spd: " + speed + " Km");		
   				if(!vario)
   				{
   					AltTxt.setText("Alt: " + gpsaltitude + " m");
   				}	  	
   				if (!getfirstfix) {   				   
   					startlatitude = latitude;
   					startlongitude = longitude;
   					if(pointmarkerLayer!=null)
   				      mapView.getLayers().removeLayer(pointmarkerLayer);
   					getfirstfix = true;	
   				    gpsrunning=true;
   				}  					  										
   				distancetotakeoff(latitude,longitude);	
   				if(existtask)
   				{
   				ActiveTaskPoint=getActivePoint(routepos);   	
   				drawTask(ActiveTaskPoint); 
   				}
	   				if(drawgpstrack)
	   				{
	   					if(speed>10)
	   					{
	   					    routePoints.add(routepos);	
	     			   		if(!vario && routeline != null)
	     					{
	     			   		altdiff=gpsaltitude-oldaltgps;   
	   						colorRoute.add(getavgTrckColor(altdiff));  					
	   						routeline.setRoute(routePoints,colorRoute,gllinewidth);
	   						routeline.setVisible(true);   
	     					oldaltgps=gpsaltitude;	   						
	     					}else if (routeline != null) {   						
	   					 	colorRoute.add(routecolor); 					 	
	   						routeline.setRoute(routePoints,colorRoute,gllinewidth);
	   						routeline.setVisible(true);	  						
	     					}
	   					}
	   				}	 
	   				    drawRouteMarker(routepos,baselayer);
     					bearing = (int) location.getBearing();	
     					mapView.setRotation(-1*bearing);     					
     			}catch(Exception e){   			
			 }              
            }
   		@Override
   		public void onProviderDisabled(String arg0) {
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
        };      
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);        
        locationManager.requestLocationUpdates(0L, // minTime
   				0.0f, // minDistance
   				myCriteria, // criteria
   				locationListener, // listener
   				null); // looper	         
    }
    int red=250,green=250,blue=0,routecolor=0;
    private void getavgRouteColor(double diffalt)	{      
 	    if(diffalt>0.2){
 	    	red=red+(int) (intdrawfactor*diffalt);    	    	
 	    	green=green-(int) (intdrawfactor*diffalt); 	    	
 	    	if(red>250)red=250; 	    	
 	    	if(green<0)green=0;
 	    	routecolor=Color.rgb(red,green,blue); 	    
 		}				
 		else if(diffalt<-0.2){			
 			red=red-(int) (-1*intdrawfactor*diffalt);    	    	
 	    	green=green+(int) (-1*intdrawfactor*diffalt);  
 	    	if(red<0)red=0; 	    	
 	    	if(green>250)green=250; 	
 	    	routecolor=Color.rgb(red,green,blue); 	    
 		}
 		else{			
 			red=red+intdrawfactor;    	    	
 	    	green=green+intdrawfactor; 	    	
 	    	if(red>250)red=250;
 	    	if(green>250)green=250; 	    	
 	    	routecolor=Color.rgb(red,green,blue); 	    
 		}	 	  	 	      
    }
     private int getavgTrckColor(double diffalt)	{		
    	 if(diffalt>0.2){
  	    	red=red+(int) (intdrawfactor*diffalt);    	    	
  	    	green=green-(int) (intdrawfactor*diffalt); 	    	
  	    	if(red>250)red=250; 	    	
  	    	if(green<0)green=0;
  	    	routecolor=Color.rgb(red,green,blue); 	    
  		}				
  		else if(diffalt<-0.2){			
  			red=red-(int) (-1*intdrawfactor*diffalt);    	    	
  	    	green=green+(int) (-1*intdrawfactor*diffalt);  
  	    	if(red<0)red=0; 	    	
  	    	if(green>250)green=250; 	
  	    	routecolor=Color.rgb(red,green,blue); 	    
  		}
  		else{			
  			red=red+15;    	    	
  	    	green=green+15; 	    	
  	    	if(red>250)red=250;
  	    	if(green>250)green=250; 	    	
  	    	routecolor=Color.rgb(red,green,blue); 	    
  		}	
  	  return routecolor; 	 
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.flight_computer, menu);     
      return true;
    } 
    private void clearMap()
	{
    	geomLayer_route.clear();
    	mapView.getLayers().removeLayer(geomLayer_route);    
    	if(pointmarkerLayer!=null)
		mapView.getLayers().removeLayer(pointmarkerLayer);
		routemarkerLayer.clear();
		pointmarkerLayer.clear();
		trckmarkerLayer.clear();
		trckclrmarkerLayer.clear();
		taskmarkerLayer.clear();
		taskMarkerlist.clear();
		radarMarkerlist.clear();
		radarmarkerLayer.clear();
		taskline.clear();
		routePoints.clear();
		trackPoints.clear();
		circlePoints.clear();		
		colorTrck.clear();
		colorRoute.clear();
		colorTask.clear();
		trcktime.clear();	
		mapView.clearFocus();
		mapView.clearAnimation();	
		DistEdgeTxt.setText("DisEdg: 0 Km");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.menu_loadigc:	
			pointmarkerLayer.clear();
			startActivityForResult(new Intent(this,IgcLoad.class),IGCFILE);
		return true;
		case R.id.menu_position_radar:				
			radarmarkerLayer.clear();
			if(radarready)
				{
				  getFlightData("http://www.flightradar24.com/zones/"+ radarregion +"_all.json");					
				}
		return true;
		case R.id.menu_task:	
			i = new Intent(this, Task.class);
			this.startActivity(i);					
		return true;
		case R.id.menu_draw_task:
			if(existtask)
			{			
				gettask();	
				gettasktime();		
				drawTaskMarkers();
				ActiveTaskPoint=getActivePoint(null);
				mapView.selectVectorElement(taskMarkerlist.get(ActiveTaskPoint));
				drawTask(ActiveTaskPoint);				
			}else
			{	
				taskMarkerlist.clear();
				taskline.clear();
				circlePoints.clear();	
				taskmarkerLayer.clear();
			}
		return true;
		case R.id.menu_settings:	
			i = new Intent(this, EditPreferences.class);
			this.startActivity(i);
			return true;
		case R.id.menu_position_onoff:
			if(item.getTitle().toString().equals("STOP GPS"))
			{
				if(getfirstfix)
					{	
					stopGps();	
					if(igcstart)
					callIgcFunc("normal");							
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					radarready = preferences.getBoolean("checkradar", false);
					if(radarready)
						gotoLastKnownPosition();
					}					
				   if(livetrackenabled && loginLW)
					{					
					setLivePos emitPos = new setLivePos();
					emitPos.execute(3);				
					}
				 item.setTitle("START GPS");
			}
			 else {
				 	item.setTitle("STOP GPS");	
				 	startProperties();	
				    initGps();     
			}
			return true;
		case R.id.menu_altimeter:	
			if(barometer)
			{
			i = new Intent(this, PressureAltimeterActivity.class);
			i.putExtra("slp_inHg_", String.valueOf(slp_inHg_));
     		setResult(RESULT_OK, i);
			startActivityForResult(i,SETALT);	
			}else
				Toast.makeText(getApplicationContext(), "There is no barometer sensor!", Toast.LENGTH_SHORT).show();
			return true;		
		case R.id.menu_mapoffline:		
			startMapFilePicker();
			return true;	
		case R.id.menu_map_online1:	
			mapPath="http://a.tile.thunderforest.com/outdoors/";	
			setOnlineMap(mapPath,0,18,6); 				
			return true;
		case R.id.menu_map_online2:		
			mapPath="http://a.tile.openstreetmap.org/";	
			setOnlineMap(mapPath,0,18,6); 
			return true;
		case R.id.menu_map_online3:		
			mapPath="http://b.tile.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/1/256/";	
			setOnlineMap(mapPath,0,20,11); 				
			return true;
		case R.id.menu_map_url:		
			LayoutInflater factory = LayoutInflater.from(this);
			textEntryView = factory.inflate(
					R.layout.dialog_enter_mapurl, null);			
			final EditText inputurl = (EditText) textEntryView
					.findViewById(R.id.mapurl);			
			inputurl.setText(mapPath, TextView.BufferType.EDITABLE);				
			final AlertDialog.Builder alerturl = new AlertDialog.Builder(this);
			alerturl.setTitle("Enter the Url Of Map")
					.setView(textEntryView)
					.setPositiveButton("Load Map",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {									
									mapPath=inputurl.getText().toString();	
									setOnlineMap(mapPath,0,18,6);									
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});
			alerturl.show();			
			return true;		
		case R.id.menu_position_search:	
			isNetworkAvailable(hsearch,2500);			
			return true;
		case R.id.menu_gpsstatus:		
			if(gps)
			{
			i = new Intent(this, GpsSkyActivity.class);
			this.startActivity(i);
			}else
				Toast.makeText(getApplicationContext(), "There is no gps!", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_clearmap:	
			clearMap();
			return true;	
		case R.id.menu_2d:	
			mapView.setTilt(100.0f);
			return true;	
		case R.id.menu_3d:	
			mapView.setTilt(0.0f);
			return true;	
		case R.id.menu_position_last_known:	
			if(gps){
			gotoLastKnownPosition();	
			}else
				Toast.makeText(getApplicationContext(), "There is no gps!", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_exit:	
			 if(livetrackenabled && loginLW)
				{					
					setLivePos emitPos = new setLivePos();
					emitPos.execute(3);				
					}
			radarready=false;	
			savevalues();
			clearMap();			 	
			if (igcstart) 
			{  try{	
				    variohandler.removeCallbacks(variorunnable);
				    loghandler.removeCallbacks(logrunnable);								    
					beeps.onDestroy();
					locationManager.removeUpdates(locationListener);						
				}catch(Exception ex){}  
			callIgcFunc("exit");			   
			}
			else{
				exit();
			}
			
			return true;		
		case R.id.menu_position_enter_coordinates:		
			factory = LayoutInflater.from(this);
			textEntryView = factory.inflate(
					R.layout.dialog_enter_coordinates, null);
			// text_entry is an Layout XML file containing two text field to
			// display in alert dialog
			final EditText input1 = (EditText) textEntryView
					.findViewById(R.id.latitude);
			final EditText input2 = (EditText) textEntryView
					.findViewById(R.id.longitude);
			if (routepos != null) {
				GeoPoint point=new GeoPoint(proj.toWgs84(routepos.x, routepos.y).y,proj.toWgs84(routepos.x, routepos.y).x);
				input1.setText(String.valueOf(point.getLatitude())
					    , TextView.BufferType.EDITABLE);
				input2.setText(String.valueOf(point.getLongitude())
						, TextView.BufferType.EDITABLE);
			} else if (lastcoord != null) {
				input1.setText(String.valueOf(lastcoord.getLatitude())
					    , TextView.BufferType.EDITABLE);
				input2.setText(String.valueOf(lastcoord.getLongitude())
						, TextView.BufferType.EDITABLE);
			} else {
				input1.setText("0.000000", TextView.BufferType.EDITABLE);
				input2.setText("0.000000", TextView.BufferType.EDITABLE);
			}
			final AlertDialog.Builder alertcoord = new AlertDialog.Builder(this);
			alertcoord.setTitle("Enter the Coordinates:")
					.setView(textEntryView)
					.setPositiveButton("GoToCoord",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									GeoPoint point = new GeoPoint(Double
											.parseDouble(input1.getText()
													.toString()
													.replace(",", ".")), Double
											.parseDouble(input2.getText()
													.toString()
													.replace(",", ".")));
									routemarkerLayer.clear();
									goToPosition(point.getLatitude(),point.getLongitude());									
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});
			alertcoord.show();
			return true;	
		}
		return super.onOptionsItemSelected(item);
	}
	private void callIgcFunc(String type)
	{
		new CreateIgc(this).execute(type);	
	}
	private Runnable logrunnable = new Runnable() {
		   @Override
		   public void run() {				   
			   if(getfirstfix && acc<=30 && acc!=0)
				{
				   if(enablelog && gpsrunning)
				     setigcfile();	
				}
			   if(livetrackenabled && gpsrunning && acc<=30 && acc!=0)
				{				  
					if(!loginLW)
					{
						TrackTxt.setText("Live: trying");					
						setLivePos emitPos = new setLivePos();
						emitPos.execute(1);		
					}else
					{							
						setLivePos emitPos = new setLivePos();
						emitPos.execute(2);			            		
					}
				}
			    loghandler.postDelayed(this,intlogtime);			  
		   }
		};	
	private Runnable radarrunnable = new Runnable() {
		   @Override
		   public void run() {				  
			   if(radarready)
  				{  					
  					getFlightData("http://www.flightradar24.com/zones/"+ radarregion +"_all.json");  					
  					
  				}
			    radarhandler.postDelayed(this,intlogtime);			  
		   }
		};	
	
	Handler hsearch = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        if (msg.what != 1) { // code if not connected
	        	 Toast.makeText(getApplicationContext(), "You are not online !!!", Toast.LENGTH_SHORT).show();
	        } else { // code if connected
	        	searchMarkerLayer = new MarkerLayer(mapView.getLayers().getBaseLayer().getProjection());
	    	    mapView.getLayers().addLayer(searchMarkerLayer);
				onSearchRequested();
	        }
	    }
	};
	public static void isNetworkAvailable(final Handler handler, final int timeout) {
        new Thread() {
            private boolean responded = false;
            @Override
            public void run() {   
                new Thread() {
                    @Override
                    public void run() {
                        HttpGet requestForTest = new HttpGet("http://www.google.com");
                        try {
                            new DefaultHttpClient().execute(requestForTest); // can last...
                            responded = true;
                        } catch (Exception e) {}
                    }
                }.start();

                try {
                    int waited = 0;
                    while(!responded && (waited < timeout)) {
                        sleep(100);
                        if(!responded ) { 
                            waited += 100;
                        }
                    }
                } 
                catch(InterruptedException e) {} // do nothing 
                finally { 
                    if (!responded) { handler.sendEmptyMessage(0); } 
                    else { handler.sendEmptyMessage(1); }
                }
            }
        }.start();
}
	
    public static void setSearchResult(Marker marker) {
        searchResult = marker;
    }

    public MapView getMapView() {
        return mapView;
    }
    @Override
    protected void onStart() {
        mapView.startMapping();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.stopMapping();
    }
    private Handler variohandler = new Handler();
    private Runnable variorunnable = new Runnable() {
		   @Override
		   public void run() {				  
			    setvario(pressure_hPa_);
			    variohandler.postDelayed(this,1000);			  
		   }
		};	 		
  public void setvario(double value)  
  {	  
	 if (slp_inHg_ < 28.1 || slp_inHg_ > 31.0) slp_inHg_ = 29.92;
	 if(oldaltbaro!=0)
      {	
		 try{			 
			 final double curr_measurement_time = SystemClock.elapsedRealtime() / 1000.;
			 final double dt = curr_measurement_time - last_measurement_time_;			
		     pressure_hPa_filter_.update(value, KF_VAR_MEASUREMENT, dt);	
		     last_measurement_time_ = curr_measurement_time;
		     baroaltitude=(long)hPaToMeter(slp_inHg_,pressure_hPa_filter_.getXAbs());	
		     baroaltitude=((currentdump*baroaltitude/100)+((100-(currentdump))*oldaltbaro/100)); 			    
		     avgvario=baroaltitude-oldaltbaro;	
		     oldaltbaro=baroaltitude; 
		     getavgRouteColor(avgvario);
		     playsound();
		     AvgVarTxt.setText(String.format("%.1f m/s",avgvario));	
			 AltTxt.setText("Alt: " + String.format("%.1f",baroaltitude) + " m");	
		    if(avgvario>=0)
		    {			    	
		    	AvgVarTxt.setTextColor(Color.BLUE);
		    	avgprogbar.setProgress((int) (avgvario*10));
		    	avgprogbar_reverse.setProgress(0);
		    }
		    else if(avgvario<0)
		    {			    	
		    	AvgVarTxt.setTextColor(Color.RED);
		    	avgprogbar_reverse.setProgress((int) (-1*avgvario*10));
		    	avgprogbar.setProgress(0);
		    }
		    		   
		 }catch(Exception e){			
			 }
      }else
      {	
    	  oldaltbaro= hPaToMeter(slp_inHg_,value);	    	
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
  
 
 protected void stopGps() {			   		    
		locationManager.removeUpdates(locationListener);	
		gpsrunning=false;
		getfirstfix=false;
		locationListener=null;
		satellites=0;		
		latitude=0;
		longitude=0;		
		oldaltgps=0;		
		intalt=0;
		acc=0;
		speed =0;  					
		gpsaltitude = 0;	 
		gpstime=0;	
		bearing=0;		
		LatTxt.setText("0.000000");
		LonTxt.setText("0.000000");	
		AltTxt.setText("Alt: 0 m");
		SpeedTxt.setText("Spd: 0 Km");	
		DTkf.setText("DTkf: 0 Km");	
		TrackTxt.setText("Trck: 0");	
 }
 
 private float getMapViewWidth(MapView mapView){
     MapPos bottomLeft = mapView.screenToWorld(0, mapView.getHeight(), 0);
     MapPos bottomRight = mapView.screenToWorld(mapView.getWidth(), mapView.getHeight(), 0);
     
     float[] results = new float[3];
     
     Location.distanceBetween(this.proj.toWgs84(bottomLeft.x,bottomLeft.y).x,
             this.proj.toWgs84(bottomLeft.x,bottomLeft.y).y,
             this.proj.toWgs84(bottomRight.x,bottomRight.y).x,
             this.proj.toWgs84(bottomRight.x,bottomRight.y).y, results);
     
     return results[0];
}
	private void startMapFilePicker() {		
		FilePicker.setFileDisplayFilter(new FilterByFileExtension(".map;.tif"));
		startActivityForResult(new Intent(this, FilePicker.class), MAPFILE);		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();		
		wl.release();	
		 if(livetrackenabled && loginLW)
			{					
				setLivePos emitPos = new setLivePos();
				emitPos.execute(3);				
				}
		exit();
	}
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);		
		switch (requestCode) {
		case IGCFILE:					
			if (resultCode == Activity.RESULT_OK) {				
				String string = data.getStringExtra("file");
				if (string.length() != 0)				
				{
					ReadIgc(string.trim());					
				}
			}
			break;	
		case SETALT:					
			if (resultCode == Activity.RESULT_OK) {				
				String string = data.getStringExtra("slp_inHg_");
				if (string.length() != 0)				
				{
					slp_inHg_=Double.parseDouble(string);						
					if (sensor_pressure_ != null) {
						sensor_manager_.unregisterListener(mSensorListener);
				    	sensor_manager_.registerListener(mSensorListener, sensor_pressure_, 1000000);	
				    }	
				}
			}
			break;			
		case MAPFILE:					
			if (resultCode == Activity.RESULT_OK) {				
				if (data != null && data.getStringExtra(FilePicker.SELECTED_FILE) != null) {					  
				    mapPath =data.getStringExtra(FilePicker.SELECTED_FILE);				   
				    if(mapPath.contains(".map"))
					{		
				    	setMapsForgeDatabase(mapPath);
					}				    
				}			
			}
			break;	
		}
	}
      
    private void setOnlineMap(String map,int minzoom,int maxzoom,int id) {      	
    	TMSMapLayer onlinemapplayer = new TMSMapLayer(proj, minzoom, maxzoom, id,map, "/", ".png");
		baselayer=onlinemapplayer;  
		mapView.getLayers().setBaseLayer(baselayer); 
        adjustMapDpi();    
    }
    private void setMapsForgeDatabase(String path) {      	
    	JobTheme renderTheme = MapsforgeMapLayer.InternalRenderTheme.OSMARENDER;         
    	MapsforgeMapLayer maplayer = new MapsforgeMapLayer(proj,0, 20, 1044, path, renderTheme);		
		baselayer=maplayer;
        mapView.getLayers().setBaseLayer(baselayer); 
        adjustMapDpi();  
        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
    }
    
    void gotoLastKnownPosition() {
		routemarkerLayer.clear();
		Location currentLocation;	
		Location bestLocation = null;
		for (String provider : locationManager.getProviders(true)) {
			currentLocation = locationManager.getLastKnownLocation(provider);
			if (currentLocation == null)
				continue;
			if (bestLocation == null
					|| currentLocation.getAccuracy() < bestLocation.getAccuracy()) {
				bestLocation = currentLocation;
			}
		}
		if (bestLocation != null) {			
			latitude = bestLocation.getLatitude();
			longitude = bestLocation.getLongitude();	
			GeoPoint gpoint=new GeoPoint(latitude,longitude);
			MapPos point = baselayer.getProjection().fromWgs84(longitude,latitude);		
			drawPointMarker(point,"Last Known Point",Getinfo(gpoint));			
			lastcoord=gpoint;							
		}		
	}
    public void goToPosition(double lat,double lon)
    {
    	GeoPoint gpoint = new GeoPoint(lat, lon);	
    	MapPos point = baselayer.getProjection().fromWgs84(lon,lat);		
    	drawPointMarker(point,"Point",Getinfo(gpoint));	 		
		lastcoord=gpoint;	
		/*Location temp  = new Location(LocationManager.NETWORK_PROVIDER);
		temp.setLatitude(lat);
		temp.setLongitude(lon);*/		
    }  
   
    private void distancetotakeoff(double targetlt, double targetlon) {
    	try{
		Location currentLocation = new Location("reverseGeocoded");
		currentLocation.setLatitude(startlatitude);
		currentLocation.setLongitude(startlongitude);
		
		Location targetLocation = new Location("reverseGeocoded");
		targetLocation.setLatitude(targetlt);
		targetLocation.setLongitude(targetlon);
		String str = null;
		double distance = (int) currentLocation.distanceTo(targetLocation);
		if (distance <= 999)
			str = String.valueOf(String.format("DTkf: %.0f", distance)) + " m";
		else
			str = String.valueOf(String.format("DTkf: %.1f", distance / 1000))
					+ " Km";
		DTkf.setText(str);
    	}catch(Exception e){ 
		}
	}
    	
    private void checkLocationProviders(){
	    //String provider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	 try{gps_enabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}          
	 		if(!gps_enabled)
	 		{
	    	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	   builder.setMessage("Gps providers is not available. Enable GPS ?")
	    	          .setCancelable(false)
	    	          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	              public void onClick(DialogInterface dialog, int id) {
	    	            	  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    	   	          startActivityForResult(intent, 1);
	    	              }
	    	          })
	    	          .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	              public void onClick(DialogInterface dialog, int id) {
	    	            	  //finish();
	    	              }
	    	          }).show();
	    	   }	
	 		gpscheck=true;
	   }		
    @Override
    public Object onRetainNonConfigurationInstance() {
        Log.debug("onRetainNonConfigurationInstance");
        return this.mapView.getComponents();
    }   
    public void SetProgressColor(String color,ProgressBar bar){
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String MyColor =color;
		pgDrawable.getPaint().setColor(Color.parseColor(MyColor));
		ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		bar.setProgressDrawable(progress);   
		bar.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));	
		bar.refreshDrawableState();
	}
    private void adjustMapDpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float dpi = metrics.densityDpi;
        // following is equal to  -log2(dpi / DEFAULT_DPI)
        float adjustment = (float) - (Math.log(dpi / DisplayMetrics.DENSITY_HIGH) / Math.log(2));
        Log.debug("adjust DPI = "+dpi+" as zoom adjustment = "+adjustment);
        mapView.getOptions().setTileZoomLevelBias(adjustment / 2.0f);
    }
    
    protected double DDMMmmmToDecimalLat(String coord,String hems)
	{
		String coorddegree=coord.substring(0,2);
		String coordminute=coord.substring(2,7);
		coordminute=coordminute.substring(0,2)+"."+coordminute.substring(2,5);
		double latcoordminute= Double.parseDouble(coordminute);
		latcoordminute=latcoordminute/60;
		DecimalFormat df = new DecimalFormat("0.000000");			
		coord=df.format(latcoordminute).substring(2);
		double result=Double.parseDouble(coorddegree + "." + coord);
		if(hems.equals("S"))
			result=-1*result;
		return result;	
	}
	protected double DDMMmmmToDecimalLon(String coord,String hems)
	{
		String coorddegree=coord.substring(0,3);
		String coordminute=coord.substring(3,8);
		coordminute=coordminute.substring(0,2)+"."+coordminute.substring(2,5);
		double latcoordminute= Double.parseDouble(coordminute);
		latcoordminute=latcoordminute/60;
		DecimalFormat df = new DecimalFormat("0.000000");			
		coord=df.format(latcoordminute).substring(2);
		double result=Double.parseDouble(coorddegree + "." + coord);
		if(hems.equals("W"))
			result=-1*result;
		return result;
	}
	public String decimalToDMSLat(double coord) {
		try {
			String output, degrees, minutes, hemisphere;
			if (coord < 0) {
				coord = -1 * coord;
				hemisphere = "S";
			} else {
				hemisphere = "N";
			}
			double mod = coord % 1;
			int intPart = (int) coord;
			degrees = String.format("%02d", intPart);
			coord = mod * 60;
			DecimalFormat df = new DecimalFormat("00.000");
			minutes = df.format(coord).replace(".", "");
			minutes = minutes.replace(",", "");
			output = degrees + minutes + hemisphere;
			return output;
		} catch (Exception e) {
			return null;
		}
	}
	public String decimalToDMSLon(double coord) {
		try {
			String output, degrees, minutes, hemisphere;
			if (coord < 0) {
				coord = -1 * coord;
				hemisphere = "W";
			} else {
				hemisphere = "E";
			}
			double mod = coord % 1;
			int intPart = (int) coord;
			degrees = String.format("%03d", intPart);
			coord = mod * 60;
			DecimalFormat df = new DecimalFormat("00.000");
			minutes = df.format(coord).replace(".", "");
			minutes = minutes.replace(",", "");
			output = degrees + minutes + hemisphere;
			return output;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void setigcfile() {
		if (!logheader) 
		preparelogheader();
		// B
		Date date = new Date(gpstime);
		String igcval = null;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String igcgpstime = sdf.format(date);
		String igclat = decimalToDMSLat(latitude);
		String igclon = decimalToDMSLon(longitude);
		// A
		String igcaltpressure = String.format("%05d", (int) baroaltitude);
		String igcaltgps = String.format("%05d", (int) gpsaltitude);		
			igcval = "B" + igcgpstime.replace(":", "") + igclat + igclon + "A"	+ igcaltpressure + igcaltgps;
			igclog.add(igcval + "\r\n");
			trackcount++;
			if(!livetrackenabled)
			TrackTxt.setText("Trck: " + String.valueOf(trackcount));		
				if (!igcstart) {
					Toast.makeText(getApplicationContext(),
							"IGC LOG START", Toast.LENGTH_LONG).show();				
					igcstart=true;	
				}			
	}
	private void preparelogheader() {		
			Calendar c = Calendar.getInstance();
			SimpleDateFormat dfdetail = new SimpleDateFormat("ddMMyy");
			String formattedDate = dfdetail.format(c.getTime());
			String value = "AXFC FLIGHT COMPUTER" + "\r\n";
			value = value + "HFDTE" + formattedDate + "\r\n";
			value = value + "HOPLTPILOT:" + pilotname + "\r\n";
			value = value + "HOGTYGLIDERTYPE: " + wingmodel + "\r\n";			
			value = value + "HOCIDCOMPETITIONID:" + pilotid + "\r\n";
			value = value + "HODTM100GPSDATUM: WGS84" + "\r\n";		
			igclog.add(value);
			logheader = true;		
	}
	public String getHemisphereLat(double coord) {
		if (coord < 0) {
			return "S";
		} else {
			return "N";
		}
	}
	public String getHemisphereLon(double coord) {
		if (coord < 0) {
			return "W";
		} else {
			return "E";
		}
	}	
	
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
		 switch (keyCode) {
		    case KeyEvent.KEYCODE_BACK:
		    	Toast.makeText(getApplicationContext(),
						"Please use Exit Button!...", Toast.LENGTH_SHORT).show();
		        return true;
		    case KeyEvent.KEYCODE_VOLUME_UP:
		        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
		        return true;
		    case KeyEvent.KEYCODE_VOLUME_DOWN:
		        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
		        return true;
		    default:
		        return false;
		    }
		}
    public void exit() {
    	try{
	    	variohandler.removeCallbacks(variorunnable);
		    loghandler.removeCallbacks(logrunnable);								    
			beeps.onDestroy();
			locationManager.removeUpdates(locationListener);	
    	}catch(Exception e){}
    		finish();
			//android.os.Process.killProcess(android.os.Process.myPid());			
	}
	
	public void ReadIgc(String path) {		
		new DownloadFileAsync(this).execute(path);			
	}	 
	 private String root = Environment.getExternalStorageDirectory().getPath();
	 public class DownloadFileAsync extends AsyncTask<String, String, String> {
			public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
			private ProgressDialog mProgressDialog;
			StringBuilder filedata = new StringBuilder();						
			KalmanFilter filter;
			public DownloadFileAsync(Context context) 
			{				 
			     mProgressDialog = new ProgressDialog(context);
			     mProgressDialog.setMessage("Reading Tracks..Please Wait");
			     mProgressDialog.setIndeterminate(false);
			     mProgressDialog.setMax(0);
			     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			     mProgressDialog.setCancelable(true);			    
			     colorTrck.clear();
			     trcktime.clear();
			     trackPoints.clear();		
			}
			@Override
			protected void onPreExecute() {
			    super.onPreExecute();			   
			    mProgressDialog.show();
			}
			@Override
			protected String doInBackground(String... aurl) {			
				String path=aurl[0];
				int i=0,count=0;
				GeoPoint routecoord=null;
				String lat=null,lathems=null,lon=null,lonhems=null;				
				File file = new File(root,path);
				if (file.exists()) 
				{
					try {
		    		    BufferedReader br = new BufferedReader(new FileReader(file));
		    		    String line;
		    		    while ((line = br.readLine()) != null) {
		    		    	if(line.startsWith("B"))	    		    		
		    		    	{		    		    		 
		    		    		 for(int fc=0;fc<inttrackfactor-1;fc++)
    		    			     {
    		    			       br.skip(line.length());	
    		    			     }
		    		    		 count++;
		    		    	}
		    		    }
		    		}
		    		catch (IOException e) {	    		   
		    		}	
					mProgressDialog.setMax(count);						
					file = new File(root,path);		
					startalttrck=0;	
					trckvario=0;
					MapPos pos=null;
					long tdiff=inttrackfactor;
					try {
			    		   BufferedReader br = new BufferedReader(new FileReader(file));
			    		    String line;			    		 
			    		    while ((line = br.readLine()) != null) {
			    		    try{
			    		    	if(line.startsWith("B"))
			    		    	{			    		    		 
			    		    		lat=line.substring(7, 14);
			    		    		lathems=line.substring(14, 15);
			    		    		lon=line.substring(15, 24);
			    		    		lonhems=line.substring(24, 25);	
			    		    		routecoord=new GeoPoint(DDMMmmmToDecimalLat(lat,lathems),DDMMmmmToDecimalLon(lon,lonhems));				    		    	    
			    		    		String strtrckalt=line.substring(line.indexOf("A")+6, line.indexOf("A")+11);	
			    		    		intalt=Integer.parseInt(strtrckalt);			    		    		
			    		    		String strtrcktime=line.substring(1,3) + ":" +
				    		    			 line.substring(3,5) + ":" +
				    		    			 line.substring(5,7);	
		    		    		 	trcktime.add(strtrcktime);	
		    		    		 	startalttrck=startalttrck+trckvario;	
		    		    		 	if(!drawmultitrack)
			    		    		colorTrck.add(getavgTrckColor(trckvario));	
			    		    		pos=new MapPos(
			    		    				(float)proj.fromWgs84(routecoord.getLongitude(),routecoord.getLatitude()).x,
			    		    				(float)proj.fromWgs84(routecoord.getLongitude(),routecoord.getLatitude()).y,
			    		    				startalttrck);
				    		    	    trackPoints.add(pos);					    		    	    	    		    		    		 	
				    		    		if(oldaltgps!=0)
				    		    		{				    		    			
				    		    			if(trcktime.size()>1)
				    		    			tdiff=getTimeDiff(trcktime.get(trcktime.size()-2),trcktime.get(trcktime.size()-1));					    		    			
				    		    			trckvario=(float) ((intalt-oldaltgps)/tdiff); 
				    		    			if(trckvario>15)trckvario=15;
				    		    			if(trckvario<-1*15)trckvario=-1*15;				    		    			
				    		    			oldaltgps=intalt;
				    		    		}else
				    		    			oldaltgps=intalt;				    		    		
			    		    		i=i+inttrackfactor;				    		    		
			    		    		for(int fc=0;fc<inttrackfactor-1;fc++)
	    		    			    {
	    		    			      br.skip(line.length());	
	    		    			    }
	    		    			    mProgressDialog.setProgress(i);	
			    		    	}		    		    	
			    		    }catch(Exception e){			    		    	
			    		    }	
			    		   }
			    		   br.close();	    		   
			    		}
			    		catch (IOException e) {			    			
			    		}	
					
				}
			    return null;
			}				
			protected void onProgressUpdate(String... progress) {
			    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
			}		
			@Override
			protected void onPostExecute(String unused) {		
				    drawTrack(trackPoints);				   
				    mProgressDialog.dismiss();					    
			}		
		}
	 public class CreateIgc extends AsyncTask<String, String, String> {
			public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
			private ProgressDialog mProgressDialog;		
			private String cmdtype=null;
			public CreateIgc(Context context) 
			{	
				createigcrunning=true;
			     mProgressDialog = new ProgressDialog(context);
			     mProgressDialog.setMessage("Creating Igc File..Total tracks: "+String.valueOf(igclog.size()));
			     mProgressDialog.setIndeterminate(false);
			     mProgressDialog.setMax(igclog.size());
			     mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			     mProgressDialog.setCancelable(true);
			}
			@Override
			protected void onPreExecute() {
			    super.onPreExecute();			   
			    mProgressDialog.show();
			}
			@Override
			protected String doInBackground(String... aurl) {
					int i=0;
					cmdtype=aurl[0];
					try {
						preparelogfooter();						
						String sFileName=logfilename + ".igc";
						try {
							File root = new File(Environment.getExternalStorageDirectory(),
									"VarioLog");
							if (!root.exists()) {
								root.mkdirs();
							}
							File igcfile = new File(root, sFileName);
							FileWriter writer = new FileWriter(igcfile);
							for (String str : igclog) {
								writer.write(str);
								i++;
								mProgressDialog.setProgress(i);
							}
							writer.flush();
							writer.close();
							igclog.clear();
							Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
							Uri uri = Uri.fromFile(igcfile);
							intent.setData(uri);
							sendBroadcast(intent);							
						} catch (IOException e) {
						}
					} catch (Exception e) {						
					}				
			    return null;
			}	
			protected void onProgressUpdate(String... progress) {
			    mProgressDialog.setProgress(Integer.parseInt(progress[0]));
			}		
			@Override
			protected void onPostExecute(String unused) {	
				if(cmdtype.equals("exit"))
				{
					mProgressDialog.dismiss();
					exit();
				}
				else
				{
				mProgressDialog.dismiss();
				trackcount=0;
				igcstart=false;				
				}
				createigcrunning=false;	
			}				
			private void preparelogfooter() {
				if (!logfooter) {
					Calendar c = Calendar.getInstance();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDate = df.format(c.getTime());
					String value = "LXGD Turkay Biliyor Android Igc Version 1.00"
							+ "\r\n";
					value = value + ("LXGD Downloaded " + formattedDate);
					igclog.add(value);
					df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					formattedDate = df.format(c.getTime());
					logfilename = "FlightLog_" + formattedDate.replace(" ", "_");
					logfooter = true;
				}
			}			
			
		}	
	
	 void playsound(){						
			beeps.setAvgVario(avgvario);			
		}
	 public void drawRouteMarker(MapPos markerLocation,Layer layer)
		{	
		 try{			
				Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.plane);
			    MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.YELLOW).build();		    
			    routemarkerLayer.clear();
			    Marker marker=new Marker(markerLocation, null, markerStyle, null);
			    routemarkerLayer.add(marker);
			    mapView.getLayers().addLayer(routemarkerLayer);	  
			    mapView.setFocusPoint(markerLocation);	
		 }catch(Exception e){ }
		}
	 
	 private void drawTrack(ArrayList<MapPos> Points) 
	    { 	  
		   
			 if(colorTrck.size()==Points.size() && !drawmultitrack)
			 {				 
				 if (trckline != null) {
					 trckline.setRoute(Points,colorTrck,gllinewidth,threeaxis);
					 trckline.setVisible(true);
			   			}
			 }else
			 {				
				 drawLine(Points,Color.BLUE);
			 }
			 MapPos start=Points.get(0);
			 MapPos end=Points.get(Points.size()-1);
			 GeoPoint spoint=new GeoPoint(proj.toWgs84(start.x, start.y).y,proj.toWgs84(start.x, start.y).x);
			 GeoPoint epoint=new GeoPoint(proj.toWgs84(end.x, end.y).y,proj.toWgs84(end.x, end.y).x);
			 double distance=getDistance(spoint,epoint);	
			 trckclrmarkerLayer.clear();
			 drawTrckMarker(start,"Start Point",Getinfo(spoint));
			 drawTrckMarker(end,"End Point","Distance:" + String.format("%.1f",distance/1000) + " Km");	
	         GotoMitPosition(spoint,epoint,distance);	
	    }  
	
	 @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	    }
	 
	    @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	        super.onRestoreInstanceState(savedInstanceState);
	    }	 
	   
	 private void drawTrckMarker(MapPos markerLocation,String desc1,String desc2 )
	 {
	    Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.marker);
        MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
        Label markerLabel = new DefaultLabel(desc1,desc2,labelStyle);   
        Marker marker=new Marker(markerLocation, markerLabel, markerStyle, null);
       if(drawmultitrack)
       {    	  
    	   trckmarkerLayer.add(marker);
    	   mapView.getLayers().addLayer(trckmarkerLayer);
       }
       else
       {    	   
    	   trckclrmarkerLayer.add(marker);
           mapView.getLayers().addLayer(trckclrmarkerLayer);
       }
        mapView.setFocusPoint(markerLocation); 
        mapView.selectVectorElement(marker);
	 }
	 private void drawPointMarker(MapPos markerLocation,String desc1,String desc2 )
	 {		 
		  Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.marker);
	      MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
	      Label markerLabel = new DefaultLabel(desc1,desc2,labelStyle);  
	      Marker marker=new Marker(markerLocation, markerLabel, markerStyle, null);
	      pointmarkerLayer.add(marker);
	      mapView.getLayers().addLayer(pointmarkerLayer);
	      mapView.setFocusPoint(markerLocation);
	      mapView.selectVectorElement(marker);
	 }	 
	  private String Getinfo(GeoPoint point)
		{
			String info = String.format("Lat: %.6f", point.getLatitude()).replace(",",
					".")
					+ String.format("  Lon: %.6f", point.getLongitude())
							.replace(",", ".");
			return info;
		}
	  
	 public static double getDistance(GeoPoint StartP, GeoPoint EndP) {
		    double lat1 = StartP.getLatitude();
		    double lat2 = EndP.getLatitude();
		    double lon1 = StartP.getLongitude();
		    double lon2 = EndP.getLongitude();
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLon = Math.toRadians(lon2-lon1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		    Math.sin(dLon/2) * Math.sin(dLon/2);
		    double c = 2 * Math.asin(Math.sqrt(a));
		    return 6366000 * c;
		}
	 void GotoMitPosition(GeoPoint StartP, GeoPoint EndP,double distance) {		
		 double lon1 = StartP.getLongitude();
		 double lon2 = EndP.getLongitude();
		 double lat1 = StartP.getLatitude();
		 double lat2 = EndP.getLatitude();
		 double finalLat,finalLng;		 
		 double dLon = Math.toRadians(lon2 - lon1);

	        lat1 = Math.toRadians(lat1);
	        lat2 = Math.toRadians(lat2);
	        lon1 = Math.toRadians(lon1);

	        double Bx = Math.cos(lat2) * Math.cos(dLon);
	        double By = Math.cos(lat2) * Math.sin(dLon);
	        finalLat= Math.toDegrees(Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By)));
	        finalLng= Math.toDegrees(lon1 + Math.atan2(By, Math.cos(lat1) + Bx));
		    mapView.setFocusPoint(baselayer.getProjection().fromWgs84(finalLng,finalLat));
		    if(highress)
		        mapView.setZoom(zoomLevel(distance)*4/3); 
		    else
		    	mapView.setZoom(zoomLevel(distance)); 
		    float bearingtrck=getbearing(StartP,EndP);
		    mapView.setRotation(-1*bearingtrck);		   		  
	}
	 private float zoomLevel(double distance) {
		 double equatorLength = 40075004; // in meters
		 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		           DisplayMetrics metrics = new DisplayMetrics();
		           wm.getDefaultDisplay().getMetrics(metrics);
		            double heightInPixels = metrics.heightPixels / metrics.density;
		             double metersPerPixel = equatorLength / 256 / metrics.density;
		             // 68 is percent
		             double diameter = (distance / 55 * 100) * 2;
		             int zoomLevel = 1;
		             while ((metersPerPixel * heightInPixels) > diameter) {
		                 metersPerPixel /= 2;
		                 ++zoomLevel;
		            }			             
		       return zoomLevel;  
		}	 
	 protected float getbearing(GeoPoint start, GeoPoint end){
		 float bearing=0;
		 try{
		  double longitude1 = start.getLongitude();
		  double longitude2 = end.getLongitude();
		  double latitude1 = Math.toRadians(start.getLatitude());
		  double latitude2 = Math.toRadians(end.getLatitude());
		  double longDiff= Math.toRadians(longitude2-longitude1);
		  double y= Math.sin(longDiff)*Math.cos(latitude2);
		  double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);		 
		  bearing=(float) ((Math.toDegrees(Math.atan2(y, x))+360)%360);
		 }catch(Exception e){}
		 return bearing;
		}
	 private long getTimeDiff(String stime1,String stime2)
	 {
		 SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");			
		 Calendar startDateTime = Calendar.getInstance();
		        try {
					startDateTime.setTime(sdf.parse(stime1));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        Calendar endDateTime = Calendar.getInstance();
		        try {
					endDateTime.setTime(sdf.parse(stime2));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        long milliseconds1 = startDateTime.getTimeInMillis();
		        long milliseconds2 = endDateTime.getTimeInMillis();
		        long diff = milliseconds2 - milliseconds1;
		        long timediff=diff / (1000);
				return timediff;				
	 }
	 public void setInfo(float zoom) {		
			this.zoomlevel=zoom;	
			//ZoomTxt.setText(String.format("Zoom: %.1f",zoomlevel)) ;						
		}
	public String GetMapPath()
	{
		return this.mapPath;
	}
	 
private void drawLine(ArrayList<MapPos> list,int color)	 {
	 if(list.size()>1)
	 {			
		 StyleSet<LineStyle> lineStyleColor= new StyleSet<LineStyle>();
		 lineStyleColor.setZoomStyle(0, LineStyle.builder().setWidth(linewidth).setColor(color).build());	
		 geomLayer_route.add(new Line(list, new DefaultLabel("Route Line"), lineStyleColor, null));	  
	     mapView.getLayers().removeLayer(geomLayer_route);
	     mapView.getLayers().addLayer(geomLayer_route);  
	 }	     
	} 
private double distancetoplane(double targetlt, double targetlon) {	
	Location currentLocation = new Location("reverseGeocoded");
	
	currentLocation.setLatitude(latitude);
	currentLocation.setLongitude(longitude);

	Location targetLocation = new Location("reverseGeocoded");
	targetLocation.setLatitude(targetlt);
	targetLocation.setLongitude(targetlon);	
	double distance = (int) currentLocation.distanceTo(targetLocation);
	return distance;	
}
public void getJsonData(String id)
{
	JSONArray data;
	String val = null;
	String fligtname;	
	double lat,lon;
	try {
		data = jObj.getJSONArray(id);
		if(data != null) {				     
				  lat=Double.parseDouble(data.getString(1).toString());
			      lon=Double.parseDouble(data.getString(2).toString());
			      int meters=(int) (Double.parseDouble(data.getString(4).toString()) * 0.3048); 
			      MapPos mapPos = proj.fromWgs84(lon,lat);  	
			      double distance=distancetoplane(lat,lon);
				if(distance<=radarrange*1000 && meters<=radaralt && meters>0) 
				{					 
				  fligtname=data.getString(13).toString();
			      if(fligtname.length()<1)
			      fligtname=data.getString(16).toString();						 
				  Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.airplane);
				  MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
			      Label markerLabel = new DefaultLabel(fligtname,
			    		  "Alt:" + String.valueOf(meters)+"m" +
			    	      " Dist:" + String.format("%d",(int)distance/1000) +"km"
			    	      ,labelStyle);  
			      Marker marker=new Marker(mapPos, markerLabel, markerStyle, null);
			      marker.setRotation(-1*Integer.parseInt(data.getString(3).toString())-mapView.getRotation());
			      radarMarkerlist.add(marker);	
			      radardist.add(distance);
				}
		}
	} catch (JSONException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}		
}

public class getFlightRadar extends AsyncTask<String, String, String> {
	
	public getFlightRadar(Context context) 
	{	
		if(createigcrunning && radarready)
			return;
		radarready=false;
		radarMarkerlist.clear();	
		radardist.clear();
		jstring=null;
	}
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();	
	}
	@Override
	protected String doInBackground(String... aurl) {			
		String url=aurl[0];	
		try {    	    
    	    StringBuilder builder = new StringBuilder();
    	    httpclient = new DefaultHttpClient();
    	    HttpGet httpget = new HttpGet(url);
    	    httpget.getRequestLine();	    
			HttpResponse response = httpclient.execute(httpget);
    	    HttpEntity entity = response.getEntity();
    	    if (entity != null) {
    	        InputStream inputStream = entity.getContent();
    	        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    	        for (String line = null; (line = bufferedReader.readLine()) != null;) {
    	            builder.append(line).append("\n");    	           
    	        }
    	        jstring = builder.toString();
    	        jstring = jstring.replace("pd_callback(","");
    	        jstring = jstring.replace(");","");    	        
    	        bufferedReader.close();
        	    httpclient.getConnectionManager().shutdown();       	        
    	    }
    	} catch (ClientProtocolException e) {            
        } catch (IOException e) {           
        } catch (Exception e) {                          
        }		
		if(jstring!=null)
		{
			try {
				jObj=new JSONObject(jstring);	
				nameArray = jObj.names();	
				jstring=null;
	            for(int flight=0;flight<nameArray.length();flight++)
	            {	
	            	getJsonData(nameArray.getString(flight));
	            }      
	           
			} catch (JSONException e) {			
			} catch (Exception e) {                          
	        }
		}	
	    return null;
	}				
	protected void onProgressUpdate(String... progress) {	   
	}		
	@Override
	protected void onPostExecute(String unused) {	
		try {			
		 radarmarkerLayer.clear();		 
		 int minIndex = radardist.indexOf(Collections.min(radardist));
		 Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.airplanered);
		 MarkerStyle markerStyle = MarkerStyle.builder().setBitmap(pointMarker).setSize(markersize).setColor(Color.WHITE).build();
		 radarMarkerlist.get(minIndex).setStyle(markerStyle);
		 radarmarkerLayer.addAll(radarMarkerlist);	 
		 mapView.getLayers().addLayer(radarmarkerLayer);	
		} catch (Exception e) {}
		 radarready=true;
	}		
}

private class setLivePos extends AsyncTask<Object, Void, Boolean>{			
	 @Override
	    protected void onPreExecute() {
	        super.onPreExecute();	               	        
	        errorinfo="";
	        error=false;
	 }
	@Override
	protected Boolean doInBackground(Object... params)  {					
		try {				 
			 type = (Integer) params[0];	
			 if(!loginLW && type==1)
			 {
           liveWriter = new LeonardoLiveWriter(
          		  basecontext,
          		  serverUrl,
          		  username,
          		  password,
          		  wingmodel,
          		  vechiletype,
          		  intlogtime/1000);	
           	 liveWriter.emitProlog();
			 }else if(loginLW && type==2)
			 {				 
				 liveWriter.emitPosition(gpstime, latitude, longitude, (float)gpsaltitude, (int)bearing, (float)speed);	
			 }else if(loginLW && type==3)
			 {	
				 liveWriter.emitEpilog();  	
				 loginLW=false;					
			 }				 
           return true;
      }	      
      catch (Exception e) {	            
      	errorinfo="Live Connection Error";		
      }
		return false;
	}			
	@Override
	protected void onPostExecute(Boolean result) {			
	        super.onPostExecute(result);
	        if(result)
	        {	error=false;		        	
	        	if(type==1)
		        {
		        	loginLW=true;				        		        	
		        }	
	        	else if(type==3)
	        	{
	        		loginLW=false;
					type=0;	
					LWcount=0;
					TrackTxt.setText("Trck: 0");
	        	}else{		        		
	        	LWcount=liveWriter.getLWCount();	
	        	if(livetrackenabled)
					TrackTxt.setText("Live: " + String.valueOf(LWcount));
	        	}
	        	
	        }else
	        {
	        	error=true;	
	        	TrackTxt.setText("Live: trying");
	        }	      
	    }
}	
} 