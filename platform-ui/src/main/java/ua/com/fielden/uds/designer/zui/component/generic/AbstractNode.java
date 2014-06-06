package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import ua.com.fielden.uds.designer.zui.interfaces.ILinkedNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This class provides the basic functionality for visual nodes used in UDS Designer framework. Majority of nodes, which form part of this framework or need to interact with it
 * should most likely extend AbstractNode.
 * 
 * @author 01es
 */
public abstract class AbstractNode extends PPath implements IBasicNode, ILinkedNode {
    private Paint backgroundColor;
    private PDimension minConstraint;
    private Pedding pedding = new Pedding(10, 10, 10, 10);
    private BorderRounding rounding = new BorderRounding(false, false, false, false);

    private transient ReshapeActivity reshapeActivity;

    private boolean useIncrementalParentResizing = true;
    /**
     * layoutIgnorantNodes is a list of visual nodes, which are not taken into account when performing nodes' layout
     */
    private List<PNode> layoutIgnorantNodes = new ArrayList<PNode>();

    private void init() {
        setStroke(new DefaultStroke(1));
        setBackgroundColor(new Color(100, 100, 100));
        setReshapeActivity(new ReshapeActivity(300) {
            protected void onActivityFinished() {
                handleBorder();
            } // do nothing here
        });
    }

    protected void handleBorder() {
        roundBorderCorners(getBounds(), rounding.isLeftTop(), rounding.isLeftBottom(), rounding.isRightTop(), rounding.isRightBottom());
    }

    public AbstractNode() {
        super();
        init();
    }

    public AbstractNode(final Shape shape, final Stroke stroke) {
        super(shape, stroke);
        init();
        setStroke(stroke);
    }

    public AbstractNode(final Shape shape) {
        super(shape);
        init();
    }

    public Paint getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(final Paint backgroundColor) {
        this.backgroundColor = backgroundColor;
        setPaint(backgroundColor);
    }

    /**
     * Adjusts the bounds of the node to contain all children. Relocates children in one column.
     * 
     * @param animate
     */
    public void reshape(final boolean animate) {
        layoutComponents(); // can be overridden to provide an alternative
        // behaviour

        final Rectangle2D newBounds = calcBounds();
        if (animate && getReshapeActivity() != null && getRoot() != null) {
            // Must schedule the activity with the root for it to run.
            getReshapeActivity().init(this, newBounds);
            getRoot().addActivity(getReshapeActivity());
        } else {
            setBounds(newBounds);
            resetAll();
            handleParent(this);
        }

        roundBorderCorners(getBounds(), rounding.isLeftTop(), rounding.isLeftBottom(), rounding.isRightTop(), rounding.isRightBottom());
    }

    /**
     * This method is used by reshape() to determine new bounds. It can be overridden in order to provide a custom border determination;
     * 
     * @return
     */
    protected Rectangle2D calcBounds() {
        // need to remove all layout ignorant nodes begore recalculating the
        // bounds
        for (final PNode node : layoutIgnorantNodes) {
            node.removeFromParent();
        }

        final Rectangle2D childrenBounds = getUnionOfChildrenBounds(null);

        // add layout ignorant nodes back
        for (final PNode node : layoutIgnorantNodes) {
            addChild(node);
        }

        final double proposedWidth = childrenBounds.getWidth() + getPedding().getLeft() + getPedding().getRight();
        final double proposedHeight = childrenBounds.getHeight() + getPedding().getTop() + getPedding().getBottom();
        final Rectangle2D newBounds = new Rectangle2D.Double(getBounds().getX(), getBounds().getY(), (getMinConstraint() != null && getMinConstraint().width > proposedWidth ? getMinConstraint().width
                : proposedWidth), (getMinConstraint() != null && getMinConstraint().height > proposedHeight ? getMinConstraint().height : proposedHeight));

        // need to adjust offices of children
        for (final Object obj : getChildrenReference()) {
            final PNode child = (PNode) obj;
            if (!layoutIgnorantNodes.contains(child)) {
                final Point2D currOffset = child.getOffset();
                child.setOffset(currOffset.getX() + getPedding().getLeft(), currOffset.getY() + getPedding().getTop());
            }
        }
        return newBounds;
    }

    /**
     * This method is the default laying out embedded components
     */
    @SuppressWarnings("unchecked")
    protected void layoutComponents() {
        final double xOffset = 0;
        double yOffset = 0;
        // it is necessary to have nodes in some predefined order so that they
        // do not "jump" when they are removed/added.
        // the current implementation is using nod's toString() methods for
        // sorting.
        final Set<PNode> sortedChildren = new TreeSet<PNode>(new Comparator<PNode>() {
            public int compare(PNode node1, PNode node2) {
                return node1.toString().compareTo(node2.toString());
            }
        });
        sortedChildren.addAll(getChildrenReference());
        for (final PNode each : sortedChildren) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            each.setOffset(xOffset, yOffset - each.getY());
            yOffset += each.getFullBoundsReference().getHeight() + 3;
        }
    }

    /**
     * This method is responsible for horizontal layout of embedded components from the left to the right
     */
    @SuppressWarnings("unchecked")
    protected <T extends PNode> void hlrLayoutComponents(double xOffset, final Comparator<T> comparator) {
        // it is necessary to have nodes in some predefined order so that they
        // do not "jump" when they are removed/added.
        // the current implementation is using nod's toString() methods for
        // sorting.
        Set<T> sortedChildren = null;
        if (comparator == null) {
            sortedChildren = new TreeSet<T>(new Comparator<T>() {
                public int compare(final PNode node1, final PNode node2) {
                    return node1.toString().compareTo(node2.toString());
                }
            });
        } else {
            sortedChildren = new TreeSet<T>(comparator);
        }
        sortedChildren.addAll(getChildrenReference());

        double H = 0.0;
        for (final PNode each : sortedChildren) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            if (each.getHeight() > H) {
                H = each.getHeight();
            }
        }

        for (final PNode each : sortedChildren) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            final double yOffset = (H - each.getHeight()) / 2.0;
            each.setOffset(xOffset - each.getX(), yOffset);
            xOffset += each.getFullBoundsReference().getWidth() + 3;
        }
    }

    /**
     * This method is responsible for horizontal layout of embedded components from the right to the left
     */
    @SuppressWarnings("unchecked")
    protected void hrlLayoutComponents(final double yOffset, final Comparator<? extends PNode> comparator) {
        // it is necessary to have nodes in some predefined order so that they
        // do not "jump" when they are removed/added.
        // the current implementation is using node's toString() methods for
        // sorting by default.
        Set<PNode> sortedChildren = null;
        if (comparator == null) {
            sortedChildren = new TreeSet<PNode>(new Comparator<PNode>() {
                public int compare(final PNode node1, final PNode node2) {
                    return node1.toString().compareTo(node2.toString());
                }
            });
        } else {
            sortedChildren = new TreeSet(comparator);
        }
        sortedChildren.addAll(getChildrenReference());

        double xOffset = getWidth() - getPedding().getRight();
        for (final PNode each : sortedChildren) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            xOffset -= (each.getFullBoundsReference().getWidth() + 3);
            each.setOffset(xOffset + each.getX(), yOffset);
        }
    }

    /**
     * This method is responsible for vertical layout of embedded components from top to bottom.
     */
    @SuppressWarnings("unchecked")
    protected <T extends PNode> void vtbLayoutComponents(final double xOffset, double yOffset, final Comparator<T> comparator) {
        // it is necessary to have nodes in some predefined order so that they
        // do not "jump" when they are removed/added.
        // the current implementation is using nod's toString() methods for
        // sorting.
        Set<T> sortedChildren = null;
        if (comparator == null) {
            sortedChildren = new TreeSet<T>(new Comparator<T>() {
                public int compare(final PNode node1, final PNode node2) {
                    return node1.toString().compareTo(node2.toString());
                }
            });
        } else {
            sortedChildren = new TreeSet<T>(comparator);
        }
        sortedChildren.addAll(getChildrenReference());

        for (final PNode each : sortedChildren) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            each.setOffset(xOffset - each.getX(), yOffset - each.getY());
            yOffset += each.getFullBoundsReference().getHeight() + 3;
        }
    }

    protected void handleParent(final PNode node) {
        if (node.getParent() instanceof IContainer && node.getParent() instanceof AbstractNode) {
            ((AbstractNode) node.getParent()).reshape(isUseIncrementalParentResizing());
        }
    }

    protected PDimension getMinConstraint() {
        return minConstraint;
    }

    public void setMinConstraint(final PDimension minConstraint) {
        this.minConstraint = minConstraint;
    }

    private Stroke originalStroke;

    public void highlight(final Stroke stroke) {
        super.setStroke(stroke);
        // if there are links they should be highlighted
        for (final ILink link : getLinks()) {
            link.hightlight(stroke);
        }
    }

    public void dehighlight() {
        super.setStroke(originalStroke);
        super.setPaint(getBackgroundColor());
        // if there are links they should be de-highlighted
        for (final ILink link : getLinks()) {
            link.dehightlight();
        }
    }

    public void highlight(final PNode node, final Paint paint) {
        super.setPaint(paint);
    }

    public void setStroke(final Stroke stroke) {
        assert (stroke instanceof Serializable || stroke == null) : "Serializable stoke is required.";
        super.setStroke(stroke);
        setOriginalStroke(stroke);
    }

    public void moveToFront() {
        // move parents to front
        if (getParent() instanceof AbstractNode) {
            getParent().moveToFront();
        }
        // move itself to front
        super.moveToFront();
        // move its links to front
        final List<ILink> links = getLinks();
        if (links != null) {
            for (final ILink link : links) {
                ((PNode) link).moveToFront();
            }
        }
    }

    public AbstractNode getDeepParent(final MutablePoint2D offset) {
        PNode deepParent = getParent();
        if (deepParent instanceof AbstractNode) {
            final PNode prevParent = deepParent;
            deepParent = ((AbstractNode) deepParent).getDeepParent(offset);
            if (deepParent != prevParent && (offset != null)) {
                offset.x += prevParent.getOffset().getX();
                offset.y += prevParent.getOffset().getY();
            }
            return (AbstractNode) deepParent;
        }
        return this; // returns itself if there is no parent of type
        // AbstractNode
    }

    /**
     * This is a helper class.
     * 
     * @author 01es
     * 
     */
    public static class MutablePoint2D {
        public int x = 0;
        public int y = 0;
    }

    public Pedding getPedding() {
        return pedding;
    }

    public void setPedding(final Pedding pedding) {
        this.pedding = pedding;
    }

    public static class Pedding implements Serializable {
        private static final long serialVersionUID = 8045879901386235813L;

        private int top;
        private int bottom;
        private int left;
        private int right;

        public Pedding(final int top, final int bottom, final int left, final int right) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        public int getBottom() {
            return bottom;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }

        public int getTop() {
            return top;
        }
    }

    public static class BorderRounding implements Serializable {
        private static final long serialVersionUID = 7617895439582551366L;

        private boolean leftTop, leftBottom, rightTop, rightBottom;

        public BorderRounding(final boolean leftTop, final boolean leftBottom, final boolean rightTop, final boolean rightBottom) {
            this.leftTop = leftTop;
            this.leftBottom = leftBottom;
            this.rightTop = rightTop;
            this.rightBottom = rightBottom;
        }

        public boolean isLeftBottom() {
            return leftBottom;
        }

        public boolean isLeftTop() {
            return leftTop;
        }

        public boolean isRightBottom() {
            return rightBottom;
        }

        public boolean isRightTop() {
            return rightTop;
        }
    }

    @SuppressWarnings("unchecked")
    public List<ILink> getLinks() {
        if (getAttribute("links") != null) {
            return (List<ILink>) getAttribute("links");
        } else {
            final List<ILink> links = new ArrayList<ILink>();
            addAttribute("links", links);
            return links;
        }
    }

    @SuppressWarnings("unchecked")
    public void addLink(final ILink link) {
        if (getAttribute("links") == null) {
            addAttribute("links", new ArrayList<ILink>());
        }
        final List<ILink> links = ((List<ILink>) getAttribute("links"));
        if (!links.contains(link)) {
            links.add(link);
        }
    }

    public boolean isLinked(final ILinkedNode node) {
        for (final ILink link : getLinks()) {
            if (link.getStartNode() == node || link.getEndNode() == node) {
                return true;
            }
        }
        return false;
    }

    // TODO both resetAll implementations have some issues pertaining to extra
    // invocations,
    // especially when there are embedded linked containers.
    public void resetAll() {
        // handle links
        final List<ILink> links = getLinks();
        if (links != null) {
            for (final ILink link : links) {
                link.reset();
            }
        }
        // handle children of type ILinkedNode
        for (final Object kid : getChildrenReference()) {
            if (kid instanceof ILinkedNode) {
                ((ILinkedNode) kid).resetAll();
            }
        }
    }

    public void resetAll(final PDimension delta, final Set<PNode> processedNodes) {
        final List<ILink> links = getLinks();
        if (links.size() > 0) {
            final ILink link = links.get(0); // can pick any
            if (delta != null) {
                link.reset(this, delta, processedNodes);
            }
        }

        // handle children of type ILinkedNode
        for (final Object kid : getChildrenReference()) {
            if (kid instanceof ILinkedNode) {
                ((ILinkedNode) kid).resetAll();
            }
        }
    }

    public boolean canBeDetached() {
        return false;
    }

    public void onMouseEntered(final PInputEvent event) {
    }

    public void onMouseExited(final PInputEvent event) {
    }

    public void onStartDrag(final PInputEvent event) {
    }

    public void onEndDrag(final PInputEvent event) {
    }

    public void onDragging(final PInputEvent event) {
    }

    public boolean showToolTip() {
        return true;
    }

    public ReshapeActivity getReshapeActivity() {
        return reshapeActivity;
    }

    public void setReshapeActivity(final ReshapeActivity resizeActivity) {
        this.reshapeActivity = resizeActivity;
    }

    public List<PNode> getLayoutIgnorantNodes() {
        return layoutIgnorantNodes;
    }

    public void setLayoutIgnorantNodes(final List<PNode> layoutIgnorantNodes) {
        this.layoutIgnorantNodes.clear();
        this.layoutIgnorantNodes.addAll(layoutIgnorantNodes);
    }

    float curvaturePrc = 5.0f; // curvature %

    /**
     * Determines the shape of a border.
     * 
     * @param newBounds
     */
    private void roundBorderCorners(Rectangle2D newBounds, final boolean leftTop, final boolean leftBottom, final boolean rightTop, final boolean rightBottom) {
        newBounds = new Rectangle2D.Double(newBounds.getX(), newBounds.getY(), newBounds.getWidth() - 1, newBounds.getHeight() - 1);
        reset();
        double side = newBounds.getWidth();
        double shortSide = newBounds.getHeight();
        // curvature should be determined against a longer side
        if (newBounds.getWidth() < newBounds.getHeight()) {
            side = newBounds.getHeight();
            shortSide = newBounds.getWidth();
        }
        float curvature = (float) (side * curvaturePrc) / 100.0f;
        if (curvature > shortSide / 2.) {
            curvature = (float) (shortSide / 2.);
        }
        // left top corner
        moveTo(curvature, 0.0f);
        curveTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, leftTop ? curvature : 0.0f);
        lineTo(0.0f, (float) (newBounds.getHeight() - curvature));

        // left bottom corner
        curveTo(0.0f, (float) (newBounds.getHeight()), 0.0f, (float) (newBounds.getHeight()), leftBottom ? curvature : 0.0f, (float) (newBounds.getHeight()));
        lineTo((float) (newBounds.getWidth() - curvature), (float) (newBounds.getHeight()));
        // right bottom corner
        curveTo((float) (newBounds.getWidth()), (float) (newBounds.getHeight()), (float) (newBounds.getWidth()), (float) (newBounds.getHeight()), (float) (newBounds.getWidth()), (float) (newBounds.getHeight())
                - (rightBottom ? curvature : 0.0f));
        lineTo((float) (newBounds.getWidth()), curvature);
        // right top corner
        curveTo((float) (newBounds.getWidth()), 0.0f, (float) (newBounds.getWidth()), 0.0f, (float) (newBounds.getWidth() - (rightTop ? curvature : 0.0f)), 0.0f);
        closePath();
    }

    /**
     * If necessary any descendant of AbstractNode may provide an optional ring menu. By default there is no menu.
     * 
     * @return
     */
    public RingMenu getRingMenu() {
        return null;
    }

    protected Stroke getOriginalStroke() {
        return originalStroke;
    }

    protected void setOriginalStroke(final Stroke originalStroke) {
        this.originalStroke = originalStroke;
    }

    protected float getCurvaturePrc() {
        return curvaturePrc;
    }

    public void setCurvaturePrc(final float curvaturePrc) {
        this.curvaturePrc = curvaturePrc;
    }

    public boolean isUseIncrementalParentResizing() {
        return useIncrementalParentResizing;
    }

    public void setUseIncrementalParentResizing(final boolean useIncrementalParentResizing) {
        this.useIncrementalParentResizing = useIncrementalParentResizing;
    }

    public BorderRounding getRounding() {
        return rounding;
    }

    public void setRounding(final BorderRounding rounding) {
        this.rounding = rounding;
    }
}
