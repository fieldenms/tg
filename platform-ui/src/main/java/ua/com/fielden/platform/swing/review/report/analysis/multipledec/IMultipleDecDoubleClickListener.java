package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.EventListener;

import org.jfree.chart.ChartMouseEvent;

import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;

public interface IMultipleDecDoubleClickListener extends EventListener {

    void doubleClick(AnalysisDataEvent<ChartMouseEvent> event);
}
