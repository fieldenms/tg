package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This class represents a tool tip or a hint. Displaying of ToolTip can be implemented as part of mouse event handlers. E.g. mouseEntered for showing and mouseExited for hiding.
 * 
 * @author 01es
 * 
 */
public class ToolTipNode extends AbstractNode {
    private static final long serialVersionUID = 6366278629085624337L;

    private boolean wasHidden = true;

    /**
     * Will be set after node's bounds is known
     */
    private Point2D tipPoint;

    /**
     * Camera, to witch {@link ToolTipNode} should be sticked
     */
    private PCamera camera = null;

    /**
     * Displays a tool tip with <code>text</code> at position <code>whereToShow</code>.
     * 
     * @param text
     * @param whereToShow
     * @param tooltipPosition
     *            - determines, on what side of the tip point, tooltip will be displayed
     */
    public void show(final String text, final Point2D whereToShow, final ToolTipPosition tooltipPosition) {
        if (wasHidden) {
            wasHidden = false;
            compose(text, whereToShow, tooltipPosition);
        }

        setOffset(whereToShow, tooltipPosition);
    }

    /**
     * Displays a tool tip with <code>text</code> at position <code>whereToShow</code>. <br>
     * <br>
     * Note : tooltip will be diplayed on the {@link ToolTipPosition#RIGHT_BOTTOM} side of the tip point
     * 
     * @param text
     * @param whereToShow
     */
    public void show(final String text, final Point2D whereToShow) {
        show(text, whereToShow, ToolTipPosition.RIGHT_BOTTOM);
    }

    /**
     * Initializes tipPoint according to the <code>tooltipPosition</code>
     * 
     * @param tooltipPosition
     */
    private void setTipPoint(final ToolTipPosition tooltipPosition) {
        switch (tooltipPosition) {
        case RIGHT_BOTTOM: {
            tipPoint = new Point2D.Double(-30d, -10d);
            break;
        }
        case RIGHT_TOP: {
            tipPoint = new Point2D.Double(-30d, 10d + getHeight());
            break;
        }
        case LEFT_BOTTOM: {
            tipPoint = new Point2D.Double(30d + getWidth(), -10d);
            break;
        }
        case LEFT_TOP: {
            tipPoint = new Point2D.Double(30d + getWidth(), 10d + getHeight());
            break;
        }
        }
    }

    /**
     * Offsets whole tooltip node and shifts it for 5 pixels, so that mouse won't hover it
     */
    private void setOffset(final Point2D whereToShow, final ToolTipPosition tooltipPosition) {
        switch (tooltipPosition) {
        case RIGHT_BOTTOM: {
            setOffset(new Point2D.Double(whereToShow.getX() - tipPoint.getX() + 5, whereToShow.getY() - tipPoint.getY() + 5));
            break;
        }
        case RIGHT_TOP: {
            setOffset(new Point2D.Double(whereToShow.getX() - tipPoint.getX(), whereToShow.getY() - tipPoint.getY() - 5));
            break;
        }
        case LEFT_BOTTOM: {
            setOffset(new Point2D.Double(whereToShow.getX() - tipPoint.getX() - 5, whereToShow.getY() - tipPoint.getY() + 5));
            break;
        }
        case LEFT_TOP: {
            setOffset(new Point2D.Double(whereToShow.getX() - tipPoint.getX() - 5, whereToShow.getY() - tipPoint.getY() - 5));
            break;
        }
        }
    }

    /**
     * Hides a tool tip by removing it from its parent and removing all its child nodes.
     */
    public void hide() {
        removeFromParent();
        removeAllChildren();
        wasHidden = true;
    }

    @SuppressWarnings("unchecked")
    protected void layoutComponents() {
        final double xOffset = 0;
        double yOffset = 0;
        for (final PNode each : messages) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            each.setOffset(xOffset, yOffset - each.getY());
            yOffset += each.getFullBoundsReference().getHeight() + 3;
        }
    }

    private List<PText> messages = new ArrayList<PText>();

    private void compose(final String text, final Point2D whereToShow, final ToolTipPosition tooltipPosition) {
        removeFromParent();
        removeAllChildren();
        messages.clear();
        // add text
        final String[] linesOfText = text.split("\n");
        for (final String textLine : linesOfText) {
            final PText textLineNode = new PText(textLine);
            addChild(textLineNode);
            messages.add(textLineNode);
        }
        reshape(false); // readjust boundaries

        setTipPoint(tooltipPosition);

        // add pointer
        final GeneralPath pointer = createPointer(tooltipPosition);
        final PPath pointerNode = new PPath(pointer);
        pointerNode.setPaint(getPaint());
        pointerNode.setStrokePaint(getStrokePaint());

        addChild(pointerNode);
        // add tool top to a camera
        getCamera().addChild(this);
        moveToFront();
    }

    /**
     * Creates and returns pointer path, according to <code>tooltipPosition</code> value
     * 
     * @param tooltipPosition
     */
    private GeneralPath createPointer(final ToolTipPosition tooltipPosition) {
        final GeneralPath pointer = new GeneralPath();

        switch (tooltipPosition) {
        case RIGHT_BOTTOM: {
            pointer.moveTo(.0f, (float) getHeight() * 0.1f); // 10% from the top
            pointer.lineTo((float) tipPoint.getX(), (float) tipPoint.getY());
            pointer.lineTo(.0f, (float) getHeight() * 0.3f); // 30% from the top
            break;
        }
        case RIGHT_TOP: {
            pointer.moveTo(.0f, (float) getHeight() * 0.9f); // 10% from the top
            pointer.lineTo((float) tipPoint.getX(), (float) tipPoint.getY());
            pointer.lineTo(.0f, (float) getHeight() * 0.7f); // 30% from the top
            break;
        }
        case LEFT_TOP: {
            pointer.moveTo((float) getWidth(), (float) getHeight() * 0.9f); // 10% from the top
            pointer.lineTo((float) tipPoint.getX(), (float) tipPoint.getY());
            pointer.lineTo((float) getWidth(), (float) getHeight() * 0.7f); // 30% from the top
            break;
        }
        case LEFT_BOTTOM: {
            pointer.moveTo((float) getWidth(), (float) getHeight() * 0.1f); // 10% from the top
            pointer.lineTo((float) tipPoint.getX(), (float) tipPoint.getY());
            pointer.lineTo((float) getWidth(), (float) getHeight() * 0.3f); // 30% from the top
            break;
        }
        }

        pointer.closePath();

        return pointer;
    }

    /**
     * Initializes instance of {@link ToolTipNode} with {@link PCamera} reference, to which this instance would be sticked <br>
     * <br>
     * Note : <code>camera</code> should not be null
     */
    public ToolTipNode(final PCamera camera) {
        super(new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2));
        final Color color = new Color(1, 1, 0.5f, 0.9f);
        setBackgroundColor(color);
        setStrokePaint(color);
        setCamera(camera);
    }

    /**
     * @return camera, to which this {@link ToolTipNode} is sticked
     */
    public PCamera getCamera() {
        return camera;
    }

    /**
     * Sets reference to {@link PCamera}. <br>
     * <br>
     * Note : <code>camera</code> should not be null
     */
    protected void setCamera(final PCamera camera) {
        if (camera == null) {
            throw new IllegalArgumentException("camera should not be null");
        }
        this.camera = camera;
    }

    /**
     * Position of tooltip relatively to topPoint: RIGHT_BOTTOM - means that the tooltip will be displayed in the right-bottom corner with relation to tip point(used by default),
     * and so on.
     */
    public enum ToolTipPosition {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

}
