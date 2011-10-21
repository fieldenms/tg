package ua.com.fielden.platform.swing.analysis.ndec;

import java.util.EventListener;

import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;

public interface IAnalysisDoubleClickListener extends EventListener {

    void doubleClick(AnalysisDoubleClickEvent event);
}
