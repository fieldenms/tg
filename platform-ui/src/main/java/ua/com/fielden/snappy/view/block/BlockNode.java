package ua.com.fielden.snappy.view.block;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.poifs.storage.PropertyBlock;

import ua.com.fielden.snappy.model.Type;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Represents a block, which is associated with some object of the supported
 * type.
 *
 * NOTE: when inheriting this class a special care should be taken to check that
 * type T corresponds to one of the values of Type enumeration. If not then an
 * exception should be raised.
 *
 * The block acts as a container similar to GenericSpotContainerNode, but it has
 * a specific way to layout slots.
 *
 * The container logic automatically determines, which slot should be
 * highlighted when another block is being dragged over it, and to which slot a
 * block should be attached upon the drop.
 *
 * One of the important properties is the ability of dynamically add new slots.
 *
 * @author 01es
 *
 */
public abstract class BlockNode<T> extends AbstractNode implements IContainer, IDraggable, Serializable {
    private Double originalWidth = null;
    /**
     * This is the slot where this block is snapped into; it has null value if
     * this block is not snapped anywhere.
     */
    private Slot slot;
    /**
     * This is the slot of the block's previous location; prevSlot could be ==
     * to slot.
     */
    private Slot prevSlot;
    /**
     * This is the first slot where block was snapped in.
     */
    private Slot originalSlot;
    /**
     * This is a list of slot that belong to this block.
     */
    private final List<Slot> slots = new ArrayList<Slot>();
    /**
     * The original slot paint, which is required for dehighlighting
     */
    private final List<Paint> slotsPaint = new ArrayList<Paint>();

    private final Point2D jointOffset; // coordinates are local to this block

    /**
     * This constructor should not be used directly for instantiation of blocks.
     * Its purpose is to provide a convenient base constructor.
     *
     * @param shape
     *            -- The shape of the block
     * @param jointOffset
     *            -- The point in relation to which happens snapping of this
     *            block into a slot.
     */
    protected BlockNode(final Shape shape, final Point2D jointOffset) {
	super(shape);
	this.jointOffset = jointOffset;
    }

    /**
     * Constructs a block with a given shape and slots lay outed in accordance
     * with method layoutComponents().
     *
     * @param shape
     *            -- Shape of the block, which should accommodate the joint.
     * @param jointOffset
     *            -- The point in relation to which happens snapping of this
     *            block into a slot.
     * @param slots
     *            -- A list of slots for this block.
     */
    protected BlockNode(final Shape shape, final Point2D jointOffset, final List<? extends Slot> slots) {
	this(shape, jointOffset);
	for (final Slot slot : slots) {
	    append(slot);
	}
	// layout slots
	layoutComponents();
    }

    /**
     * Helper method for appending slots.
     *
     * @param slot
     */
    protected void append(final Slot slot) {
	addChild(slot);
	getSlots().add(slot);
	slotsPaint.add(slot.getPaint());
    }

    /**
     * /** Removes slot from the block.
     *
     * @param slot
     *            -- slot to be removed
     * @return -- true if the actual removal took place
     */
    public boolean remove(final Slot slot) {
	final int index = getSlots().indexOf(slot);
	if (index >= 0) {
	    getSlots().remove(index);
	    slot.removeFromParent();
	    slotsPaint.remove(index);
	}
	return index > -1;
    }

    @Override
    public void reshape(final boolean animate) {
	// no reshaping is necessary
    }

    /**
     * This method should be overridden to layout slots and potentially any
     * other nodes of the block.
     */
    @Override
    protected abstract void layoutComponents();

    public boolean isCompatible(final PNode node) {
	return (node instanceof BlockNode) ? (findAvailableSlot((BlockNode<?>) node) != null) : false;
    }

    /**
     * Looks for geometrically closest (Euclidian distance is used) compatible
     * slot for a block.
     *
     * @param block
     *            Block to be snapped
     * @return slot, which should be used for snapping
     */
    protected Slot findAvailableSlot(final BlockNode<?> block) {
	// need to use global coordinates for finding a closes vacant slot,
	// because the block may not have been yet attached to the container
	final Point2D blockJointOffset = block.getGlobalJointOffset();
	double minDist = Double.MAX_VALUE;
	// find geometrically closest available and compatible slot
	Slot closestSlot = null;
	for (final Slot slot : slots()) {
	    if (!slot.available(block)) {
		continue;
	    }
	    final Point2D spotSnapPoint = slot.getGlobalJointOffset();
	    final double dist = Math.pow(blockJointOffset.getX() - spotSnapPoint.getX(), 2) + Math.pow(blockJointOffset.getY() - spotSnapPoint.getY(), 2);
	    if (minDist > dist) {
		minDist = dist;
		closestSlot = slot;
	    }
	}
	return closestSlot;
    }

    public void attach(final PInputEvent event, final PNode node, final boolean animate) {
	if (!isCompatible(node)) {
	    return;
	}
	final BlockNode<?> block = (BlockNode<?>) node;
	// find the closest spot
	final Slot slot = findAvailableSlot(block);

	// handle detachment of a node from its current parent just in case if
	// it was not already detached
	if (block.getParent() instanceof Slot) { // if block is snapped into
	    // a slot ...
	    ((Slot) block.getParent()).snapOut();
	} else if (block.getParent() instanceof IContainer) { // if block is
	    // associated
	    // with some
	    // other
	    // container ...
	    ((IContainer) block.getParent()).detach(event, node, animate, false);
	} else { // otherwise ...
	    node.removeFromParent();
	}
	// snap block into slot
	slot.snapIn(block);
	// invoke a custom post attachment action
	doAfterAttach(block);
    }

    public void detach(final PInputEvent event, final PNode block, final boolean animiate, final boolean forcedDetach) {
	for (final Slot slot : slots()) {
	    if (slot.block() == block) {
		slot.snapOut();
		break;
	    }
	}
	doAfterDetach(block);
    }

    @Override
    public void dehighlight() {
	super.setStroke(getOriginalStroke());
	super.setPaint(getBackgroundColor());
	// if there are links they should be de-highlighted
	for (final ILink link : getLinks()) {
	    link.dehightlight();
	}
	// remove highlighting of spots
	for (int index = 0; index < slots().size(); index++) {
	    final Slot slot = slots().get(index);
	    slot.setPaint(slotsPaint().get(index));
	}
    }

    @Override
    public void highlight(final PNode node, final Paint paint) {
	if (node != null) {
	    final Slot closestSpot = findAvailableSlot((BlockNode<?>) node);
	    closestSpot.setPaint(paint);
	} else {
	    setPaint(paint);
	}

    }

    public boolean canDrag() {
	return true;
    }

    public boolean getRemoveAfterDrop() {
	return true;
    }

    public void setRemoveAfterDrop(final boolean flag) {
    }

    @Override
    public boolean canBeDetached() {
	return true;
    }

    public List<? extends Slot> slots() {
	return Collections.unmodifiableList(slots);
    }

    /**
     * This method should be used strictly for internal needs on the BlockNode
     * descendants.
     *
     * @return The <code>slots</code> property.
     */
    private List<Slot> getSlots() {
	return slots;
    }

    public List<Slot> getSlotCopies() {
	final List<Slot> slotCopies = new ArrayList<Slot>();
	for (final Slot slot : slots) {
	    slotCopies.add(slot.copy(slot));
	}
	return slotCopies;
    }

    public void doAfterAttach(final PNode node) {
    };

    public void doAfterAttachAll() {
	for (final Slot slot : slots()) {
	    if (slot.block() != null && !isPossiblyFetching(slot.block())) {
		doAfterAttach(slot.block());
		slot.block().doAfterAttachAll();
	    }
	}
    }

    /**
     * Determines whether the specified block could represent "fetching" property (PropewrtyBlock or PropertyHotBlock).
     * @return
     */
    protected boolean isPossiblyFetching(final BlockNode<?> block) {
	////////////////////////////////////////////////////////////////////////////////
	// TODO ////////////////////////////////////////////////////////////////////////
	// TODO ///// The following lines should be uncommented! ///////////////////////
	// TODO ////////////////////////////////////////////////////////////////////////
	// TODO // They have been commented during snappy <-> TG dependencies change ///
	// TODO ////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	return true; // TODO (block instanceof PropertyBlock && ((PropertyBlock<?>) block).isFetchingProperty()) || //
		     // TODO (block instanceof PropertyHotBlock && ((PropertyHotBlock<?>) block).isPossiblyFetching());
	////////////////////////////////////////////////////////////////////////////////
	// TODO ////////////////////////////////////////////////////////////////////////
	// TODO ///// The following lines should be uncommented! ///////////////////////
	// TODO ////////////////////////////////////////////////////////////////////////
	// TODO // They have been commented during snappy <-> TG dependencies change ///
	// TODO ////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
    }

    public void restoreOriginalWidth(){
	if (originalWidth != null){
	    setWidth(originalWidth);
	    for (final Slot slot : slots()) {
		if (slot.block() != null) {
		    slot.block().restoreOriginalWidth();
		}
	    }
	}
    }

    public void doAfterDetach(final PNode node) {
    }

    /**
     * Returns the value of jointOffset on global coordinate system.
     *
     * @return
     */
    public Point2D getGlobalJointOffset() {
	final Point2D.Double global = new Point2D.Double(jointOffset.getX(), jointOffset.getY());
	return localToGlobal(global);
    }

    /**
     * Returns a copy of jointOffset with no transformation.
     *
     * @return
     */
    public Point2D getJointOffset() {
	final Point2D.Double offset = new Point2D.Double(jointOffset.getX(), jointOffset.getY());
	return offset;
    }

    protected List<Paint> slotsPaint() {
	return slotsPaint;
    }

    /**
     * Returns an object associated with this block.
     */
    public abstract T object();

    /**
     * Returns one of the high grained types as defined in enumeration Type.
     */
    public abstract Type type();

    /**
     * Returns a slot containing this block.
     *
     * @return -- <code>null</code> if this block is not snapped anywhere
     */
    public Slot getSlot() {
	return slot;
    }

    /**
     * This setter is used by the {@link Slot#snapIn(BlockNode)} when this block
     * is snapped into or out of the slot.
     *
     * @param slot
     */
    protected void setSlot(final Slot slot) {
	this.slot = slot;
    }

    /**
     * Identifies whether this block is snapped somewhere.
     *
     * @return
     */
    public boolean isSnapped() {
	return getSlot() != null;
    }

    /**
     * Returns a slot, which was the previous location of this block. There can
     * be a situation where <code>getSlot() == getPrevSlot()</code>. This is a
     * legitimate case that could be a result of re-snapping of a block into the
     * same slot without any other intermediate slots.
     *
     * @return
     */
    public Slot getPrevSlot() {
	return prevSlot;
    }

    /**
     * This method is used by the {@link Slot#snapOut()} when this block is
     * snapped out if the slot.
     *
     * @param prevSlot
     */
    void setPrevSlot(final Slot prevSlot) {
	this.prevSlot = prevSlot;
    }

    /**
     * This method is invoked by
     * {@link ua.com.fielden.uds.designer.zui.event.DragEventHandler
     * DragEventHandler} upon the drop of the block after dragging. If this drop
     * is the result of the block being snapped out of some slot then the slot's
     * parent is notified to layout its components.
     */
    @Override
    public void onEndDrag(final PInputEvent event) {
	super.onEndDrag(event);
	if (getSlot() != getPrevSlot() && getPrevSlot() != null) {
	    getPrevSlot().parent().layoutComponents();
	}
    }

    /**
     * Creates a new instances using the copy constructor. This method should be
     * used instead of the clone().
     *
     * @param <B>
     * @return null if copy constructor is not present or in case of any other
     *         failure to construct a new instance.
     */
    @SuppressWarnings("unchecked")
    public <B extends BlockNode<T>> B copy() {
	final Class<?> clazz = this.getClass();
	try {
	    final Constructor<B> constructor = (Constructor<B>) clazz.getConstructor(clazz);
	    return constructor.newInstance(this);
	} catch (final Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Constructs the string containing hierarchy where this block belongs from
     * the very top block down to this one.
     *
     * @return
     */
    public String path() {
	final List<String> path = new ArrayList<String>();
	traverse(this, path);
	final StringBuffer buffer = new StringBuffer();
	for (final ListIterator<String> iter = path.listIterator(path.size()); iter.hasPrevious();) {
	    final String value = iter.previous();
	    buffer.append(value);
	    if (iter.hasPrevious()) {
		buffer.append(".");
	    }
	}
	return buffer.toString();
    }

    /**
     * Recursively traverses block's hierarchy to the top block, and populates
     * the list <code>path</code> with toString() of the blocks in the
     * hierarchy.
     * <p>
     * <b>Important note:</b> the block's original slot is used for determining
     * the hierarchy. Other words, the traversing process disregards block's
     * current location.
     * </p>
     *
     * @param block
     * @param path
     */
    private void traverse(final BlockNode<?> block, final List<String> path) {
	path.add(block.toString());
	if (block.getOriginalSlot() != null) {
	    traverse(block.getOriginalSlot().parent(), path);
	}
    }

    /**
     * Traverses through block's hierarchy upwards and looks for the first block
     * that matches the provided predicate.
     *
     * @param <P>
     * @param clazz
     * @param block
     * @param predicate
     * @return Actual block if found, otherwise null
     */
    public <P extends BlockNode> BlockNode lookForParentOfType(final Class<P> clazz, final BlockNode<?> block, final Predicate<P> predicate) {
	if (predicate.match(block, clazz)) { // try to match current block
	    return block;
	} else if (block.isSnapped()) { // if the current block does not match then move up the hierarchy if possible
	    return lookForParentOfType(clazz, block.getSlot().parent(), predicate);
	}
	// no match found and the current block is not snapped anywhere, which indicates the end of the hierarchy, and therefore search should be stopped
	return null;
    }

    /**
     * This is simply a convenience method, which can be used with the default
     * predicate that compares only the classes (see also
     * {@link #lookForParentOfType(Class, BlockNode, ua.com.fielden.snappy.view.block.BlockNode.Predicate)}
     * ).
     *
     * @param <P>
     * @param clazz
     * @param block
     * @return
     */
    public <P extends BlockNode<?>> BlockNode<?> lookForParentOfType(final Class<P> clazz, final BlockNode<?> block) {
	return lookForParentOfType(clazz, block, new Predicate<P>() {
	    public boolean match(final BlockNode<?> block, final Class<P> clazz) {
		return block.getClass() == clazz;
	    }
	});
    }

    /**
     * This is an interface describing predicate used in
     * {@link PropertyBlock#lookForParentOfType(Class, BlockNode, ua.com.fielden.snappy.view.block.BlockNode.Predicate)}
     * .
     *
     * @author 01es
     *
     */
    public static interface Predicate<P extends BlockNode> {
	boolean match(final BlockNode<?> block, Class<P> clazz);
    }

    /**
     * Test <code>block</code> for presents of <code>parent</code> in its
     * hierarchy. Method traverses block's actual (as opposite to original)
     * hierarchy upwards.
     *
     * @param <P>
     * @param parent
     *            -- parent, which is tested for presence in block's hierarchy.
     * @param block
     *            -- block whose hierarchy is tested for presence of the parent.
     * @return true is parent is in block's hierarchy, false otherwise.
     */
    public <P extends BlockNode<?>> boolean isInHierarchy(final P parent, final BlockNode<?> block) {
	if (block.isSnapped()) {
	    if (block.getSlot().parent() == parent) {
		return true;
	    }
	    return isInHierarchy(parent, block.getSlot().parent());
	}
	return false;
    }

    /**
     * Returns the original slot for this block.
     *
     * @return
     */
    public Slot getOriginalSlot() {
	return originalSlot;
    }

    /**
     * Sets the original slot for this block.
     *
     * @param originalSlot
     */
    protected void setOriginalSlot(final Slot originalSlot) {
	if (getOriginalSlot() == null) { // this is a guard
	    this.originalSlot = originalSlot;
	}
    }

    /**
     * Checks if this block has any snapped in blocks.
     *
     * @return true of at least one slot is not empty, false otherwise
     */
    public boolean hasSnappedInBlocks() {
	for (final Slot slot : getSlots()) {
	    if (!slot.isEmpty()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Tests block for being currently snapped in its original slot, where it
     * was snapped the very first time.
     *
     * @return
     */
    public boolean isInOriginalSlot() {
	return getOriginalSlot() == getSlot();
    }

    public void removeItself() {
	final BlockNode<?> parent = getSlot().parent();
	parent.remove(getSlot());
	getSlot().snapOut();
    }

    public Double getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(final Double originalWidth) {
        this.originalWidth = originalWidth;
    }
}
