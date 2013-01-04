package ua.com.fielden.platform.javafx.gis;

import java.awt.geom.Point2D;

/**
 * This class contains utility methods to transform screen coordinates to world and vice versa based on Mercator Projection.
 * 
 * @see one of the answers at http://stackoverflow.com/questions/460174/what-is-a-good-algorithm-for-mapping-gps-coordinates-to-screen-locations-when-us
 * 
 * @author TG Team
 */
public class WorldToScreenMercatorProjection implements IWorldToScreen {
    private final Point2D mapPosition;
    private final double resolution, u180dPiResolution, y0, viewWidthHalf, viewHeightHalf;
    
    public WorldToScreenMercatorProjection(final double pixelWidth, final double pixelHeight, final int currentZoom,
	    				final double lowerLeftCornerX, final double lowerLeftCornerY, final double upperRightCornerX, final double upperRightCornerY) {
	mapPosition = new Point2D.Double((lowerLeftCornerX + upperRightCornerX) / 2.0, (lowerLeftCornerY + upperRightCornerY) / 2.0); // centre position
	resolution = 360.0 / (Math.pow(2, currentZoom) * 256);
	u180dPiResolution = 40.7436654315252 * Math.pow(2, currentZoom);
	y0 = Math.log(Math.tan(Math.PI * (0.25 + mapPositionLatitude() / 360))) * u180dPiResolution;
	
	viewWidthHalf = pixelWidth / 2.0f;
	viewHeightHalf = pixelHeight / 2.0f;
    }
    
    private double mapPositionLongitude() {
	return mapPosition.getX();
    }
    
    private double mapPositionLatitude() {
	return mapPosition.getY();
    }

    @Override
    public Point2D world2pixelXY(final Point2D p) {
	return world2pixelXY(p.getY(), p.getX());
    }
    
    @Override
    public Point2D world2pixelXY(final double longitude, final double latitude) {
	final double x = (longitude - mapPositionLongitude()) / resolution;
	final double y = Math.log(Math.tan(Math.PI * (0.25 + latitude / 360.0))) * u180dPiResolution;

	return new Point2D.Double(x + viewWidthHalf, (y0 - y) + viewHeightHalf);
    }

    @Override
    public Point2D pixel2worldXY(final Point2D p) {
	// TODO implement
	throw new UnsupportedOperationException("");
    }
    
    @Override
    public Point2D pixel2worldXY(final double x, final double y) {
	// TODO implement
	throw new UnsupportedOperationException("");
    }
}
