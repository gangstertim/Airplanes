package airplane.classes;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;



public class LocationList {
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
	//Contains all of the plane details at each turn of the game
	ArrayList<PlaneDetails> locs;
	
	public LocationList() {locs = new ArrayList<PlaneDetails> ();}
	public void insertLoc(PlaneDetails d)			{locs.add(d);}
	public void setLocAt(int t, PlaneDetails d) 	{locs.add(t, d);}
	public int size()								{return locs.size();}
	public Point2D.Double getLocAt(int t) 			{return locs.get(t).getLoc();}
	public double getBearingAt(int t)				{return locs.get(t).getBearing();}
	
} 