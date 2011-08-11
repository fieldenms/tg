package ua.com.fielden.platform.swing.analysis;

public interface IAnalysisReportPersistentObject {

    boolean isIdentical(final IAnalysisReportPersistentObject analysisPersistentObject);

    IAnalysisReportType getType();

    boolean isVisible();

    void setVisible(boolean visible);

    void updateFromAnalysis(final IAnalysisReportPersistentObject analysisReportPersistentObject);
}
