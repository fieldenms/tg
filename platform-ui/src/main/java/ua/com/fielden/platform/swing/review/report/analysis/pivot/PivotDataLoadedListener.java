package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.util.EventListener;

public interface PivotDataLoadedListener extends EventListener {

    void pivotDataLoaded(PivotDataLoadedEvent event);
}
