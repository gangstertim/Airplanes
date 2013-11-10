package airplane.sim;

import java.util.ArrayList;

public class SimulationResult {
	
	public static final int NORMAL = 0;
	public static final int STOPPED = 1;
	public static final int OUT_OF_BOUNDS = 3;
	public static final int TOO_CLOSE = 4;
	public static final int ILLEGAL_BEARING = 5;
	public static final int NULL_BEARINGS = 6;
	public static final int TOO_EARLY = 7;
	
	private int reason;
	private int round;
	private ArrayList<Plane> planes;
	
	public SimulationResult(int _reason, int _round, ArrayList<Plane> _planes) {
		reason = _reason;
		round = _round;
		planes = _planes;
	}
	
	public int getReason() { return reason;	}
	public int getRound() { return round;	}
	public ArrayList<Plane> getPlanes() { return planes; }
	public boolean isSuccess() { return reason == NORMAL; }
}
