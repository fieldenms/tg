package ua.com.fielden.platform.reportquery;

import java.util.EventObject;

public class ChartModelChangedEvent extends EventObject {

    private static final long serialVersionUID = -3919820231084583440L;

    public ChartModelChangedEvent(final Object source) {
	super(source);
    }

}
