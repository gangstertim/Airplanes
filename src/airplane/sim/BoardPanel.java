package airplane.sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public final class BoardPanel extends JPanel implements MouseListener,
		MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Point2D MouseCoords;

	private Board board;
	private Color[] colors = {Color.ORANGE, Color.GREEN, Color.CYAN, Color.BLUE, 
			Color.MAGENTA, Color.LIGHT_GRAY, Color.GRAY, Color.YELLOW, Color.RED, Color.PINK};

	private GameEngine engine;
	public void recalculateDimensions() {
		int my_w = this.getWidth();
		int my_h = this.getHeight();
		int d = Math.min(my_w, my_h);
		d -= 10;
		if (d > 0)
			Board.pixels_per_pixel = (100
					* Board.pixels_per_meter )/ d;
		repaint();
	}
	Cursor curCursor;

	public BoardPanel() {
//		this.setPreferredSize(new Dimension(600, 600));
//		this.setBackground(Color.white);
//		addMouseListener(this);
//		addMouseMotionListener(this);
	}

	Rectangle2D boardBox = null;
	public static Line2D debugLine = null;

	/**
	 * Makes sure that there are no enclosed/unreachable
	 * 
	 * @return
	 */
	public boolean validateReachable() {
		int[][] blobs = new int[(int) Board.toScreenSpace(101)][(int) Board
				.toScreenSpace(101)];
		BufferedImage im = new BufferedImage((int) Board.toScreenSpace(101),
				(int) Board.toScreenSpace(101), BufferedImage.TYPE_INT_RGB);
		this.paint(im.getGraphics());
		Raster ra = im.getRaster();
		int nblob = 1;

		for (int i = 0; i < (int) Board.toScreenSpace(101); i++) {
			for (int j = 0; j < (int) Board.toScreenSpace(101); j++) {
				double[] px = null;
				blobs[i][j] = -1;
				px = ra.getPixel(i, j, px);
				if (px[0] == 0 && px[1] == 0 && px[2] == 0) {
					if (i > 0 && blobs[i - 1][j] > 0) {
						blobs[i][j] = blobs[i - 1][j];

						// Check for merge corner-case
						if (j > 0 && blobs[i][j - 1] > 0
								&& blobs[i][j - 1] != blobs[i][j]) {
							// These need to be merged
							blobs = replaceVals(blobs, blobs[i][j],
									blobs[i][j - 1]);
						}
					} else if (j > 0 && blobs[i][j - 1] > 0) {
						blobs[i][j] = blobs[i][j - 1];
						// Add to the mass

						// Check for merge corner-case
						if (i > 0 && blobs[i - 1][j] > 0
								&& blobs[i - 1][j] != blobs[i][j]) {
							// These two need to be merged
							blobs = replaceVals(blobs, blobs[i][j],
									blobs[i - 1][j]);
						}
					} else {
						// This pixel isn't neighboring other skin-like pixels.
						// Make it a "new blob"
						blobs[i][j] = nblob;
						nblob++;
					}
				}
			}
		}
//		debugBlobs(blobs, im);
		int blobNum = blobs[0][0];
		int i = 0;
		while(blobNum < 0)
		{
			i++;
			blobNum = blobs[i][i];
		}
		for(i = 0;i<blobs.length;i++)
		{
			for(int j = 0; j<blobs[0].length;j++)
			{
				if(blobs[i][j] != blobNum && blobs[i][j] != -1)
				{
					return false;
				}
			}
		}
		return true;
	}
	private void debugBlobs(int[][] blobs,BufferedImage im)
	{
		WritableRaster rw = im.getRaster();
		for(int i = 0; i<blobs.length;i++)
		{
			for(int j = 0; j<blobs[0].length;j++)
			{
					double[] px = new double[] {blobs[i][j]*4,blobs[i][j]*4,blobs[i][j]*4};
					rw.setPixel(i, j, px);
			}
		}
		im.setData(rw);
		ImageIcon i = new ImageIcon(im);
		JFrame f = new JFrame();
		f.setSize(blobs.length, blobs[0].length);
		f.add(new JLabel(i));
		f.setVisible(true);
	}
	/**
	 * Replaces all values of needle with r in blobs
	 * 
	 * @param blobs
	 *            Int array to search and replace in
	 * @param needle
	 *            What to search for
	 * @param r
	 *            What to replace it with
	 * @return Modified blobs array with needle replaced with r
	 */
	private int[][] replaceVals(int[][] blobs, int needle, int r) {
		for (int i = 0; i < blobs.length; i++) {
			for (int j = 0; j < blobs[0].length; j++) {
				if (blobs[i][j] == needle)
					blobs[i][j] = r;
			}
		}
		return blobs;
	}

	
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(Color.black);
		if (board != null)
		{
			boardBox = new Rectangle2D.Double(0, 0, Board.toScreenSpace(board
					.getWidth()), Board.toScreenSpace(board.getHeight()));
			g2D.fillRect((int) boardBox.getX(), (int) boardBox.getY(),
					(int) boardBox.getWidth(), (int) boardBox.getHeight());
		}
		g2D.setColor(Color.red);
		if (engine != null && board.getPlanes() != null) {
			ArrayList<Plane> planes = board.getPlanes();
			for (int i = 0; i<planes.size(); i++) {
				if (planes.get(i).getBearing() != -2) {
					g2D.setColor(colors[i%colors.length]);
					if (planes.get(i).isOn(engine.getCurrentRound())) {
						/*
						g2D.drawOval((int) Board.toScreenSpace(planes.get(i).getX() - 20),
								(int) Board.toScreenSpace(planes.get(i).getY() - 20),
								(int) Board.toScreenSpace(40),
								(int) Board.toScreenSpace(40));
						g2D.setColor(Color.RED);
						*/
						g2D.drawOval((int) Board.toScreenSpace(planes.get(i).getX() - GameConfig.SAFETY_RADIUS/2.0),
								(int) Board.toScreenSpace(planes.get(i).getY() - GameConfig.SAFETY_RADIUS/2.0),
								(int) Board.toScreenSpace(GameConfig.SAFETY_RADIUS),
								(int) Board.toScreenSpace(GameConfig.SAFETY_RADIUS));
						
						for (int j = 0; j < planes.get(i).getHistory().size()-1; j++) {
							Point2D.Double start = planes.get(i).getHistory().get(j);
							Point2D.Double end = planes.get(i).getHistory().get(j+1);
							int drawXstart = (int)Board.toScreenSpace(start.x);
							int drawYstart = (int)Board.toScreenSpace(start.y);
							int drawXend = (int)Board.toScreenSpace(end.x);
							int drawYend = (int)Board.toScreenSpace(end.y);
							g2D.drawLine(drawXstart, drawYstart, drawXend, drawYend);
						}
					}
				}
			}
		}

		if (engine != null && board.getPlanes() != null) {
			ArrayList<Plane> planes = board.getPlanes();
			for (int i = 0; i<planes.size(); i++) {
				if (planes.get(i).getBearing() != -2) {
					g2D.setColor(colors[i%colors.length]);
					if (planes.get(i).isOn(engine.getCurrentRound())) {
						g2D.fillOval((int) Board.toScreenSpace(planes.get(i).getX() - .5),
								(int) Board.toScreenSpace(planes.get(i).getY() - .5),
								(int) Board.toScreenSpace(1),
								(int) Board.toScreenSpace(1));
					} else
						g2D.drawOval((int) Board.toScreenSpace(planes.get(i).getX() - .5),
								(int) Board.toScreenSpace(planes.get(i).getY() - .5),
								(int) Board.toScreenSpace(1),
								(int) Board.toScreenSpace(1));
				}
			}

		}
		if (engine != null && board.getAirports() != null) {
			for (Airport c : board.getAirports()) {
				g2D.setColor(Color.WHITE);
				g2D.fillOval(
						(int) Board.toScreenSpace(c.getX()
								- Airport.DIAMETER / 2),
						(int) Board.toScreenSpace(c.getY()
								- Airport.DIAMETER / 2),
						(int) Board.toScreenSpace(Airport.DIAMETER),
						(int) Board.toScreenSpace(Airport.DIAMETER));
				
			}
		}
		
		g2D.setStroke(new BasicStroke(1));
		
		if (debugLine != null) {
			g2D.setColor(Color.orange);
			g2D.drawLine((int) Board.toScreenSpace(debugLine.getX1()),
					(int) Board.toScreenSpace(debugLine.getY1()),
					(int) Board.toScreenSpace(debugLine.getX2()),
					(int) Board.toScreenSpace(debugLine.getY2()));

		}
		if (curCursor != null)
			setCursor(curCursor);
	}

	public BoardPanel(GameEngine eng, boolean editable) {
		setEngine(eng);
		setBoard(engine.getBoard(), editable);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setEngine(GameEngine eng) {
		engine = eng;
	}

	public void setBoard(Board b, boolean editable) {
		board = b;
		this.setPreferredSize(new Dimension((int) Board.toScreenSpace(b
				.getWidth()), (int) Board.toScreenSpace(b.getHeight())));


		boardBox = new Rectangle2D.Double(0, 0, Board.toScreenSpace(board
				.getWidth()), Board.toScreenSpace(board.getHeight()));
		repaint();
		revalidate();
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
