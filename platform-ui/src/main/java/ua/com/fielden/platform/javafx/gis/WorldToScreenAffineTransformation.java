package ua.com.fielden.platform.javafx.gis;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * This class contains utility methods to transform screen coordinates to world and vice versa based on simple affine transformations.
 * 
 * @see http://docs.geotools.org/latest/userguide/tutorial/affinetransform.html
 * 
 * @author TG Team
 */
public class WorldToScreenAffineTransformation implements IWorldToScreen {
    private final AffineTransform world2pixel;
    private final AffineTransform pixel2world;

    public WorldToScreenAffineTransformation(final double pixelWidth, final double pixelHeight, final double lowerLeftCornerX, final double lowerLeftCornerY, final double upperRightCornerX, final double upperRightCornerY) {

        final AffineTransform translate = AffineTransform.getTranslateInstance(-lowerLeftCornerX, -lowerLeftCornerY);

        final Point2D upperRightCorner = new Point2D.Double(upperRightCornerX, upperRightCornerY);
        final Point2D newUpperRightCorner = translate.transform(upperRightCorner, null);

        final AffineTransform scale = AffineTransform.getScaleInstance(pixelWidth / newUpperRightCorner.getX(), pixelHeight / newUpperRightCorner.getY());

        final AffineTransform mirror_y = new AffineTransform(1, 0, 0, -1, 0, pixelHeight);

        world2pixel = new AffineTransform(mirror_y);
        world2pixel.concatenate(scale);
        world2pixel.concatenate(translate);

        try {
            pixel2world = world2pixel.createInverse();
        } catch (final NoninvertibleTransformException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("The inverse 'pixel2world' transform could not be created.");
        }
    }

    @Override
    public Point2D world2pixelXY(final Point2D p) {
        return world2pixel.transform(p, null);
    }

    @Override
    public Point2D world2pixelXY(final double x, final double y) {
        return world2pixelXY(new Point2D.Double(x, y));
    }

    @Override
    public Point2D pixel2worldXY(final Point2D p) {
        return pixel2world.transform(p, null);
    }

    @Override
    public Point2D pixel2worldXY(final double x, final double y) {
        return pixel2worldXY(new Point2D.Double(x, y));
    }
}
