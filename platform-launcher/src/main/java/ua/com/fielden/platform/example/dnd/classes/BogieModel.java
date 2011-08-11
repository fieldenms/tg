package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.util.ArrayList;

import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.BogieSlot;
import ua.com.fielden.platform.example.entities.BogieWithRotables;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableMovementLogic;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.Workshop;
import edu.umd.cs.piccolo.PNode;

public class BogieModel extends AbstractBogieWidget<WheelsetModel> {

    private static final long serialVersionUID = 6190972583886744381L;
    private final BogieWithRotables bogieWithRotables;
    private final Workshop workshop;

    public BogieModel(final BogieWithRotables bogieWithRotables, final Workshop workshop) {
	super(WheelsetModel.class, bogieWithRotables.getBogie().getKey());
	this.workshop = workshop;
	this.bogieWithRotables = bogieWithRotables;
	fill(getBogie().getStatus().getColor());
	for (final BogieSlot slot : getBogie().getSlots()) {
	    addSpotCaption(slot.getPosition() - 1, slot.getSlotIndex(), 10, Color.WHITE);
	    final Wheelset wheelset = bogieWithRotables.getWheelsetInSlot(slot);
	    if (wheelset != null) {
		final WheelsetModel wheelsetm = new WheelsetModel(wheelset);
		plug(wheelsetm, getSlotNodes().get(slot.getPosition() - 1));
	    }
	}
    }

    public BogieModel(final Bogie bogie, final Workshop workshop) {
	this(new BogieWithRotables(bogie, new ArrayList<Wheelset>()), workshop);
    }

    public void refreshCaption() {
	for (int slotCounter = 0; slotCounter < getBogie().getSlots().size(); slotCounter++) {
	    addSpotCaption(slotCounter, getBogie().getSlots().get(slotCounter).getSlotIndex(), 10, Color.WHITE);
	}
    }

    @Override
    protected boolean canAccept(final int slotIndex, final WheelsetModel widgetToTest) {
	try {
	    return RotableMovementLogic.canSlotAcceptWheelset(widgetToTest.getWheelset(), bogieWithRotables, slotIndex + 1);
	} catch (final Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    public Bogie getBogie() {
	return bogieWithRotables.getBogie();
    }

    public BogieWithRotables getBogieWithRotables() {
	return bogieWithRotables;
    }

    @Override
    public boolean canDrag() {
	return true;
    }

    @Override
    public void doAfterAttach(final PNode node) {
	try {
	    RotableMovementLogic.fitWheelset(((WheelsetModel) node).getWheelset(), bogieWithRotables, getSlotAttachamnets().indexOf(node) + 1);
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void doAfterDetach(final PNode node) {
	RotableMovementLogic.defitWheelset(((WheelsetModel) node).getWheelset(), bogieWithRotables, workshop);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rotable getRotable() {

	return bogieWithRotables.getBogie();
    }

}
