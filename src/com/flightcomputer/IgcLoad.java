package com.flightcomputer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class IgcLoad extends Activity {
	public String root = Environment.getExternalStorageDirectory().getAbsolutePath();
	private Button btnClearAll, btnExit;
	private ListView lvCheckBox;
	private  String path;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.igc_list_view);  
        btnExit = (Button)findViewById(R.id.btnExitIgc);
        btnClearAll = (Button)findViewById(R.id.btnClearAll);
        lvCheckBox = (ListView)findViewById(R.id.lvCheckBox);
        lvCheckBox.setClickable(true); 
        lvCheckBox.setChoiceMode(ListView.CHOICE_MODE_SINGLE);   
        lvCheckBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {	       
	         @SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {	       
	        	    Object o = lvCheckBox.getItemAtPosition(position);	
	        	    String[] values = o.toString().split("igc=");
	        	    path=values[1].trim().substring(0,  values[1].trim().length()-1);
	        	    AlertDialog alertDialog = new AlertDialog.Builder(IgcLoad.this).create();
	        	    alertDialog.setTitle("Igc Item Selected");
		          	alertDialog.setMessage(path);
		          	alertDialog.setButton("LOAD IGC",new DialogInterface.OnClickListener(){
		          	    public void onClick(DialogInterface dialog, int which) {
		          	    	Intent intent = new Intent();
		                 		intent.putExtra("file", path);
		                 		setResult(RESULT_OK, intent);
		                 		finish();
		          	    return;
		          	  } });     		          	 
		          	  alertDialog.setButton2("CANCEL",new DialogInterface.OnClickListener(){
		            	    public void onClick(DialogInterface dialog, int which) {		            	    	
		            	    return;
		            	  } });   	
		          	  alertDialog.setButton3("DELETE",new DialogInterface.OnClickListener(){
		            	    public void onClick(DialogInterface dialog, int which) {
		            	    	File file = new File(root + path);
			        	       	if(file.exists())
			        	       	{
			        	       		boolean deleted = file.delete();
			        	       		showFiles();
			        	    	}
		            	    return;
		            	  } });   
		          	  alertDialog.show();
	         }
	         });
        btnClearAll.setOnClickListener(new OnClickListener() 
        {           
            @Override
            public void onClick(View arg0) 
            {
                for(int i=0 ; i < lvCheckBox.getAdapter().getCount(); i++)
                {
                    Object o = lvCheckBox.getItemAtPosition(i);	
	        	    String[] values = o.toString().split("igc=");
	        	    path=values[1].trim().substring(0,  values[1].trim().length()-1);
	        	    if(path.contains("/VarioLog/"))
	        	    	{
		        	    	File file = new File(root + path);
		        	       	if(file.exists())
		        	       	{
		        	       		boolean deleted = file.delete();
		        	    	}
	        	    	}
                }    
                showFiles();
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
        showFiles();
    } 
	private void deligc(String dir)
    {
    	File file = new File(root + dir);
       	if(file.exists())
       	{
       		boolean deleted = file.delete();
       		if(deleted)
       		 showFiles();
       	}else
       	 Toast.makeText(getBaseContext(), path + " not found", Toast.LENGTH_SHORT).show();
    }
	static final ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String,String>>(); 
	public void showFiles() { 
		list.clear();
		AsyncTask<String, Void, String> extractionTask = new AsyncTask<String, Void, String>() {
			private ProgressDialog mProgressDialog;			
			protected void onPreExecute() {
				super.onPreExecute();
				mProgressDialog = new ProgressDialog(IgcLoad.this);
				mProgressDialog.setCancelable(false);
				mProgressDialog.setMessage("Loading IGC Files Please wait...");
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setProgress(0);
				mProgressDialog.show();
			}

			protected String doInBackground(String... connection) {
				// Here you will get your arraylist with the Method listftpitems()
				File home = new File(root);
				try{
				walkdir(home, root);
				}catch(Exception e){finish();}
				return null;
			}

			protected void onPostExecute(String result) {
				  SimpleAdapter adapter = new SimpleAdapter(
						  IgcLoad.this,
			        		list,
			        		R.layout.custom_row_view,
			        		new String[] {"igc","dec"},
			        		new int[] {R.id.text1,R.id.text2}
			        		);				
				lvCheckBox.setAdapter(adapter);			
				mProgressDialog.dismiss();
			}
			
			public void walkdir(File dir, String root) {				
				File listFile[] = dir.listFiles();		
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				if (listFile != null) {
					for (int i = 0; i < listFile.length; i++) {
						if (listFile[i].isDirectory()) {
							walkdir(listFile[i], root);
						} else {
							double bytes = listFile[i].length();
							double kilobytes = (bytes / 1024);
							//double megabytes = (kilobytes / 1024);							
							String name = listFile[i].getName();
							String formattedDateString = sdf.format(listFile[i].lastModified());
							String dec= "Size: " + String.format("%.2f", kilobytes)+ " KB" + "  Created: " + formattedDateString;
							if (name.endsWith(".igc") || name.endsWith(".IGC")) {								
								HashMap<String,String> temp = new HashMap<String,String>();
						    	temp.put("igc",listFile[i].getPath().replace(root, ""));
						    	temp.put("dec", dec);						    	
						    	list.add(temp);
						    	
							}
						}
					}
				}
			}

		};
		extractionTask.execute();
	}
}