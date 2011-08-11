package ua.com.fielden.platform.example.dnd.classes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.example.dnd.WidgetFactory;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.Workshop;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * TransferNode is used when displaying the transfered rotables from workshop. <br>
 * It will indicate whether the transfered data may be droped or not.<br>
 * if the transfer data may be droped then this TransferNode will have green color otherwise it will be red.<br>
 *
 * @author oleh
 *
 */
public class TransferNode extends PPath {

    // represent the list of bogies that must placed after the importData was invoked
    private List<Bogie> bogies;
    // represent the list of wheelsets that must be placed after the importData was invoked
    private List<Wheelset> wheelsets;

    private float hgapBetweenWidgets;
    private float vgapBetweenWidgets;

    private Workshop workshop;

    private static final long serialVersionUID = -3678301771668350221L;

    /**
     * creates new empty TransferNode instance
     */
    public TransferNode(final Workshop workshop) {
	super();
	setWorkshop(workshop);
	setPathToRectangle(0, 0, 0.1f, 0.1f);
	setPaint(Color.WHITE);
	setTransparency(0.5f);
	setStrokePaint(Color.BLACK);
	setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0));
	hgapBetweenWidgets = 2.0f;
	vgapBetweenWidgets = 2.0f;
    }

    /**
     * creates new TransferNode instance and set the size and location of this node according to the number and type of specified rotables
     *
     * @param rotables -
     *                specified rotables
     */
    public TransferNode(final List<Rotable> rotables, final Workshop workshop) {
	this(workshop);
	setRotables(rotables);
    }

    /**
     * @return the list of rotables that must be dropped
     */
    public List<Rotable> getRotables() {
	final List<Rotable> rotables = new ArrayList<Rotable>();
	if (bogies != null) {

	    rotables.addAll(bogies);
	}
	if (wheelsets != null) {
	    rotables.addAll(wheelsets);
	}
	return rotables;
    }

    /**
     * set the list of rotables that must be dropped after the importData was invoked
     *
     * @param rotables -
     *                specified list of rotables
     */
    public void setRotables(final List<Rotable> rotables) {
	if (bogies == null) {
	    bogies = new ArrayList<Bogie>();
	}
	if (wheelsets == null) {
	    wheelsets = new ArrayList<Wheelset>();
	}
	bogies.clear();
	wheelsets.clear();
	split(rotables);
	updateNode();
    }

    // splits the list of rotables in to two list of bogies and wheelsets
    private void split(final List<Rotable> rotables) {
	final Iterator<Rotable> rotableIterator = rotables.iterator();
	while (rotableIterator.hasNext()) {
	    final Rotable rotable = rotableIterator.next();
	    if (rotable instanceof Bogie) {
		bogies.add((Bogie) rotable);
	    } else {
		if (rotable instanceof Wheelset) {
		    wheelsets.add((Wheelset) rotable);
		}
	    }
	}
    }

    // updates the size of this node
    private void updateNode() {
	final double bogieArea = WidgetFactory.BOGIE_HEIGHT * WidgetFactory.BOGIE_WIDTH * bogies.size();
	final double wheelsetArea = WidgetFactory.WHEELSET_HEIGHT * WidgetFactory.WHEELSET_WIDTH * wheelsets.size();
	double lineWidth = 0;
	if (bogieArea < wheelsetArea) {
	    lineWidth = Math.sqrt(wheelsetArea);
	} else {
	    lineWidth = Math.sqrt(bogieArea);
	}
	Dimension widgetDim = countDim(WidgetFactory.BOGIE_WIDTH, WidgetFactory.BOGIE_HEIGHT, lineWidth, bogies.size(), new Dimension(0, 0));
	widgetDim = new Dimension(widgetDim.width, (int) (widgetDim.height + vgapBetweenWidgets));
	widgetDim = countDim(WidgetFactory.WHEELSET_WIDTH, WidgetFactory.WHEELSET_HEIGHT, lineWidth, wheelsets.size(), widgetDim);
	setWidth(widgetDim.getWidth());
	setHeight(widgetDim.getHeight());
    }

    // count the bounds of the rectangle that will be needed to place widgets specified by their bounds and number of widgets
    private Dimension countDim(final double widgetWidth, final double widgetHeight, final double lineWidth, final int size, final Dimension dimension) {
	double width = dimension.getWidth();
	double height = dimension.getHeight();
	int numOfWidgetPerLine = (int) Math.ceil(lineWidth / widgetWidth);
	if (numOfWidgetPerLine > size) {
	    numOfWidgetPerLine = size;
	}
	final int numOfLines = (int) Math.ceil(((double) size) / ((double) numOfWidgetPerLine));
	final double nWidth = numOfWidgetPerLine * widgetWidth + (numOfWidgetPerLine - 1) * hgapBetweenWidgets;
	final double nHeight = numOfLines * widgetHeight + (numOfLines - 1) * vgapBetweenWidgets;
	if (width < nWidth) {
	    width = nWidth;
	}
	height += nHeight;
	return new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
    }

    /**
     * set the appropriate paint for the node that indicates whether the transfered data can be imported or not
     *
     * @param accept -
     *                boolean value that indicates whether the transfered data can be imported or not
     */
    public void setAccept(final boolean accept) {
	if (accept) {
	    setPaint(Color.GREEN);
	} else {
	    setPaint(Color.RED);
	}
    }

    /**
     * @return the horizontal gap between the widget
     */
    public float getHgapBetweenWidgets() {
	return hgapBetweenWidgets;
    }

    /**
     * set the horizontal gap between widgets
     *
     * @param hgapBetweenWidge1ts -
     *                specified horizontal gap. It must be greater then zero
     */
    public void setHgapBetweenWidgets(final float hgapBetweenWidge1ts) {
	if (hgapBetweenWidge1ts < 0) {
	    return;
	}
	this.hgapBetweenWidgets = hgapBetweenWidge1ts;
	updateNode();
    }

    /**
     *
     * @return the vertical gap between widgets
     */
    public float getVgapBetweenWidgets() {
	return vgapBetweenWidgets;
    }

    /**
     * set the vertical gap between widgets
     *
     * @param vgapBetweenWidgets -
     *                specified vertical gap. it must be greater then zero
     */
    public void setVgapBetweenWidgets(final float vgapBetweenWidgets) {
	if (vgapBetweenWidgets < 0) {
	    return;
	}
	this.vgapBetweenWidgets = vgapBetweenWidgets;
	updateNode();
    }

    /**
     * add new rotable and update the size of the node
     *
     * @param rotable -
     *                specified rotable
     */
    public void addRotable(final Rotable rotable) {
	if (rotable instanceof Bogie) {
	    bogies.add((Bogie) rotable);
	} else {
	    wheelsets.add((Wheelset) rotable);
	}
	updateNode();
    }

    /**
     * removes the specified rotable from the list of bogies or wheelsets
     *
     * @param rotable -
     *                specified rotable
     */
    public void removeRotable(final Rotable rotable) {
	boolean success = false;
	if (rotable instanceof Bogie) {
	    success |= bogies.remove(rotable);
	} else {
	    success |= wheelsets.remove(rotable);
	}
	if (success) {
	    updateNode();
	}
    }

    /**
     * arranges nodes according to the size of this node. User can override it to provide custom layout algorithm
     *
     * @param nodes -
     *                specified list of AbstractNodes that must be arranged
     */
    @SuppressWarnings("unchecked")
    public List<AbstractNode> createRotables() {
	final List<AbstractBogieWidget> bogieWidgets = new ArrayList<AbstractBogieWidget>();
	final List<AbstractWheelsetWidget> wheelsetWidgets = new ArrayList<AbstractWheelsetWidget>();
	final double lineWidth = getWidth();
	double currentX = getOffset().getX();
	double currentY = getOffset().getY();
	final double originX = currentX;
	for (final Bogie bogie : bogies) {
	    final BogieModel bogieWidget = new BogieModel(bogie, workshop);
	    bogieWidgets.add(bogieWidget);
	    if (currentX + WidgetFactory.BOGIE_WIDTH > originX + lineWidth) {
		currentX = originX;
		currentY += WidgetFactory.BOGIE_HEIGHT + vgapBetweenWidgets;
	    }
	    bogieWidget.setOffset(currentX, currentY);
	    currentX += WidgetFactory.BOGIE_WIDTH + hgapBetweenWidgets;
	}
	currentX = originX;
	if (bogieWidgets.size() > 0) {
	    currentY += WidgetFactory.BOGIE_HEIGHT + vgapBetweenWidgets;
	}
	for (final Wheelset wheelset : wheelsets) {
	    final WheelsetModel wheelsetWidget = new WheelsetModel(wheelset);
	    wheelsetWidgets.add(wheelsetWidget);
	    if (currentX + WidgetFactory.WHEELSET_WIDTH > originX + lineWidth) {
		currentX = originX;
		currentY += WidgetFactory.WHEELSET_HEIGHT + vgapBetweenWidgets;
	    }
	    wheelsetWidget.setOffset(currentX, currentY);
	    currentX += WidgetFactory.WHEELSET_WIDTH + hgapBetweenWidgets;
	}
	final List<AbstractNode> rotables = new ArrayList<AbstractNode>();
	rotables.addAll(bogieWidgets);
	rotables.addAll(wheelsetWidgets);
	return rotables;
    }

    /**
     *
     * @param workshop
     */
    private void setWorkshop(final Workshop workshop) {
	this.workshop = workshop;
    }

    /**
     *
     * @return
     */
    public Workshop getWorkshop() {
	return workshop;
    }

    /**
     *
     * @return
     */
    public List<Bogie> getBogies() {
	return bogies;
    }

    /**
     *
     * @return
     */
    public List<Wheelset> getWheelsets() {
	return wheelsets;
    }
}
