package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventObject;

public class SortRangeChangedEvent extends EventObject {

    private static final long serialVersionUID = -5867323344919804154L;

    private final boolean singleSorting;

    public SortRangeChangedEvent(final Object source, final boolean singleSorting) {
	super(source);
	this.singleSorting = singleSorting;
    }

    public boolean isSingleSorting() {
	return singleSorting;
    }

}
