package airplane.classes;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

//Describes the location and bearing of a plane at a specific point
public class PlaneDetails {
	Point2D.Double loc;
	double bearing;
	
	public PlaneDetails(Point2D.Double loc, double bearing) {};
	public Point2D.Double getLoc() 							{return loc;}
	void setLoc(Point2D.Double l)							{loc = l;}
	public double getBearing() 								{return bearing;}
	void setBearing(double b)								{bearing = b;}
	void copy(PlaneDetails pd)								{this.loc = pd.loc; 
															 this.bearing=pd.bearing;}
}
