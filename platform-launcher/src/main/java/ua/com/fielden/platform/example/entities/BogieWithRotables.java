package ua.com.fielden.platform.example.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Bogie with its wheelsets. Not mapped to db.
 * 
 * @author nc
 * 
 */

public class BogieWithRotables {

    private static final long serialVersionUID = 1L;

    private final List<Wheelset> wheelsets;

    private final Bogie bogie;

    public BogieWithRotables(final Bogie bogie, final List<Wheelset> wheelsets) {
	this.wheelsets = wheelsets;
	this.bogie = bogie;
    }

    public BogieWithRotables(final Bogie bogie) {
	this.wheelsets = new ArrayList<Wheelset>();
	this.bogie = bogie;
    }

    public Wheelset getWheelsetInSlot(final BogieSlot slot) {
	for (final Wheelset wheelset : wheelsets) {
	    if (wheelset.getLocation().equals(slot)) {
		return wheelset;
	    }
	}
	return null;
    }

    public Wheelset getWheelsetInSlotPosition(final Integer slotPosition) throws Exception {
	return getWheelsetInSlot(bogie.getSlot(slotPosition));
    }

    public Bogie getBogie() {
	return bogie;
    }

    public List<Wheelset> getWheelsets() {
	return wheelsets;
    }

    @Override
    public String toString() {
	final StringBuffer result = new StringBuffer();
	result.append(getBogie() + "\n\n");

	for (final BogieSlot boSlot : getBogie().getSlots()) {
	    if (getWheelsetInSlot(boSlot) != null) {
		final StringBuffer buffer = formWheelsetDesc(getWheelsetInSlot(boSlot));
		result.append("Slot " + boSlot.getSlotNumber() + " has wheelset:\n" + buffer + "\n\n");

	    } else {
		result.append("Slot " + boSlot.getSlotNumber() + " has no wheelset\n\n");

	    }
	}

	return result.toString();
    }

    private StringBuffer formWheelsetDesc(final Wheelset wheelset) {
	final String[] splitDesc = wheelset.toString().split("\n");
	final StringBuffer buffer = new StringBuffer();
	for (final String line : splitDesc) {
	    buffer.append("    " + line + "\n");
	}
	return buffer;
    }
}
