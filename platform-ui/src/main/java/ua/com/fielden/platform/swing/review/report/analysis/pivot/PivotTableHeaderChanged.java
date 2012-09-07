package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventListener;

public interface PivotTableHeaderChanged extends EventListener {

    void tableHeaderChanged(PivotTableHeaderChangedEvent event);

    void columnOrderChanged(PivotColumnOrderChangedEvent event);
}
