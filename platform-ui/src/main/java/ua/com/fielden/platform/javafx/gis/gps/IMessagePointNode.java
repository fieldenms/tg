package ua.com.fielden.platform.javafx.gis.gps;

import java.awt.geom.Point2D;

import javafx.scene.Group;

/**
 * An abstraction for JavaFX node that represent a view of {@link MessagePoint}.
 * 
 * @author Developers
 * 
 */
public interface IMessagePointNode {

    /**
     * Returns a corresponding {@link MessagePoint}.
     * 
     * @return
     */
    MessagePoint messagePoint();

    /**
     * Updates a color.
     */
    void updateColor();

    /**
     * Updates a tooltip.
     */
    void updateTooltip();

    /**
     * Updates a direction of a node in degrees.
     */
    void updateDirection(final double vectorAngle);

    /**
     * Returns a size.
     * 
     * @return
     */
    double getSize();

    /**
     * Updates a size and centre location.
     */
    void updateSizesAndCentre(final double size);

    /**
     * Provides an action when the node has been clicked.
     */
    void clickedAction();

    void select();

    boolean selected();

    void unselect();

    /**
     * Updates state.
     */
    void update();

    void setZoom(final int zoom);

    void add(final Group parent);

    void makeVisibleAndUpdate(final Point2D xY, final Group parent, final int zoom);

    void makeInvisible();

    void createAndAddCallout(final Group parent);

    void closeCallout();
}