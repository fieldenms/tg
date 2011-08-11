package ua.com.fielden.platform.swing.treetable;

import java.awt.Dimension;
import java.awt.Point;
import java.util.EventObject;

import javax.swing.JViewport;

/**
 * {@link EventObject} that represents an event that is triggered when {@link JViewport} changes it's state (e.g. viewport position, viewport size, e.t.c.).
 * 
 * @author TG Team
 *
 */
public class ViewportChangeEvent extends EventObject {

    private static final long serialVersionUID = -6076415399910551989L;
    /**
     * Holds the new viewport's position.
     */
    private final Point viewportPosition;
    /**
     * Holds the viewport's extent size.
     */
    private final Dimension extSize;
    /**
     * Holds the view's size.
     */
    private final Dimension size;

    /**
     * Initiates this {@link ViewportChangeEvent} instance with {@link #viewportPosition}, {@link #extSize} and view's {@link #size}.
     * 
     * @param source
     * @param viewportPosition
     * @param extSize
     * @param size
     */
    public ViewportChangeEvent(final Object source, final Point viewportPosition,final Dimension extSize,final Dimension size) {
	super(source);
	this.viewportPosition=viewportPosition;
	this.extSize=extSize;
	this.size=size;
    }

    /**
     * Returns new viewport's position.
     * 
     * @return
     */
    public Point getViewportPosition() {
	return new Point(viewportPosition);
    }

    /**
     * Returns new view's size.
     * 
     * @return
     */
    public Dimension getSize() {
	return new Dimension(size);
    }

    /**
     * Returns the extent size of viewport.
     * 
     * @return
     */
    public Dimension getExtSize() {
	return new Dimension(extSize);
    }

}
