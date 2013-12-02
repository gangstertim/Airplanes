/* 
 * 	$Id: GameEngine.java,v 1.6 2007/11/28 16:30:47 johnc Exp $
 * 
 * 	Programming and Problem Solving
 *  Copyright (c) 2007 The Trustees of Columbia University
 */
package airplane.sim;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import airplane.sim.GameListener.GameUpdateType;
import airplane.sim.ui.GUI;;



public final class GameEngine 
{

	private GameConfig config;
	private Board board;
	// private PlayerWrapper player;
	private int round = 0;
	public GUI gui;
	private ArrayList<GameListener> gameListeners;
	private Logger log;
	boolean initDone = false;
	private static double EPSILON = 1*Math.pow(10, -10);
	static ArrayList<Plane> planes;
	
	public boolean isSimulated = false;
	static {
		PropertyConfigurator.configure("logger.properties");
	}
	public GameEngine(GameConfig config)
	{
		this.config = (GameConfig) config.clone();
		gameListeners = new ArrayList<GameListener>();
		board = new Board(10, 10);
		board.engine=this;
		this.isSimulated = true;
			try {
				board.load(config.getSelectedBoard());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    log = Logger.getLogger(GameController.class);
	}
	public GameEngine(String configFile)
	{
		config = new GameConfig(configFile);
		gameListeners = new ArrayList<GameListener>();
		board = new Board(10, 10);
		board.engine=this;
		if(config.getSelectedBoard() != null)
			try {
				board.load(config.getSelectedBoard());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    log = Logger.getLogger(GameController.class);
	}

	public void addGameListener(GameListener l)
	{
		gameListeners.add(l);
	}

	public int getCurrentRound()
	{
		return round;
	}
	
	public int getPower() {
		return board.powerUsed;
	}
	
	public int getDelay() {
		return board.delay;
	}

	public GameConfig getConfig()
	{
		return config;
	}
	private Player curPlayer;
	public Board getBoard()
	{
		return board;
	}
	
	public int getNumLanded()
	{
		return board.planesLanded;
	}
	
	
	protected double distance(double x1, double y1, double x2, double y2) {
		double xdist = x1 - x2;
		double ydist = y1 - y2;
		return Math.sqrt(xdist*xdist + ydist*ydist);
	}

	
	public boolean step()
	{
		try
		{			
			// remember previous positions of the planes to detect illegal moves
			int size = board.getPlanes().size();
			double xCoords[] = new double[size];
			double yCoords[] = new double[size];
			Object[] planeArray = board.getPlanes().toArray();
			for (int i = 0; i < planeArray.length; i++) {
				Plane l = (Plane)planeArray[i];
				xCoords[i] = l.getX();
				yCoords[i] = l.getY();
			}
			round++;

			
			// ask the Player for the new position of the planes
			board.bearings = curPlayer.updatePlanes(board.getPlanes(), round, board.bearings);

			// NOTE: at this point, the Plane's bearings have not been updated!
			// that doesn't happen until we call move on each Plane!
			
			// make sure no plane took off too early
			for (int i = 0; i < planes.size(); i++) {
				if (board.bearings[i] > -1) {
					if (planes.get(i).getDepartureTime() > round) {
						System.err.println("ERROR! plane took off before its departure time!");
						gui.setErrorMessage("Error! Plane took off before its departure time!");
						notifyListeners(GameUpdateType.ERROR);
						return false;
					}
					else if (planes.get(i).dependenciesHaveLanded(board.bearings) == false) {
						System.err.println("ERROR! plane took off before its dependency landed!");
						gui.setErrorMessage("Error! Plane took off before its dependency landed!");
						notifyListeners(GameUpdateType.ERROR);
						return false;
					}
				}
			}
			
			// make sure there's no monkey business
			if (planes.size() != size || board.bearings.length != size) {
				System.err.println("ERROR! wrong number of planes!");
				notifyListeners(GameUpdateType.ERROR);
				return false;
			}
			else {
				for (int i = 0; i < planes.size(); i++) {
					Plane p = planes.get(i);
					// only move a player that's in the air
					if (board.bearings[i] >= 0) {
						int move = p.move(board.bearings[i]); 
						if (move == Plane.LEGAL_MOVE) {
							if (distance(p.getX(), p.getY(), xCoords[i], yCoords[i]) > p.getVelocity() + EPSILON) {
								System.err.println("ERROR! Plane moved by more than DISTANCE!");
								gui.setErrorMessage("Error! Plane " + i + " moved by more than allowable distance!");
								notifyListeners(GameUpdateType.ERROR);
								return false;
							}
							board.powerUsed++;
							log.trace("Moved plane " + i + " to: (" + p.getX() + ", " + p.getY() + "); bearing = " + p.getBearing());
							p.addToHistory(new Point2D.Double(p.getX(), p.getY()));
						}
						else if (move == Plane.ILLEGAL_MOVE) {
							System.err.println("ERROR! illegal move!");
							gui.setErrorMessage("Error! Plane " + i + " tried to make illegal move from bearing " + p.getBearing() + " to " + board.bearings[i]);
							notifyListeners(GameUpdateType.ERROR);
							return false;
						}
						else if (move == Plane.OUT_OF_BOUNDS) {
							System.err.println("ERROR! out of bounds!");
							gui.setErrorMessage("Error! Plane " + i + " tried to go out of bounds!");
							notifyListeners(GameUpdateType.ERROR);
							return false;
						}
					}
					// see if it's been delayed on the ground
					else if (board.bearings[i] == -1) {
						if (p.getDepartureTime() <= round) {
							board.delay++;
						}
					}
					else if (board.bearings[i] < -2) {
						System.err.println("ERROR! illegal move!");
						gui.setErrorMessage("Error! Plane " + i + " tried to make illegal move from bearing " + p.getBearing() + " to " + board.bearings[i]);
						notifyListeners(GameUpdateType.ERROR);
						return false;
					}
				}
			}
			//System.err.println("Power = " + board.powerUsed + "; round = " + round);
			
			board.setPlanes(planes);

			// Check if plane has landed
			for(int i=0; i<board.getPlanes().size(); i++)
			{
				Plane p = board.getPlanes().get(i);
				if(p.getBearing() != -2)
				{
					// if it's within 0.5 of the destination, that's good enough
    				if (p.getLocation().distance(p.getDestination()) <= 0.5) {
						p.setBearing(-2);
						board.bearings[i] = -2;
						board.planesLanded++;
						log.info("Plane #" + i + " landed at time " + round);
					}
				}
			}
			// make sure planes aren't too close to each other
			for(Plane l1 : planes)
			{
				for(Plane l2: planes)
				{
					if (!l1.equals(l2) && l1.getBearing() != -2 && l1.getBearing() != -1 && l2.getBearing() != -2 && l2.getBearing() != -1) 
					{
						if (l1.getLocation().distance(l2.getLocation()) < GameConfig.SAFETY_RADIUS)
						{
							System.err.println("Error! Planes are too close!");
							gui.setErrorMessage("Error! Planes are too close!");
							notifyListeners(GameUpdateType.ERROR);
							return false;
						}
					}
				}
			}
			
		}
		catch(ConcurrentModificationException e)
		{
			
		}
		notifyListeners(GameUpdateType.MOVEPROCESSED);
		if(board.planesLanded == board.planes.size()) {
			//GAME OVER!
			notifyListeners(GameUpdateType.GAMEOVER);
			return false;
		}

		return true;
	}

	private final static void printUsage()
	{
		System.err.println("Usage: GameEngine <config file>");
	}

	public void removeGameListener(GameListener l)
	{
		gameListeners.remove(l);
	}
	public void notifyRepaint()
	{
		Iterator<GameListener> it = gameListeners.iterator();
		while (it.hasNext())
		{
			it.next().gameUpdated(GameUpdateType.REPAINT);
		}
	}
	private void notifyListeners(GameUpdateType type)
	{
		Iterator<GameListener> it = gameListeners.iterator();
		while (it.hasNext())
		{
			it.next().gameUpdated(type);
		}
	}

	public static final void main(String[] args)
	{
		System.setOut(
    		    new PrintStream(new OutputStream() {
					@Override
					public void write(int b) throws IOException {						
					}
				}));
		if (args.length < 1)
		{
			printUsage();
			System.exit(-1);
		}
		GameEngine engine = new GameEngine(args[0]);
		new GUI(engine);
	}


	
	public boolean setUpGame()
	{
		try
		{
			round = 0;
			board.load(config.getSelectedBoard());
			board.numPlanes = board.origins.size();
			board.planes = new ArrayList<Plane>();
			board.powerUsed = 0;
			board.delay = 0;
			
			for (int i=0; i<board.numPlanes; i++) {
				
				Point2D.Double origin = board.origins.get(i);
				Point2D.Double destination = board.destinations.get(i);
				ArrayList<Integer> dependencies = board.dependencies.get(i);
				
				int depart = board.departureTimes.get(i);
				Plane p = new Plane(origin.getX(), origin.getY(), destination.getX(), 
						destination.getY(), depart, dependencies);
				//log.info("Dependencies for plane " + i + " is " + dependencies);
				
				board.planes.add(p);
			}
			
			board.planesLanded = 0;
			board.setPlanes(board.planes); // TODO: do we need this?
			
			initDone = false;
			curPlayer = config.getPlayerClass().newInstance();
			curPlayer.setMyConfig((GameConfig) config.clone());
			curPlayer.Register();
			
			curPlayer.startNewGame(board.planes);
			
			board.setInteractive(false);

			ArrayList<Plane> planes= board.planes; 
			if(planes == null)
			{
				System.err.println("Error: Player returned null for planes");
				return false;
			}
			/*
			if(planes.size() > config.getNumPlanes()) 
			{
				System.err.println("Error: You needed to give "  +config.getNumPlanes() +", but you gave " + planes.size() + " planes instead!");
				return false;
			}
			*/
			for(Plane l : planes)
			{
				if(l.getX() < 0 || l.getX() > 100 || l.getY() < 0 || l.getY() > 100)
				{
					System.err.println("Error: Planes are OOB");
					System.err.println(l.getX() + ", " + l.getY());
					return false;
				}
			}
			board.setPlanes(planes);
			this.planes = planes;

			Set<Airport> airports = new HashSet<Airport>();
			for (Plane p : board.planes) {
				Airport a = new Airport(p.getDestination().getX(), p.getDestination().getY());
				airports.add(a); 
			}
			
			for (Airport a : airports) {
				if(a.getX() < 0 || a.getX() > 100 || a.getY() < 0 || a.getY() > 100)
				{
					System.err.println("Error: Airport is OOB");
					return false;
				}
			}
			board.setAirports(airports); 

			initDone = true;
		} catch (IOException e)
		{
			log.error("Exception: " + e);
			return false;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		round = 0;
		notifyListeners(GameUpdateType.STARTING);
		return true;
	}
	
	
	
	public void mouseChanged() {
		notifyListeners(GameUpdateType.MOUSEMOVED);
	}
}
