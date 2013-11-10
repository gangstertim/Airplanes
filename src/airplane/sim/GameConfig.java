/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package airplane.sim;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;

public class GameConfig implements Cloneable{
	public Object clone()
	{
		GameConfig r = new GameConfig(this.confFileName);
		r.num_planes = this.num_planes;
		r.num_collectors = this.num_collectors;
		//r.num_airplanes = this.num_airplanes;
		r.num_airplanes = 0;
		r.number_of_rounds = this.number_of_rounds;
		r.max_rounds = this.max_rounds;
		r.selectedBoard = this.selectedBoard;
		r.playerClass = this.playerClass;
		r.boardFile =this.boardFile;
		return r;
	}
	static int gameDelay = 100;
	int number_of_rounds;
	int current_round;
	public int num_airplanes = 1000;
	String selectedBoard = null;
	int max_rounds = max_rounds_max;
	private ArrayList<Class<Player>> availablePlayers;
	private Class<Player> playerClass;
	public static Random random;
	private ArrayList<File> availableBoards;
	private Properties props;
	private String confFileName;
	private Logger log = Logger.getLogger(this.getClass());
	private File boardFile;
	int num_planes = 5;
	int num_collectors = 1;
	public static int threshold = 50;
	public static int SAFETY_RADIUS = 5;

	public Class<Player> getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(Class<Player> playerClass) {
		this.playerClass = playerClass;
	}


	public HashSet<Plane> getPlanes() {
		return planes;
	}

	HashSet<Plane> planes;

	public void setPlanes(HashSet<Plane> planes) {
		this.planes = planes;
	}

	public static final int max_rounds_max = 5000;

	public int getNumPlanes() {
		return num_planes;
	}

	public void setNumPlanes(int num_planes) {
		this.num_planes = num_planes;
	}
	
	public int getNumCollectors() {
		return num_collectors;
	}

	public void setNumCollectors(int num_collectors) {
		this.num_collectors = num_collectors;
	}
	

	public void setSelectedBoard(File f) {
		boardFile = f;
	}

	/**
	 * Obtain the list of all valid boards in the location specified by the xml
	 * configuration file.
	 * 
	 * @return An array of valid board files.
	 */
	public File[] getBoardList() {
		File[] ret = new File[availableBoards.size()];
		return availableBoards.toArray(ret);
	}

	public void setMaxRounds(int v) {
		this.max_rounds = v;
	}

	public int getMaxRounds() {
		return max_rounds;
	}

	public GameConfig(String filename) {
		confFileName = filename;
		props = new Properties();
		availablePlayers = new ArrayList<Class<Player>>();
		availableBoards = new ArrayList<File>();
		load();
	}

	/**
	 * Read in configuration file.
	 * 
	 * @param file
	 */
	public void load() {
		try {
			FileInputStream in = new FileInputStream(confFileName);
			props.loadFromXML(in);
		} catch (IOException e) {
			System.err.println("Error reading configuration file:"
					+ e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		extractProperties();
	}

	/**
	 * Get the game configuration parameters out of the Property object.
	 * 
	 */
	private void extractProperties() {
		String s;

		// READ IN CLASSES
		s = props.getProperty("airplane.classes");
		if (s != null) {
			String[] names = s.split(" ");
			for (int i = 0; i < names.length; i++) {
				try {
					availablePlayers.add((Class<Player>) Class
							.forName(names[i]));
				} catch (ClassNotFoundException e) {
					log.error("[Configuration] Class not found: " + names[i]);
				}
			}
		}
		
		File sourceFolder = new File("bin/airplane/");
		for(File f : sourceFolder.listFiles())
		{
			if(f.getName().length() == 2 && f.getName().substring(0,1).equals("g"))
			{
				for(File c : f.listFiles())
				{
					if(c.getName().endsWith(".class") ){
						String className = c.toString().replaceAll("/", ".").replace("bin.","");
						className = className.substring(0, className.length() - 6);
						 Class theClass = null;
				          try{
				            theClass = Class.forName(className, false,this.getClass().getClassLoader());
				            if(theClass.getSuperclass() != null && theClass.getSuperclass().toString().equals("class airplane.sim.Player"))
				            {
				            	if(!availablePlayers.contains((Class<Player>) theClass))
				            		availablePlayers.add((Class<Player>) theClass);
				            }
				          }catch(NoClassDefFoundError e){
				            continue;
				          } catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							continue;
						}

					}
					else if(c.isDirectory())
					{
						for(File ca : c.listFiles())
						{
							if(ca.getName().endsWith(".class") ){
								String className = ca.toString().replace(c.toString(),"").replaceAll("/", ".");
								className = className.substring(0, className.length() - 6);
								 Class theClass = null;
						          try{
						            theClass = Class.forName(className, false,this.getClass().getClassLoader());
						            if(theClass.getSuperclass() != null && theClass.getSuperclass().toString().equals("class airplane.sim.Player"))
						            {
						            	if(!availablePlayers.contains((Class<Player>) theClass))
						            		availablePlayers.add((Class<Player>) theClass);
						            }
						          }catch(NoClassDefFoundError e){
						            continue;
						          } catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									continue;
								}

							}
							else if(c.isDirectory())
							{
								
							}
						}
					}
				}
			}
		}
		if (availablePlayers.size() == 0)
			log.fatal("No player classes loaded!!!");
		if(props.getProperty("airplane.seed") != null)
		{
			long seed = Long.valueOf(props.getProperty("airplane.seed"));
			random = new Random(seed);
		}
		else
			random = new Random();
		readBoards();
	}

	/**
	 * Read all xml files from the board directory. Accept them only if valid.
	 * 
	 */
	public void readBoards() {
		availableBoards.clear();
		String s = props.getProperty("airplane.board.dir");
		if (s == null) {
			log.error("No board directory specified in conf file.");
		}

		File dir = new File(s);
		if (!dir.isDirectory()) {
			log.error("Board directory is invalid " + s);
			return;
		}

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".txt");
			}
		});
		/* Board b = new Board(1,1); */
		for (int i = 0; i < files.length; i++) {
			/*
			 * try{ b.load(files[i]); availableBoards.add(files[i]);
			 * }catch(IOException e){ log.error("Problem loading board file " +
			 * files[i]); } catch(BoardSanityException e){
			 * log.error("Sanity problem loading board file " +files[i]+". " +
			 * e); }
			 */
			availableBoards.add(files[i]);
		}
		if (availableBoards.size() > 0)
			boardFile = availableBoards.get(0);
		else
			boardFile = null;
	}

	public File getSelectedBoard() {
		return boardFile;
	}

	public ComboBoxModel getPlayerListModel() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (Class c : availablePlayers) {
			m.addElement(c);
		}
		return m;
	}


}
