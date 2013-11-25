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
	public boolean fst = true; 
	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		offsets = new int[planes.size()];
			
		allPlaneLocs = new LocationList[planes.size()];

		
		setInitialPaths(planes, allPlaneLocs);
		
		
		FlightComparator comparator = new FlightComparator(allPlaneLocs);
		indexes = comparator.createIndexArray();

		
		Arrays.sort(indexes, comparator);
		
		
		
		for(int i=0; i<planes.size(); i++)
		{
			//logger.info(indexes[i]+" "+allPlaneLocs[indexes[i]].distance);
		}
		
		
		for(int i=0; i<planes.size(); i++) offsets[i] = planes.get(i).getDepartureTime();
		
		
		for (int i=0; i<planes.size(); i++) {
			for (int j=i+1; j<planes.size(); j++) {if(checkCollisions(indexes[i], indexes[j], planes)){i=-1; break;}}
			//TODO:  feed planes to checkCollisions in order of longest path rather than order of appearance
			//note that i always maintains its path and j is always the corrected plane 
		}
		
	}
	
	 double goGreedy(ArrayList<Plane> planes, int planeNumber, int round) {
	        
         Plane p = planes.get(planeNumber);
         double initialBearing = p.getBearing();
         double minDistance = Double.MAX_VALUE;
         double bestDirection = 0;
         double toReturn = initialBearing;

         
 
         if((initialBearing == -1 && round > offsets[planeNumber])) {
        	 return calculateBearing(p.getLocation(), p.getDestination());
                 
         } else if(initialBearing == -1 && round < offsets[planeNumber]) {
        	 	return -1;
         }
         
         SimulationResult sr = startSimulation(planes, round);
         boolean initialValid = sr.isSuccess();
         int reason = sr.getReason();
         logger.info("initvalid"+" "+sr.getReason());
        boolean allvalid = true;
         for(double i = -9; i <= 9; i=i+0.5) {
        	 double bearn = initialBearing + i;
        	  if(bearn < 0) { bearn += 360; }
              if(bearn > 360) { bearn -= 360; }
           
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
	public boolean arcCollide(int b, ArrayList<Plane> planes)
	{
		
		
			for (int j=0; j<planes.size(); j++) {if(j!=b){return checkCollisionsNoAction(b, j, planes);}}
		
		return false;
	}
			boolean checkCollisionsNoAction(int a, int b, ArrayList<Plane> planes) {
				boolean happened = false;
				LocationList first = allPlaneLocs[a];
				LocationList second = allPlaneLocs[b];
				int offsetA = offsets[a];  //amount to shift b's path, should the original path result in a collision
				int offsetB = offsets[b];
				//logger.info("checking " + a + " (" + offsets[a] + ") and " + b + " (" + offsets[b] + ").");
				boolean collisions = false;
				boolean arccollision=false;
				int collisionCount = 0;
				boolean dp =false;
				int sig = second.arc%2;
				if(sig==0)
					sig = -1;
				else
					sig = 1;
				int amn = (int)second.arc/2;
				double ang = (amn+1)*sig*2;
				
				outerWhile:
					
				while (!collisions) { //as long as there are no collisions...
					int flag = 1;
					
					//logger.info("currently in collisions loop");
					for (int i=0; i<first.size() && i-offsetB+offsetA<second.size(); i++) {
						
						
						
						if (i<offsetB-offsetA) {continue;}   //if i<offset, plane hasn't taken off yet; there can be no collisions 
						else {
							if (first.getLocAt(i).distance(second.getLocAt(i-offsetB+offsetA)) < 6) { 
								
								
						
							
									
									return true;
								
								
								
								
							
							/*
							if(collisionCount > 50) {
								Plane pa = planes.get(a);
								Plane pb = planes.get(b);
								
								double bdist = Math.abs(calculateBearing(pa.getLocation(), pa.getDestination())-calculateBearing(pb.getLocation(), pb.getDestination()));
								//logger.info(bdist);
								if((bdist>170 && bdist<190) )
								{
								
								dynamicPlanes.add(b);
								
								happened = false;
								dp = true;
								break outerWhile;
								}
								
							}
							*/
						}
					}
		
					}	

				}
			
				
				return false;
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
		boolean arccollision=false;
		int collisionCount = 0;
		boolean dp =false;
		int sig = second.arc%2;
		if(sig==0)
			sig = -1;
		else
			sig = 1;
		int amn = (int)second.arc/2;
		double ang = (amn+1)*sig*2;
		
		outerWhile:
			
		while (!collisions) { //as long as there are no collisions...
			int flag = 1;
			
			//logger.info("currently in collisions loop");
			for (int i=0; i<first.size() && i-offsetB+offsetA<second.size(); i++) {
				
				
				
				if (i<offsetB-offsetA) {continue;}   //if i<offset, plane hasn't taken off yet; there can be no collisions 
				else {
					if (first.getLocAt(i).distance(second.getLocAt(i-offsetB+offsetA)) < 6.0) { 
						
						
				
						if(collisionCount>10 && second.arc==0 && planes.get(b).getLocation().distance(planes.get(b).getDestination())>40) 
						{
							
							
							if(formArc(b,planes,(-30/180.0)*Math.PI))
							{
							dp=true;
							return true;
							}
							else
							{
								
								if(formArc(b,planes,(30.0/180.0)*Math.PI))
								{
									
									return true;
								}
								else
								{
									collisionCount ++;
									happened=true;
									collisions = false;
									flag=0;
									offsetB+=1;
									break;	
									
								}
								
							}
						
						}
						else if(second.arc>0)
						{
							collisionCount ++;
							happened=true;
							collisions = false;
							flag=0;
							offsetB+=1;
							break;			
							
							
						}
						else
						{
							collisionCount ++;
							happened=true;
							collisions = false;
							flag=0;
							offsetB+=1;
							break;	
						}
						
						
						
						
						
						
					} 
					/*
					if(collisionCount > 50) {
						Plane pa = planes.get(a);
						Plane pb = planes.get(b);
						
						double bdist = Math.abs(calculateBearing(pa.getLocation(), pa.getDestination())-calculateBearing(pb.getLocation(), pb.getDestination()));
						//logger.info(bdist);
						if((bdist>170 && bdist<190) )
						{
						
						dynamicPlanes.add(b);
						
						happened = false;
						dp = true;
						break outerWhile;
						}
						
					}
					*/
				}
			}
			if(flag==1) {
				break;
			}	

		}
		//logger.info("necessary offset is: " + offsetB);
		if(!dp)
		offsets[b]=offsetB;
		//logger.info("offset " + a + ": " + offsets[a]);
		//logger.info("offset " + b + ": " + offsets[b]);
		
		return happened;
	}
	
	public boolean formArc(int b,ArrayList<Plane> planes,double angle)
	{
		
		
		LocationList curr = allPlaneLocs[b];
		curr.locs.clear();
		curr.arc++;
		 int time=0;
		Plane p = planes.get(b);
		
	

		

  
     	double dist = p.getLocation().distance(p.getDestination());
        double radius = (dist/Math.sin(2*angle))*Math.cos(angle);
        double change =-(2*Math.asin(1.0/(2*radius)));
        logger.info(change);
        double thres = change*180.0/Math.PI;
        if(Math.abs(change*180.0/(Math.PI))>10.0)
        {
        	
        	return false;
        }
		double bearn = calculateBearing(p.getLocation(), p.getDestination());
bearn = bearn+(angle+change/(dist))*180.0/Math.PI;

   	  if(bearn < 0) { bearn += 360; }
     if(bearn > 360) { bearn -= 360; }
     bearn = bearn % 360;
     
		curr.setLocAt(time,
				new PlaneDetails(
						new Point2D.Double(p.getX(), p.getY()), 		
						bearn)
				);
		double accbear = 0.0;
		while (curr.size() >= time+1 && curr.getLocAt(time).distance(p.getDestination()) > 4) {
			time++;
			Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));
			//logger.info(currentLoc);
			double newBearing = curr.getBearingAt(time-1)+((change)*180.0/Math.PI);
			accbear += change*180.0/Math.PI;
		   	  if(newBearing < 0.0) { newBearing += 360; }
		     if(newBearing > 360.0) { newBearing -= 360; }
		     newBearing = newBearing % 360.0;
		    
			
			if(currentLoc.x>=100.0 || currentLoc.x<=0.0 ||currentLoc.y>=100.0 || currentLoc.y<=0.0)
			{
				
				return false;
			}
			//if (time <= p.getDepartureTime()) curr.setLocAt(time, new PlaneDetails(currentLoc, -1));
			curr.setLocAt(time, new PlaneDetails(currentLoc,newBearing));
			
			// logger.info(change*180.0/Math.PI+" "+time+" "+newBearing);
		
			
		}
		
		while (curr.size() >= time+1 && curr.getLocAt(time).distance(p.getDestination()) >1) {

			Point2D.Double currentLoc = getLocation(curr.getLocAt(time), 1, curr.getBearingAt(time));

			double newBearing = calculateBearing(currentLoc, p.getDestination());
			//logger.info(time);
			
			if (curr.getBearingAt(time)==-2) {
				newBearing=-2; //how the hell are we getting illegal moves from -2 to other bearings???
			} else if (Math.abs(newBearing-curr.getBearingAt(time))>10.0) {
				
					int sign = (int) Math.signum(newBearing-curr.getBearingAt(time));
					newBearing = curr.getBearingAt(time)+sign*9.9;
					 	  if(newBearing < 0.0) { newBearing += 360; }
				     if(newBearing > 360.0) { newBearing -= 360; }
				     newBearing = newBearing % 360.0;
				     logger.info("hey");
					
						
						
			}
			time++;
			//if (time <= p.getDepartureTime()) curr.setLocAt(time, new PlaneDetails(currentLoc, -1));
			curr.setLocAt(time, new PlaneDetails(currentLoc,newBearing));
			
		}
		
		allPlaneLocs[b]=curr;

		
		
	return true;
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
			
			//TODO: this time++ in the while loop is bad style; should fix
			while (curr.size() >= time+1 && curr.getLocAt(time++).distance(p.getDestination()) > 1) {

				Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));

				double newBearing = calculateBearing(currentLoc, p.getDestination());
				//logger.info(time);
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
			if (planes.get(i).getLocation().distance(planes.get(i).getDestination())<=2)
			{
			}else if(dynamicPlanes.contains(i) && round > 0 && bearings[i]!=-2) {
                double bear = goGreedy(planes, i, round);
                if(bear!=-1)
                	bearings[i]=bear;
			}
			else if( bearings[i] == -2)
			{}
			else if (round < offsets[i] ) { }  //plane hasn't taken off yet
			else if (round >= allPlaneLocs[i].size()+offsets[i]) {  } //plane has landed
			else  { 
				bearings[i] = allPlaneLocs[i].getBearingAt(round-offsets[i]); 
			} //plane is flying
		
		}
		return bearings;
	}

}
