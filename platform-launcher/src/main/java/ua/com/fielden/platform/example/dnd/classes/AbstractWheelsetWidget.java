package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingWorker;

import ua.com.fielden.platform.events.IDecorable;
import ua.com.fielden.platform.example.dnd.WidgetFactory;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * <code>AbstractWheelsetWidget</code> provides implementation of the graphical widget representing a wheelset. It does not support any actions and features -- strictly visual
 * representation.
 * 
 * @author 01es
 */
public abstract class AbstractWheelsetWidget extends AbstractNode implements IDraggable, IDecorable, RotableContainerRetriever {
    private static final long serialVersionUID = 1L;

    private PartModel wheelsetParts[] = new PartModel[WidgetFactory.NUM_OF_WHEELSET_PARTS];

    private final PPath title;

    private IContainer container;

    /**
     * An interface, implementation of which can be passed as one of the constructor parameters, to provide a custom left mouse click event handler on the widget.
     * 
     * @author 01es
     */
    public static interface MouseClickCallback {
        void doClick(final AbstractWheelsetWidget instance);

        boolean isHandled();
    }

    private SwingWorker<Object, Object> worker; // used for executing mouse click event handler

    /**
     * Instantiates a widget representing a wheelset.
     * 
     * @param caption
     * @param orientation
     */
    public AbstractWheelsetWidget(final String caption, final WidgetOrientation orientation, final MouseClickCallback callback) {
        super(new Rectangle2D.Double(0., 0., 10., 10.), new DefaultStroke(0.01f));
        setBounds(0, 0, WidgetFactory.WHEELSET_WIDTH, WidgetFactory.WHEELSET_HEIGHT);
        setPedding(new AbstractNode.Pedding(1, 1, 1, 1));

        final float dash[] = { 6.0f };
        setStroke(new DefaultStroke(0.1f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        setBackgroundColor(new Color(1, 1, 1, 0.2f));
        composeWheelShape(orientation);
        title = createTitle(caption);
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
                                    callback.doClick(AbstractWheelsetWidget.this);
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

    /**
     * A convenience constructor for cases where there is no need to provide mouse click event handler.
     * 
     * @param caption
     * @param orientation
     */
    public AbstractWheelsetWidget(final String caption, final WidgetOrientation orientation) {
        this(caption, orientation, null);
    }

    /**
     * A convenience constructor for cases where there is a need to provide mouse click event handler, and the default vertical orientation is appropriate.
     * 
     * @param caption
     * @param orientation
     */
    public AbstractWheelsetWidget(final String caption, final MouseClickCallback callback) {
        this(caption, WidgetOrientation.VERTICAL, callback);
    }

    /**
     * A convenience constructor for cases where the default vertical orientation is appropriate.
     * 
     * @param caption
     */
    public AbstractWheelsetWidget(final String caption) {
        this(caption, WidgetOrientation.VERTICAL, null);
    }

    /**
     * Creates a title node based on the provided text, which is displayed when mouse is over a widget.
     * 
     * @param text
     * @return
     */
    protected PPath createTitle(final String text) {
        final PText caption = new PText(text);
        caption.setPickable(false);
        final Font newFont = caption.getFont().deriveFont(Font.BOLD, 15);
        caption.setTextPaint(Color.black);
        caption.setFont(newFont);

        final PPath title = new PPath(new Rectangle2D.Double(0., 0., 10., 10.));
        title.addChild(caption);

        title.setPaint(new Color(1, 1, 1, 0.8f));
        title.setStrokePaint(new Color(1, 1, 1, 0.8f));
        title.setBounds(title.getUnionOfChildrenBounds(null).getX(), title.getUnionOfChildrenBounds(null).getY(), title.getUnionOfChildrenBounds(null).getWidth() + 10, title.getUnionOfChildrenBounds(null).getHeight());
        title.translate(-5, -20);
        caption.translate(5, 0);
        return title;
    }

    /**
     * Makes the widget look and feel. By default orientation is horizontal.
     * 
     * @param orientation
     */
    private void composeWheelShape(final WidgetOrientation orientation) {

        for (int counter = 0; counter < WidgetFactory.NUM_OF_WHEELSET_PARTS; counter++) {
            final PartModel part = WidgetFactory.getWheelsetPart(counter);
            wheelsetParts[counter] = part;
            part.setPickable(false);
            addChild(part);
        }

        if (orientation == WidgetOrientation.HORIZONTAL) {
            final double x = getBounds().getMaxX();
            final double y = getBounds().getMinY();
            rotateAboutPoint(Math.toRadians(-90), x, y);
            translate(0, -x);
        }
    }

    public boolean canDrag() {
        return false;
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

    @Override
    public void highlight(final Stroke stroke) {
        // override a stroke...
        final float dash[] = { 6.0f };
        final Stroke highlightingStroke = new DefaultStroke(1.0f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_MITER, 10.0f, dash, 0.0f);

        super.highlight(highlightingStroke);
        // links if any should be highlighted with a passed stroke...
        for (final ILink link : getLinks()) {
            link.hightlight(stroke);
        }
    }

    /**
     * Fills wheelset widget with the specified colour.
     * 
     * @param colour
     * @return
     */
    public void fill(final Color colour) {
        for (int counter = 0; counter < WidgetFactory.NUM_OF_WHEELSET_PARTS; counter++) {
            wheelsetParts[counter].fill(colour);
        }
    }

    /**
     * Sets the colour for the wheelset widget stroke.
     * 
     * @param colour
     * @return
     */
    public AbstractWheelsetWidget stroke(final Color colour) {
        for (int counter = 0; counter < WidgetFactory.NUM_OF_WHEELSET_PARTS; counter++) {
            wheelsetParts[counter].stroke(colour);
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
    public IContainer getContainer() {
        return container;
    }

    @Override
    public void setContainer(final IContainer container) {
        this.container = container;
    }

    @Override
    public void Decorate() {
        final Stroke selectableStroke = new DefaultStroke(2.0f);
        for (int counter = 0; counter < WidgetFactory.NUM_OF_WHEELSET_PARTS; counter++) {
            wheelsetParts[counter].stroke(selectableStroke);
            wheelsetParts[counter].stroke(Color.RED);
        }
    }

    @Override
    public void Undecorate() {
        final Stroke unselectableStroke = null;
        final Color unselectableColor = null;
        for (int counter = 0; counter < WidgetFactory.NUM_OF_WHEELSET_PARTS; counter++) {
            wheelsetParts[counter].stroke(unselectableStroke);
            wheelsetParts[counter].stroke(unselectableColor);
        }
    }

}
