package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventListener;

public interface PivotTableSorterListener extends EventListener {

    void sorterChanged(PivotSorterChangeEvent event);
}
