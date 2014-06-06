package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This is a container similar to GenericContainerNode, but it does not re-size when nodes are attached/detached, and most importantly, it attaches nodes only in specific locations
 * -- spots.
 * 
 * The container logic automatically determines, which spot should be highlighted when a node is being dragged over it, and to which spot a node should be attached when the mouse
 * button is released.
 * 
 * Horizontal and vertical spot layouts are supported out of the box, which evenly distribute spots in the middle of the container from left to right or from top to bottom
 * respectively. However an alternative layout can be implemented by overriding GenericSpotContainterNode.
 * 
 * @author 01es
 * 
 */
public class GenericSlotContainerNode<T extends AbstractNode> extends AbstractNode implements IContainer, IDraggable {
    private static final long serialVersionUID = -5910719015574785677L;

    // spotAttachamnets has the size of the number of spots; spotAttachamnets[index] contains a node, which is attached at spot index;
    // spotAttachamnets is just a convenient way of referencing attached nodes by slot index.
    private final List<T> slotAttachamnets = new ArrayList<T>();
    protected final List<PNode> slotNodes = new ArrayList<PNode>();
    private final List<Paint> slotNodesPaint = new ArrayList<Paint>();
    // default shape of the slot
    protected transient Shape slotShape = new Rectangle2D.Double(0, 0, 10, 50);
    protected double gapBetweenSpots = 5.; // this is the minimum gap...
    protected final int numberOfSpots;

    /**
     * A constructor, which instantiates GenericSpotContainterNode by the number of slots, which is used for determining the initial size of the container.
     * 
     * @param numberOfSpots
     */
    public GenericSlotContainerNode(final int numberOfSpots, final Shape shape) {
        super(shape);

        assert numberOfSpots > 1 : "at least two spots are required...";

        setPedding(new AbstractNode.Pedding(5, 5, 5, 5));
        this.numberOfSpots = numberOfSpots;
        setBackgroundColor(Color.white);
        // initialise spotAttachamnets
        final float dash[] = { 2.0f };
        final Stroke stroke = new DefaultStroke(0.1f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        for (int index = 0; index < numberOfSpots; index++) {
            slotAttachamnets.add(null);
            final PPath spot = new PPath(slotShape);
            spot.setPaint(new Color(1, 1, 1, 1f));
            spot.setStroke(stroke);
            spot.setPickable(false);
            slotNodes.add(spot);
            slotNodesPaint.add(getBackgroundColor());
        }

        reshape(false);
    }

    public void reshape(final boolean animate) {
        layoutComponents();
    }

    protected void layoutComponents() {
        // TODO at the moment the horizontal layout is used, however a more flexible approach should be provided at the later stage
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
        for (final PNode node : slotAttachamnets) {
            if (node != null) { // there can be spots with no node attached
                node.removeFromParent();
            }
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
        gapBetweenSpots = remainingSpaceForGapsHoriz / (numberOfSpots - 1);
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
        for (final PNode node : slotAttachamnets) {
            if (node != null) {
                addChild(node);
            }
        }
    }

    public boolean isCompatible(final PNode node) {
        return (node instanceof AbstractNode && findClosestVacantSlot(node) != null); // compatible with anything...
    }

    public void attach(final PInputEvent event, final PNode node, final boolean animate) {
        if (!isCompatible(node)) {
            return;
        }
        // find the closest spot
        final PNode slot = findClosestVacantSlot(node);
        // handle detachment of a node from its current parent
        if (node.getParent() instanceof IContainer) {
            ((IContainer) node.getParent()).detach(event, node, animate, false);
        } else {
            node.removeFromParent();
        }
        // attache a node to a found spot
        plug(node, slot);
        // invoke a custom post attachment action
        doAfterAttach(node);
    }

    /**
     * Moves a node onto a slot.
     * 
     * @param node
     * @param spot
     */
    @SuppressWarnings("unchecked")
    protected void plug(final PNode node, final PNode spot) {
        addChild(node);

        final Point2D point = calcDelta(node, spot);
        node.translate(point.getX(), point.getY());
        final int index = slotNodes.indexOf(spot);
        slotAttachamnets.set(index, (T) node);

        doAfterPlug(node, spot);
    }

    /**
     * This methods is invoked at the end of plug() method. It is catered for post plug actions.
     * 
     * @param node
     * @param spot
     */
    protected void doAfterPlug(final PNode node, final PNode spot) {
    }

    public void detach(final PInputEvent event, final PNode node, final boolean animiate, final boolean forcedDetach) {
        if (slotAttachamnets.contains(node)) {
            slotAttachamnets.set(slotAttachamnets.indexOf(node), null);
        }
        node.removeFromParent();
        doAfterDetach(node);
    }

    /**
     * Looks for geometrically closest spot to a node, which are compatible.
     * 
     * @param node
     *            Node to be dropped into a container
     * @return slot, which should be used to attaching a node
     */
    protected PNode findClosestVacantSlot(final PNode node) {
        // need to use global coordinates for finding a closes spot because a node may not have been yet attached to container
        final Point2D globOffset = node.getGlobalBounds().getCenter2D(); // localToGlobal(node.getOffset());
        final double nodeCentreX = globOffset.getX();// + node.getBounds().getWidth() / 2.; // node.getGlobalTranslation().getX() +
        // node.getBounds().getWidth() / 2.;
        double minDist = Double.MAX_VALUE;
        int closestSpotIndex = -1;
        // find geometrically closest compatible spot
        for (int index = 0; index < slotNodes.size(); index++) {
            // only empty spots should be used or the one, which already contains this node
            if (slotAttachamnets.get(index) != null && slotAttachamnets.get(index) != node) {
                continue;
            }
            final double spotCentreX = slotNodes.get(index).getGlobalBounds().getCenter2D().getX(); // spotNodes.get(index).getGlobalTranslation().getX() +
            // spotNodes.get(index).getBounds().getWidth() / 2.;
            if (minDist > Math.abs(spotCentreX - nodeCentreX)) {
                minDist = Math.abs(spotCentreX - nodeCentreX);
                closestSpotIndex = index;
            }
        }

        return closestSpotIndex >= 0 ? slotNodes.get(closestSpotIndex) : null;
    }

    public void dehighlight() {
        super.setStroke(getOriginalStroke());
        super.setPaint(getBackgroundColor());
        // if there are links they should be de-highlighted
        for (final ILink link : getLinks()) {
            link.dehightlight();
        }
        // remove highlighting of spots
        for (int index = 0; index < slotNodes.size(); index++) {
            final PNode spotNode = slotNodes.get(index);
            spotNode.setPaint(slotNodesPaint.get(index));
        }
    }

    public void highlight(final PNode node, final Paint paint) {
        if (node != null) {
            final PNode closestSpot = findClosestVacantSlot(node);
            closestSpot.setPaint(paint);
        } else {
            setPaint(paint);
        }

    }

    protected Point2D calcDelta(final PNode whatToMove, final PNode whereToMove) {
        final double whatToMoveCentreX = whatToMove.getOffset().getX() + whatToMove.getBounds().getWidth() / 2.;
        final double whatToMoveCentreY = whatToMove.getOffset().getY() + whatToMove.getBounds().getHeight() / 2.;
        final double whereToMoveCentreX = whereToMove.getOffset().getX() + whereToMove.getBounds().getWidth() / 2.;
        final double whereToMoveCentreY = whereToMove.getOffset().getY() + whereToMove.getBounds().getHeight() / 2.;

        final double dx = whereToMoveCentreX - whatToMoveCentreX;
        final double dy = whereToMoveCentreY - whatToMoveCentreY;
        return new Point2D.Double(dx, dy);
    }

    public boolean canDrag() {
        return true;
    }

    public boolean getRemoveAfterDrop() {
        return true;
    }

    public void setRemoveAfterDrop(final boolean flag) {
    }

    public boolean canBeDetached() {
        return true;
    }

    public void doAfterAttach(final PNode node) {
    }

    public void doAfterDetach(final PNode node) {
    }

    protected List<PNode> getSlotNodes() {
        return slotNodes;
    }

    public List<T> getSlotAttachamnets() {
        return slotAttachamnets;
    }

    protected double getGapBetweenSpots() {
        return gapBetweenSpots;
    }

    protected void setGapBetweenSpots(final double gapBetweenSpots) {
        this.gapBetweenSpots = gapBetweenSpots;
    }

    protected void addSpotCaption(final int spotIndex, final String captionText, final int fontSize, final Color color) {
        final PNode slotNode = getSlotNodes().get(spotIndex);
        slotNode.removeAllChildren();
        final PText caption = new PText(captionText);
        caption.setPickable(false);
        final Font newFont = caption.getFont().deriveFont(Font.BOLD, fontSize);
        caption.setTextPaint(new Color(80, 80, 80));
        caption.setFont(newFont);
        slotNode.addChild(caption); // add a caption
        slotNode.setPaint(color);
        slotNodesPaint.set(spotIndex, color);

        final double x = (slotNode.getWidth() - caption.getWidth()) / 2.;
        final double y = (slotNode.getHeight() - caption.getHeight()) / 2.;

        caption.translate(x, y);
    }
}
