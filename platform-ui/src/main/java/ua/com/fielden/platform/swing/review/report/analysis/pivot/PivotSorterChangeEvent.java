package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventObject;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

public class PivotSorterChangeEvent extends EventObject {

    private static final long serialVersionUID = 7623813973749861100L;

    private final List<Pair<String, Ordering>> orderingList;

    public PivotSorterChangeEvent(final PivotTreeTableModel source, final List<Pair<String, Ordering>> orderingList) {
        super(source);
        this.orderingList = orderingList;
    }

    @Override
    public PivotTreeTableModel getSource() {
        return (PivotTreeTableModel) super.getSource();
    }

    public List<Pair<String, Ordering>> getOrderingList() {
        return orderingList;
    }
}
