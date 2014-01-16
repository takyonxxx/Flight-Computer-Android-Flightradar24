package com.flightcomputer;
import javax.microedition.khronos.opengles.GL10;
import android.graphics.Color;
import com.nutiteq.components.MapPos;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.utils.Const;

import static javax.microedition.khronos.opengles.GL10.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class openGlUtils {
	private static float lX;
	private static float lY;
	private static float lZ;	
	private static Projection proj=new EPSG3857();
    public static void drawPoint(GL10 gl, MapPos point ,int ired,int igreen,int iblue) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 2 * 1);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertices = vbb.asFloatBuffer();
        lX = (float) proj.toInternal(point.x, point.y).x;
        lY = (float) proj.toInternal(point.x, point.y).y;  
        vertices.put(lX);
        vertices.put(lY);        
        vertices.position(0);
        float red =(1.0f/255)*ired;     
        float green =(1.0f/255)*igreen;     	
        float blue =(1.0f/255)*iblue;   
        gl.glColor4f(red,green,blue, 1.0f);
        gl.glPointSize(5);
        gl.glVertexPointer(2, GL_FLOAT, 0, vertices);
        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glDrawArrays(GL_POINTS, 0, 1);
        gl.glDisableClientState(GL_VERTEX_ARRAY);
    }      
    public static void drawPoints(GL10 gl, ArrayList<MapPos> points,ArrayList<Integer> colorlist,float width,boolean threeaxis) { 
      	try{
	    	ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 3 * points.size());
	        vbb.order(ByteOrder.nativeOrder());
	        FloatBuffer vertices = vbb.asFloatBuffer();         
	        FloatBuffer colorBuffer;   
	        ByteBuffer cbb = ByteBuffer.allocateDirect(4 * 4 * points.size());
	        cbb.order(ByteOrder.nativeOrder()); // Use native byte order (NEW)
	        colorBuffer = cbb.asFloatBuffer();           
	        for (int i = 0; i < points.size(); i++) {   
		    	lX = (float) proj.toInternal(points.get(i).x, points.get(i).y).x;
		        lY = (float) proj.toInternal(points.get(i).x, points.get(i).y).y; 
		        lZ = (float) points.get(i).z;
		        float red =(1.0f/255)*Color.red(colorlist.get(i));     
		        float green =(1.0f/255)*Color.green(colorlist.get(i));     	
		        float blue =(1.0f/255)*Color.blue(colorlist.get(i));          
		           vertices.put(lX);
		           vertices.put(lY);  
		           if(threeaxis){	   	  
		        	   vertices.put(lZ);  
		           }else
		        	   vertices.put(0); 
		           
		           colorBuffer.put(red); 
		           colorBuffer.put(green); 
		           colorBuffer.put(blue); 
		           colorBuffer.put(1f); 
	        } 
	        colorBuffer.position(0); 
	        vertices.position(0);
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	        gl.glLineWidth(width); 
	        
	        gl.glEnableClientState(GL_VERTEX_ARRAY);       
	        gl.glEnableClientState(GL_COLOR_ARRAY);
	        
	        gl.glVertexPointer(3, GL_FLOAT , 0, vertices);  	       
	        gl.glColorPointer(4, GL_FLOAT , 0,colorBuffer);
	        gl.glPushMatrix();
	        gl.glDrawArrays(GL_LINE_STRIP, 0, points.size()); 
	        gl.glPopMatrix();
	        
	        gl.glDisableClientState(GL_VERTEX_ARRAY); 
	        gl.glDisableClientState(GL_COLOR_ARRAY);
	       
      	}catch(Exception e){}
    }   
    public static void drawPoints2D(GL10 gl, ArrayList<MapPos>  points,  ArrayList<Integer> colorlist,float width) { 
      	ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 3 * points.size());
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertices = vbb.asFloatBuffer();         
        FloatBuffer colorBuffer;   
        ByteBuffer cbb = ByteBuffer.allocateDirect(4 * 4 * points.size());
        cbb.order(ByteOrder.nativeOrder()); // Use native byte order (NEW)
        colorBuffer = cbb.asFloatBuffer();           
        for (int i = 0; i < points.size(); i++) {   
	    	lX = (float) proj.toInternal(points.get(i).x, points.get(i).y).x;
	        lY = (float) proj.toInternal(points.get(i).x, points.get(i).y).y;   	       
	        float red =(1.0f/255)*Color.red(colorlist.get(i));     
	        float green =(1.0f/255)*Color.green(colorlist.get(i));     	
	        float blue =(1.0f/255)*Color.blue(colorlist.get(i));          
	           vertices.put(lX);
	           vertices.put(lY);   
	           colorBuffer.put(red); 
	           colorBuffer.put(green); 
	           colorBuffer.put(blue); 
	           colorBuffer.put(1f); 
        } 
        colorBuffer.position(0); 
        vertices.position(0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glLineWidth(width); 
        
        gl.glEnableClientState(GL_VERTEX_ARRAY);       
        gl.glEnableClientState(GL_COLOR_ARRAY);
        
        gl.glVertexPointer(2, GL_FLOAT , 0, vertices);  
       
        gl.glColorPointer(4, GL_FLOAT , 0,colorBuffer);
        gl.glPushMatrix();
        gl.glDrawArrays(GL_LINE_STRIP, 0, points.size()); 
        gl.glPopMatrix();
        
        gl.glDisableClientState(GL_VERTEX_ARRAY); 
        gl.glDisableClientState(GL_COLOR_ARRAY);
    }   
    public static void drawLine(GL10 gl, MapPos origin, MapPos destination,float ired,float igreen,float iblue) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 2 * 2);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertices = vbb.asFloatBuffer();        
            lX = (float) proj.toInternal(origin.x, origin.y).x;
	        lY = (float) proj.toInternal(origin.x, origin.y).y;  
	        vertices.put(lX);
	        vertices.put(lY);
	        lX = (float) proj.toInternal(origin.x, destination.y).x;
	        lY = (float) proj.toInternal(origin.x, destination.y).y;  
	        vertices.put(lX);
	        vertices.put(lY);        
        vertices.position(0);
        float red =(1.0f/255)*ired;     
        float green =(1.0f/255)*igreen;     	
        float blue =(1.0f/255)*iblue;   
        gl.glColor4f(red,green,blue, 1.0f);
        gl.glLineWidth(5.0f); 
        gl.glVertexPointer(2, GL_FLOAT, 0, vertices);
        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glDrawArrays(GL_LINE_STRIP, 0, 2);
        gl.glDisableClientState(GL_VERTEX_ARRAY);        
    }
private static final int NR_OF_CIRCLE_VERTS = 180;   
public static void drawCircle(GL10 gl, MapPos center, double r, Integer color,float zoomPow2) {     
	    double incomingdiameter=r/2;
    	lX= (float) proj.toInternal(center.x, center.y).x;
    	lY = (float) proj.toInternal(center.x, center.y).y;
    	ByteBuffer byteBuffer = ByteBuffer
                .allocateDirect((NR_OF_CIRCLE_VERTS + 2) * 3 * Float.SIZE / 8);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertices = byteBuffer.asFloatBuffer();
        float degreesPerVert = 360.0f / NR_OF_CIRCLE_VERTS;
        vertices.put(0);
        vertices.put(0);
        vertices.put(0);
        for (float tsj = 0; tsj < 360; tsj += degreesPerVert) {
        	vertices.put(android.util.FloatMath.cos(tsj * Const.DEG_TO_RAD));
        	vertices.put(android.util.FloatMath.sin(tsj * Const.DEG_TO_RAD));
        	vertices.put(0);
        }
        vertices.put(1);
        vertices.put(0);
        vertices.put(0);
        vertices.position(0);
        float diameter = (float) Math.max(
                Const.UNIT_SIZE * incomingdiameter / 7500000f, // based on GPS accuracy. This constant depends on latitude
                Const.UNIT_SIZE / zoomPow2 * 0.2f);         
      
        gl.glBindTexture(GL_TEXTURE_2D, 0);
        if(color==Color.RED)
        	gl.glColor4f(1,0,0,0.5f);     
        else if(color==Color.GREEN)
        	gl.glColor4f(0,1,0,0.5f);	
        else
        	gl.glColor4f(0,0,1,0.5f);	
        gl.glEnableClientState(GL_VERTEX_ARRAY); 
        gl.glVertexPointer(3, GL_FLOAT , 0, vertices);   
        gl.glPushMatrix();
        gl.glTranslatef(lX, lY, 0);        
        gl.glScalef(diameter ,diameter , 1);
        gl.glDrawArrays(GL_TRIANGLE_FAN, 0,  NR_OF_CIRCLE_VERTS + 2); 
        gl.glPopMatrix();        
        gl.glDisableClientState(GL_VERTEX_ARRAY);        
        
    }
}