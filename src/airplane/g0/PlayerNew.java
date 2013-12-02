package airplane.g0;

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import org.apache.log4j.Logger;

import airplane.classes.LocationList;
import airplane.classes.PlaneDetails;
import airplane.sim.Plane;
import airplane.sim.Player;
import airplane.sim.SimulationResult;

public class PlayerNew extends Player {
	private Logger logger = Logger.getLogger(this.getClass()); // for logging  
    //array of PlaneLocations; each PlaneLocation has the location of a single plane at every point in time
    public LocationList[] allPlaneLocs;  
    //this array stores the departure time necessary for each plane
    public int[] offsets; 
    //indexes is an array which maps planes in order of longest route to plane indices in allPlaneLocs
    Integer[] indexes;
    
    //dynamicPlanes is an array of those planes which fly a non-deterministic route as a result
    //of having coincident-but-opposite paths with other planes
    private ArrayList<Integer> dynamicPlanes = new ArrayList<Integer> ();
    //flowPlanes is an array of those planes which are in a flow
    private ArrayList<Integer> flowPlanes = new ArrayList<Integer> ();
    // reroutedPlanes is an array of those planes which intersect flows and thus need nondeterministic
    // directions
    private ArrayList<Integer> reroutedPlanes = new ArrayList<Integer> ();
    //reroutedPlanes take new routes based on intermediate waypoints.  This array holds those waypoints, such that
    //waypoints[i] has the waypoint for plane i
    public Point2D.Double[] waypoints;
    
    @Override
    public String getName() {
    	return "PlayerNew";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        offsets = new int[planes.size()];       
        allPlaneLocs = new LocationList[planes.size()];
        waypoints = new Point2D.Double[planes.size()];
        setInitialPaths(planes, allPlaneLocs);              
        FlightComparator comparator = new FlightComparator(allPlaneLocs);
        indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
             
        for(int i=0; i<planes.size(); i++) {
        	offsets[i] = planes.get(i).getDepartureTime();
        	
	        if (checkFlow(planes, i)) {
	    		flowPlanes.add(i);
	    		logger.info("plane "+i+" is in a flow");
	    	}
    	}
    
        for (int i=0; i<planes.size(); i++) {
            for (int j=i+1; j<planes.size(); j++)  {
            	if (checkCollisions(indexes[i], indexes[j], planes)) {
            		i=-1; 
            		break;
        		}
            }
        }     
    }
    
   boolean checkFlow(ArrayList<Plane> planes, int p) {
	   //checks to see if the a plane p is part of a flow
	   int planesWithSameRoute = 0;
	   
	   for (int i=0; i<planes.size(); i++) {
		   if (planes.get(p).getDestination().equals(planes.get(i).getDestination())) {
			   if (planes.get(p).getLocation().equals(planes.get(i).getLocation()))
				   planesWithSameRoute++;
		   }
	   } 
	   if (planesWithSameRoute > 7) return true;
	   else return false;
   }
   
   double goGreedy(ArrayList<Plane> planes, int planeNumber, int round) {    
	   //Tests all possible routes in range -10 to +10 and chooses the one which gets the plane closest to
	   //the destination and simulaneously valid
	   
		 Plane p = planes.get(planeNumber);
		 double initialBearing = p.getBearing();

		 double minDistance = Integer.MAX_VALUE;
		 double bestDirection = 0;
		 double toReturn = initialBearing;
	 
	     if((initialBearing == -1 && round > offsets[planeNumber])) 
	     	return calculateBearing(p.getLocation(), p.getDestination());      
	     
	     else if(initialBearing == -1 && round < offsets[planeNumber]) 
	    	return -1;
	     
	         
	     SimulationResult sr = startSimulation(planes, round);
	     boolean initialValid = sr.isSuccess();
	     logger.info("initvalid"+" "+sr.getReason());
	     
	     for(double i = -9; i <= 9; i=i+0.5) {
	    	 double bearn = initialBearing + i;
	    	 if(bearn < 0) bearn += 360;
	    	 if(bearn > 360) bearn -= 360;
	         bearn = bearn % 360;
	         p.setBearing(bearn);
	         SimulationResult srNew = startSimulation(planes, round);
	         boolean valid = srNew.isSuccess();
	             
	         double distance = getLocation(p.getLocation(), 1, bearn).distance(p.getDestination());
	         logger.info(valid);
	         if(distance < minDistance && valid) {
	        	 bestDirection = i;
	        	 minDistance = distance;
	         }
	     }

         if(!initialValid && bestDirection == 0) 
        	 bestDirection = -10;
	     toReturn += bestDirection;
	     p.setBearing(initialBearing);
	     
	     if(toReturn < 0) { toReturn += 360; }
	     if(toReturn > 360) { toReturn -= 360; }
	     logger.info("toReturn + " + toReturn);
	     return toReturn % 360;
     }
        
    boolean checkIfCrossesFlow (int a, int b) {
    	//checks to see if a plane will cross a flow path and returns true if it does.
    	//Also adds those planes which do cross flow paths to the array reroutedPlanes
    	//if a flow crosses a flow path, that flow is rerouted arbitrarily
    	//TODO: this can be done more intelligently
    	if (flowPlanes.contains(a) || flowPlanes.contains(b)) {
    		int smaller, larger;
    		if (flowPlanes.contains(a) && flowPlanes.contains(b)) {
        		if (a<b) {
        			smaller = a;
        			larger = b;
        		} else {
        			smaller = b;
        			larger = a;
        		}
        		reroutedPlanes.add(smaller);
        		waypoints[smaller]=calculateWaypoint(larger, smaller);
        	} else if (flowPlanes.contains(a)) {
        		reroutedPlanes.add(b);
        		waypoints[b]=calculateWaypoint(a, b);
        	} else {
        		reroutedPlanes.add(a);
        		waypoints[a]=calculateWaypoint(b, a);
        	}
        	return true;
    	} else return false;
    }
    
    Point2D.Double calculateWaypoint(int endPoint, int nearestTo) {
    	//This function returns a waypoint which is 10 units away from the endpoint
    	//of one plane's path.  The endpoint selected is that which is nearest to
    	//the second point provided.
    	Point2D.Double loc = allPlaneLocs[nearestTo].getLocAt(0);
    	Point2D.Double start = allPlaneLocs[endPoint].getLocAt(0);
    	Point2D.Double end = allPlaneLocs[endPoint].getLocAt(allPlaneLocs[endPoint].size()-1);
    	Point2D.Double dest = (loc.distance(start) < loc.distance(end)) ? start : end;
    	
    	double slope = (end.getY()-start.getY())/(end.getX()-start.getX());
    	if (start.getY() > end.getY()) slope = -slope;
    	slope = 1/Math.tan(slope);
    	Point2D.Double waypoint = 
    			new Point2D.Double(dest.getX() + 5*Math.sin(slope), dest.getY() + 5*Math.cos(slope));
    	return waypoint;
    }
    
    
    boolean checkCollisions(int a, int b, ArrayList<Plane> planes) {
        //Check collisions checks and repairs collisions between planes a and b
        boolean happened = false;
        LocationList first = allPlaneLocs[a];
        LocationList second = allPlaneLocs[b];
        int offsetA = offsets[a];  //amount to shift b's path, should the original path result in a collision
        int offsetB = offsets[b];
        //logger.info("checking " + a + " (" + offsets[a] + ") and " + b + " (" + offsets[b] + ").");
        boolean collisions = false; 
        int collisionCount = 0;
        boolean dp = false;
        
        outerWhile:
        while (!collisions) { //as long as there are no collisions...
            int flag = 1;
            //logger.info("currently in collisions loop");
            for (int i=0; i<first.size() && i-offsetB+offsetA<second.size(); i++) {
                if (i<offsetB-offsetA) {continue;}   //if i<offset, plane hasn't taken off yet; there can be no collisions 
                else {
                    if (first.getLocAt(i).distance(second.getLocAt(i-offsetB+offsetA)) < 6) {     
                    //ie, if a collision is detected...
                    	//Check to see if the planes cross a flow path.  If they don't, check for other collisions
                    	if (checkIfCrossesFlow(a,b)) {
                    		break outerWhile;
                    	}
                    	collisionCount++;
                        happened = true;
                        collisions = false;
                        flag = 0;
                        offsetB+=1;
                        break;
                    } 
                  
                    if(collisionCount > 25) {  //this is arbitrary
                        Plane pa = planes.get(a);
                        Plane pb = planes.get(b);
                        
                        double bdist = Math.abs(calculateBearing(pa.getLocation(), pa.getDestination())-calculateBearing(pb.getLocation(), pb.getDestination()));
                        if((bdist>170 && bdist<190) ) {
                            dynamicPlanes.add(b);
                            happened = false;
                            dp = true;
                            break outerWhile;
                        }          
                    }
                }
            }
            if(flag==1) break;
        }
        if (!dp) {
        	offsets[b]=offsetB;
        }

        return happened;
    }
        
        
    public void setInitialPaths(ArrayList<Plane> planes, LocationList[] allPlaneLocs) {
        //looks for straight-line path to each destination; assumes simultaneous start times
        for(int i = 0; i < planes.size(); i++) {
            allPlaneLocs[i] = new LocationList();
            double distance = planes.get(i).getLocation().distance(planes.get(i).getDestination());
                            
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

	double goAroundTheFlow(ArrayList<Plane> planes, int p) {
		double bearing = -1;
		Point2D.Double loc = planes.get(p).getLocation();
		Point2D.Double dest = planes.get(p).getDestination();
		double bear = planes.get(p).getBearing();
		boolean crosses = false;
		if (loc.distance(dest) < .5) return -1;
		else if (loc.distance(waypoints[p]) < .5) {
			for (int i=0; i<planes.size(); i++) {
				if (checkIfCrossesFlow(p, i)) {
    				waypoints[p] = calculateWaypoint(i, p);
    				bearing = moveTowards(bear, calculateBearing(loc, waypoints[p]));
    				crosses=true;
				}
				
				if (!crosses){
					waypoints[p]=dest;
					bearing = moveTowards(bear, calculateBearing(loc, dest));
				}
			}
		} else {
			bearing = moveTowards(bear, calculateBearing(loc, dest));
		}
		return bearing;
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
        } else if(Math.abs(currentBearing - targetBearing) < 10) {
            toReturn = targetBearing;
        } else if(targetBearing > currentBearing) {
            toReturn = currentBearing + 10;
        } else if(targetBearing < currentBearing) {
            toReturn = currentBearing - 10;
        }
        return toReturn % 360;
    } 
   
    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        //This is the only function that's actually called by the simulator to update the planes each turn
    	
    	for(int i = 0; i < planes.size(); i++) {
            if (planes.get(i).getLocation().distance(planes.get(i).getDestination())<=2) {}
            else if(reroutedPlanes.contains(i) && round > 0 && bearings[i] !=-2) {
            	bearings[i] = goAroundTheFlow(planes, i);
            } else if(dynamicPlanes.contains(i) && round > 0 && bearings[i]!=-2) {
                double bear = goGreedy(planes, i, round);
                if(bear!=-1) bearings[i]=bear;
            } else if (round < offsets[i] ) { }  //plane hasn't taken off yet
            else if (round >= allPlaneLocs[i].size()+offsets[i]) {  } //plane has landed
            else bearings[i] = allPlaneLocs[i].getBearingAt(round-offsets[i]); 
            //plane is flying
        
        }
        return bearings;
    }
}