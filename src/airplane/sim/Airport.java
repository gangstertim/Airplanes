package airplane.sim;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Airport extends GameObject {
	public Airport(double x, double y) {
		this.x = x;
		this.y = y;
		boundingBox = new Ellipse2D.Double(x - DIAMETER / 2, y - DIAMETER / 2,
				DIAMETER, DIAMETER);
	}

	private Ellipse2D boundingBox;


	public boolean intersects(Line2D line) {
		return line.intersects(boundingBox.getBounds());
	}
	public boolean contains(Point2D p)
	{
		return boundingBox.contains(p);
	}
	public static final double DIAMETER = 1;
}
