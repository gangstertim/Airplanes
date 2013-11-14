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
			//TODO:  feed planes to checkCollisions in order of longest path rather than order of appearance
			//note that i always maintains its path and j is always the corrected plane 
		}
		
	}
	
	//Check collisions checks and repairs collisions between planes a and b; 
	void checkCollisions(int a, int b) {
		LocationList first = allPlaneLocs[a];
		LocationList second = allPlaneLocs[b];
		int offset = 0;  //amount to shift b's path, should the original path result in a collision
		boolean collisions = false;  
		while (!collisions) { //as long as there are no collisions...
			logger.info("currently in collisions loop");
			for (int i=0; i<first.size(); i++) {
				//if i<offset, b hasn't taken off yet.  
				if (i<offset) {collisions=true; continue;}
				else if ((first.getBearingAt(i) == -2) || first.getLocAt(i).distance(second.getLocAt(i-offset)) <= 5) { 
					logger.info("Collision between " + a + " & " + b + "at " + i);
					collisions = false;
					offset+=5;
					break;
				} 
			} 
		}
		logger.info("necessary offset is: " + offset);
		offsets[b]=offsets[a]+offset;
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
			while (curr.size() >= time+1 && curr.getLocAt(time++).distance(p.getDestination()) > 0.5) {
				//logger.info("time: " + time);
				//logger.info("loc: " + curr.getLocAt(time-1));
		 		//logger.info("destination: " + p.getDestination());
				Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));
				//logger.info("currloc: " + currentLoc);
				
				double newBearing = calculateBearing(currentLoc, p.getDestination());
				//TODO 1:  I think these checks are the right idea, but this doesn't seem to work
			
				if (curr.getBearingAt(time-1)==-2) {
					logger.info("OMG WE IN DA CITY");
					newBearing=-2; //how the hell are we getting illegal moves from -2 to other bearings???
				}
				else if (Math.abs(newBearing-curr.getBearingAt(time-1))>10) {
					newBearing = (newBearing>curr.getBearingAt(time-1) ? newBearing+10 : newBearing-10);
				}
				else if(currentLoc.distance(p.getDestination()) < .1) {
					newBearing = -2;
				}
				curr.setLocAt(time, new PlaneDetails(currentLoc,newBearing));
				//END TODO 1
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
			if(bearings[i] == -2) { bearings[i] = -2; }
			else if (round < offsets[i]) { bearings[i] = -1; }  //plane hasn't taken off yet
			else if (round >= allPlaneLocs[i].size()+offsets[i]) { bearings[i]=-2; } //plane has landed
			else  { bearings[i] = allPlaneLocs[i].getBearingAt(round-offsets[i]); } //plane is flying
		}
		return bearings;
	}

}
