package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is a button-like node, which can be used for imitating button l&f in Piccolo based applications.
 * 
 * @author 01es
 * 
 */
public class Button extends AbstractNode {
    private static final long serialVersionUID = -4704430911149217902L;

    private PText title = new PText();
    private List<IOnClickEventListener> onClickEvents = new ArrayList<IOnClickEventListener>();
    private TitleBar parentBar; // parent
    private PBasicInputEventHandler defaultEventHandler;

    public Button() {
	super(new RoundRectangle2D.Double(0., 0., 10., 10., 4, 8));
	setRounding(new BorderRounding(true, true, true, true));
	setCurvaturePrc(10);
	reshape(false);
    }

    public Button(String title) {
	super(new RoundRectangle2D.Double(0., 0., 10., 10., 4, 8));
	setUp(title);
    }

    public Button(String title, Shape shape) {
	super(shape);
	setUp(title);
    }

    public Button(String title, Button button) {
	super(new RoundRectangle2D.Double(0., 0., 10., 10., 4, 8));
	setUp(title);

	double horisontalPedding = button.getWidth() - this.title.getWidth();
	double verticalPedding = button.getHeight() - this.title.getHeight();
	setPedding(new AbstractNode.Pedding((int) (verticalPedding / 2. - 1), (int) (verticalPedding / 2. + 0.6), (int) (horisontalPedding / 2.), (int) (horisontalPedding / 2.)));
	setMinConstraint(new PDimension(button.getWidth() + 1, button.getHeight() + 1));
	reshape(false);
    }

    protected void setUp(String title) {
	this.title.setPickable(false);
	this.title.setTextPaint(Color.white);
	setPedding(new AbstractNode.Pedding(2, 5, 2, 5));
	setTitle(title);
	addChild(this.title);
	setRounding(new BorderRounding(true, true, true, true));
	setCurvaturePrc(20);
	reshape(false);
	// fillWithGradient(new Color(17, 64, 111), new Color(148, 193, 239), true); // default color filling
	fillWithGradient(new Color(0, 0, 0), new Color(255, 255, 255), true); // default color filling

	setDefaultEventHandler(new DefaultEventHandler(this));
    }

    /**
     * Fills button with gradient color from the top to the bottom.
     * 
     * @param fromColor --
     *                the color from which filling starts
     * @param toColor --
     *                the color at which filling ends
     * @param resetColor --
     *                if true the background color property is changed, other wise onlt paint is changed.
     */
    public void fillWithGradient(Color fromColor, Color toColor, boolean resetColor) {
	Paint paint = new SerializableGradientPaint((float) getX(), (float) getY(), fromColor, (float) (getX()), (float) (getY() + +getHeight()), toColor);
	setStroke(new DefaultStroke(0));
	setPaint(paint);

	if (resetColor) {
	    setBackgroundColor(paint);
	}
    }

    private void setTitle(String title) {
	this.title.setText(title);
    }

    /**
     * Registers an event to be executed when a button is clicked. There can be more than one event handler. Their execution is carried out in the order in which they were added.
     * 
     * @param eventListener
     */
    public void addOnClickEventListener(IOnClickEventListener eventListener) {
	onClickEvents.add(eventListener);
    }

    /**
     * Removes an event fro the list of on_click events.
     * 
     * @param eventListener
     */
    public void removeOnClickEventListener(IOnClickEventListener eventListener) {
	onClickEvents.remove(eventListener);
    }

    /**
     * This class implements button's behaviour (e.g. highlighting, onClick event).
     * 
     * @author 01es
     * 
     */
    protected static class DefaultEventHandler extends PBasicInputEventHandler implements Serializable {
	private static final long serialVersionUID = 3762542608255322737L;

	protected Paint highPaint;

	protected Button button;

	public DefaultEventHandler(Button button) {
	    this.button = button;
	}

	/**
	 * Handles button highlighting.
	 */
	public void mouseEntered(PInputEvent event) {
	    if (event.isLeftMouseButton()) {
		return;
	    }

	    if (highPaint == null) {
		/*
		 * highPaint = new SerializableGradientPaint((float) button.getX(), (float) button.getY(), new Color(185, 223, 244), (float) (button.getX() ),
		 * (float) (button.getY() + + button.getHeight()), new Color(61, 168, 226));
		 */
		highPaint = new SerializableGradientPaint((float) button.getX(), (float) button.getY(), new Color(255, 255, 255), (float) (button.getX()), (float) (button.getY() + +button.getHeight()), new Color(0, 0, 0));
	    }

	    button.setPaint(highPaint);
	}

	/**
	 * Handles button highlighting.
	 */
	public void mouseExited(PInputEvent event) {
	    if (event.isLeftMouseButton()) {
		return;
	    }

	    // Button button = (Button) event.getPickedNode();
	    button.setPaint(button.getBackgroundColor());
	}

	/**
	 * Sequentially invokes all registered on_click_event_listeners.
	 */
	public void mouseClicked(PInputEvent event) {
	    // Button button = (Button) event.getPickedNode();
	    for (IOnClickEventListener listener : button.onClickEvents) {
		listener.click(event);
	    }
	}

	/**
	 * Handles button highlighting.
	 */
	public void mousePressed(PInputEvent event) {
	    button.fillWithGradient(new Color(12, 118, 17), new Color(182, 248, 185), false);
	}

	/**
	 * Handles button highlighting.
	 */
	public void mouseReleased(PInputEvent event) {
	    Rectangle2D bounds = new Rectangle2D.Double(event.getPosition().getX(), event.getPosition().getY(), 1, 1);
	    bounds = button.globalToLocal(bounds);

	    if (button.intersects(bounds)) {
		button.setPaint(highPaint);
	    } else {
		button.setPaint(button.getBackgroundColor());
	    }
	}
    }

    public TitleBar getParentBar() {
	return parentBar;
    }

    private void setParentBar(TitleBar parentBar) {
	this.parentBar = parentBar;
    }

    public void addToTitleBar(TitleBar bar) {
	setParentBar(bar);
	bar.addButton(this);
    }

    public PBasicInputEventHandler getDefaultEventHandler() {
	return defaultEventHandler;
    }

    protected void setDefaultEventHandler(PBasicInputEventHandler defaultEventHandler) {
	if (this.defaultEventHandler != null) {
	    removeInputEventListener(this.defaultEventHandler);
	}
	this.defaultEventHandler = defaultEventHandler;
	addInputEventListener(defaultEventHandler);
    }

    /**
     * Disabled default (inherited) highlighting
     */
    public void highlight(PNode node, Paint paint) {
    }

    /**
     * Disabled default (inherited) highlighting
     */
    public void highlight(Stroke stroke) {
    }

    public String toString() {
	return title.getText();
    }
}
