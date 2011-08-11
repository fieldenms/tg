package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.SwingWorker;

import ua.com.fielden.platform.events.IDecorable;
import ua.com.fielden.platform.example.dnd.WidgetFactory;
import ua.com.fielden.platform.pmodels.GenericSlotContainerNodeExtender;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * <code>AbstractBogieWidget</code> provides implementation of the graphical
 * widget representing a bogie. It does not support any actions and features --
 * strictly visual representation and inherited from GenericSpotContainerNode
 * snapping behaviour.
 * 
 * Please note that method {@link #canAccept(int, T)} needs to be implemented by
 * every descendant to provide custom logic that determines compatibility
 * between spots and a widget being snapped-in.
 * 
 * @author 01es
 */
public abstract class AbstractBogieWidget<T extends AbstractWheelsetWidget> extends GenericSlotContainerNodeExtender<T> implements RotableContainerRetriever, IDecorable {
    private static final long serialVersionUID = 8801220078341054978L;

    private final PPath title;

    private PartModel bogie;
    private PartModel caps[] = new PartModel[WidgetFactory.NUM_OF_BOGIE_PARTS];

    private final Class<T> klass;

    private IContainer container;

    /**
     * An interface, implementation of which can be passed as one of the
     * constructor parameters, to provide a custom left mouse click event
     * handler on the widget.
     * 
     * @author 01es
     */
    public static interface MouseClickCallback<T extends AbstractWheelsetWidget> {
	void doClick(final AbstractBogieWidget<T> instance);

	boolean isHandled();
    }

    private SwingWorker<Object, Object> worker; // used for executing mouse click event handler

    /**
     * Instantiates a widget representing a bogie.
     * 
     * @param caption
     * @param orientation
     */
    public AbstractBogieWidget(final Class<T> klass, final String caption, final WidgetOrientation orientation, final MouseClickCallback<T> callback) {
	super(2, new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2)); // bogie has two wheelsets... at least that is the current specification...
	this.klass = klass;
	setBounds(0, 0, WidgetFactory.BOGIE_WIDTH, WidgetFactory.BOGIE_HEIGHT);
	setPedding(new AbstractNode.Pedding(25, 25, 15, 14));
	final float dash[] = { 10.0f };
	setStroke(new DefaultStroke(0.1f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f));
	setBackgroundColor(new Color(1, 1, 1, 0.2f));
	initBogie();

	reshape(false); // need reshaping to make sure all added nodes fit nicely
	title = createTitle(caption);
	// handle widget orientation
	if (orientation == WidgetOrientation.HORIZONTAL) {
	    final double x = getBounds().getMaxX();
	    final double y = getBounds().getMinY();
	    rotateAboutPoint(Math.toRadians(-90), x, y);
	    translate(0, -x);
	}
	// add mouse click event handler
	if (callback != null) {
	    addInputEventListener(new PBasicInputEventHandler() {
		@Override
		public void mouseClicked(final PInputEvent event) {
		    if (event.getButton() == 1) {
			if (worker == null || worker.isDone() || worker.isCancelled()) {
			    worker = new SwingWorker<Object, Object>() {
				@Override
				protected Object doInBackground() throws Exception {
				    callback.doClick(AbstractBogieWidget.this);
				    return null;
				}
			    };
			    worker.execute();
			    event.setHandled(callback.isHandled());
			}
		    }
		}
	    });
	}
    }

    private void initBogie() {
	bogie = WidgetFactory.getBogie();
	bogie.setPickable(false);
	for (int counter = 0; counter < WidgetFactory.NUM_OF_BOGIE_PARTS; counter++) {
	    final PartModel bogiePart = WidgetFactory.getBogiePart(counter);
	    bogiePart.setPickable(false);
	    bogie.addChild(bogiePart);
	    caps[counter] = bogiePart;
	}
	addChild(bogie);
    }

    /**
     * A convenience constructor for cases where there is no need for a mouse
     * click event handler, but the default orientation is not appropriate.
     * 
     * @param caption
     */
    public AbstractBogieWidget(final Class<T> klass, final String caption, final WidgetOrientation orientation) {
	this(klass, caption, orientation, null);
    }

    /**
     * A convenience constructor for cases where there is a need for a mouse
     * click event handler, and the default orientation is appropriate.
     * 
     * @param caption
     */
    public AbstractBogieWidget(final Class<T> klass, final String caption, final MouseClickCallback<T> callback) {
	this(klass, caption, WidgetOrientation.VERTICAL, callback);
    }

    /**
     * A convenience constructor for cases where the default vertical
     * orientation is appropriate, and there is no need to a mouse click event
     * handler.
     * 
     * @param caption
     */
    public AbstractBogieWidget(final Class<T> klass, final String caption) {
	this(klass, caption, WidgetOrientation.VERTICAL, null);
    }

    /**
     * Creates a title node based on the provided text, which is displayed when
     * mouse is over a widget.
     * 
     * @param text
     * @return
     */
    protected PPath createTitle(final String text) {
	final PText caption = new PText(text);
	caption.setPickable(false);
	final Font newFont = caption.getFont().deriveFont(Font.BOLD, 20);
	caption.setTextPaint(Color.black);
	caption.setFont(newFont);

	final PPath title = new PPath(new Rectangle2D.Double(0., 0., 10., 10.));
	title.addChild(caption);

	title.setPaint(new Color(1, 1, 1, 0.8f));
	title.setStrokePaint(new Color(1, 1, 1, 0.8f));
	title.setBounds(title.getUnionOfChildrenBounds(null).getX(), title.getUnionOfChildrenBounds(null).getY(), title.getUnionOfChildrenBounds(null).getWidth() + 10, title
		.getUnionOfChildrenBounds(null).getHeight());
	title.translate(0, -25);
	caption.translate(5, 0);

	return title;
    }

    @Override
    public void highlight(final Stroke stroke) {
	// override a stroke...
	final float dash[] = { 10.0f };
	final Stroke highlightingStroke = new DefaultStroke(1.0f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f);

	super.highlight(highlightingStroke);
	// links if any should be gighlighed with a passed stroke...
	for (final ILink link : getLinks()) {
	    link.hightlight(stroke);
	}
    }

    @Override
    public boolean isCompatible(final PNode node) {
	return findClosestVacantSpot(node) != null;
    }

    protected PNode findClosestVacantSpot(final PNode node) {
	if (!node.getClass().equals(klass)) {
	    return null;
	}
	final T wNode = klass.cast(node);
	// need to use global coordinates for finding a closes spot because a node may not have been yet attached to container
	final Point2D globOffset = wNode.getGlobalBounds().getCenter2D();

	final double nodeCentreX = globOffset.getX();
	double minDist = Double.MAX_VALUE;
	int closestSpotIndex = -1;
	for (int index = 0; index < getSlotNodes().size(); index++) {
	    try {
		// only empty spots should be used or the one, which already contains this node
		if (getSlotAttachamnets().get(index) == null && canAccept(index, wNode)) {
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
     * This method is an extension point used by
     * {@link #findClosestVacantSpot(PNode)} in search of the closes suitable
     * spot for attaching a widget. A custom logic should be provided by
     * descendants.
     * 
     * @param slotIndex
     *            -- index of the slot being currently tested for compatibility
     *            with a widget
     * @param widgetToTest
     *            -- a widget being tested for compatibility with current slot.
     * @return
     */
    protected abstract boolean canAccept(final int slotIndex, final T widgetToTest);

    @Override
    public boolean canDrag() {
	return false;
    }

    /**
     * Fills bogie widget with the specified colour.
     * 
     * @param colour
     * @return
     */
    public void fill(final Color colour) {
	bogie.fill(colour);
	for (int counter = 0; counter < WidgetFactory.NUM_OF_BOGIE_PARTS; counter++) {
	    caps[counter].fill(colour);
	}
    }

    /**
     * Sets the colour for the bogie widget stroke.
     * 
     * @param colour
     * @return
     */
    public AbstractBogieWidget<T> stroke(final Color colour) {
	bogie.stroke(colour);
	for (int counter = 0; counter < WidgetFactory.NUM_OF_BOGIE_PARTS; counter++) {
	    caps[counter].stroke(colour);
	}
	return this;
    }

    public PPath getTitle() {
	return title;
    }

    @Override
    public void onMouseEntered(final PInputEvent event) {
	addChild(title);
	title.getParent().moveToFront();
	title.moveToFront();
    }

    @Override
    public void onMouseExited(final PInputEvent event) {
	title.removeFromParent();
    }

    @Override
    public void onStartDrag(final PInputEvent event) {
	title.moveToFront();
	addChild(title);
    }

    @Override
    public void onEndDrag(final PInputEvent event) {
	title.removeFromParent();
    }

    @Override
    public void Decorate() {
	final Stroke selectableStroke = new DefaultStroke(2.0f);
	bogie.stroke(selectableStroke);
	bogie.stroke(Color.RED);
	for (int counter = 0; counter < WidgetFactory.NUM_OF_BOGIE_PARTS; counter++) {
	    caps[counter].stroke(selectableStroke);
	    caps[counter].stroke(Color.RED);
	}
    }

    @Override
    public void Undecorate() {
	final Stroke unselectStroke = null;
	final Color unselectableColor = null;
	bogie.stroke(unselectableColor);
	bogie.stroke(unselectStroke);
	for (int counter = 0; counter < WidgetFactory.NUM_OF_BOGIE_PARTS; counter++) {
	    caps[counter].stroke(unselectStroke);
	    caps[counter].stroke(unselectableColor);
	}
    }

    @Override
    public IContainer getContainer() {
	return container;
    }

    @Override
    public void setContainer(final IContainer container) {
	this.container = container;
    }

}
