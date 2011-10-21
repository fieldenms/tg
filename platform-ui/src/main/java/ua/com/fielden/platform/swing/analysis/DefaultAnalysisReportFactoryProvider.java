package ua.com.fielden.platform.swing.analysis;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

public class DefaultAnalysisReportFactoryProvider<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IAnalysisReportFactoryProvider<T, DAO> {

    @Override
    public IAnalysisReportFactory<T, DAO> getAnalysisReportFactory(final IAnalysisReportType reportType) {
	if (reportType instanceof AnalysisReportType) {
	    final AnalysisReportType analysisType = (AnalysisReportType) reportType;
	    switch (analysisType) {
	    case ANALYSIS:
		return new AnalysisReportFactory<T, DAO>();
	    case LIFECYCLE:
		return new LifecycleReportFactory<T, DAO>();
	    case PIVOT:
		return new PivotReportFactory<T, DAO>();
	    case NDEC :
		return new NDecReportFactory<T, DAO>();
	    }
	}
	return null;
    }

}
