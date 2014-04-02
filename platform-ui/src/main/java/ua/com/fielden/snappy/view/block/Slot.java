package ua.com.fielden.snappy.view.block;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;

import ua.com.fielden.snappy.model.Type;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * Defines the concept of a slot, which belongs to a block (parent) and may hold another block.
 * 
 * @author 01es
 */
public abstract class Slot extends PPath {
    private static final long serialVersionUID = 1L;

    private BlockNode<?> parent;
    private BlockNode<?> block;
    private final Point2D jointOffset; // coordinates are local to this

    // slot

    /**
     * Constructs a slot of a given shape.
     * 
     * @param shape
     *            The shape of a slot.
     * @param jointOffset
     *            The point into which compatible block should be snapped in. Each block has a corresponding property.
     */
    public Slot(final Shape shape, final Point2D jointOffset) {
        super(shape);
        this.jointOffset = jointOffset;

        final float dash[] = { 2.0f };
        final Stroke stroke = new DefaultStroke(0.1f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        setPaint(new Color(1, 1, 1, 1f));
        setStroke(stroke);
    }

    public Slot(final Slot slot) {
        super(slot.getPathReference());
        this.jointOffset = slot.jointOffset;

        final float dash[] = { 2.0f };
        final Stroke stroke = new DefaultStroke(0.1f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        setPaint(new Color(1, 1, 1, 1f));
        setStroke(stroke);
    }

    /**
     * This method is overridden in order to ensure the integrity of the slot/block relationship. Basically, a slot can be associated only with descendants of BlockNode and only
     * once.
     */
    @Override
    public void setParent(final PNode parent) {
        if (parent() != null && parent != null && parent() != parent) {
            throw new IllegalStateException("This slot is already associated with a block.");
        }
        if (parent instanceof BlockNode) {
            super.setParent(parent);
            this.parent = (BlockNode<?>) parent;
        } else if (parent != null) {
            throw new IllegalArgumentException("Slot can be added only to a descendant of the BlockNode class.");
        }
        super.setParent(parent);
    }

    /**
     * Returns the block to which this slot belongs
     */
    public BlockNode<?> parent() {
        return parent;
    }

    /**
     * Returns the snapped in block, or null if no block was snapped in.
     * 
     * @return
     */
    public BlockNode<?> block() {
        return block;
    }

    /**
     * Snaps the block into this slot.
     * 
     * @param block
     *            Block to be snapped in.
     * @return indicates success (true) of failure (false) of the snapping.
     */
    public void snapIn(final BlockNode<?> block) {
        // snap out the block it is for some reason snapped into a different
        // slot
        if (block.isSnapped()) {
            block.getSlot().snapOut();
        }

        /*
         * // this trivial checking prevents adding as children block if this
         * slot has another block snapped in if (block() == null){}
         */

        // just in case
        block.removeFromParent();
        // block is always the first child of the slot
        super.addChild(0, block);
        // move the block to the top of the slot node
        block.moveToFront();
        this.block = block;
        final Point2D point = calcDelta(block);
        block.translate(point.getX(), point.getY());
        block.setOriginalSlot(this);// assign the original slot for the block

        block.setSlot(this);
        ////////////////////////////////////////////////////////////////////////////////
        // TODO ////////////////////////////////////////////////////////////////////////
        // TODO ///// The following lines should be uncommented! ///////////////////////
        // TODO ////////////////////////////////////////////////////////////////////////
        // TODO // They have been commented during snappy <-> TG dependencies change ///
        // TODO ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
        // TODO if ((block instanceof SingletonBlock) && (this.parent() instanceof SingletonBlock)) {
        // TODO     ((SingletonBlock<?>) block).setConditionRoot(((SingletonBlock<?>) this.parent()).getConditionRoot());
        // TODO }
        ////////////////////////////////////////////////////////////////////////////////
        // TODO ////////////////////////////////////////////////////////////////////////
        // TODO ///// The following lines should be uncommented! ///////////////////////
        // TODO ////////////////////////////////////////////////////////////////////////
        // TODO // They have been commented during snappy <-> TG dependencies change ///
        // TODO ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        if (parent != null) {
            parent.reshape(false);
        }
        //	fireModificationListening();
    }

    //    private void fireModificationListening() {
    //	// The ruleBlock that might change its modified status.
    //	final RuleBlock<?> parentRule = findParentRule(parent());
    //	boolean oldModifiedStatus = false;
    //	if (parentRule != null) { // && getComponentsActivator() != null
    //	    oldModifiedStatus = parentRule.isModified();
    //	}
    //
    //	if (parent() instanceof SingletonBlock<?> && !(parent() instanceof RuleBlock<?>)) {
    //	    if (((SingletonBlock<?>) parent()).getConditionRoot() != null) {
    //		//		System.out.println("::: fire modif. listening(property)");
    //		final PropertyBlock<?> condRoot = ((SingletonBlock<?>) parent()).getConditionRoot();
    //		final AbstractConditionalProperty<?> oldModel = condRoot.model();
    //		// System.err.println("condRoot.isCollectionProperty() == " + condRoot.isCollectionProperty());
    //		final AbstractConditionalProperty<?> newModel = condRoot.createModel();
    //
    //		//		System.out.println("\t||| new = " + newModel);
    //		//		System.out.println("\t||| old = " + oldModel);
    //		if (newModel != null && oldModel != null) {
    //		    condRoot.activateModifiedStatus(!ModelComparator.equals(newModel, oldModel));
    //		}
    //	    }
    //	} else { // The slot does not belong to any property. It is the child of Conditioned/PropertyHot or Rule block.
    //
    //	    if (parentRule != null) {
    //		//		System.out.println("::: fire modif. listening(rule)");
    //		final Rule oldModel = parentRule.model();
    //		final Rule newModel = parentRule.createModel();
    //
    //		//		System.out.println("\t||| new = " + newModel);
    //		//		System.out.println("\t||| old = " + oldModel);
    //		if (newModel != null) {
    //		    final boolean skeletonModified = (oldModel == null) ? true : !ModelComparator.equals(newModel, oldModel);
    //		    parentRule.activateSkeletonModifiedStatus(skeletonModified);
    //		    //		    System.out.println("parentRule.activateSkeletonModifiedStatus(" + skeletonModified + ")");
    //		}
    //	    }
    //	}
    //
    //	// It's dangerous trick!
    //	if (parentRule != null && getComponentsActivator() != null) {
    //	    final boolean newModifiedStatus = parentRule.isModified();
    //	    if (newModifiedStatus && !oldModifiedStatus) {
    //		getComponentsActivator().increaseModifiedRulesNumber();
    //	    } else if (!newModifiedStatus && oldModifiedStatus) {
    //		getComponentsActivator().decreaseModifiedRulesNumber();
    //	    }
    //	}
    //    }
    //
    //    private ComponentsActivator getComponentsActivator() {
    //	final JFrame mainFrame = GlobalObjects.frame;
    //	if (mainFrame != null && mainFrame instanceof SnappyApplicationFrame && ((SnappyApplicationFrame) mainFrame).getApplicationModel() != null) {
    //	    return ((SnappyApplicationFrame) mainFrame).getApplicationModel().getComponentsActivator();
    //	}
    //	return null;
    //    }
    //
    //    private RuleBlock<?> findParentRule(final BlockNode<?> block1) {
    //	if (block1 != null) {
    //	    BlockNode<?> block = block1;
    //	    try {
    //		while (block.getSlot() != null) {
    //		    block = block.getSlot().parent();
    //		}
    //	    } catch (final Exception e) {
    //		e.printStackTrace();
    //	    }
    //	    return (block instanceof RuleBlock<?>) ? (RuleBlock<?>) block : null;
    //	} else {
    //	    return null;
    //	}
    //    }

    /**
     * Detaches the block from this slot.
     * 
     * @return Snapped out block.
     */
    public BlockNode<?> snapOut() {
        if (block() == null) {
            return null;
        }
        try {
            block().removeFromParent();
            return block();
        } finally {
            block.setSlot(null);
            ////////////////////////////////////////////////////////////////////////////////
            // TODO ////////////////////////////////////////////////////////////////////////
            // TODO ///// The following lines should be uncommented! ///////////////////////
            // TODO ////////////////////////////////////////////////////////////////////////
            // TODO // They have been commented during snappy <-> TG dependencies change ///
            // TODO ////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////
            // TODO if ((block instanceof SingletonBlock)) {
            // TODO 	((SingletonBlock<?>) block).setConditionRoot(null);
            // TODO }
            ////////////////////////////////////////////////////////////////////////////////
            // TODO ////////////////////////////////////////////////////////////////////////
            // TODO ///// The following lines should be uncommented! ///////////////////////
            // TODO ////////////////////////////////////////////////////////////////////////
            // TODO // They have been commented during snappy <-> TG dependencies change ///
            // TODO ////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////
            block.setPrevSlot(this);
            block = null;

            //	    if (!(parent() instanceof SegmentedExpandableBlock<?>)) {
            //		fireModificationListening();
            //	    }
        }
    }

    /**
     * Indicates slot availability. Return true if block can be snapped in, false otherwise.
     */
    public final boolean available(final BlockNode<?> block) {
        return (block() != null && block() == block) || // slot is not empty and
                // already contains the
                // block being snapped
                // in
                (block() == null && compatible(block)); // slot is empty and
        // block is compatible
        // with it;
    }

    /**
     * Each slot defines with what blocks it is compatible. This method should be invoked in the snapIn() method for compatibility test.
     * 
     * @param block
     *            Block passed for compatibility test.
     * @return true is block is compatible, false otherwise.
     */
    protected abstract boolean compatible(final BlockNode<?> block);

    /**
     * The same as {@link #getJointOffset()}, but in global coordinates.
     * 
     * @return
     */
    public Point2D getGlobalJointOffset() {
        final Point2D.Double global = new Point2D.Double(jointOffset.getX(), jointOffset.getY());
        return localToGlobal(global);
    }

    /**
     * Returns the value of jointOffset -- the spot where the block is moved upon snapping.
     * 
     * @return
     */
    public Point2D getJointOffset() {
        return new Point2D.Double(jointOffset.getX(), jointOffset.getY());
    }

    /**
     * Calculates the offset between block's and slot's joints. Used by {@link #snapIn(BlockNode) snapIn} to move the block into the slot.
     * 
     * @param block
     *            -- block, which is being snapped in.
     * @return -- the offset in global coordinates.
     */
    protected Point2D calcDelta(final BlockNode<?> block) {
        final double dx = getGlobalJointOffset().getX() - block.getGlobalJointOffset().getX();
        final double dy = getGlobalJointOffset().getY() - block.getGlobalJointOffset().getY();
        return new Point2D.Double(dx, dy);
    }

    /**
     * Indicates whether slot is associated with any block.
     * 
     * @return
     */
    public boolean isEmpty() {
        return block() == null;
    }

    @SuppressWarnings("unchecked")
    public <S extends Slot> S copy(final S slot) {
        final Class<?> clazz = slot.getClass();
        try {
            final Constructor<S> constructor = (Constructor<S>) clazz.getConstructor(slot.getClass());
            final S newInstance = constructor.newInstance(slot);
            return newInstance;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean parentWasEverSnappedIn() {
        return parent().getOriginalSlot() != null;
    }

    protected boolean parentTypeEquals(final Type type) {
        return parent().type() == type;
    }

}
