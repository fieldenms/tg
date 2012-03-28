package ua.com.fielden.uds.designer.zui.event;

import java.awt.Color;
import java.awt.geom.Point2D;

import ua.com.fielden.snappy.view.block.BlockNode;
import ua.com.fielden.snappy.view.block.Slot;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.AnimationDelegate;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import ua.com.fielden.uds.designer.zui.component.generic.StubNode;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILinkedNode;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * This is an event handler for handling D&D activities (and deletion node tree
 * by double click).
 * 
 * @author 01es
 * 
 */
public class DragEventHandler extends PDragEventHandler {
    protected PNode stub;
    protected PNode draggedNode;
    protected Color highlightColor = new Color(150, 255, 150);
    protected IBasicNode highlightedNode;
    protected PNode originalParent;
    protected Point2D originalTransformedPosition;
    protected Point2D originalPosition;
    protected PLayer nodeLayer;

    protected Runnable postDragAction;

    public DragEventHandler(final PLayer nodeLayer) {
	getEventFilter().setMarksAcceptedEventsAsHandled(true);
	this.nodeLayer = nodeLayer;
    }

    /**
     * This methods prepares a node for being dragged (e.g. makes it
     * transparent), and creates a cloned node, which is used as a placeholder
     * to visualise where the node being dragged was originally located. Also
     * when double clicked - it deletes pickedBlock.
     */
    @Override
    protected void startDrag(final PInputEvent event) {
	if (event.getClickCount() == 1 || event.getClickCount() == 2) { // single and double click need to be ignored....
	    return;
	}
	if (!(event.getPickedNode() instanceof IBasicNode)) {
	    return;
	}
	if (!(event.getPickedNode() instanceof IDraggable)) {
	    return;
	}
	if (!((IDraggable) event.getPickedNode()).canDrag()) {
	    return;
	}

	assert GlobalObjects.isInitialised() : "GlobalObjects is not initialised properly.";
	super.startDrag(event);

	draggedNode = event.getPickedNode();

	draggedNode.setChildrenPickable(false);
	draggedNode.setTransparency(0.6f);
	draggedNode.moveToFront(); // the moveToFront() method should be overridden if necessary to make a recursive call to getParent().moveToFront()

	// spawn a stub-node, which stays in place of draggedNode, while it is being dragged
	stub = new StubNode(draggedNode);
	// TODO improve the following functionality
	// Previously draggedNode was not detached from its parent until the drop.
	// However it has become required to D'n'D nodes between different cameras and for this reason it is necessary,
	// to associate draggedNode with top camera (or rather main layer) at the very beginning of the D'n'D process.
	// This on its own has introduced several issues, which are mostly fixed, however some still remain and require further work.
	// Please also note, that there is a custom behaviour to handle GenericSpotContainterNode correctly. This also needs to be improved at some stage,
	// in order to make the code more generic.
	Point2D coords = draggedNode.getOffset();
	originalPosition = coords;
	final Point2D deltaMouse = new Point2D.Double(event.getPositionRelativeTo(draggedNode.getParent()).getX() - coords.getX(), event.getPositionRelativeTo(
		draggedNode.getParent()).getY()
		- coords.getY());
	originalParent = draggedNode.getParent();
	if (originalParent instanceof IContainer) {
	    ((IContainer) originalParent).detach(event, draggedNode, false, false);
	    // ((IContainer) originalParent).reshape(false);
	    originalParent.addChild(stub); // this is required to correct coordinate transformation, which is used in the next call
	    // ((IContainer) originalParent).attach(null, clone, false); // most likely this never passes compatibility with a clone, which has type of StubNode
	} else if (originalParent instanceof Slot) {
	    ((IContainer) originalParent.getParent()).detach(event, draggedNode, false, false);
	    // ((IContainer) originalParent.getParent()).reshape(false);
	    originalParent.addChild(stub); // this is required to correct coordinate transformation, which is used in the next call
	    // ((IContainer) originalParent.getParent()).attach(null, clone, false); // most likely this never passes compatibility with a clone, which has type of StubNode
	} else {
	    draggedNode.removeFromParent();
	    originalParent.addChild(stub);
	}

	GlobalObjects.canvas.getLayer().addChild(draggedNode);
	coords = new Point2D.Double(event.getPositionRelativeTo(nodeLayer).getX() - deltaMouse.getX(), event.getPositionRelativeTo(nodeLayer).getY() - deltaMouse.getY());
	draggedNode.setOffset(coords);
	originalTransformedPosition = coords;

	((IBasicNode) draggedNode).onStartDrag(event);

	// the call to drag() is required to properly handle mouse click, which is otherwise leads to detachment of nodes from containers
	// however the passed event instance is incorrect therefore sometimes it leads to unexpected behaviour...
	// TODO need to come up with a better solution
	// drag(event);
    }

    /**
     * This method is needed by descendants in order not to invoke this drag().
     * 
     * @param event
     */
    protected void superDrag(final PInputEvent event) {
	super.drag(event);
    }

    /**
     * This method is responsible for highlighting a container if it can accept
     * a node being dragged.
     */
    @Override
    protected void drag(final PInputEvent event) {
	super.drag(event);
	dehighlight();

	refreshSecondaryCamera();

	final PPickPath path = event.getInputManager().getMouseOver();
	PNode nextNode = path.nextPickedNode();
	while (nextNode != null) {
	    if (nextNode != stub && nextNode instanceof IContainer) {
		highlightedNode = (IBasicNode) nextNode;
		highlight();
		break;
	    }
	    nextNode = path.nextPickedNode();
	}
	if (draggedNode instanceof ILinkedNode) {
	    (((ILinkedNode) draggedNode)).resetAll();
	}

	((IBasicNode) draggedNode).onDragging(event);
    }

    private void refreshSecondaryCamera() {
	if (GlobalObjects.secondaryCamera != null) {
	    GlobalObjects.secondaryCamera.repaint();
	}
    }

    protected void dehighlight() {
	if (highlightedNode != null) {
	    highlightedNode.dehighlight();
	}
	highlightedNode = null;
    }

    protected void highlight() {
	if (((IContainer) highlightedNode).isCompatible(draggedNode)) {
	    highlightedNode.highlight(draggedNode, highlightColor);
	}
    }

    /**
     * This method is needed by descendants in order not to invoke this drag().
     * 
     * @param event
     */
    protected void superEndDrag(final PInputEvent event) {
	super.endDrag(event);
    }

    @Override
    protected void endDrag(final PInputEvent event) {
	final PPickPath path = event.getInputManager().getMouseOver();
	final PNode dropTarget = path.nextPickedNode();

	nodeLayer.setChildrenPickable(false);
	originalParent.setPickable(false); // need in order to finalise clone animation properly....
	originalParent.setChildrenPickable(false);
	draggedNode.setPickable(false);
	draggedNode.setChildrenPickable(false);

	stub.animateToTransparency(0.1f, 300); // return transparency to normal

	// perform dropping...
	final PActivity activity = draggedNode.animateToTransparency(1.0f, 100);
	activity.setDelegate(new DropAnimationDelegate(this, dropTarget, stub, draggedNode));

	super.endDrag(event);
    }

    /**
     * This a static inner class, which caters for a sequential execution of all
     * animations and drop related actions.
     * 
     * It also handles fade out effect and removal for the clone node.
     * 
     * @author 01es
     * 
     */
    protected static class DropAnimationDelegate extends AnimationDelegate {
	private DragEventHandler handler;
	private PNode dropTarget;

	public DropAnimationDelegate(final DragEventHandler handler, final PNode dropTarget, final PNode cloneNode, final PNode draggedNode) {
	    super(cloneNode, draggedNode);
	    this.handler = handler;
	    this.dropTarget = dropTarget;
	}

	@Override
	public void activityStepped(final PActivity activity) {
	    super.activityStepped(activity);
	    handler.refreshSecondaryCamera();
	}

	@Override
	public void activityFinished(final PActivity activity) {
	    super.activityFinished(activity);
	    handler.drop(null, dropTarget);
	}
    }

    /**
     * This is where draggedNode gets attached to a different or its original
     * container.
     * 
     * @param event
     * @param dropTarget
     */
    private void drop(final PInputEvent event, final PNode dropTarget) {
	final IContainer container = (IContainer) highlightedNode;
	final PNode draggedNodeParent = originalParent;
	// /////// check if the target where draggedNode is being dropped is a container /////////////
	if ((container != null && container != originalParent) || container == originalParent) {
	    if (container.isCompatible(draggedNode)) { // if draggedNode and the target container are compatible then perform the drop
		container.attach(event, draggedNode, true);
		finaliseDrop(event);
	    } else { // otherwise move draggedNode back to its original parent
		animateToOriginalParent();
	    }
	}
	// //////// if the target where draggedNode is being dropped is NOT a container /////////////////
	else if (((IBasicNode) draggedNode).canBeDetached() && (dropTarget == GlobalObjects.canvas.getCamera())
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
		container == originalParent) { // if the parent of the node being dragged is not a layer then return the node back to its original parent...
	    animateToOriginalParent();
	} else {
	    finaliseDrop(event);
	}

    }

    /**
     * This is a convenience method for doing some clean up work. The important
     * thing is that it should be invoked at an appropriate time. For instance,
     * if a node is not accepted as a result of a drop, then an animation is
     * performed where a node is returned to its original parent. So,
     * finaliseDrop() should be invoked at the end of this animation -- not
     * earlier.
     * 
     * @param event
     */
    private void finaliseDrop(final PInputEvent event) {
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
		stub.getParent() != draggedNode.getParent() && // stub and dragged nodes have different parents... this means that the dragged node has actually been dropped into a
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

	if (getPostDragAction() != null) {
	    getPostDragAction().run();
	}
	nodeLayer.setChildrenPickable(true);
	// reset fields
	draggedNode = null;
	stub = null;
	originalParent = null;
	dehighlight();

	refreshSecondaryCamera();
    }

    /**
     * Returns a note it its original location by animating the transformation
     * and attaching a node to its original parent.
     * 
     */
    protected void animateToOriginalParent() {
	draggedNode.setPickable(false);
	final Point2D position = originalTransformedPosition;
	// before moving draggedNode back to its original parent it is necessary to reassociate it with nodeLayer
	// in most cases draggedNode is already attached to nodeLayer, however in some cases like in iRMS it is not,
	// which leads to undesirable visual artifacts.
	final Point2D globalOffset = draggedNode.localToGlobal(draggedNode.getOffset());
	if (draggedNode.getParent() instanceof IContainer) {
	    ((IContainer) draggedNode.getParent()).detach(null, draggedNode, false, false);
	} else {
	    draggedNode.removeFromParent();
	}
	draggedNode.setOffset(globalOffset.getX() - draggedNode.getOffset().getX(), globalOffset.getY() - draggedNode.getOffset().getY());
	GlobalObjects.canvas.getLayer().addChild(draggedNode);

	final PActivity activity = draggedNode.animateToPositionScaleRotation(position.getX(), position.getY(), 1, 0, 300);
	activity.setDelegate(new AnimationDelegate(originalParent, draggedNode) {
	    @Override
	    public void activityFinished(final PActivity activity) {
		final PNode origParent = getCloneNode();
		final PNode node = getDragNode();

		if (origParent instanceof IContainer) {
		    // note that the original position is not necessarily needed and can even be harmful in case of a GenericSpotContainerNode descendants
		    // this is related to the fact that this container type uses coordinates to determine, which spot should be used for attaching a node
		    ((IContainer) origParent).attach(null, node, false);
		} else if (origParent instanceof Slot) {
		    ((Slot) origParent).parent().attach(null, node, false);
		} else if (origParent instanceof AbstractNode) {
		    // need to handle a case where draggedNode is draggable but not removable
		    if (node instanceof IDraggable && !((IDraggable) node).getRemoveAfterDrop()) {
			stub.removeFromParent();
		    }
		    node.setOffset(originalPosition);
		    ((AbstractNode) origParent).addChild(node); // TODO this one if weird because if typecasting isn't performed then the system behaviours
		    // incorrectly....need to determine why????
		} else {
		    node.setOffset(originalPosition);
		    origParent.addChild(node);
		}

		finaliseDrop(null);
	    }
	});
    }

    /**
     * This method highlights the border of the node that can be dragged.
     */
    @Override
    public void mouseEntered(final PInputEvent event) {
	if (draggedNode == null && event.getButton() == 0 && event.getPickedNode() instanceof IBasicNode) {
	    event.getPickedNode().moveToFront();
	    ((IBasicNode) event.getPickedNode()).highlight(new DefaultStroke(2));
	    ((IBasicNode) event.getPickedNode()).onMouseEntered(event);
	}
    }

    /**
     * This method returns the original border of the node -- reverse to the
     * mouseEntered action.
     */
    @Override
    public void mouseExited(final PInputEvent event) {
	if (draggedNode == null && event.getButton() == 0 && event.getPickedNode() instanceof IBasicNode) {
	    ((IBasicNode) event.getPickedNode()).dehighlight();
	    ((IBasicNode) event.getPickedNode()).onMouseExited(event);
	}
    }

    public Runnable getPostDragAction() {
	return postDragAction;
    }

    public void setPostDragAction(final Runnable postDragAction) {
	this.postDragAction = postDragAction;
    }

}
