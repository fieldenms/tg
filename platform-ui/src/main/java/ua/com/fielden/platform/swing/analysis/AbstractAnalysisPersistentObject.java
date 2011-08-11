package ua.com.fielden.platform.swing.analysis;

public abstract class AbstractAnalysisPersistentObject implements IAnalysisReportPersistentObject {

    private boolean visible = true;

    protected AbstractAnalysisPersistentObject() {

    }

    public AbstractAnalysisPersistentObject(final boolean visible) {
	setVisible(visible);
    }

    @Override
    public boolean isVisible() {
	return visible;
    }

    @Override
    public void setVisible(final boolean visible) {
	this.visible = visible;
    }

    @Override
    public void updateFromAnalysis(final IAnalysisReportPersistentObject analysisReportPersistentObject) {
	setVisible(analysisReportPersistentObject.isVisible());
    }

}
