package airplane.sim;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Plane extends GameObject {

	public static final double MAX_BEARING_CHANGE = 10.0;
	public static final double VELOCITY = 1.0;
	private int departureTime = 0;
	protected double bearing = -1;
	
	public static final int LEGAL_MOVE = 0;
	public static final int OUT_OF_BOUNDS = 1;
	public static final int ILLEGAL_MOVE = 2;

	private double destinationX;
	private double destinationY;
	private final ArrayList<Integer> dependencies;
	
	public int id; // this is new
	
	private ArrayList<Point2D.Double> allPoints = new ArrayList<Point2D.Double>();

	public void addToHistory(Point2D.Double point) {
		allPoints.add(point);
	}
	public ArrayList<Point2D.Double> getHistory() {
		return allPoints;
	}
	
	public Plane(double x, double y, double dx, double dy, int departureTime, ArrayList<Integer> list) {
		this.x = x;
		this.y = y;
		this.destinationX = dx;
		this.destinationY = dy;
		this.departureTime = departureTime;
		this.dependencies = list;
	}
	
	public Plane(Plane other) {
		this.x = other.x;
		this.y = other.y;
		this.destinationX = other.destinationX;
		this.destinationY = other.destinationY;
		this.departureTime = other.departureTime;
		this.bearing = other.bearing;
		this.id = other.id;
		this.dependencies = other.dependencies;
	}
	
	
	public double getVelocity() {
		return VELOCITY;
	}
	
	public double getBearing() {
		return bearing;
	}
	
	public void setBearing(double b) {
		bearing = b;
	}
	
	public int getDepartureTime(){
		return this.departureTime;
	}
		
	public Point2D.Double getDestination() {
		return new Point2D.Double(this.destinationX, this.destinationY);
	}
	
	public ArrayList<Integer> getDependencies() {
		if (dependencies == null) return null;
		else return (ArrayList<Integer>)(dependencies.clone());
	}
	
	public boolean dependenciesHaveLanded(double[] bearings) {
		if (dependencies == null) return true;
		for (int p : dependencies) 
			if (bearings[p] != -2) return false;
		return true;
	}
	
	public boolean isOn(int time) {
		return bearing >= 0;
	}
	
	public boolean intersects(double x, double y, int r) 
	{
		double d = Math.sqrt(Math.pow((x-this.x), 2.0) + Math.pow((y-this.y), 2.0));
		if (d <= r)
		{
			return true;
		}
		return false;
	}
	
	
	public boolean isLegalMove(double newBearing) {
		// because 0 and 360 are the same, this makes things a little easier
		if (newBearing == 360) newBearing = 0;
		
		// make sure it's in bounds
		if (newBearing < -2 || newBearing > 360) {
			System.err.println("ERROR! " + newBearing + " is not a legal bearing");
			return false;
		}
		
		// if the plane is taking off and we made it here, that's fine
		if (bearing == -1) return true;
		
		// here is a regular situation
		if (bearing < 360 - MAX_BEARING_CHANGE && bearing > MAX_BEARING_CHANGE) {
			if (Math.abs(bearing - newBearing) > MAX_BEARING_CHANGE) {
				System.err.println("ERROR! " + newBearing + " is too much of a change from old bearing of " + bearing);
				return false;
			}
			else return true;
		}
		// here's if they're close to the 0/360 border
		else {
			// if still within the max change, then it's fine
			if (Math.abs(bearing-newBearing) <= MAX_BEARING_CHANGE) return true;
			// otherwise, they must have crossed the border
			else {
				double diff = Math.abs(bearing-newBearing);
				if (360 - diff <= MAX_BEARING_CHANGE) return true;
				else {
					System.err.println("ERROR! " + newBearing + " is too much of a change from old bearing of " + bearing);
					return false;
				}
			}
		}
	}
	
	public int move(double newBearing) {
		// if the newBearing is -1, then leave it on the ground
		if ((bearing == -1 && newBearing == -1))
			return LEGAL_MOVE;
		// same if it's -2
		else if ((bearing == -2 && newBearing == -2))
			return LEGAL_MOVE;
		// but you can't change yourself to -1 or -2
		else if (newBearing == -1 || newBearing == -2)
			return ILLEGAL_MOVE;

		// see if it's a legal move
		if (!isLegalMove(newBearing)) {
			return ILLEGAL_MOVE;
		}
				
		// this allows bearing to be equal to 360, in which case we treat it as 0
		double radialBearing = newBearing % 360;
		
		radialBearing = (radialBearing-90) * Math.PI/180;
		double newx = this.x + (Math.cos(radialBearing)*VELOCITY);
		double newy = this.y + (Math.sin(radialBearing)*VELOCITY);
		
		// make sure they're still in bounds
		if (newx < 0 || newx > 100) {
			System.err.println("Error! new x-coordinate position " + newx + " is out of bounds!");
			return OUT_OF_BOUNDS;
		}
		if (newy < 0 || newy > 100) {
			System.err.println("Error! new y-coordinate position " + newy + " is out of bounds!");
			return OUT_OF_BOUNDS;
		}
				
		this.x = newx; //this.x + (Math.cos(bearing)*DISTANCE);
		this.y = newy; //this.y + (Math.sin(bearing)*DISTANCE);
		//this.bearing = bearing * 180/Math.PI;
		this.bearing = newBearing;
		
		return LEGAL_MOVE;
	}

	
}
