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

    void select(final boolean showTooltip);
    void unselect();

    /**
     * Updates state.
     */
    void update();

    void setZoom(final int zoom);

    void updateAndAdd(final Point2D xY, final Group parent, final int zoom);

    /** Returns <code>true</code> if callout exist at this stage for this message point node. */
    boolean hasCallout();

    /** Adds existent callout to scene. */
    void addExistingCalloutToScene(final Group parent);
}