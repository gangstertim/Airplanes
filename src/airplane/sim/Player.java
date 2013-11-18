
package airplane.sim;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Chris Murphy
 */
public abstract class Player {
	private GameConfig myConfig;
	protected boolean continueSimulation = true;
	
	public void setMyConfig(GameConfig myConfig) {
		this.myConfig = myConfig;
	}
	protected int getConfigNumPlanes() {
		return myConfig.num_planes;
	}

    /**
     * Returns the name for this player
     */
    public abstract String getName();
    
    /**
     * Called on the player when it is instantiated
     */
	public void Register()
	{
		//Do nothing is OK!
	}
	
	/**
	 * Called on the player when a new game starts
	 */
	public abstract void startNewGame(ArrayList<Plane> planes);

	/**
	 * Allows you to move the planes 
	 */
	public abstract double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings);
	
	/*
	 * This is used when you're running your own simulation
	 */
	protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
		// not implemented
		return bearings;
	}
	
	/*
	 * This is a helper method that will calculate the bearing between two points
	 */
    public static double calculateBearing(double x1, double y1, double x2, double y2) {

		double dx = x1 - x2;
		double dy = y1 - y2;

		double bearing = Math.atan2(dy, dx) * 180 / Math.PI - 90;

		if (bearing < 0) bearing += 360;

		return bearing;
    }
    
    public static double calculateBearing(Point2D.Double start, Point2D.Double end) {
    	return calculateBearing(start.x, start.y, end.x, end.y);
    }

    /*
     * This runs a simulation from the specified state.
	 * It returns the SimulationResult indicating what happened.
     */
    protected SimulationResult startSimulation(ArrayList<Plane> planes, int round) {
    	continueSimulation = true;
    	int x = 0;
    	// make a copy of all the Planes (so the originals don't get affected)
    	ArrayList<Plane> simPlanes = new ArrayList<Plane>();
    	for (Plane p : planes) {
    		simPlanes.add(new Plane(p));
    	}
    	// make an array of all the bearings
    	double simBearings[] = new double[simPlanes.size()];
    	for (int i = 0; i < simBearings.length; i++) {
    		simBearings[i] = simPlanes.get(i).getBearing();
    	}
    	// count how many have landed
    	int landed = 0;
    	for (double b : simBearings) {
    		if (b == -2) landed++;
    	}
    	
    	// now loop through the simulation
    	while(landed != simBearings.length && continueSimulation && x < 15) {
    		// update the round number
    		round++;
    		x++;
    		// the player simulates the update of the planes
    		simBearings = simulateUpdate(simPlanes, round, simBearings);
    		// if it's null, then don't bother
    		if (simBearings == null) return new SimulationResult(SimulationResult.NULL_BEARINGS, round, simPlanes);
    		// make sure no planes took off too early
			for (int i = 0; i < simPlanes.size(); i++) {
				if (simPlanes.get(i).getDepartureTime() > round && simBearings[i] > -1) {
					return new SimulationResult(SimulationResult.TOO_EARLY, round, simPlanes);
				}
			}
    		// update the locations
    		for (int i = 0; i < simPlanes.size(); i++) {
    			Plane p = simPlanes.get(i);
    			if (simBearings[i] >= 0) {
    				if (p.move(simBearings[i]) == Plane.LEGAL_MOVE) {
	    				// see if it landed, i.e. it's within 0.5 of its destination
	    				if (p.getLocation().distance(p.getDestination()) <= 0.5) {
	    					// the plane has landed
	    					p.setBearing(-2);
	    					simBearings[i] = -2;
	    					landed++;
	    				}
    				}
    				// if an error occurs
    				else if (p.isLegalMove(simBearings[i]) == false) 
    					return new SimulationResult(SimulationResult.ILLEGAL_BEARING, round, simPlanes);
    				else return new SimulationResult(SimulationResult.OUT_OF_BOUNDS, round, simPlanes);
    			}
    			else if (simBearings[i] < -2) return new SimulationResult(SimulationResult.ILLEGAL_BEARING, round, simPlanes);
    		}
    		// make sure the planes aren't too close to each other
			// make sure planes aren't too close to each other
			for(Plane l1 : simPlanes)
			{
				for(Plane l2: simPlanes)
				{
					if (!l1.equals(l2) && l1.getBearing() != -2 && l1.getBearing() != -1 && l2.getBearing() != -2 && l2.getBearing() != -1) 
					{
						if (l1.getLocation().distance(l2.getLocation()) < GameConfig.SAFETY_RADIUS)
							return new SimulationResult(SimulationResult.TOO_CLOSE, round, simPlanes);
					}
				}
			}

    	}
    
    	if (continueSimulation) return new SimulationResult(SimulationResult.NORMAL, round, simPlanes);
    	else return new SimulationResult(SimulationResult.STOPPED, round, simPlanes);
    }
    


    /*
     * Call this method when you want to stop the simulation.
     */
	protected void stopSimulation() {
		continueSimulation = false;
	}
	

    protected SimulationResult startSimulationForResult(ArrayList<Plane> planes, int round) {
    	return startSimulation(planes, round);
    }

}

