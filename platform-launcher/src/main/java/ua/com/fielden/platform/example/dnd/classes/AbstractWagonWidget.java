package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import ua.com.fielden.platform.events.IDecorable;
import ua.com.fielden.platform.example.dnd.WidgetFactory;
import ua.com.fielden.platform.pmodels.GenericSlotContainerNodeExtender;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * <code>AbstractWagonWidget</code> provides implementation of the graphical widget representing a wagon. It does not support any actions and features -- strictly visual
 * representation and inherited from GenericSpotContainerNode snapping behaviour.
 * 
 * Please note that method {@link #canAccept(int, T)} needs to be implemented by every descendant to provide custom logic that determines compatibility between spots and a widget
 * being snapped-in.
 * 
 * @author 01es
 */
public abstract class AbstractWagonWidget<T extends AbstractBogieWidget<?>> extends GenericSlotContainerNodeExtender<T> implements IDecorable {
    private static final long serialVersionUID = -5399282869996057483L;

    private PText frontMark;
    private PText rearMark;

    private final Class<T> klass;

    private Stroke selectableStroke = new DefaultStroke(2.0f);
    private Stroke unselectableStroke = null;
    private Paint selectablePaint = Color.RED;
    private Paint unselectablePaint = Color.BLACK;

    /**
     * Instantiates a widget representing a wagon.
     * 
     * @param klass
     * @param shape
     * @param numberOfSlots
     * @param orientation
     */
    public AbstractWagonWidget(final Class<T> klass, final Shape shape, final int numberOfSlots, final WidgetOrientation orientation) {
        super(numberOfSlots, shape);
        this.klass = klass;

        setBackgroundColor(new Color(1, 1, 1, 0.8f));
        setPedding(new AbstractNode.Pedding(25, 25, 60, 60));
        setBounds(0, 0, WidgetFactory.BOGIE_WIDTH * numberOfSlots + getGapBetweenSpots() * (numberOfSlots - 1) + getPedding().getLeft() + getPedding().getRight(), 70);
        reshape(false);
        addMarks();
        unselectableStroke = getStroke();
        // handle widget orientation
        if (orientation == WidgetOrientation.VERTICAL) {
            double x = getBounds().getMaxX();
            double y = getBounds().getMinY();
            rotateAboutPoint(Math.toRadians(-90), x, y);
            translate(0, -x);

            x = frontMark.getBounds().getMaxX();
            y = frontMark.getBounds().getMinY();
            frontMark.rotateAboutPoint(Math.toRadians(90), x, y);
            frontMark.setOffset(frontMark.getOffset().getX(), (getHeight() - frontMark.getHeight() / 2) / 2);

            x = rearMark.getBounds().getMaxX();
            y = rearMark.getBounds().getMinY();
            rearMark.rotateAboutPoint(Math.toRadians(90), x, y);
            rearMark.setOffset(rearMark.getOffset().getX(), (getHeight() - rearMark.getHeight() / 2) / 2);
        }
    }

    /**
     * A convenience constructor for cases where the default horizontal orientation is appropriate.
     * 
     * @param klass
     * @param shape
     * @param numberOfSlots
     */
    public AbstractWagonWidget(final Class<T> klass, final Shape shape, final int numberOfSlots) {
        this(klass, shape, numberOfSlots, WidgetOrientation.HORIZONTAL);
    }

    private void addMarks() {
        frontMark = new PText("B");
        frontMark.setPickable(false);
        final Font newFont = frontMark.getFont().deriveFont(Font.BOLD, 20);
        frontMark.setTextPaint(new Color(192, 192, 192));
        frontMark.setFont(newFont);
        getLayoutIgnorantNodes().add(frontMark);
        addChild(frontMark);
        frontMark.setOffset(-30, (getHeight() - frontMark.getHeight()) / 2);

        rearMark = new PText("A");
        rearMark.setPickable(false);
        rearMark.setTextPaint(new Color(192, 192, 192));
        rearMark.setFont(newFont); // the same font as for frontMark
        getLayoutIgnorantNodes().add(rearMark);
        addChild(rearMark);
        rearMark.setOffset(getWidth() + 15, (getHeight() - rearMark.getHeight()) / 2);
    }

    public boolean isCompatible(final PNode node) {
        return findClosestVacantSpot(node) != null;
    }

    protected PNode findClosestVacantSpot(final PNode node) {
        if (!node.getClass().equals(klass)) {
            return null;
        }
        final T bNode = klass.cast(node);
        // need to use global coordinates for finding a closes spot because a node may not have been yet attached to container
        final Point2D globOffset = bNode.getGlobalBounds().getCenter2D();

        final double nodeCentreX = globOffset.getX();
        double minDist = Double.MAX_VALUE;
        int closestSpotIndex = -1;
        for (int index = 0; index < getSlotNodes().size(); index++) {
            try {
                // only empty spots should be used or the one, which already contains this node
                if (getSlotAttachamnets().get(index) == null && canAccept(index, bNode)) {
                    final double spotCentreX = getSlotNodes().get(index).getGlobalBounds().getCenter2D().getX();
                    if (minDist > Math.abs(spotCentreX - nodeCentreX)) {
                        minDist = Math.abs(spotCentreX - nodeCentreX);
                        closestSpotIndex = index;
                    }
                }
            } catch (final Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return closestSpotIndex >= 0 ? getSlotNodes().get(closestSpotIndex) : null;
    }

    /**
     * This method is an extension point used by {@link #findClosestVacantSpot(PNode)} in search of the closes suitable spot for attaching a widget. A custom logic should be
     * provided by descendants.
     * 
     * @param slotIndex
     *            -- index of the slot being currently tested for compatibility with a widget
     * @param widgetToTest
     *            -- a widget being tested for compatibility with current slot.
     * @return
     */
    protected abstract boolean canAccept(final int slotIndex, final T widgetToTest);

    @Override
    public boolean canDrag() {
        return true;
    }

    @Override
    public void Decorate() {
        setStroke(selectableStroke);
        setStrokePaint(selectablePaint);
    }

    @Override
    public void Undecorate() {
        setStroke(unselectableStroke);
        setStrokePaint(unselectablePaint);
    }

}
