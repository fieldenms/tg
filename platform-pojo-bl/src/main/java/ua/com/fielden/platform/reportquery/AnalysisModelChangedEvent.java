package ua.com.fielden.platform.reportquery;

import java.util.EventObject;

public class AnalysisModelChangedEvent extends EventObject {

    private static final long serialVersionUID = -3919820231084583440L;

    private final boolean isSorted;

    public AnalysisModelChangedEvent(final Object source, final boolean isSorted) {
	super(source);
	this.isSorted = isSorted;
    }

    public boolean isSorted() {
	return isSorted;
    }

}
