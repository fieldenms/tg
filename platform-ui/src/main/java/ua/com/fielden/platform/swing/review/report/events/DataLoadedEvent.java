package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

public class DataLoadedEvent extends EventObject {

    private static final long serialVersionUID = 4230774044331457073L;

    private final Object loadedData;

    public DataLoadedEvent(final Object source, final Object loadedData) {
	super(source);
	this.loadedData = loadedData;
    }

    public Object getLoadedData() {
	return loadedData;
    }

}
