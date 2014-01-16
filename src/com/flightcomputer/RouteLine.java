package com.flightcomputer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import com.nutiteq.components.MapPos;
public class RouteLine {
    private boolean visible = false;    
    private  ArrayList<MapPos> routePos=null;
    ArrayList<Integer> routecolor=null;  
    openGlUtils glUtil=new openGlUtils();  
    float width;
    public RouteLine() {               
    }    
    public void draw(GL10 gl){
        if(!visible){
            return;
        }         
        try{	
        	openGlUtils.drawPoints2D(gl,routePos,routecolor,width);    
        }catch(Exception e){}
    }    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public void setRoute(ArrayList<MapPos> mapPos,ArrayList<Integer> colorlist,float width) {
    	this.routePos=mapPos;    
    	this.routecolor=colorlist;    
    	this.width=width;    	
    }
}