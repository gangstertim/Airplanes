package airplane.classes;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;



public class LocationList {
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	public int arc;
	public int flighttime;
	//Contains all of the plane details at each turn of the game
	public ArrayList<PlaneDetails> locs;
	public double distance;
	public int totalFlightTime;
	public LocationList() {locs = new ArrayList<PlaneDetails>(); arc=0; flighttime=0;}
	public void insertLoc(PlaneDetails d)			{locs.add(d);}
	public void setLocAt(int t, PlaneDetails d) 	{locs.add(t, d);}
	public int size()								{return locs.size();}
	public Point2D.Double getLocAt(int t) 			{return locs.get(t).getLoc();}
	public double getBearingAt(int t)				{return locs.get(t).getBearing();}
	public int compareTo(LocationList locationList) {
		return (int)-(this.flighttime-locationList.flighttime);
	}
	
} 