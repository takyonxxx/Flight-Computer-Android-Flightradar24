package com.flightcomputer;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
public class Task extends Activity {
	public String root = Environment.getExternalStorageDirectory().getAbsolutePath();	
	private ListView listturnpoint,listtask;
	SimpleAdapter adapterturnpoint;
	SimpleAdapter adaptertask ;
	static ArrayList<HashMap<String, String>> turnpoint = new ArrayList<HashMap<String, String>>();
	static ArrayList<HashMap<String, String>> task = new ArrayList<HashMap<String, String>>();
	Button btnaddtp,btnExit,btndeltask;
	EditText tname,tlat,tlon,talt,task_cyl;
	static EditText edit_starttime,edit_endtime;
	Spinner task_typ;
	View textEntryView;
	String oldname=null;
	static String starttime=null;
	static String endtime=null;
	double taskdistance=0;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_view);    
        turnpoint.clear();
        task.clear();
        btnaddtp= (Button)findViewById(R.id.btnaddtp); 
        btnExit= (Button)findViewById(R.id.btnexittp); 
        btndeltask=(Button)findViewById(R.id.btndelltask); 
        edit_starttime=(EditText)findViewById(R.id.txt_starttime); 
        edit_endtime=(EditText)findViewById(R.id.txt_endtime);
        listturnpoint = (ListView)findViewById(R.id.listturnpoint);        
        listtask = (ListView)findViewById(R.id.listtask);
        listturnpoint.setClickable(true); 
        listtask.setClickable(true); 
        edit_starttime.setText("00:00");
        edit_endtime.setText("00:00");  
        starttime="00:00:00";
		endtime="00:00:00";
        gettasktime();
        getturnpoint();
        gettask();          
        edit_starttime.setInputType(InputType.TYPE_NULL);        
        edit_starttime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                	showTruitonTimePickerDialogS(v);    
                }
            }
        });
        edit_starttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	showTruitonTimePickerDialogS(v);             
            }
        });
        edit_endtime.setInputType(InputType.TYPE_NULL);        
        edit_endtime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                	showTruitonTimePickerDialogE(v);    
                }
            }
        });
        edit_endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	showTruitonTimePickerDialogE(v);             
            }
        });
        btnExit.setOnClickListener(new OnClickListener() 
        {           
            @Override
            public void onClick(View v) 
            {        		
                finish();
            }
        });
        btndeltask.setOnClickListener(new OnClickListener() 
        {           
            @Override
            public void onClick(View v) 
            {
        		String sFileName="task.txt";        		
        		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
        		File file = new File(root, sFileName);
  			    if (!file.delete()) {
  			    System.out.println("Could not delete file");
  			    return;
  			  }
  			  gettask();
            }
        });
        btnaddtp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				LayoutInflater factory = LayoutInflater.from(Task.this);
				textEntryView = factory.inflate(
						R.layout.dialog_enter_turnpoint, null);
				    tname = (EditText) textEntryView.findViewById(R.id.turnpName);
			       	tlat = (EditText) textEntryView.findViewById(R.id.turnpLat);
					tlon = (EditText) textEntryView.findViewById(R.id.turnpLon);
					talt = (EditText) textEntryView.findViewById(R.id.turnpAlt);	
					talt.setText("0", TextView.BufferType.EDITABLE);
				final AlertDialog.Builder alert = new AlertDialog.Builder(Task.this);
				alert.setTitle("Enter Turn Point Info:")
						.setView(textEntryView)
						.setPositiveButton("Add To List",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {	
									if(!existsTP(tname.getText().toString()))
									saveturnpoint(tname.getText().toString(),tlat.getText().toString() ,tlon.getText().toString(),talt.getText().toString());										
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
        });               						
        listturnpoint.setOnItemClickListener(new AdapterView.OnItemClickListener() {	       
	         @SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {	       
	        	    Object o = listturnpoint.getItemAtPosition(position);	
	        	    String[] values = o.toString().split("id=");	        	 
	        	    final String name =values[1].substring(0,  values[1].indexOf(",")).trim();
	        	    AlertDialog alertDialog = new AlertDialog.Builder(Task.this).create();
	        	    alertDialog.setTitle("Turn Point Selected");
		          	 alertDialog.setButton("DELETE",new DialogInterface.OnClickListener(){
		          	    public void onClick(DialogInterface dialog, int which) {
		          	    	String sFileName="task.txt";        		
		            		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
		            		File file = new File(root, sFileName);
		      			    if (!file.delete()) {
		      			    System.out.println("Could not delete file");
		      			    }
		          	    	dellturnpoint(name.trim());
		          	    	getturnpoint();
		          	    return;
		          	  } });     		          	 
		          	 alertDialog.setButton2("CANCEL",new DialogInterface.OnClickListener(){
		            	    public void onClick(DialogInterface dialog, int which) {		            	    	
		            	    return;
		            	  } });   		
		          	 alertDialog.setButton3("ADD TO TASK",new DialogInterface.OnClickListener(){
		            	    public void onClick(DialogInterface dialog, int which) {		            	    	
		            	    	savetask(name,"Cylinder","0.4","0.0",starttime,endtime);		            	    	
		            	    	getturnpoint();
		            	    return;
		            	  } });   	
		          	 alertDialog.show();	        	           	   
	         }
        });     
        listtask.setOnItemClickListener(new AdapterView.OnItemClickListener() {	       
	         @SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {	       
	        	    Object o = listtask.getItemAtPosition(position);	
	        	    
	        	    String[] values = o.toString().split("id=");
	        	    final String id =values[1].substring(0,  values[1].indexOf(",")).trim();	
	        	    
	        	    values = o.toString().split("dist=");
	        	    final String dist =values[1].substring(0,  values[1].indexOf(",")).trim();	
	        	    
	        	    values = o.toString().split("cyl=");
	        	    final String cyl =values[1].substring(0,  values[1].indexOf(",")).trim();	
	        	    
	        	    values = o.toString().split("typ=");
	        	    final String typ =values[1].substring(0,  values[1].indexOf(",")).trim();	
	        	    
	        	    values = o.toString().split("name=");
	        	    final String name =values[1].substring(0,  values[1].length()-1).trim();	        	    
	        	   
	        	    LayoutInflater factory = LayoutInflater.from(Task.this);
					textEntryView = factory.inflate(R.layout.dialog_enter_task, null);
					task_cyl = (EditText) textEntryView.findViewById(R.id.task_cyl);
					task_typ = (Spinner) textEntryView.findViewById(R.id.task_typ);				  
					task_cyl.setText(cyl);
					
					int typselection=0;					   
					if(typ.equals("Cylinder"))
						typselection=0;
					else if (typ.equals("StartOut-Enter"))
						typselection=1;
					else if (typ.equals("StartIn-Exit"))
						typselection=2;
					else if (typ.equals("TakeOff"))
						typselection=3;
					else if (typ.equals("Goal"))
						typselection=4;					
					task_typ.setSelection(typselection);
					
				       	final AlertDialog.Builder alert = new AlertDialog.Builder(Task.this);
						alert.setTitle("Edit Task Point:")
								.setView(textEntryView)
								.setPositiveButton("Update",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {	
							          	    	updatetask(id,name,String.valueOf(task_typ.getSelectedItem()),task_cyl.getText().toString(),dist);
							          	    	gettask();
											}
										})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int whichButton) {
											}
										})
								.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
						            public void onClick(DialogInterface dialog, int which) {
						            	delltask(id);
					          	    	gettask();
						            }
						        });
						alert.show();	
	         }
       });     
	}	
	public void dellturnpoint(String name)
    {	
		String sFileName="turnpoints.txt";
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),"VarioLog");
			File file = new File(root, sFileName);
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			  BufferedReader br = new BufferedReader(new FileReader(file));
			  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			  String line = null;
			  //Read from the original file and write to the new
			  //unless content matches data to be removed.
			  while ((line = br.readLine()) != null) {
				  String[] parts = line.split(";");					
			    if (!parts[0].equals(name)) {
			      pw.println(line);
			      pw.flush();
			    }
			  }
			  pw.close();
			  br.close();

			  //Delete the original file
			  if (!file.delete()) {
			    System.out.println("Could not delete file");
			    return;
			  }
			  //Rename the new file to the filename the original file had.
			  if (!tempFile.renameTo(file))
			    System.out.println("Could not rename file");
			}
			catch (FileNotFoundException ex) {
			  ex.printStackTrace();
			}
			catch (IOException ex) {
			  ex.printStackTrace();
			}
    }
	public void updatetask(String id,String name,String cyl,String typ,String dist)
    {	
		String sFileName="task.txt";		
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),"VarioLog");
			File file = new File(root, sFileName);
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			  BufferedReader br = new BufferedReader(new FileReader(file));
			  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			  String line = null;
			  //Read from the original file and write to the new
			  //unless content matches data to be removed.
			  while ((line = br.readLine()) != null) {
				 if(line.length()>0)
				 {
					String[] parts = line.split(";");					
				    if (parts[0].equals(id)) {
				    	 pw.println(id+";"+name+";"+cyl+";"+typ+";"+dist+";"+starttime+";"+endtime);
				    }else{			    	
				    	 pw.println(line);
				    }
				    pw.flush();		
				 }
			  }
			  pw.close();
			  br.close();

			  //Delete the original file
			  if (!file.delete()) {
			    System.out.println("Could not delete file");
			    return;
			  }
			  //Rename the new file to the filename the original file had.
			  if (!tempFile.renameTo(file))
			    System.out.println("Could not rename file");
			}
			catch (FileNotFoundException ex) {
			  ex.printStackTrace();
			}
			catch (IOException ex) {
			  ex.printStackTrace();
			}
    }		
	public static void updatetasktime(String st,String et)
    {	
		String sFileName="task.txt";		
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),"VarioLog");
			File file = new File(root, sFileName);
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			  BufferedReader br = new BufferedReader(new FileReader(file));
			  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			  String line = null;
			  //Read from the original file and write to the new
			  //unless content matches data to be removed.
			  while ((line = br.readLine()) != null) {
				 if(line.length()>0)
				 {
					String[] parts = line.split(";");	
				    pw.println(parts[0]+";"+parts[1]+";"+parts[2]+";"+parts[3]+";"+parts[4]+";"+st+";"+et);
				    pw.flush();		
				 }
			  }
			  pw.close();
			  br.close();

			  //Delete the original file
			  if (!file.delete()) {
			    System.out.println("Could not delete file");
			    return;
			  }
			  //Rename the new file to the filename the original file had.
			  if (!tempFile.renameTo(file))
			    System.out.println("Could not rename file");
			}
			catch (FileNotFoundException ex) {
			  ex.printStackTrace();
			}
			catch (IOException ex) {
			  ex.printStackTrace();
			}    	
    }		
	public void delltask(String id)
    {
		int count=0;
		String sFileName="task.txt";
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),"VarioLog");
			File file = new File(root, sFileName);
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			  BufferedReader br = new BufferedReader(new FileReader(file));
			  PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			  String line = null;
			  //Read from the original file and write to the new
			  //unless content matches data to be removed.
			  while ((line = br.readLine()) != null) {
				  String[] parts = line.split(";");					
			    if (!parts[0].equals(id)) {			    	
			      pw.println(String.valueOf(count)+";"+parts[1]+";"+parts[2]+";"+parts[3]+";"+parts[4]+";"+parts[5]+";"+parts[6]);
			      pw.flush();
			      count++;
			    }
			  }
			  pw.close();
			  br.close();

			  //Delete the original file
			  if (!file.delete()) {
			    System.out.println("Could not delete file");
			    return;
			  }
			  //Rename the new file to the filename the original file had.
			  if (!tempFile.renameTo(file))
			    System.out.println("Could not rename file");
			}
			catch (FileNotFoundException ex) {
			  ex.printStackTrace();
			}
			catch (IOException ex) {
			  ex.printStackTrace();
			}
    }		
	public void savetask(String name,String cylinder,String type,String dist,String starttime,String endtime)
    {
    	String sFileName="task.txt";
    	try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"VarioLog");
			if (!root.exists()) {
				root.mkdirs();
			}
				HashMap<String,String> map = new HashMap<String,String>();   
				  map.put("id", String.valueOf(listtask.getCount()));
				  map.put("name",  name.trim());
				  map.put("typ", cylinder.trim());
				  map.put("cyl", type.trim());			
				  map.put("dist", dist.trim());	
				  map.put("stime",starttime.trim());	
				  map.put("etime",endtime.trim());
				task.add(map);	
				File file = new File(root, sFileName);
				FileWriter writer = new FileWriter(file);	
				for(Map<String, String> temp : task)
			    {		       	     	   
					writer.write(temp.get("id") +";");	
					writer.write(temp.get("name") +";");	
					writer.write(temp.get("typ") +";");	
					writer.write(temp.get("cyl") +";");	
					writer.write(temp.get("dist")+";"); 
					writer.write(temp.get("stime")+";"); 
					writer.write(temp.get("etime")+"\r\n");	
				}	
				writer.flush();
				writer.close();				
		}catch(Exception e){}    	
    	gettask();
    }
	public void gettask()
    {	
		task.clear();
		taskdistance=0;
		oldname=null;
		
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
    		    		 String[] parts = line.split(";");
						 HashMap<String,String> map = new HashMap<String,String>(); 						
				    	 map.put("id", parts[0]);
				    	 map.put("name", parts[1]);
						 map.put("typ", parts[2]);
						 map.put("cyl", parts[3]);								
						 if(oldname!=null && !parts[1].equals(oldname))		            	    		
					    		taskdistance=taskdistance+getdistance(oldname,parts[1])/1000;							
						  map.put("dist",String.format("%.1f",taskdistance).replace(",","."));	
						 oldname=parts[1];
						 map.put("stime", starttime);	
						 map.put("etime", endtime);	
					 task.add(map);	
    		    	 }
    		    	 br.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}  			    		   
			adaptertask = new SimpleAdapter(Task.this,
		              task, R.layout.simple_row_view_task,
		              new String[] { "id", "name", "typ", "cyl","dist","stime","etime"}, 
		              new int[] {0,R.id.task_name, R.id.task_typ, R.id.task_cyl, R.id.task_dist, R.id.task_stime, R.id.task_etime}); 
	        listtask.setAdapter(adaptertask);   
	        getturnpoint();
    }	
	public void gettasktime()
    {					
		String sFileName="task.txt";
		String line = null;
		File root = new File(Environment.getExternalStorageDirectory(),	"VarioLog");
		File turnpfile = new File(root, sFileName);
		if (turnpfile.exists()) 
		{				
    		    BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(turnpfile));				
					line = br.readLine();
    		    		 String[] parts = line.split(";");
    		    		 starttime=parts[5];
    		    		 endtime=parts[6];       		    		
    		    		 edit_starttime.setText(starttime.substring(0,5));
    		    		 edit_endtime.setText(endtime.substring(0,5));    		    		
					 	 br.close();    		    		
    		    	}
				 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();}
		} 		
		
    }	
	public void saveturnpoint(String name,String lat,String lon,String alt)
    {					
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
			}	else
				Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
		}catch(Exception e){}    	
    	getturnpoint();
    }
	 private double distance(double startlt,double startlon,double targetlt, double targetlon) {
		 double distance=0;
	    	try{	    	
			Location currentLocation = new Location("reverseGeocoded");
			currentLocation.setLatitude(startlt);
			currentLocation.setLongitude(startlon);

			Location targetLocation = new Location("reverseGeocoded");
			targetLocation.setLatitude(targetlt);
			targetLocation.setLongitude(targetlon);			
			distance = (int) currentLocation.distanceTo(targetLocation);			
	    	}catch(Exception e){}
			return distance;
		}
	public double getdistance(String firtpoint,String Secondpoint)
    {	
		turnpoint.clear();	
		double firstlat=0,firstlon=0,seclat=0,seclon=0;
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
				    if (parts[0].equals(firtpoint)) {
				    	firstlat=Double.parseDouble(parts[1]);
				    	firstlon=Double.parseDouble(parts[2]);
				    }else if (parts[0].equals(Secondpoint))
				    {
				    	seclat=Double.parseDouble(parts[1]);
				    	seclon=Double.parseDouble(parts[2]);
				    }
		      }
		      //Close the input stream
		      br.close();
		      in.close();
		        }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		      }
			
			return distance(firstlat,firstlon,seclat,seclon);   
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
			adapterturnpoint = new SimpleAdapter(Task.this,
		              turnpoint, R.layout.simple_row_view_tp,
		              new String[] { "id", "lat", "lon", "alt"}, 
		              new int[] {R.id.turn_name, R.id.turn_lat, R.id.turn_lon, R.id.turn_alt}); 
	        listturnpoint.setAdapter(adapterturnpoint);   
    }	
	public void showTruitonTimePickerDialogS(View v) {
        DialogFragment newFragment = new TimePickerFragmentS();
        android.app.FragmentManager fm =getFragmentManager();        
        newFragment.show(fm, "timePicker");       
    }
	public void showTruitonTimePickerDialogE(View v) {
        DialogFragment newFragment = new TimePickerFragmentE();
        android.app.FragmentManager fm =getFragmentManager();        
        newFragment.show(fm, "timePicker");       
    }
	 public static class TimePickerFragmentS extends DialogFragment implements
     TimePickerDialog.OnTimeSetListener {		
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
		     // Use the current time as the default values for the picker
		     final Calendar c = Calendar.getInstance();
		     int hour = c.get(Calendar.HOUR_OF_DAY);
		     int minute = c.get(Calendar.MINUTE);
		
		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(), this, hour, minute,
		             DateFormat.is24HourFormat(getActivity()));
		 }
		
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     // Do something with the time chosen by the user
			 edit_starttime.setText(String.format("%02d", (int) hourOfDay) + ":" + String.format("%02d", (int) minute));
			 starttime=edit_starttime.getText().toString()+":00";			 
			 updatetasktime(starttime,endtime);			 
		 }	
	 }
	 public static class TimePickerFragmentE extends DialogFragment implements
     TimePickerDialog.OnTimeSetListener {		
		 @Override
		 public Dialog onCreateDialog(Bundle savedInstanceState) {
		     // Use the current time as the default values for the picker
		     final Calendar c = Calendar.getInstance();
		     int hour = c.get(Calendar.HOUR_OF_DAY);
		     int minute = c.get(Calendar.MINUTE);
		
		     // Create a new instance of TimePickerDialog and return it
		     return new TimePickerDialog(getActivity(), this, hour, minute,
		             DateFormat.is24HourFormat(getActivity()));
		 }
		
		 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		     // Do something with the time chosen by the user			
			 edit_endtime.setText(String.format("%02d", (int) hourOfDay) + ":" + String.format("%02d", (int) minute));				 
			 endtime=edit_endtime.getText().toString()+":00";
			 updatetasktime(starttime,endtime);			 
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