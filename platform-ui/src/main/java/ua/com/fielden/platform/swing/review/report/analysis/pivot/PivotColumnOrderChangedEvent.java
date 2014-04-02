package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventObject;

public class PivotColumnOrderChangedEvent extends EventObject {

    private static final long serialVersionUID = -4725864751507595399L;

    private final String property;
    private final int from, to;

    public PivotColumnOrderChangedEvent(final PivotTreeTableModel source, final String property, final int from, final int to) {
        super(source);
        this.property = property;
        this.from = from;
        this.to = to;
    }

    @Override
    public PivotTreeTableModel getSource() {
        return (PivotTreeTableModel) super.getSource();
    }

    public String getProperty() {
        return property;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
