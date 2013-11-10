package airplane.sim;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class Board {
	public static final int pixels_per_meter = 150;
	public static int pixels_per_pixel = 25;
	private int width;
	private int height;
	public boolean impersonated = false;
	public GameEngine engine;
	public int planesLanded;
	public int powerUsed = 0;
	public int delay = 0;
	
	public ArrayList<Integer> departureTimes;
	public ArrayList<Point2D.Double> origins;
	public ArrayList<Point2D.Double> destinations;
	public int numPlanes;
	public double[] bearings;

	private Set<Airport> airports;

	public void setAirports(Set<Airport> collectors) {
		this.airports = collectors;
	}
	
	public Set<Airport> getAirports() {
		return airports;
	}
	
	public void addAirport(Airport c) {
		if (airports == null) 
			airports = new HashSet<Airport>();
		airports.add(c);
	}


	public static Point2D toScreenSpace(Point2D point2d) {
		Point2D clone = new Point2D.Double();
		clone.setLocation(toScreenSpace(point2d.getX()),
				toScreenSpace(point2d.getY()));
		return clone;
	}


	public int cacheHits = 0;
	public int cacheMisses = 0;

	public ArrayList<Plane> getPlanes() {
		return planes;
	}

	ArrayList<Plane> planes;

	public void setPlanes(ArrayList<Plane> planes) {
		for (int i=0; i<this.numPlanes; i++) {
			
			//Plane l = new Plane(origins.get(i).getX(), origins.get(i).getY());
		}
		this.planes = planes;
	}

	public static double fromScreenSpace(double v) {
		return v * pixels_per_pixel / pixels_per_meter;
	}

	public static double toScreenSpace(double v) {
		return v * pixels_per_meter / pixels_per_pixel;
	}

	public static Point2D fromScreenSpace(Point2D p) {
		Point2D r = new Point2D.Double();
		r.setLocation(fromScreenSpace(p.getX()), fromScreenSpace(p.getY()));
		return r;
	}


	public Board() {
		this.width = 100;
		this.height = 100;
		init();
	}

	public Board(int width, int height) {
		this.width = 100;
		this.height = 100;
		init();
	}

	public Board(String file) throws IOException {
		load(new File(file));

	}

	public void load(File f) throws IOException {
		if (f == null) {
			System.out.println("File is null!");
		}
		try {

			FileReader input = new FileReader(f);
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;
			origins = new ArrayList<Point2D.Double>();
			destinations = new ArrayList<Point2D.Double>();
			departureTimes = new ArrayList<Integer>();
			
			while ( (myLine = bufRead.readLine()) != null)
			{    
				// Flight schedule--start, destination, departure--is delimited by semicolons: 
			    String[] flight = myLine.split(";");
			    
			    Point2D.Double origin = new Point2D.Double(Double.parseDouble(flight[0].split(",")[0]), 
			    		Double.parseDouble(flight[0].split(",")[1]));
			    Point2D.Double destination = new Point2D.Double(Double.parseDouble(flight[1].split(",")[0]), 
			    		Double.parseDouble(flight[1].split(",")[1]));
			    
			    origins.add(origin);
			    
			    destinations.add(destination);
			    
			    departureTimes.add(Integer.parseInt(flight[2]));
			    
			}
			bearings = new double[origins.size()];
			for (int i=0; i<origins.size(); i++) {
				bearings[i] = -1;
			}
			
		} catch (Exception e) {
//			throw new IOException("XML Parsing exception: " + e);
			e.printStackTrace();
			throw new IOException("Problem loading txt file: " + e);
		}

		// sanityCheck();

	}


	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private void init() {
		powerUsed = 0;
		delay = 0;
	}

	public boolean inBounds(int x, int y) {
		return (x >= 0 && x < width) && (y >= 0 && y < height);
	}

	boolean interactive = false;

	public boolean isInteractive() {
		return interactive;
	}

	public void setInteractive(boolean b) {
		interactive = b;
	}

}
