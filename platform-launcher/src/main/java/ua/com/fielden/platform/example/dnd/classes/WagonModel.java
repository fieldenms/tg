package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;

import ua.com.fielden.platform.example.entities.BogieWithRotables;
import ua.com.fielden.platform.example.entities.RotableMovementLogic;
import ua.com.fielden.platform.example.entities.WagonSlot;
import ua.com.fielden.platform.example.entities.WagonWithRotables;
import ua.com.fielden.platform.example.entities.Workshop;
import edu.umd.cs.piccolo.PNode;

public class WagonModel extends AbstractWagonWidget<BogieModel> {

    private static final long serialVersionUID = 356616047848937058L;

    private final WagonWithRotables wagonWithRotables;
    private final Workshop workshop;

    public WagonModel(final WagonWithRotables wagonWithrotables, final Workshop workshop) {
	super(BogieModel.class, new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2), wagonWithrotables.getWagon().getSlots().size());
	this.workshop = workshop;
	this.wagonWithRotables = wagonWithrotables;
	for (final WagonSlot slot : wagonWithrotables.getWagon().getSlots()) {
	    addSpotCaption(slot.getPosition() - 1, slot.getSlotIndex(), 10, Color.WHITE);
	    final BogieWithRotables bogieWithRotables = wagonWithrotables.getBogieInSlot(slot);
	    if (bogieWithRotables != null) {
		final BogieModel bogieModel = new BogieModel(bogieWithRotables, workshop);
		plug(bogieModel, getSlotNodes().get(slot.getPosition() - 1));
	    }
	}
    }

    @Override
    protected boolean canAccept(final int slotIndex, final BogieModel widgetToTest) {
	try {
	    return RotableMovementLogic.canSlotAcceptBogie(widgetToTest.getBogie(), wagonWithRotables, slotIndex + 1);
	} catch (final Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    @Override
    public void doAfterAttach(final PNode node) {
	try {
	    RotableMovementLogic.fitBogie(((BogieModel) node).getBogieWithRotables(), wagonWithRotables, getSlotAttachamnets().indexOf(node) + 1);
	    ((BogieModel) node).refreshCaption();
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void doAfterDetach(final PNode node) {
	RotableMovementLogic.defitBogie(((BogieModel) node).getBogieWithRotables(), wagonWithRotables, workshop);
	((BogieModel) node).refreshCaption();
    }

    public WagonWithRotables getWagonWithRotables() {
	return wagonWithRotables;
    }
}
