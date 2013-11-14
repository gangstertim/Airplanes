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
	public int[] offsets;  //this array stores the departure time necessary for each plane
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	
	@Override
	public String getName() {
		return "PlayerT";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		allPlaneLocs = new LocationList[planes.size()];
		offsets = new int[planes.size()];
		setInitialPaths(planes, allPlaneLocs);
		
		for (int i=0; i<planes.size(); i++) {
			for (int j=i+1; j<planes.size(); j++) {checkCollisions(i, j);}
			//TODO:  The planes should be sent to checkCollisions in order, from longest distance to travel to shortest
		}
		
	}
	
	void checkCollisions(int a, int b) {
		LocationList first = allPlaneLocs[a];
		LocationList second = allPlaneLocs[b];
		int offset = 0;  //amount to shift b's path, should the original path result in a collision
		boolean collisions = false;
		while (!collisions) { //as long as there are no collisions...
			for (int i=0; i<first.size(); i++) {
				if (i<offset) continue;  //if i<offset, b hasn't taken off yet.
				if (first.getLocAt(i).distance(second.getLocAt(i-offset)) < 5) { 
					logger.info("Collision between " + a + " & " + b + "at " + i);
					collisions = true;
					offset+=5;
					break;
				} else collisions = false;
			}
		}
		logger.info("necessary offset is: " + offset);
		offsets[b]=offset;
	}
	public void setInitialPaths(ArrayList<Plane> planes, LocationList[] allPlaneLocs) {
		//looks for straight-line path to each destination; assumes simultaneous start times
		for(int i = 0; i < planes.size(); i++) {
			allPlaneLocs[i] = new LocationList();  //Instantiate a LocationList for each plane
			LocationList curr = allPlaneLocs[i];
			Plane p = planes.get(i);
			int time = 0;		
			
			//Set the initial location and initial bearing 
			logger.info("PX: " + p.getX());
			curr.insertLoc(
				new PlaneDetails(
						new Point2D.Double(p.getX(), p.getY()), 		
						calculateBearing(p.getLocation(), p.getDestination())
				)
			);
			
			//Set all subsequent locations and bearings until the plane has successfully
			//found a path to its destination
			
			//TODO: this time++ in the while loop is bad style; should fix
			//TODO: we can't change the bearing > +/- 10
			while (curr.size() >= time+1 && curr.getLocAt(time++).distance(p.getDestination()) > 0.5) {
				//logger.info("time: " + time);
				//logger.info("loc: " + curr.getLocAt(time-1));
				//logger.info("destination: " + p.getDestination());
				Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));
				//logger.info("currloc: " + currentLoc);
				
				curr.setLocAt(
					time,
					new PlaneDetails(currentLoc,calculateBearing(currentLoc, p.getDestination()))	
				);
			}
		}
	}
	
	
	public Point2D.Double getLocation(Point2D.Double currLocation, int steps, double bearing) {
		//logger.info("bearing: " + bearing);
		Point2D.Double toReturn = new Point2D.Double();
		double deltaX = Math.sin(Math.toRadians(bearing));
		double deltaY = -1*Math.cos(Math.toRadians(bearing));

		toReturn.setLocation(currLocation.getX() + (steps*deltaX), currLocation.getY() + (steps*deltaY));
		return toReturn;
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
		for(int i = 0; i < planes.size(); i++) {
			if (round < offsets[i]) bearings[i] = -1;
			else bearings[i] = allPlaneLocs[i].getBearingAt(round+offsets[i]);
		}
		return bearings;
	}

}
