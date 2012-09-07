package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventListener;

public interface PivotHierarchyChangedListener extends EventListener {

    void pivotHierarchyChanged(PivotHierarchyChangedEvent event);
}
