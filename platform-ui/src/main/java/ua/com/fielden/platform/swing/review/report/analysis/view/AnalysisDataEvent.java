package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.util.EventObject;

public class AnalysisDataEvent<T> extends EventObject {

    private static final long serialVersionUID = 4033303284939058363L;

    private final T data;

    public AnalysisDataEvent(final Object source, final T data) {
	super(source);
	this.data = data;
    }

    public T getData() {
	return data;
    }

}
