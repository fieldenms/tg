package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import ua.com.fielden.uds.designer.zui.component.MetaTypeNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILinkedNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This class is a generic implementation of a container entity, which can contain other IContainer and IDraggable instances. <b/> It provides an convenient way to implemented
 * specific containers by creating a descendant. As a general rule the following two methods should be overrode in order to provide custom behaviour:
 * <ul>
 * <li>doAfterAttach()</li>
 * <li>doAfterDetach()</li>
 * <li>isCompatible()</li>
 * </ul>
 * 
 * Methods attach() and detach() provide implementation suitable for most foreseeable cases. However, these methods can be overrode.
 * 
 * @author 01es
 * 
 */

public class GenericContainerNode extends AbstractNode implements IContainer, IDraggable {

    private static final long serialVersionUID = 707002333812843679L;

    private boolean removeAfterDrop = true;

    private boolean drag = true; // By default generic container can be dragged

    public GenericContainerNode() {
        super(new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2));
    }

    public GenericContainerNode(final Shape shape) {
        super(shape);
        reshape(false);
    }

    public GenericContainerNode(final Shape shape, final boolean needReshape) {
        super(shape);
        if (needReshape) {
            reshape(false);
        }
    }

    public GenericContainerNode(final boolean needReshape) {
        this();
        if (needReshape) {
            reshape(false);
        }
    }

    public GenericContainerNode(final PDimension minConstraint) {
        super(new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2));
        setMinConstraint(minConstraint);
        reshape(false);
    }

    public void attach(final PInputEvent event, final PNode node, final boolean animate) {
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
            reshape(animate); // it is essential to perform reshape after doAfterAttach as it is possible that this method would introduce new visual
            // components
        }
    }

    public void detach(final PInputEvent event, final PNode node, final boolean animate, final boolean forcedDetach) {
        node.removeFromParent();
        doAfterDetach(node);
        reshape(animate); // it is essential to perform reshape after doAfterDetach as it is possible that this method would introduce new visual components
    }

    /**
     * This method defines node-to-container compatibility.
     * 
     * @return true if node and container (this) are compatible, otherwise -- false.
     */
    public boolean isCompatible(final PNode node) {
        if (node instanceof MetaTypeNode) {
            return false;
        }
        // if nodes are linked then deny docking
        if (node instanceof ILinkedNode && this instanceof ILinkedNode && ((ILinkedNode) this).isLinked((ILinkedNode) node)) {
            return false;
        }

        return true;
    }

    public boolean getRemoveAfterDrop() {
        return removeAfterDrop;
    }

    public void setRemoveAfterDrop(final boolean flag) {
        removeAfterDrop = flag;
    }

    public void doAfterAttach(final PNode node) {
    }

    public void doAfterDetach(final PNode node) {
    }

    public boolean canDrag() {
        return drag;
    }

    public void setDrag(final boolean flag) {
        drag = flag;
    }

    public boolean canBeDetached() {
        return true;
    }

}
