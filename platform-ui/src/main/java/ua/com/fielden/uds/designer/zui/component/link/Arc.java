/**
 *
 */
package ua.com.fielden.uds.designer.zui.component.link;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.piccolo.nodes.PPath;

/**
 * This is a helper class for building links.
 * 
 * @author 01es
 * 
 */
class Arc {
    PPath arc;
    List<Point2D.Double> points;

    public Arc(final List<Point2D.Double> points) {
        this.points = Collections.unmodifiableList(points);
        arc = new PPath();
        arc.moveTo((float) points.get(0).getX(), (float) points.get(0).getY());
        arc.curveTo((float) points.get(1).getX(), (float) points.get(1).getY(), (float) points.get(2).getX(), (float) points.get(2).getY(), (float) points.get(3).getX(), (float) points.get(3).getY());

    }
}