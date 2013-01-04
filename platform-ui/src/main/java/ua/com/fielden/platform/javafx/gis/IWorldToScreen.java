package ua.com.fielden.platform.javafx.gis;

import java.awt.geom.Point2D;

/**
 * This interface contains utility methods to transform screen coordinates to world and vice versa.
 * 
 * @author TG Team
 */
public interface IWorldToScreen {
    Point2D world2pixelXY(final Point2D p);
    
    Point2D world2pixelXY(final double x, final double y);

    Point2D pixel2worldXY(final Point2D p);
    
    Point2D pixel2worldXY(final double x, final double y);
}
