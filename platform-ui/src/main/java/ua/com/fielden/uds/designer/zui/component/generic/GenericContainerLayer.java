package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Paint;
import java.awt.Stroke;

import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode.MutablePoint2D;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

public class GenericContainerLayer extends PLayer implements IContainer, IDraggable, IBasicNode {
    private static final long serialVersionUID = 6599993811557181761L;

    private boolean drag = false;

    private Paint backgroundColor;

    public void attach(PInputEvent event, PNode node, boolean animate) {
        if (isCompatible(node)) {
            // need to remove a node from its original parent
            if (node.getParent() instanceof IContainer) {
                ((IContainer) node.getParent()).detach(event, node, animate, false);
            } else {
                node.removeFromParent();
            }

            addChild(node);

            if (node instanceof IDraggable) {
                ((IDraggable) node).setRemoveAfterDrop(true);
            }
            doAfterAttach(node);
        }
    }

    public void detach(PInputEvent event, PNode node, boolean animate, boolean forcedDetach) {
        node.removeFromParent();
        doAfterDetach(node);
    }

    public boolean isCompatible(PNode node) {
        return true;
    }

    public boolean canDrag() {
        return drag;
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }

    public boolean getRemoveAfterDrop() {
        return true;
    }

    public void setRemoveAfterDrop(boolean flag) {
    }

    public boolean canBeDetached() {
        return false;
    }

    public void dehighlight() {
        super.setPaint(getBackgroundColor());
    }

    public Paint getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Paint backgroundColor) {
        this.backgroundColor = backgroundColor;
        setPaint(backgroundColor);
    }

    public IBasicNode getDeepParent(MutablePoint2D offset) {
        return this;
    }

    public void highlight(Stroke stroke) {
        // TODO Auto-generated method stub

    }

    public void highlight(PNode node, Paint paint) {
        super.setPaint(paint);
    }

    public void doAfterAttach(PNode node) {
    }

    public void doAfterDetach(PNode node) {
    }

    public void onMouseEntered(PInputEvent event) {
    }

    public void onMouseExited(PInputEvent event) {
    }

    public void onStartDrag(PInputEvent event) {
    }

    public void onEndDrag(PInputEvent event) {
    }

    public void onDragging(PInputEvent event) {
    }

    public boolean showToolTip() {
        return false;
    }

    public void reshape(boolean animate) {
    }
}
