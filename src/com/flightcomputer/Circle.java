package com.flightcomputer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import com.nutiteq.components.MapPos;
public class Circle {
    private boolean visible = false;       
    openGlUtils glUtil=new openGlUtils();    
    private  ArrayList<MapPos> mapPos=null;   
    private  ArrayList<Double> diameter=null;   
    private  ArrayList<Integer> color=null;  
    public Circle() {               
    }    
    public void draw(GL10 gl,float zoomPow2){
        if(!visible){
            return;
        }         
        try{
        	 for (int i = 0; i < mapPos.size(); i++) {   
        		 openGlUtils.drawCircle(gl,mapPos.get(i),diameter.get(i),color.get(i),zoomPow2);   
        	 }
        }catch(Exception e){}
    }    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public void setCircle( ArrayList<MapPos> mapPos,ArrayList<Double> diameter, ArrayList<Integer> color) {
    	this.mapPos=mapPos;    
    	this.diameter=diameter; 
    	this.color=color;
    }
}