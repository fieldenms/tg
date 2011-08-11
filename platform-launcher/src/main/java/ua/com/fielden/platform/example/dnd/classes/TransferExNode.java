package ua.com.fielden.platform.example.dnd.classes;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.example.dnd.WidgetFactory;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.Workshop;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;

public class TransferExNode extends TransferNode {

    public TransferExNode(final List<Rotable> rotables, final Workshop workshop) {
	super(rotables, workshop);

    }

    public TransferExNode() {
	this(null);
    }

    public TransferExNode(final Workshop workshop) {
	super(workshop);
    }

    /**
     *
     */
    private static final long serialVersionUID = -1113245304195478914L;

    @SuppressWarnings("unchecked")
    public List<AbstractNode> createRotables() {
	final List<AbstractBogieWidget> bogieWidgets = new ArrayList<AbstractBogieWidget>();
	final List<AbstractWheelsetWidget> wheelsetWidgets = new ArrayList<AbstractWheelsetWidget>();
	final double lineWidth = getWidth();
	double currentX = getOffset().getX();
	double currentY = getOffset().getY();
	final double originX = currentX;
	for (final Bogie bogie : getBogies()) {
	    final BogieDragModel bogieWidget = new BogieDragModel(bogie);
	    bogieWidgets.add(bogieWidget);
	    if (currentX + WidgetFactory.BOGIE_WIDTH > originX + lineWidth) {
		currentX = originX;
		currentY += WidgetFactory.BOGIE_HEIGHT + getVgapBetweenWidgets();
	    }
	    bogieWidget.setOffset(currentX, currentY);
	    currentX += WidgetFactory.BOGIE_WIDTH + getHgapBetweenWidgets();
	}
	currentX = originX;
	if (getBogies().size() > 0) {
	    currentY += WidgetFactory.BOGIE_HEIGHT + getVgapBetweenWidgets();
	}
	for (final Wheelset wheelset : getWheelsets()) {
	    final WheelsetDragModel wheelsetWidget = new WheelsetDragModel(wheelset);
	    wheelsetWidgets.add(wheelsetWidget);
	    if (currentX + WidgetFactory.WHEELSET_WIDTH > originX + lineWidth) {
		currentX = originX;
		currentY += WidgetFactory.WHEELSET_HEIGHT + getVgapBetweenWidgets();
	    }
	    wheelsetWidget.setOffset(currentX, currentY);
	    currentX += WidgetFactory.WHEELSET_WIDTH + getHgapBetweenWidgets();
	}
	final List<AbstractNode> rotables = new ArrayList<AbstractNode>();
	rotables.addAll(bogieWidgets);
	rotables.addAll(wheelsetWidgets);
	return rotables;
    }

}
