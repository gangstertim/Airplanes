package airplane.g0;

import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import airplane.classes.LocationList;
import airplane.classes.PlaneDetails;
import airplane.sim.Plane;
import airplane.sim.Player;

public class PlayerT extends Player {
	
	//array of PlaneLocations; each PlaneLocation has the location of a single plane at every point in time
	public LocationList[] allPlaneLocs;  
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
	@Override
	public String getName() {
		return "PlayerT";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		allPlaneLocs = new LocationList[planes.size()];
		setInitialPaths(planes, allPlaneLocs);
		
		for (int i=0; i<planes.size(); i++) {
			for (int j=0; j<planes.size(); j++) {
				checkCollisions(i, j);
			}
		}
		
	}
	
	boolean checkCollisions(int a, int b) {
		//checks for collisions between allPlaneLocs[a] and allPlaneLocs[b]
		//tries to correct them?
		//returns true if no collisions exist;
		return true;
	}
	public void setInitialPaths(ArrayList<Plane> planes, LocationList[] allPlaneLocs) {
		//looks for straight-line path to each destination; assumes simultaneous start times
		for(int i = 0; i < planes.size(); i++) {
			allPlaneLocs[i] = new LocationList();  //Instantiate a LocationList for each plane
			LocationList curr = allPlaneLocs[i];
			Plane p = planes.get(i);
			int time = 0;		
			
			//Set the initial location and initial bearing 
			curr.insertLoc(
				new PlaneDetails(
						new Point2D.Double(p.getX(), p.getY()), 		
						calculateBearing(p.getLocation(), p.getDestination())
				)
			);
			
			//Set all subsequent locations and bearings until the plane has successfully
			//found a path to its destination
			while (curr.getLocAt(time++).getLoc() != p.getDestination() ) {
				logger.info("loc: " + curr.getLocAt(time-1).getLoc());
				Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1).getLoc(), 1, curr.getLocAt(time-1).getBearing());
				
				curr.setLocAt(
					time,
					new PlaneDetails(currentLoc,calculateBearing(currentLoc, p.getDestination()))	
				);
			}
		}
	}
	
	
	public Point2D.Double getLocation(Point2D.Double currLocation, int steps, double bearing) {
		Point2D.Double toReturn = new Point2D.Double();
		double deltaX = Math.sin(Math.toRadians(bearing));
		double deltaY = Math.cos(Math.toRadians(bearing));

		toReturn.setLocation(currLocation.getX() + (steps*deltaX), currLocation.getY() + (steps*deltaY));
		return toReturn;
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
		//As everything is predetermined, this function only calls the bearings of each plane
		for(int i = 0; i < planes.size(); i++) {
			bearings[i] = allPlaneLocs[i].getLocAt(round).getBearing();
		}
		return bearings;
	}

}
