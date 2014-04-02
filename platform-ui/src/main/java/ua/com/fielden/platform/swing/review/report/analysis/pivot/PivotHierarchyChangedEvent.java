package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventObject;

public class PivotHierarchyChangedEvent extends EventObject {

    private static final long serialVersionUID = 2038284068657521657L;

    private final String property;

    private final boolean isChecked;

    public PivotHierarchyChangedEvent(final PivotTreeTableModel source, final String property, final boolean isChecked) {
        super(source);
        this.property = property;
        this.isChecked = isChecked;
    }

    @Override
    public PivotTreeTableModel getSource() {
        return (PivotTreeTableModel) super.getSource();
    }

    public String getProperty() {
        return property;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
