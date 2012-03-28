package ua.com.fielden.uds.designer.zui.event;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.ILinkedNode;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This is an event handler for handling D&D activities. The only difference
 * from DragEventHandler is the way of determining that a node needs to be
 * attached/detached from a container -- node/container bounds intersection is
 * used.
 * 
 * @author 01es
 * 
 */
public class IntersectionBasedDragEventHandler extends DragEventHandler {
    private ForcedDehighlighter forcedDehighlighter;
    private final DoubleClicker doubleClicker;

    public IntersectionBasedDragEventHandler(final PLayer nodeLayer, final ForcedDehighlighter forcedDehighlighter, final DoubleClicker doubleClicker) {
	super(nodeLayer);
	assert forcedDehighlighter != null : "forcedDehighlighter has to be specified";
	this.forcedDehighlighter = forcedDehighlighter;
	this.doubleClicker = doubleClicker;
    }

    /**
     * This method is responsible for highlighting a container if it can accept
     * a node being dragged.
     */
    @Override
    protected void drag(final PInputEvent event) {
	superDrag(event); // this invocation does not trigger super.drag, but super.super.drag
	if (forcedDehighlighter.shouldDehighlight(highlightedNode)) {
	    dehighlight();
	}

	boolean foundMatch = false;

	final ArrayList<PNode> cleanedNodes = new ArrayList<PNode>();
	getIntersectingNodes(cleanedNodes);
	for (int index = cleanedNodes.size() - 1; index >= 0; index--) {
	    final PNode nextNode = cleanedNodes.get(index);
	    if (nextNode != stub && nextNode instanceof IContainer) {
		if (!nextNode.equals(highlightedNode)) { // if the picked node is not the same as previously picked then dehighlight previously picked one.
		    dehighlight();
		}
		highlightedNode = (IBasicNode) nextNode;
		highlight();
		foundMatch = true;
		break;
	    }
	}
	// if not matching node (i.e. node, which can accept dragged node) was found need to dehighlight previously highlighted node
	if (!foundMatch) {
	    dehighlight();
	}

	((ILinkedNode) draggedNode).resetAll();
    }

    @Override
    protected void startDrag(final PInputEvent event) {
	if (event.getClickCount() == 2 && doubleClicker != null) {
	    doubleClicker.doubleClickAction(event.getPickedNode());
	}
	super.startDrag(event);
    }

    @Override
    protected void endDrag(final PInputEvent event) {
	final PNode dropTarget = highlightedNode != null ? (PNode) highlightedNode : event.getPath().nextPickedNode();

	nodeLayer.setChildrenPickable(false);
	originalParent.setPickable(false); // need in order to finalise clone animation properly....
	originalParent.setChildrenPickable(false);
	draggedNode.setPickable(false);
	draggedNode.setChildrenPickable(false);

	stub.animateToTransparency(0.1f, 300); // return transparency to normal

	// perform dropping...
	final PActivity activity = draggedNode.animateToTransparency(1.0f, 100);
	activity.setDelegate(new DropAnimationDelegate(this, dropTarget, stub, draggedNode));

	superEndDrag(event);
    }

    private void getIntersectingNodes(final ArrayList<PNode> cleanedNodes) {
	final ArrayList<PNode> nodes = new ArrayList<PNode>();
	// find intersecting nodes that belong to a main layer...
	nodeLayer.findIntersectingNodes(draggedNode.getGlobalFullBounds(), nodes); //  getGlobalBounds()
	removeChildrenReferences(nodes, draggedNode); // removes draggedNode and all its children
	nodes.remove(stub); // clone is an image and does not have any children
	nodes.remove(nodeLayer);
	// remove intersected nodes' children...
	cleanedNodes.addAll(nodes);
	for (final PNode node : nodes) {
	    // handle node's children
	    for (final Object childNode : node.getChildrenReference()) {
		if (!(childNode instanceof IContainer)) {
		    cleanedNodes.remove(childNode);
		} else if (nodes.contains(childNode) && !((IContainer) childNode).isCompatible(draggedNode)) {
		    cleanedNodes.remove(childNode);
		}
	    }
	    // handle node itself
	    if (!(node instanceof IContainer)) {
		cleanedNodes.remove(node);
	    }
	}
	// there may be an intersection with a secondary layer -- the main layer of the secondary camera.
	// at the moment the rest of nodes included into a secondary layer are not of any particular interest...
	// more over it is not even interesting whether a dragged node intersects a secondary layer (this may be tricky due to difference in coordinate systems)
	// it is absolutely sufficient to check if a dragged node intersects with a secondary camera containing secondary layer...
	if (GlobalObjects.secondaryCamera != null) {
	    if (GlobalObjects.secondaryCamera.intersects(draggedNode.getGlobalBounds())) {
		cleanedNodes.add(GlobalObjects.secondaryCamera.getLayer(0));
	    }
	}
    }

    /**
     * A helper method for removing children of the node being tested for
     * intersection.
     * 
     * @param nodes
     * @param parent
     */
    @SuppressWarnings("unchecked")
    private void removeChildrenReferences(final ArrayList<PNode> nodes, final PNode parent) {
	nodes.remove(parent);
	final List<PNode> children = parent.getChildrenReference();
	for (final PNode child : children) {
	    removeChildrenReferences(nodes, child);
	}
    }

    /**
     * The solo purpose of this interface is to handle highlighting of heavy
     * nodes, i.e. nodes such as container, which may contain tens and hundreds
     * of items.
     * 
     * @author 01es
     * 
     */
    public static interface ForcedDehighlighter {
	boolean shouldDehighlight(IBasicNode node);
    }

    /**
     * This interface is used to specify custom action for double-click occurred
     * on specified piccolo node.
     * 
     * @author Jhou
     * 
     */
    public static interface DoubleClicker {
	/**
	 * Custom action for double-click occurred on specified pickedNode.
	 * 
	 * @param pickedNode
	 */
	void doubleClickAction(final PNode pickedNode);
    }

    @Override
    public void animateToOriginalParent() {
	super.animateToOriginalParent();
    }

}
