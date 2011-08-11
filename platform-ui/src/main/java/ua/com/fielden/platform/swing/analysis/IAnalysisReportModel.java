package ua.com.fielden.platform.swing.analysis;

import java.awt.Container;

public interface IAnalysisReportModel {

    void restoreReportView(Container container) throws IllegalStateException;

    void createReportView(Container container) throws IllegalStateException;

}
