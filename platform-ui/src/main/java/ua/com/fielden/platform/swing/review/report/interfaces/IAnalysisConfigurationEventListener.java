package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent;

public interface IAnalysisConfigurationEventListener extends EventListener {

    boolean analysisConfigurationEventPerformed(AnalysisConfigurationEvent event);
}
