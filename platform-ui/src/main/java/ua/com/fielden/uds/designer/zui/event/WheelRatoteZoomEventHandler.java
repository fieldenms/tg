package ua.com.fielden.uds.designer.zui.event;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This event handler implementation provides zooming functionality on mouse wheel rotate. One of interesting features of this class is that it is possible to specify a camera,
 * which should be used for zooming no matter through what camera a layout under the mouse pointer is viewed (see iRMS as an example).
 * 
 * @author 01es
 */
public class WheelRatoteZoomEventHandler extends PBasicInputEventHandler {
    private double minScale = 0;
    private double maxScale = Double.MAX_VALUE;
    private final PCamera camera;

    /**
     * Default constructor.
     */
    public WheelRatoteZoomEventHandler() {
        super();
        camera = null;
    }

    /**
     * Constructor, which accepts camera that will be used for zooming. By default PInputEvent.getCamera() is used.
     */
    public WheelRatoteZoomEventHandler(final PCamera camera) {
        super();
        this.camera = camera;
    }

    /**
     * This is a convenience constructor to construct constrained/bounded zooming.
     * 
     * @param minScale
     * @param maxScale
     */
    public WheelRatoteZoomEventHandler(final double minScale, final double maxScale) {
        this();
        setMinScale(minScale);
        setMaxScale(maxScale);
    }

    /**
     * This is a convenience constructor to construct constrained/bounded zooming with a specific camera.
     * 
     * @param minScale
     * @param maxScale
     */
    public WheelRatoteZoomEventHandler(final double minScale, final double maxScale, final PCamera camera) {
        this(camera);
        setMinScale(minScale);
        setMaxScale(maxScale);
    }

    /**
     * Used to accept only mouse wheel event.
     */
    public boolean acceptsEvent(final PInputEvent event, final int type) {
        return event.isMouseWheelEvent();
    }

    /**
     * This method should be used to control zooming by providing a custom logic determining whether zooming is allowed.
     * 
     * @param event
     * @return
     */
    protected boolean startZoom(final PInputEvent event) {
        return true;
    }

    /**
     * The event.getWheelRotation() (aka notches) value is either -1 when rotating down, which corresponds to zoom-in, or 1 when rotating up, which corresponds to zoom-up.
     */
    public void mouseWheelRotated(final PInputEvent event) {
        if (!startZoom(event)) {
            return;
        }

        final Point2D viewZoomPoint = event.getPosition();
        final PCamera camera = this.camera != null ? this.camera : event.getCamera();

        final double SCALE_FACTOR = 0.125;
        double scaleDelta = 1 - (event.getWheelRotation() * SCALE_FACTOR);

        final double currentScale = camera.getViewScale();
        final double newScale = currentScale * scaleDelta;

        if (newScale < getMinScale()) {
            scaleDelta = getMinScale() / currentScale;
        }
        if ((getMaxScale() > 0) && (newScale > getMaxScale())) {
            scaleDelta = getMaxScale() / currentScale;
        }

        camera.scaleViewAboutPoint(scaleDelta, viewZoomPoint.getX(), viewZoomPoint.getY());
    }

    public double getMinScale() {
        return minScale;
    }

    public void setMinScale(final double minScale) {
        this.minScale = minScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(final double maxScale) {
        this.maxScale = maxScale;
    }
}
