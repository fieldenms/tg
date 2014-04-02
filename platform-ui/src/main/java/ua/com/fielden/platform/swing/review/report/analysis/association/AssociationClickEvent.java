package ua.com.fielden.platform.swing.review.report.analysis.association;

import java.util.EventObject;

/**
 * Represents the double click event on {@link AssociationTable}.
 * 
 * @author TG Team
 * 
 */
public class AssociationClickEvent extends EventObject {

    private static final long serialVersionUID = -6469199899461244559L;

    private final Object rowClicked;
    private final Object columnClicked;
    private final Object valueInIntersection;

    public AssociationClickEvent(final AssociationTable source, final Object rowClicked, final Object columnClicked, final Object valueInIntersection) {
        super(source);
        this.rowClicked = rowClicked;
        this.columnClicked = columnClicked;
        this.valueInIntersection = valueInIntersection;
    }

    @Override
    public AssociationTable getSource() {
        return (AssociationTable) super.getSource();
    }

    /**
     * Returns the entity in the row that was clicked.
     * 
     * @return
     */
    public Object getRowClicked() {
        return rowClicked;
    }

    /**
     * Returns the entity in the column that was clicked.
     * 
     * @return
     */
    public Object getColumnClicked() {
        return columnClicked;
    }

    /**
     * Returns the value that is on intersection of row clicked and column clicked objects.
     * 
     * @return
     */
    public Object getValueInIntersection() {
        return valueInIntersection;
    }
}
