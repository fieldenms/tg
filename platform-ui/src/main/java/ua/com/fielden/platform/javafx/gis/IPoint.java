package ua.com.fielden.platform.javafx.gis;

import javafx.scene.paint.Color;
import ua.com.fielden.platform.gis.Point;

/**
 * A contract for additional properties for points.
 * 
 * @author TG Team
 * 
 * @param <P>
 */
public interface IPoint<P extends Point> {
    /**
     * A descriptional information for a point.
     * 
     * @param point
     * @return
     */
    String getTooltip(final P point);

    /**
     * A colour for a point.
     * 
     * @param point
     * @return
     */
    Color getColor(final P point);

    /**
     * An action to be done while a point has been clicked.
     * 
     * @param point
     */
    void clickedAction(final P point);

    /**
     * Turns off existent callout.
     * 
     * @param point
     */
    void turnOffCallout();

    /**
     * Closes existent callout.
     * 
     * @param point
     */
    void closeCallout();

    /**
     * Opens callout for specific point.
     * 
     * @param point
     */
    void openCallout(final P point);
}
