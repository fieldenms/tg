package ua.com.fielden.uds.designer.zui.event;

import java.awt.geom.Point2D;
import ua.com.fielden.uds.designer.zui.component.generic.ToolTipNode;
import ua.com.fielden.uds.designer.zui.component.generic.ToolTipNode.ToolTipPosition;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class is responsible for displaying and hiding tool tip for the PNode instance.
 * 
 * @author oleh
 */
public abstract class ToolTipListener extends PBasicInputEventHandler {

    private ToolTipNode toolTip;

    /**
     * creates new instance of tool tip for the given camera
     * 
     * @param camera
     *            - given camera. that camera is used when creating toolTip instance
     */
    public ToolTipListener(PCamera camera) {
        toolTip = new ToolTipNode(camera);
    }

    /**
     * handles mouse move event
     */
    @Override
    public void mouseMoved(PInputEvent event) {
        super.mouseMoved(event);

        // hiding previous tooltip
        toolTip.hide();
        final PNode pickNode = event.getPickedNode();

        String text = getAssociatedText(pickNode);

        if (text == null) {
            return;
        }

        // determining tooltip position and showing tooltip
        final Point2D mouseCoord = event.getCanvasPosition();
        final ToolTipPosition tooltipPosition = determineToolTipPosition(mouseCoord);
        if (isOptionKey(event)) {
            toolTip.show(text, mouseCoord, tooltipPosition);
        }
    }

    /**
     * determines the position of the tool tip for the given point
     * 
     * @param mouseCoord
     *            - specified point, which is used to find out the position of the tool tip
     * @return
     */
    public ToolTipPosition determineToolTipPosition(Point2D mouseCoord) {
        return ToolTipPosition.RIGHT_BOTTOM;
    }

    /**
     * must return the description text for the given node
     * 
     * @param pickNode
     *            - specified node, for which the description text must be returned
     * @return the description text
     */
    public abstract String getAssociatedText(PNode pickNode);

    /**
     * 
     * @return tool tip node that must displayed or hide
     */
    public ToolTipNode getToolTip() {
        return toolTip;
    }

    /**
     * checks if the option key was pressed
     * 
     * @param event
     *            - event that was triggered
     * @return the boolean value
     */
    public boolean isOptionKey(PInputEvent event) {
        return event.isShiftDown();
    }
}
