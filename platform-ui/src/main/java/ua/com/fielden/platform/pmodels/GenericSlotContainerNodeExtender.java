package ua.com.fielden.platform.pmodels;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.GenericSlotContainerNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

public class GenericSlotContainerNodeExtender<T extends AbstractNode> extends GenericSlotContainerNode<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GenericSlotContainerNodeExtender(int numberOfSpots, Shape shape) {
	super(numberOfSpots, shape);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void plug(PNode node, PNode spot) {

	if (node instanceof ContainerRetriever) {
	    ((ContainerRetriever) node).setContainer(this);
	}

	final Point2D point = calcDelta(node, spot);
	if (getChildrenReference().contains(node)) {
	    node.translate(point.getX(), point.getY());
	} else {
	    if (node instanceof GenericSlotContainerNodeExtender) {
		((GenericSlotContainerNodeExtender) node).translateHierarchy(point.getX(), point.getY());
	    } else {
		node.translate(point.getX(), point.getY());
	    }
	}

	final int index = getSlotNodes().indexOf(spot);
	getSlotAttachamnets().set(index, (T) node);

	doAfterPlug(node, spot);
    }

    @Override
    protected Point2D calcDelta(final PNode whatToMove, final PNode whereToMove) {

	final Rectangle2D whatToMoveBounds = whatToMove.getGlobalBounds();
	final Rectangle2D whereToMoveBounds = whereToMove.getGlobalBounds();

	final double whatToMoveCentreX = whatToMoveBounds.getX() + whatToMoveBounds.getWidth() / 2.;
	final double whatToMoveCentreY = whatToMoveBounds.getY() + whatToMoveBounds.getHeight() / 2.;
	final double whereToMoveCentreX = whereToMoveBounds.getX() + whereToMoveBounds.getWidth() / 2.;
	final double whereToMoveCentreY = whereToMoveBounds.getY() + whereToMoveBounds.getHeight() / 2.;

	final double dx = whereToMoveCentreX - whatToMoveCentreX;
	final double dy = whereToMoveCentreY - whatToMoveCentreY;

	return new Point2D.Double(dx, dy);
    }

    public void detach(final PInputEvent event, final PNode node, final boolean animiate, final boolean forcedDetach) {
	List<T> attachments = getSlotAttachamnets();
	if (attachments.contains(node)) {
	    attachments.set(attachments.indexOf(node), null);
	    if (node instanceof ContainerRetriever) {
		((ContainerRetriever) node).setContainer(null);
	    }
	}
	doAfterDetach(node);
    }

    @Override
    public void moveToFront() {
	super.moveToFront();
	for (T attachment : getSlotAttachamnets()) {
	    if (attachment != null && !(getChildrenReference().contains(attachment))) {
		attachment.moveToFront();
	    }
	}
    }

    @Override
    public void attach(PInputEvent event, PNode node, boolean animate) {
	if (!isCompatible(node)) {
	    return;
	}
	// find the closest spot
	final PNode slot = findClosestVacantSlot(node);
	// handle detachment of a node from its current parent
	if (node.getParent() instanceof IContainer) {
	    ((IContainer) node.getParent()).detach(event, node, animate, false);
	}
	// attache a node to a found spot
	plug(node, slot);
	// invoke a custom post attachment action
	doAfterAttach(node);
    }

    @Override
    protected void layoutComponents() {
	final double minWidth = slotShape.getBounds().getWidth() * numberOfSpots + // space for spots
		gapBetweenSpots * (numberOfSpots - 1) + // minimum space for gaps between spots
		getPedding().getLeft() + getPedding().getRight(); // space for padding...actually insets
	final double minHeight = slotShape.getBounds().getHeight() + // space for spots
		getPedding().getTop() + getPedding().getBottom(); // space for padding...actually insets
	setBounds(getBounds().getX(), getBounds().getY(), minWidth > getBounds().getWidth() ? minWidth : getBounds().getWidth(), minHeight > getBounds().getHeight() ? minHeight
		: getBounds().getHeight());
	// remove all spots and nodes in spots...
	for (final PNode node : slotNodes) {
	    node.removeFromParent();
	}
	// ////////////////////////// creating slots ////////////////////////
	// slot should be vertically centred
	final double spaceAvailableForSpotsVert = getBounds().getHeight() - (getPedding().getTop() + getPedding().getBottom());
	final double spaceOccupiedBySpotsVert = slotShape.getBounds().getHeight() * 1;
	final double remainingSpaceForGapsVert = spaceAvailableForSpotsVert - spaceOccupiedBySpotsVert;
	final double yOffset = getPedding().getTop() + remainingSpaceForGapsVert / 2.;
	// ... and horizontally centred
	double xOffset = getPedding().getLeft();
	final double spaceAvailableForSpotsHoriz = getBounds().getWidth() - (getPedding().getLeft() + getPedding().getRight());
	final double spaceOccupiedBySpotsHoriz = slotShape.getBounds().getWidth() * numberOfSpots;
	final double remainingSpaceForGapsHoriz = spaceAvailableForSpotsHoriz - spaceOccupiedBySpotsHoriz;
	if (numberOfSpots == 1) {
	    gapBetweenSpots = remainingSpaceForGapsHoriz / (numberOfSpots);
	} else {
	    gapBetweenSpots = remainingSpaceForGapsHoriz / (numberOfSpots - 1);
	}
	// layout slots evenly ...
	for (final PNode node : slotNodes) {
	    node.setOffset(xOffset, yOffset);
	    xOffset += node.getBounds().getWidth() + gapBetweenSpots;
	    addChild(node);
	}
	// because of rounding error in gapBetweenSpots calculation it is necessary to readjust the width
	xOffset -= gapBetweenSpots;
	xOffset += getPedding().getRight();
	setBounds(getBounds().getX(), getBounds().getY(), xOffset, getBounds().getHeight());
	// attache nodes to spots
    }

    @SuppressWarnings("unchecked")
    public void translateHierarchy(double dx, double dy) {
	super.offset(dx, dy);
	for (T attachment : getSlotAttachamnets()) {
	    if (attachment != null) {
		if (!(getChildrenReference().contains(attachment))) {
		    if (attachment instanceof GenericSlotContainerNodeExtender) {
			((GenericSlotContainerNodeExtender) attachment).translateHierarchy(dx, dy);
		    } else {
			attachment.offset(dx, dy);
		    }
		}
	    }
	}
    }

}
