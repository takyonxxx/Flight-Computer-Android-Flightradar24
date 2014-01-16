package com.flightcomputer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.khronos.opengles.GL10;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.nutiteq.MapView;
import com.nutiteq.geometry.VectorElement;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.ui.MapListener;

public class MapEventListener extends MapListener {

    private static FCActivity activity;
    private MapView mapView;
    private RouteLine routeline=null;
    private TrckLine trckline=null;
    private Circle circle=null;  
           // activity is often useful to handle click events 
    public MapEventListener(FCActivity activity, MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;
    }    
    // Reset activity and map view
    public void reset(FCActivity activity, MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;   
    }
    public void setRouteLine(RouteLine line) {
        this.routeline = line;         
    }
    public void setTrckLine(TrckLine line) {
        this.trckline = line;         
    }
    public void setCircle(Circle circle) {
        this.circle = circle;         
    }   
    // Map drawing callbacks for OpenGL manipulations
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {  
    	 
    }    
    @Override
    public void onDrawFrameAfter3D(GL10 gl, float zoomPow2) {       	  
    }
    @Override
    public void onDrawFrame(GL10 gl) {     	
    }
   
    @Override
    public void onDrawFrameBefore3D(GL10 gl, float zoomPow2) {    
    	//logHeap(this.getClass());
    	if(this.routeline != null)
            this.routeline.draw(gl);        
        if(this.trckline != null)
            this.trckline.draw(gl);   
        if(this.circle != null)
            this.circle.draw(gl,zoomPow2);            
           mapView.requestRender();     
    }
    @Override
    public void onMapMoved() {
    	//activity.setInfo(mapView.getZoom());
    }    
    
	@Override
	public void onLabelClicked(VectorElement arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}
	EditText tname,talt;
	TextView tlat,tlon;
	
	@Override
	public void onMapClicked(double arg0, double arg1, boolean arg2) {
		// TODO Auto-generated method stub
		if(arg2)
		{		 
		 LayoutInflater factory = LayoutInflater.from(activity);
		 View textEntryView = factory.inflate(
					R.layout.dialog_enter_mapturnpoint, null);
			    tname = (EditText) textEntryView.findViewById(R.id.MturnpName);
		       	tlat = (TextView) textEntryView.findViewById(R.id.MturnpLat);
				tlon = (TextView) textEntryView.findViewById(R.id.MturnpLon);
				talt = (EditText) textEntryView.findViewById(R.id.MturnpAlt);	
				talt.setText("0", TextView.BufferType.EDITABLE);
				String lontxt=String.format("%.6f",(new EPSG3857()).toWgs84(arg0, arg1).x).replace(",",".");
				String lattxt=String.format("%.6f",(new EPSG3857()).toWgs84(arg0, arg1).y).replace(",",".");
				tlon.setText(lontxt, TextView.BufferType.NORMAL);
				tlat.setText(lattxt, TextView.BufferType.NORMAL);
		 final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
			alert.setTitle("Enter Turn Point Info:")	
					.setView(textEntryView)
					.setPositiveButton("Add To List",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {	
								if(!existsTP(tname.getText().toString()))
								saveturnpoint(tname.getText().toString().trim(),tlat.getText().toString().trim().replace(",",".") ,tlon.getText().toString().trim().replace(",","."),talt.getText().toString().trim());										
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});
			alert.show();
		}
	}
	@Override
	public void onVectorElementClicked(VectorElement arg0, double arg1,
			double arg2, boolean arg3) {
		// TODO Auto-generated method stub
		
	}
	static ArrayList<HashMap<String, String>> turnpoint = new ArrayList<HashMap<String, String>>();
	public void saveturnpoint(String name,String lat,String lon,String alt)
    {
		getturnpoint();
    	String sFileName="turnpoints.txt";
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"VarioLog");
			if (!root.exists()) {
				root.mkdirs();
			}
			if(name.length()>0 && lat.length()>0  && lon.length()>0 )
			{
				HashMap<String,String> map = new HashMap<String,String>();   
				  map.put("id",  name.trim());
				  map.put("lat", lat.trim());
				  map.put("lon", lon.trim());
				  map.put("alt", alt.trim());			
				turnpoint.add(map);	
				File file = new File(root, sFileName);
				FileWriter writer = new FileWriter(file);	
				for(Map<String, String> temp : turnpoint)
			    {		       	     	   
					writer.write(temp.get("id") +";");	
					writer.write(temp.get("lat") +";");	
					writer.write(temp.get("lon") +";");
					writer.write(temp.get("alt") + "\r\n");	
				}	
				writer.flush();
				writer.close();	
			}	
		}catch(Exception e){}    
    	
    }
	public void getturnpoint()
    {	
		turnpoint.clear();
		String sFileName="turnpoints.txt";
		String line = null;
		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
		File turnpfile = new File(root, sFileName);
		if (turnpfile.exists()) 
		{				
    		    BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(turnpfile));				
    		    	 while ((line = br.readLine()) != null) {	
    		    		 String[] parts = line.split(";");
						 HashMap<String,String> map = new HashMap<String,String>(); 						
				    	 map.put("id", parts[0]);
						 map.put("lat", parts[1]);
						 map.put("lon", parts[2]);
						 map.put("alt", parts[3]);
					 turnpoint.add(map);	
    		    	 }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}  			    		
    }	
	
		 public Boolean existsTP(String name)
		    {  
		    	boolean exist=false;
		    	String sFileName="turnpoints.txt";
				String line = null;
				File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
				File turnpfile = new File(root, sFileName);
				if (turnpfile.exists()) 
				{				
		    		    BufferedReader br = null;
						try {
							br = new BufferedReader(new FileReader(turnpfile));				
		    		    	 while ((line = br.readLine()) != null) {	
		    		    		 String[] parts = line.split(";");
		    		    		 if(parts[0].equals(name))
		    		    			 exist=true;							
		    		    	 }
		    		    	 br.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}  	
				return exist;	    	
		    }
}
