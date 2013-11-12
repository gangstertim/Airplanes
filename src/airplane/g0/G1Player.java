package airplane.g0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.*;
import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

import airplane.sim.Plane;
import airplane.sim.Player;

public class G1Player extends Player {
	
	private Logger logger = Logger.getLogger(this.getClass());
	@Override
	public String getName() {
		return "G1 Player";
	}

	@Override
	public void startNewGame(ArrayList<Plane> planes) {
			//eventually, we probably want the whole game to be deterministically computed
	}
	
	public Point2D.Double getLocation(Point2D.Double currLocation, int steps, double bearing) {
		Point2D.Double toReturn = new Point2D.Double();
		double deltaX = Math.sin(Math.toRadians(bearing));
		double deltaY = Math.cos(Math.toRadians(bearing));

		toReturn.setLocation(currLocation.getX() + (steps*deltaX), currLocation.getY() + (steps*deltaY));
		return toReturn;
	}
	
	public double getBearing(double proposedBearing, double currentBearing) {
		double toReturn = 0;
		
		if((currentBearing <= 0) || (Math.abs(proposedBearing-currentBearing) < 10)) { return proposedBearing; }
		
		double maxCurr = (currentBearing + 10) % 360;
		double minCurr = (currentBearing - 10) < 0 ? (currentBearing - 10 + 360) : (currentBearing - 10);
		
		if(proposedBearing > maxCurr) {toReturn = maxCurr;}
		else if(proposedBearing < minCurr) {toReturn = minCurr;}
		
		logger.info("Proposed Bearing: " + proposedBearing + " Current Bearing: " + currentBearing + " maxCurr: " + maxCurr + " minCurr: " + minCurr + " toReturn: " + toReturn);
		return (toReturn);
	}

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		
		double[] toReturn = new double[planes.size()];
		HashMap<Integer, HashSet<Integer>> doNotMove = new HashMap<Integer, HashSet<Integer>> ();
		
		for(int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			
			//get all the motherfucking planes in the air
			if(p.getDepartureTime() < round) {
				logger.info("Plane: " + i);
				toReturn[i] = getBearing(calculateBearing(p.getLocation(), p.getDestination()), bearings[i]);
				//bearings[i] = getBearing(calculateBearing(p.getLocation(),p.getDestination()),bearings[i]);
			}
						
		}
		
		//if we have any collisions, deal with that shit
		for(int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			
			//look 10 steps into the future
			//TODO: this assumes they continue on their current bearings, which may not be true
			for(int k = 0; k < 10; k ++) {
				for(int j = 0; j < planes.size(); j ++) {
					if(j == i) { continue; }
					else {
						Plane p2 = planes.get(j);
						
						//figure out where planes are going to be k timesteps from now
						Point2D.Double twoLocationAtK = getLocation(p2.getLocation(), k, bearings[j]);
						Point2D.Double oneLocationAtK = getLocation(p.getLocation(), k, bearings[i]);
												
						boolean go = !doNotMove.containsKey(j) || !doNotMove.get(j).contains(i);
						
						if(oneLocationAtK.distance(twoLocationAtK) <= 15 && go) {
							//MOVE ONE DA BITCHES
							//Could result in deadlocks
							logger.info("Plane FROM IF: " + j);
							if(p2.getDepartureTime() < round) {
								toReturn[j] = getBearing(calculateBearing(p2.getLocation(), new Point2D.Double(100,0)), bearings[j]);							
								
								HashSet<Integer> putter = new HashSet<Integer> ();
								putter.add(j);
								
								if(!doNotMove.containsKey(i)) { doNotMove.put(i, putter); }
								else { doNotMove.get(i).add(j); }
							}
						}
					}
				}
			}
						
		}
		
		return toReturn;
	}

}
