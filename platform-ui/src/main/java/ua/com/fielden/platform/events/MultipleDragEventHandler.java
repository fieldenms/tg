package ua.com.fielden.platform.events;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.pmodels.ContainerRetriever;
import ua.com.fielden.platform.pmodels.SelectionHolder;
import ua.com.fielden.snappy.view.block.BlockNode;
import ua.com.fielden.snappy.view.block.Slot;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.AnimationDelegate;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import ua.com.fielden.uds.designer.zui.component.generic.GenericSlotContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.StubNode;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILinkedNode;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.nodes.PComposite;

public class MultipleDragEventHandler extends PDragEventHandler {

    protected List<PNode> stubs;
    protected List<PNode> draggedNodes;
    protected Color highlightColor = new Color(150, 255, 150);
    protected IBasicNode highlightedNode;
    protected List<PNode> originalParents;
    protected Point2D originalTransformedPosition;
    protected Point2D originalPosition;
    protected PLayer nodeLayer;
    protected PCanvas canvas;
    private ForcedDehighlighter forcedDehighlighter;
    private SelectionHolder selectionHolder;

    protected Runnable postDragAction;

    /**
     * 
     * @param marqueeParent
     * @param selectableParents
     */
    public MultipleDragEventHandler(final PCanvas canvas, final ForcedDehighlighter forcedDehighlighter, final SelectionHolder selectionHolder) {
	super();
	setCanvas(canvas);
	setSelectionHolder(selectionHolder);
	assert forcedDehighlighter != null : "forcedDehighlighter has to be specified";
	this.forcedDehighlighter = forcedDehighlighter;
	draggedNodes = new ArrayList<PNode>();
	originalParents = new ArrayList<PNode>();
	stubs = new ArrayList<PNode>();
    }

    private void setSelectionHolder(final SelectionHolder selectionHolder) {
	this.selectionHolder = selectionHolder;
    }

    public PCanvas getCanvas() {
	return canvas;
    }

    public SelectionHolder getSelectionHolder() {
	return selectionHolder;
    }

    private void setCanvas(final PCanvas canvas) {
	this.canvas = canvas;
	this.nodeLayer = canvas.getLayer();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void startDrag(final PInputEvent pie) {

	// iterate throw the node collection to create hierarchy of the attached rotables
	if (pie.getClickCount() == 1) {
	    return;
	}

	if (selectionHolder.isMarqueeSelection(pie)) {
	    return;
	}
	final Collection<PNode> selectionCollection = selectionHolder.getSelection();
	Iterator<PNode> selectionIter = selectionCollection.iterator();
	while (selectionIter.hasNext()) {
	    final PNode obj = selectionIter.next();
	    if (!(obj instanceof IBasicNode)) {
		return;
	    }
	    if (!(obj instanceof IDraggable)) {
		return;
	    }
	    if (!((IDraggable) obj).canDrag()) {
		return;
	    }
	    createWidgetHierarchy(obj);
	}

	super.startDrag(pie);

	Double originX = Double.NaN, originY = Double.NaN;

	// iterate throw the collection of created hierarchy and add to the each node that is parent of
	// PLayer to the list of draggedNodes, creates stub node associated with the current draggedNode
	// find it's original parent and set the original position of the group of selected nodes
	selectionIter = selectionCollection.iterator();
	while (selectionIter.hasNext()) {
	    final PNode node = selectionIter.next();
	    if (node.getParent() == nodeLayer) {
		final Point2D offset = node.getOffset();
		if (originX.isNaN() || offset.getX() < originX.doubleValue()) {
		    originX = offset.getX();
		}
		if (originY.isNaN() || offset.getY() < originY) {
		    originY = offset.getY();
		}
		draggedNodes.add(node);
		node.setChildrenPickable(false);
		node.setTransparency(0.6f);
		node.moveToFront();
		final StubNode stub = new StubNode(node);
		stubs.add(stub);
		PNode originParent = node.getParent();
		if ((node instanceof ContainerRetriever) && ((ContainerRetriever) node).getContainer() != null) {
		    originParent = (PNode) ((ContainerRetriever) node).getContainer();
		}
		originalParents.add(originParent);
		if (originParent instanceof IContainer) {
		    ((IContainer) originParent).detach(pie, node, false, false);

		} else if (originParent instanceof Slot) {
		    ((IContainer) originParent.getParent()).detach(pie, node, false, false);

		}
		canvas.getLayer().addChild(stub);
		((IBasicNode) node).onStartDrag(pie);
	    }
	}
	originalPosition = originalTransformedPosition = new Point2D.Double(originX, originY);
    }

    /**
     * This method is responsible for highlighting a container if it can accept a node being dragged.
     */
    @Override
    protected void drag(final PInputEvent e) {
	final PDimension d = e.getCanvasDelta();
	e.getTopCamera().localToView(d);

	refreshSecondaryCamera();

	final PDimension gDist = new PDimension();
	final Iterator<PNode> selectionEn = draggedNodes.iterator();
	while (selectionEn.hasNext()) {
	    final PNode node = selectionEn.next();

	    gDist.setSize(d);
	    node.getParent().globalToLocal(gDist);
	    node.offset(gDist.getWidth(), gDist.getHeight());
	}

	if (draggedNodes.size() == 1) {
	    final PNode draggedNode = draggedNodes.get(0);
	    final PNode stubNode = stubs.get(0);

	    if (forcedDehighlighter.shouldDehighlight(highlightedNode)) {
		dehighlight();
	    }

	    boolean foundMatch = false;

	    final ArrayList<PNode> cleanedNodes = new ArrayList<PNode>();
	    getIntersectingNodes(cleanedNodes, draggedNode, stubNode);
	    for (int index = 0; index < cleanedNodes.size(); index++) {
		final PNode nextNode = cleanedNodes.get(index);
		if (!(nextNode instanceof StubNode) && (nextNode instanceof IContainer)) {
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
    }

    private void getIntersectingNodes(final ArrayList<PNode> cleanedNodes, final PNode testNode, final PNode stubNode) {
	cleanedNodes.clear();
	final ArrayList<PNode> nodes = new ArrayList<PNode>();
	// find intersecting nodes that belong to a main layer...
	nodeLayer.findIntersectingNodes(testNode.getGlobalBounds(), nodes);
	nodes.removeAll(testNode.getChildrenReference());
	nodes.remove(testNode);
	nodes.removeAll(stubNode.getChildrenReference());
	nodes.remove(stubNode);
	nodes.remove(nodeLayer);
	// remove intersected nodes' children...
	cleanedNodes.addAll(nodes);

	for (final PNode node : nodes) {
	    // handle node's children
	    for (final Object childNode : node.getChildrenReference()) {
		if (!(childNode instanceof IContainer)) {
		    cleanedNodes.remove(childNode);
		} else if (nodes.contains(childNode) && !((IContainer) childNode).isCompatible(draggedNodes.get(0))) {
		    cleanedNodes.remove(childNode);
		}
	    }
	    // handle node itself
	    if (!(node instanceof IContainer)) {
		cleanedNodes.remove(node);
	    }
	}
    }

    private void refreshSecondaryCamera() {
	/*
	 * if (GlobalObjects.secondaryCamera != null) { GlobalObjects.secondaryCamera.repaint(); }
	 */}

    protected void dehighlight() {
	if (highlightedNode != null) {
	    highlightedNode.dehighlight();
	}
	highlightedNode = null;
    }

    protected void highlight() {
	if (((IContainer) highlightedNode).isCompatible(draggedNodes.get(0))) {
	    highlightedNode.highlight(draggedNodes.get(0), highlightColor);
	}
    }

    @Override
    protected void endDrag(final PInputEvent e) {
	super.endDrag(e);
	nodeLayer.setChildrenPickable(false);
	for (int counter = 0; counter < draggedNodes.size(); counter++) {
	    final PNode draggedNode = draggedNodes.get(counter);
	    final PNode stub = stubs.get(counter);
	    final PNode originalParent = originalParents.get(counter);

	    originalParent.setPickable(false); // need in order to finalise clone animation properly....
	    originalParent.setChildrenPickable(false);
	    draggedNode.setPickable(false);
	    draggedNode.setChildrenPickable(false);

	    // TODO further must be implemented animation of transparency level of the stub node and draggedNode
	    stub.setTransparency(0.1f); // return transparency to normal
	    draggedNode.setTransparency(1.0f);
	}
	if (draggedNodes.size() == 1) {
	    final PPickPath path = e.getInputManager().getMouseOver();
	    PNode dropTarget = path.nextPickedNode();
	    if (dropTarget == null) {
		dropTarget = path.getPickedNode();
	    }
	    drop(e, dropTarget);
	} else {
	    final ArrayList<PNode> cleanNodes = new ArrayList<PNode>();
	    for (int counter = 0; counter < draggedNodes.size(); counter++) {
		getIntersectingNodes(cleanNodes, draggedNodes.get(counter), stubs.get(counter));
		if (cleanNodes.size() > 0) {
		    animateToOriginalParent();
		    return;
		}
	    }
	    finaliseDrop(e);
	}

    }

    @SuppressWarnings("unchecked")
    private void createWidgetHierarchy(final PNode node) {
	if (node instanceof GenericSlotContainerNode) {
	    for (final Object obj : ((GenericSlotContainerNode) node).getSlotAttachamnets()) {
		if (obj != null) {
		    createWidgetHierarchy((PNode) obj);
		    ((PNode) obj).reparent(node);
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void explodeHierarchy(final PNode node) {
	final PNode parent = node.getParent();
	if (node instanceof GenericSlotContainerNode) {
	    for (final Object obj : ((GenericSlotContainerNode) node).getSlotAttachamnets()) {
		if (obj != null) {
		    ((PNode) obj).reparent(parent);
		    explodeHierarchy((PNode) obj);
		}
	    }
	}
    }

    /**
     * The solo purpose of this interface is to handle highlighting of heavy nodes, i.e. nodes such as container, which may contain tens and hundreds of items.
     * 
     * @author 01es
     * 
     */
    public static interface ForcedDehighlighter {
	boolean shouldDehighlight(IBasicNode node);
    }

    /**
     * This is where draggedNode gets attached to a different or its original container.
     * 
     * @param event
     * @param dropTarget
     */
    private void drop(final PInputEvent event, final PNode dropTarget) {
	final IContainer container = (IContainer) highlightedNode;
	final PNode draggedNodeParent = originalParents.get(0);
	final PNode draggedNode = draggedNodes.get(0);
	// /////// check if the target where draggedNode is being dropped is a container /////////////
	if ((container != null && container != originalParents.get(0)) || container == originalParents.get(0)) {
	    if (container.isCompatible(draggedNode)) { // if draggedNode and the target container are compatible then perform the drop
		container.attach(event, draggedNode, true);
		finaliseDrop(event);
	    } else { // otherwise move draggedNode back to its original parent
		animateToOriginalParent();
	    }
	}
	// //////// if the target where draggedNode is being dropped is NOT a container /////////////////
	else if (((IBasicNode) draggedNode).canBeDetached() && (dropTarget == canvas.getCamera())
		&& (draggedNodeParent instanceof IContainer || draggedNodeParent.getParent() instanceof IContainer)) { // draggedNode is being dragged out of container and dropped
	    // onto a camera
	    if (draggedNodeParent instanceof IContainer) {
		((IContainer) draggedNodeParent).detach(event, draggedNode, false, true);
	    } else {
		((IContainer) draggedNodeParent.getParent()).detach(event, draggedNode, false, true); // this is the case with slot container -- slot is not a container but its
		// parent is
	    }
	    nodeLayer.addChild(draggedNode);
	    finaliseDrop(event);
	} else if (nodeLayer != draggedNodeParent || !((IBasicNode) draggedNode).canBeDetached() || //
		container == originalParents.get(0)) { // if the parent of the node being dragged is not a layer then return the node back to its original parent...
	    animateToOriginalParent();
	} else {
	    finaliseDrop(event);
	}

    }

    /**
     * This is a convenience method for doing some clean up work. The important thing is that it should be invoked at an appropriate time. For instance, if a node is not accepted
     * as a result of a drop, then an animation is performed where a node is returned to its original parent. So, finaliseDrop() should be invoked at the end of this animation --
     * not earlier.
     * 
     * @param event
     */
    private void finaliseDrop(final PInputEvent event) {
	for (int nodeCounter = 0; nodeCounter < draggedNodes.size(); nodeCounter++) {

	    PNode draggedNode = draggedNodes.get(nodeCounter);
	    PNode stub = stubs.get(nodeCounter);
	    PNode originalParent = originalParents.get(nodeCounter);
	    // release constraints applied to the dragged node when dragging started

	    if (draggedNode != null) {
		draggedNode.setPickable(true);
		draggedNode.setChildrenPickable(true);
		((IBasicNode) draggedNode).dehighlight(); // return the original stoke if it has been changed
		((IBasicNode) draggedNode).onEndDrag(event);
		// handle parent
		if (draggedNode.getParent() != null) {
		    draggedNode.getParent().setPickable(true);
		    draggedNode.getParent().setChildrenPickable(true);
		}
	    }
	    // release constraints applied to the original parent of the dragged node when dragging started
	    if (originalParent != null) {
		originalParent.setPickable(true); // need in order to finalise clone animation properly....
		originalParent.setChildrenPickable(true);
	    }

	    // handle the situation where the dragged node should be substituted with its copy/clone after the drop
	    if (!((IDraggable) draggedNode).getRemoveAfterDrop() && // should not be removed after the drop
		    draggedNode.getParent() != nodeLayer && // the dragged node is not dropped into the node layer
		    stub.getParent() != draggedNode.getParent() && // stub and dragged nodes have different parents... this means that the dragged node has actually been dropped
		    // into
		    // a
		    // different from its original container
		    (originalParent instanceof IContainer || originalParent instanceof Slot)) {
		PNode cloneNode = null;
		if (draggedNode instanceof BlockNode) { // ancestors of the BlockNode class provide a copy constructor
		    cloneNode = ((BlockNode<?>) draggedNode).copy();
		} else { // for other nodes try to utilise cloning
		    cloneNode = (PNode) draggedNode.clone();
		}
		// parent container is either a direct parent or a parent of the slot where draggedNode used to be snapped
		final IContainer parent = (IContainer) (originalParent instanceof IContainer ? originalParent : originalParent.getParent());
		stub.removeFromParent();
		if (originalParent instanceof Slot && cloneNode instanceof BlockNode) {
		    ((Slot) (originalParent)).snapIn((BlockNode<?>) cloneNode);
		} else {
		    parent.attach(null, cloneNode, false);
		}
		parent.reshape(true);
	    }
	    // remove the stub node from its temporary parent
	    stub.removeFromParent();
	    if (originalParent instanceof IContainer || originalParent instanceof Slot) {
		final IContainer parent = (IContainer) (originalParent instanceof IContainer ? originalParent : originalParent.getParent());
		parent.reshape(true);
	    }

	    explodeHierarchy(draggedNode);
	    // reset fields
	    draggedNode = null;
	    stub = null;
	    originalParent = null;
	    dehighlight();
	}

	refreshSecondaryCamera();
	draggedNodes.clear();
	stubs.clear();
	originalParents.clear();
	nodeLayer.setChildrenPickable(true);

	if (getPostDragAction() != null) {
	    getPostDragAction().run();
	}
    }

    /**
     * Returns a note it its original location by animating the transformation and attaching a node to its original parent.
     * 
     */
    protected void animateToOriginalParent() {

	PNode draggedNode = null;
	Double originX = Double.NaN, originY = Double.NaN;
	if (draggedNodes.size() > 0) {
	    draggedNode = new PComposite();
	    for (int nodeCounter = 0; nodeCounter < draggedNodes.size(); nodeCounter++) {
		final PNode node = draggedNodes.get(nodeCounter);
		final Point2D offset = node.getOffset();
		if (originX.isNaN() || offset.getX() < originX) {
		    originX = offset.getX();
		}
		if (originY.isNaN() || offset.getY() < originY) {
		    originY = offset.getY();
		}
		node.reparent(draggedNode);
	    }
	}
	final Point2D draggedNodeOffset = draggedNode.getOffset();
	final double deltaX = originalTransformedPosition.getX() - originX;
	final double deltaY = originalTransformedPosition.getY() - originY;
	final Point2D position = new Point2D.Double(draggedNodeOffset.getX() + deltaX, draggedNodeOffset.getY() + deltaY);
	// before moving draggedNode back to its original parent it is necessary to reassociate it with nodeLayer
	// in most cases draggedNode is already attached to nodeLayer, however in some cases like in iRMS it is not,
	// which leads to undesirable visual artifacts.

	draggedNode.setPickable(false);
	canvas.getLayer().addChild(draggedNode);

	final PActivity activity = draggedNode.animateToPositionScaleRotation(position.getX(), position.getY(), 1, 0, 300);
	activity.setDelegate(new AnimationDelegate(canvas.getLayer(), draggedNode) {
	    @Override
	    public void activityFinished(final PActivity activity) {
		final PNode origParent = getCloneNode();
		PNode node = getDragNode();
		while (node.getChildrenCount() != 0) {
		    final PNode childNode = node.getChild(0);
		    childNode.reparent(origParent);
		}

		origParent.removeChild(node);
		node = null;

		for (int counter = 0; counter < draggedNodes.size(); counter++) {
		    final PNode originalParent = originalParents.get(counter);
		    final PNode dnode = draggedNodes.get(counter);
		    final PNode stub = stubs.get(counter);
		    if (originalParent instanceof IContainer) {
			// note that the original position is not necessarily needed and can even be harmful in case of a GenericSpotContainerNode descendants
			// this is related to the fact that this container type uses coordinates to determine, which spot should be used for attaching a node
			((IContainer) originalParent).attach(null, dnode, false);
		    } else if (originalParent instanceof Slot) {
			((Slot) originalParent).parent().attach(null, dnode, false);
		    } else if (originalParent instanceof AbstractNode) {
			// need to handle a case where draggedNode is draggable but not removable
			if (dnode instanceof IDraggable && !((IDraggable) dnode).getRemoveAfterDrop()) {
			    stub.removeFromParent();
			}
			// node.setOffset(originalPosition);
			((AbstractNode) originalParent).addChild(dnode); // TODO this one if weird because if typecasting isn't performed then the system behaviours
			// incorrectly....need to determine why????
		    } else {
			// node.setOffset(originalPosition);
			originalParent.addChild(dnode);
		    }
		}
		finaliseDrop(null);
	    }
	});
    }

    public Runnable getPostDragAction() {
	return postDragAction;
    }

    public void setPostDragAction(final Runnable postDragAction) {
	this.postDragAction = postDragAction;
    }

    /**
     * This method highlights the border of the node that can be dragged.
     */
    @Override
    public void mouseEntered(final PInputEvent event) {
	if (((draggedNodes == null) || (draggedNodes.size() == 0)) && event.getButton() == 0 && event.getPickedNode() instanceof IBasicNode) {
	    event.getPickedNode().moveToFront();
	    ((IBasicNode) event.getPickedNode()).highlight(new DefaultStroke(2));
	    ((IBasicNode) event.getPickedNode()).onMouseEntered(event);
	}
    }

    /**
     * This method returns the original border of the node -- reverse to the mouseEntered action.
     */
    @Override
    public void mouseExited(final PInputEvent event) {
	if (((draggedNodes == null) || (draggedNodes.size() == 0)) && event.getButton() == 0 && event.getPickedNode() instanceof IBasicNode) {
	    ((IBasicNode) event.getPickedNode()).dehighlight();
	    ((IBasicNode) event.getPickedNode()).onMouseExited(event);
	}
    }

    /**
     * 
     * @return returns the ForcedDehighlighter instance that indicates whether previously highlighted node must be dehighlighted or not
     */
    public ForcedDehighlighter getForcedDehighlighter() {
	return forcedDehighlighter;
    }
}
