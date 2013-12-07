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

public class Connect4 extends Player {
	
	//array of PlaneLocations; each PlaneLocation has the location of a single plane at every point in time
	public LocationList[] allPlaneLocs;  
	public int[] offsets;  //this array stores the departure time necessary for each plane
	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	private ArrayList<Integer> dynamicPlanes = new ArrayList<Integer> ();
	Integer[] indexes;
	public boolean fst = true; 
	public boolean depflag = false;
	@Override
	public String getName() {return "Connect4";}
	
	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		offsets = new int[planes.size()];			
		allPlaneLocs = new LocationList[planes.size()];
		setInitialPaths(planes, allPlaneLocs);
		FlightComparator comparator = new FlightComparator(allPlaneLocs);
		indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		
		
		for(int i=0; i<planes.size(); i++) {
			logger.info(indexes[i]+" "+allPlaneLocs[indexes[i]].flighttime);
		}
		
		for(int i=0; i<planes.size(); i++) {
			offsets[i] = planes.get(i).getDepartureTime();
		}	
		
		

		
	for (int i=0; i<planes.size(); i++) {
			
			ArrayList<Integer> dep = planes.get(i).getDependencies();
			int maxdep = 0;
			if(dep!=null)
			{
				depflag = true;
				
			for(int j= 0; j<dep.size(); j++)
			{
				if((allPlaneLocs[dep.get(j)].flighttime+offsets[dep.get(j)])>maxdep)
					maxdep =allPlaneLocs[dep.get(j)].flighttime+offsets[dep.get(j)];
					
			}
			if(offsets[i]<(maxdep))
			{
				offsets[i] = maxdep ;
				for (int k=0; k<planes.size(); k++) {
					if(k!=i && checkCollisions(k,i, planes)) {
						
						k=-1; 
					}
				}
				i = -1;
			}
			}
			
				
		}
	

	

		
		for (int i=0; i<planes.size(); i++) {
			for (int j=planes.size()-1; j>i; j--) {
				if(checkCollisions(indexes[i], indexes[j], planes)) {
					
						Arrays.sort(indexes, comparator);
					i=-1; break;
				}
			}
		}
		
	
	for (int i=0; i<planes.size(); i++) {
			
			ArrayList<Integer> dep = planes.get(i).getDependencies();
			int maxdep = 0;
			if(dep!=null)
			{
				
			for(int j= 0; j<dep.size(); j++)
			{
				if((allPlaneLocs[dep.get(j)].flighttime+offsets[dep.get(j)])>maxdep)
					maxdep =allPlaneLocs[dep.get(j)].flighttime+offsets[dep.get(j)];
					
			}
			if(offsets[i]<(maxdep))
			{
				offsets[i] = maxdep ;
				for (int k=0; k<planes.size(); k++) {
					if(k!=i && checkCollisions(k, i, planes)) {
						
						k=-1; 
					}
				}
				i = -1;
			}
			}
			
				
		}
		
		
		
		
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
		
		double ang = (amn+1)*sig*5;

		
		outerWhile:	
		while (!collisions) { //as long as there are no collisions...
			int flag = 1;		
			//logger.info("currently in collisions loop");
			for (int i=0; i<first.size() && i-offsetB+offsetA<second.size(); i++) {
				if (i<offsetB-offsetA) {continue;}   //if i<offset, plane hasn't taken off yet; there can be no collisions 
				else {
					if (first.getLocAt(i).distance(second.getLocAt(i-offsetB+offsetA)) <= 5.0) { 
						
						
						if(second.arc<18 && (collisionCount>second.arc)) {	
							
							if(formArc(b,planes,(ang/180.0)*Math.PI)) {	
								return true;
							} else {
								
								
								second = allPlaneLocs[b];
								offsetB = 0;
								happened=true;
								collisions = false;
								flag=0;
								break;
							}
						} else {
							collisionCount ++;
							happened=true;
							collisions = false;
							flag=0;
							
							second = allPlaneLocs[b];
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
					}
					*/
				}
			}
			if(flag==1) break;		
		}
		
		if(!dp) 
		{
			
			offsets[b]=offsetB;	
		}
		return happened;
	}
	
	public boolean formArc(int b,ArrayList<Plane> planes,double angle) {
		
		LocationList old = allPlaneLocs[b];
		LocationList curr = new LocationList();
	
		curr.arc=old.arc;
		curr.arc++;
		old.arc++;
		 int time=0;
		Plane p = planes.get(b);	

     	double dist = p.getLocation().distance(p.getDestination());
        double radius = Math.abs((dist/Math.sin(2*angle))*Math.cos(angle));
        double arcdist = (2*radius*Math.PI*(2*Math.abs(angle)/(2*Math.PI)));
        double change =-(2*(angle))/((arcdist));
        //logger.info(change);
        double thres = change*180.0/Math.PI;
        if(Math.abs(change*180.0/(Math.PI))>10.0) return false;
		double bearn = calculateBearing(p.getLocation(), p.getDestination());		
		
		//.info("radius"+radius);
		bearn = bearn+(angle+change/2.0)*180.0/Math.PI;

   	  	if(bearn < 0) { bearn += 360; }
   	  	if(bearn > 360) { bearn -= 360; }
   	  	bearn = bearn % 360;

		curr.insertLoc(new PlaneDetails(new Point2D.Double(p.getX(), p.getY()), bearn));
		
		double accbear = 0.0;
		while (curr.size() >= time+1 && curr.getLocAt(time).distance(p.getDestination()) >0.5) {
			time++;
			Point2D.Double currentLoc = getLocation(curr.getLocAt(time-1), 1, curr.getBearingAt(time-1));
			//logger.info(currentLoc);
			double newBearing = curr.getBearingAt(time-1)+((change)*180.0/Math.PI);
			accbear += change*180.0/Math.PI;
		   	  if(newBearing < 0.0) { newBearing += 360; }
		     if(newBearing > 360.0) { newBearing -= 360; }
		     newBearing = newBearing % 360.0;
				
			if(currentLoc.x>=100.0 || currentLoc.x<=0.0 ||currentLoc.y>=100.0 || currentLoc.y<=0.0) {
				allPlaneLocs[b]=old;
				return false;
			}
			
			//if (time <= p.getDepartureTime()) curr.setLocAt(time, new PlaneDetails(currentLoc, -1));
			curr.setLocAt(time, new PlaneDetails(currentLoc,newBearing));			
			// logger.info(change*180.0/Math.PI+" "+time+" "+newBearing);
		}
		if(depflag)
			curr.flighttime =  time;
			else
		curr.flighttime = time;
		allPlaneLocs[b]=curr;
		return true;
	}

	public void setInitialPaths(ArrayList<Plane> planes, LocationList[] allPlaneLocs) {
		//looks for straight-line path to each destination; assumes simultaneous start times
		for(int i = 0; i < planes.size(); i++) {
			allPlaneLocs[i] = new LocationList();
			double distance = allPlaneLocs[i].flighttime;
					
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
			if(depflag)
				curr.flighttime =  time;
				else
					curr.flighttime = time;
		}
		
	}
	
		
	public Point2D.Double getLocation(Point2D.Double currLocation, int steps, double bearing) {		
		double radialBearing = bearing % 360;	
		radialBearing = (radialBearing-90) * Math.PI/180;
		double newx =currLocation.getX() + (Math.cos(radialBearing)*1.0);
		double newy = currLocation.getY() + (Math.sin(radialBearing)*1.0);
		Point2D.Double toReturn = new Point2D.Double();
		toReturn.setLocation(newx, newy);
		return toReturn;
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
		//logger.info(round + " "+ planes.get(1).getLocation());
		for(int i = 0; i < planes.size(); i++) {
			if (planes.get(i).getLocation().distance(planes.get(i).getDestination())<=2) {
			} 
			else if( bearings[i] == -2) {}
			else if (round < offsets[i]+1 ) {}  //plane hasn't taken off yet
			else if (round >= allPlaneLocs[i].size()+offsets[i]+1) {} //plane has landed
			else { 
				bearings[i] = allPlaneLocs[i].getBearingAt(round-offsets[i]-1); 
			} //plane is flying	
		}
		return bearings;
	}
}
