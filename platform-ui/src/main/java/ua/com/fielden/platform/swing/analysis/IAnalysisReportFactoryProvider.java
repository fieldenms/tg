package ua.com.fielden.platform.swing.analysis;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Provides API that allows one to obtain {@link IAnalysisReportFactory} for specified {@link AnalysisReportType}.
 * 
 * @author oleh
 * 
 */
public interface IAnalysisReportFactoryProvider<T extends AbstractEntity, DAO extends IEntityDao<T>> {

    /**
     * Returns {@link IAnalysisReportFactory} for specified {@link AnalysisReportType}.
     * 
     * @param reportType
     * @return
     */
    IAnalysisReportFactory<T, DAO> getAnalysisReportFactory(final IAnalysisReportType reportType);
}
