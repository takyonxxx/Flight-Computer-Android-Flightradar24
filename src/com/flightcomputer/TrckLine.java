package com.flightcomputer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import com.nutiteq.components.MapPos;
public class TrckLine {
    private boolean visible = false,threeaxis=false;    
    private  ArrayList<MapPos> trckPos=null;
    ArrayList<Integer> trckcolor=null;
    openGlUtils glUtil=new openGlUtils();    
    float width;
    public TrckLine() {               
    }    
    public void draw(GL10 gl){
        if(!visible){
            return;
        }         
        try{	       	
	        openGlUtils.drawPoints(gl,trckPos,trckcolor,width,threeaxis);  
        	/*for (int i = 0; i < trckPos.size(); i++) {   
        		MapPos pos=trckPos.get(i);
        		openGlUtils.drawPoint(gl,pos,Color.red(trckcolor.get(i)),Color.green(trckcolor.get(i)),Color.blue(trckcolor.get(i)));  
        	}*/
        }catch(Exception e){}
    }    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public void setRoute(ArrayList<MapPos> mapPos,ArrayList<Integer> colorlist,float width,boolean threeaxis) {
    	this.trckPos=mapPos;    
    	this.trckcolor=colorlist;    
    	this.width=width;
    	this.threeaxis=threeaxis;    	
    }
}