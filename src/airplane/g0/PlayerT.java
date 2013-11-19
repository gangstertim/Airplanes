package airplane.g0;

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import airplane.classes.LocationList;
import airplane.classes.PlaneDetails;
import airplane.sim.Plane;
import airplane.sim.Player;
import airplane.sim.SimulationResult;

public class PlayerT extends Player {
	
	//array of PlaneLocations; each PlaneLocation has the location of a single plane at every point in time
	public LocationList[] allPlaneLocs;  
	public int[] offsets;  //this array stores the departure time necessary for each plane
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	private ArrayList<Integer> dynamicPlanes = new ArrayList<Integer> ();
	Integer[] indexes;
	@Override
	public String getName() {
		return "PlayerT";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		offsets = new int[planes.size()];
		
		
	
			
		allPlaneLocs = new LocationList[planes.size()];

		
		setInitialPaths(planes, allPlaneLocs);
		
		
		FlightComparator comparator = new FlightComparator(allPlaneLocs);
		indexes = comparator.createIndexArray();

		
		Arrays.sort(indexes, comparator);
		
		
		
		for(int i=0; i<planes.size(); i++)
			logger.info(indexes[i]+" "+allPlaneLocs[indexes[i]].distance);
		
		
		
		for(int i=0; i<planes.size(); i++) offsets[i] = planes.get(i).getDepartureTime();
		
		
		for (int i=0; i<planes.size(); i++) {
			for (int j=i+1; j<planes.size(); j++) {if(checkCollisions(indexes[i], indexes[j], planes)){i=0; }}
			//TODO:  feed planes to checkCollisions in order of longest path rather than order of appearance
			//note that i always maintains its path and j is always the corrected plane 
		}
		
	}
	
	double goGreedy(ArrayList<Plane> planes, int planeNumber, int round) {
	
		Plane p = planes.get(planeNumber);
		double initialBearing = p.getBearing();
		double minDistance = Double.MAX_VALUE;
		int bestDirection = 0;
		double toReturn = initialBearing;

		SimulationResult sr = startSimulation(planes, round);
		boolean initialValid = sr.isSuccess();
		int roundFail = Integer.MAX_VALUE;
	
		if((initialBearing == -1 && round > p.getDepartureTime())) {
			return calculateBearing(p.getLocation(), p.getDestination());
		}		

		for(int i = -10; i <= 10; i ++) {
			p.setBearing(initialBearing + i);
			SimulationResult srNew = startSimulation(planes, round);
			boolean valid = srNew.isSuccess();
			double distance = getLocation(p.getLocation(), 1, initialBearing+i).distance(p.getDestination());
		
			if(distance < minDistance && valid) {
				bestDirection = i;
				minDistance = distance;
			}
		}
		
		if(!initialValid && bestDirection == 0) {
			bestDirection = -10;
		}
		
		toReturn += bestDirection;
		p.setBearing(initialBearing);
		
		if(toReturn < 0) { toReturn += 360; }
		if(toReturn > 360) { toReturn -= 360; }
		logger.info("toReturn + " + toReturn);
		return toReturn % 360;
	}
	
	double moveTowards(double currentBearing, double targetBearing) {
		currentBearing = currentBearing + 360;
		double toReturn = currentBearing;
		
		if(Math.abs(currentBearing-targetBearing + 360) < Math.abs(currentBearing - targetBearing)) {
			if(Math.abs(currentBearing - targetBearing + 360) < 10) {
				toReturn = targetBearing;
			} else if(targetBearing > currentBearing) {
				toReturn = currentBearing - 10;
			} else if(targetBearing < currentBearing) {
				toReturn = currentBearing + 10;
			}
		}
		else if(Math.abs(currentBearing - targetBearing) < 10) {
			toReturn = targetBearing;
		} else if(targetBearing > currentBearing) {
			toReturn = currentBearing + 10;
		} else if(targetBearing < currentBearing) {
			toReturn = currentBearing - 10;
		}
		return toReturn % 360;
	}
	
	//Check collisions checks and repairs collisions between planes a and b; 
	boolean checkCollisions(int a, int b, ArrayList<Plane> planes) {
		boolean happened = false;
		LocationList first = allPlaneLocs[a];
		LocationList second = allPlaneLocs[b];
		int offsetA = offsets[a];  //amount to shift b's path, should the original path result in a collision
		int offsetB = offsets[b];
		//logger.info("checking " + a + " (" + offsets[a] + ") and " + b + " (" + offsets[b] + ").");
		boolean collisions = false; 
		
		while (!collisions) { //as long as there are no collisions...
			int flag = 1;
			//logger.info("currently in collisions loop");
			for (int i=0; i<first.size() && i-offsetB+offsetA<second.size(); i++) {
				if (i<offsetB-offsetA) {continue;}   //if i<offset, plane hasn't taken off yet; there can be no collisions 
				else {
					
					if (first.getLocAt(i).distance(second.getLocAt(i-offsetB+offsetA)) < 6) { 
						
						logger.info("Collision between " + a + " & " + b + "at " + i);
						happened=true;
						collisions = false;
						flag=0;
						offsetB+=5;
						break;
					} 
					else
					{
						
						
					}
				}
			}
			if(flag==1)
				break;
		
			
		}
		//logger.info("necessary offset is: " + offsetB);
		offsets[b]=offsetB;
		//logger.info("offset " + a + ": " + offsets[a]);
		//logger.info("offset " + b + ": " + offsets[b]);
		
		return happened;
	}
	public void setInitialPaths(ArrayList<Plane> planes, LocationList[] allPlaneLocs) {
		//looks for straight-line path to each destination; assumes simultaneous start times
		for(int i = 0; i < planes.size(); i++) {
			allPlaneLocs[i] = new LocationList();
			double distance = planes.get(i).getLocation().distance(planes.get(i).getDestination()) + planes.get(i).getDepartureTime();
					
			allPlaneLocs[i].distance = distance;  
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
			
			//TODO: this time++ in the while loop is bad style; should fix
			while (curr.size() >= time+1 && curr.getLocAt(time++).distance(p.getDestination()) > 1) {

				Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));

				double newBearing = calculateBearing(currentLoc, p.getDestination());

				if (curr.getBearingAt(time-1)==-2) {
					newBearing=-2; //how the hell are we getting illegal moves from -2 to other bearings???
				} else if (Math.abs(newBearing-curr.getBearingAt(time-1))>10) {
					newBearing = (newBearing>curr.getBearingAt(time-1) ? newBearing+10 : newBearing-10);
				} else if(currentLoc.distance(p.getDestination()) < 0.1) {
					newBearing = -2;
				}
				
				//if (time <= p.getDepartureTime()) curr.setLocAt(time, new PlaneDetails(currentLoc, -1));
				else curr.setLocAt(time, new PlaneDetails(currentLoc,newBearing));
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
			if(bearings[i] == -2) {  }
			else if (round < offsets[i] ) { }  //plane hasn't taken off yet
			else if (round >= allPlaneLocs[i].size()+offsets[i]) {  } //plane has landed
			else  { 
				bearings[i] = allPlaneLocs[i].getBearingAt(round-offsets[i]); 
			} //plane is flying
		
		}
		return bearings;
	}

}
