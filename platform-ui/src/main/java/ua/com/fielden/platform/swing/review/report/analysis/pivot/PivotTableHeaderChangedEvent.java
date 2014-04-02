package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventObject;

public class PivotTableHeaderChangedEvent extends EventObject {

    private static final long serialVersionUID = 5412602525053409663L;

    private final String property;

    private final boolean isChecked;

    public PivotTableHeaderChangedEvent(final PivotTreeTableModel source, final String property, final boolean isChecked) {
        super(source);
        this.property = property;
        this.isChecked = isChecked;
    }

    @Override
    public PivotTreeTableModel getSource() {
        return (PivotTreeTableModel) super.getSource();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public String getProperty() {
        return property;
    }
}
