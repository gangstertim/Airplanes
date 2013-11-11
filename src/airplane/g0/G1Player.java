package airplane.g0;

import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Point2D;

import airplane.sim.Plane;
import airplane.sim.Player;

public class G1Player extends Player {

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

	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round,
			double[] bearings) {
		for(int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			
			//get all the motherfucking planes in the air
			if(p.getDepartureTime() < round) {
				//probably should check if this is a legal bearing, but shouldn't be a risk
				bearings[i] = calculateBearing(p.getLocation(),p.getDestination());
			}
						
		}
		
		//if we have any collisions, deal with that shit
		for(int i = 0; i < planes.size(); i++) {
			Plane p = planes.get(i);
			
			//look 10 steps into the future
			//TODO: this assumes they continue on their current bearings, which may not be true
			for(int k = 0; k < 10; k ++) {
				for(int j = 0; j < planes.size(); j ++) {
					Plane p2 = planes.get(j);
					
					//figure out where planes are going to be k timesteps from now
					Point2D.Double twoLocationAtK = getLocation(p2.getLocation(), k, bearings[j]);
					Point2D.Double oneLocationAtK = getLocation(p.getLocation(), k, bearings[i]);
					
					if(oneLocationAtK.distance(twoLocationAtK) <= 5) {
						//MOVE ONE DA BITCHES
						
					}
				}
			}
						
		}
		
		return bearings;
	}

}
