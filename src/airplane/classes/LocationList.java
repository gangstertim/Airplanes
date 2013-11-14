package airplane.classes;

import java.util.ArrayList;

import org.apache.log4j.Logger;



public class LocationList {
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
	//Contains all of the plane details at each turn of the game
	ArrayList<PlaneDetails> locs;
	
	public LocationList() {locs = new ArrayList<PlaneDetails> ();}
	public void insertLoc(PlaneDetails d)			{locs.add(d);}
	public void setLocAt(int t, PlaneDetails d) 	{locs.get(t);}//.copy(d);}
	public PlaneDetails getLocAt(int t) 			{return locs.get(t);}
} 