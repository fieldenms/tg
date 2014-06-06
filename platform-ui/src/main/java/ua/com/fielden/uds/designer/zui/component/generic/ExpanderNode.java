package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import ua.com.fielden.uds.designer.zui.interfaces.ICollapsable;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;

public class ExpanderNode extends PPath {
    private static final long serialVersionUID = -41057708781829176L;

    private ICollapsable expandableNode;
    private boolean up;

    private final PBasicInputEventHandler defaultEventHandler;

    public ExpanderNode(ICollapsable expandableNode, boolean initialOrientationIsUp) {
        setPaint(new Color(1, 21, 118));
        setStrokePaint(getPaint());

        if (initialOrientationIsUp) {
            up();
        } else {
            down();
        }

        defaultEventHandler = new DefaultEventHandler(this);
        addInputEventListener(defaultEventHandler);

        setExpandableNode(expandableNode);
    }

    public void down() {
        setUp(false);
        reset();
        moveTo(0, -4);
        lineTo(4, 0);
        lineTo(8, -4);
        closePath();
        // collapse a node if it is provided
        if (expandableNode != null) {
            expandableNode.collapse();
        }

    }

    public void up() {
        setUp(true);
        reset();
        moveTo(0, 0);
        lineTo(4, -4);
        lineTo(8, 0);
        closePath();
        // expand a node if it is provided
        if (expandableNode != null) {
            expandableNode.expand();
        }
    }

    private static class DefaultEventHandler extends PBasicInputEventHandler implements Serializable {
        private static final long serialVersionUID = 3762542608255322737L;

        private static final Paint highPaint = new Color(107, 133, 254);
        private static Paint prevPaint;

        private ExpanderNode expander;

        public DefaultEventHandler(ExpanderNode expander) {
            this.expander = expander;
            prevPaint = expander.getPaint();
        }

        /**
         * Handles button highlighting.
         */
        public void mouseEntered(PInputEvent event) {
            if (event.isLeftMouseButton()) {
                return;
            }

            prevPaint = expander.getPaint();
            expander.setPaint(highPaint);
            expander.setStrokePaint(highPaint);
        }

        /**
         * Handles button highlighting.
         */
        public void mouseExited(PInputEvent event) {
            if (event.isLeftMouseButton()) {
                return;
            }

            expander.setPaint(prevPaint);
            expander.setStrokePaint(prevPaint);
        }

        /**
         * Sequentially invokes all registered on_click_event_listeners.
         */
        public void mouseClicked(PInputEvent event) {
            if (expander.isUp()) {
                expander.down();
            } else {
                expander.up();
            }
        }

        /**
         * Handles button highlighting.
         */
        public void mouseReleased(PInputEvent event) {
            Rectangle2D bounds = new Rectangle2D.Double(event.getPosition().getX(), event.getPosition().getY(), 1, 1);
            bounds = expander.globalToLocal(bounds);

            if (expander.intersects(bounds)) {
                expander.setPaint(highPaint);
            } else {
                expander.setPaint(prevPaint);
            }
        }
    }

    public ICollapsable getExpandableNode() {
        return expandableNode;
    }

    private void setExpandableNode(ICollapsable expandableNode) {
        this.expandableNode = expandableNode;
    }

    private boolean isUp() {
        return up;
    }

    private void setUp(boolean up) {
        this.up = up;
    }

    public PBasicInputEventHandler getDefaultEventHandler() {
        return defaultEventHandler;
    }

}
