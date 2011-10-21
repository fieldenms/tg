package ua.com.fielden.platform.swing.analysis.ndec;

import ua.com.fielden.platform.swing.analysis.AbstractAnalysisPersistentObject;
import ua.com.fielden.platform.swing.analysis.AnalysisReportType;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportType;

public class NDecAnalysisPersistentObject extends AbstractAnalysisPersistentObject {

    @Override
    public boolean isIdentical(final IAnalysisReportPersistentObject analysisPersistentObject) {
	return true;
    }

    @Override
    public IAnalysisReportType getType() {
	return AnalysisReportType.NDEC;
    }

}
