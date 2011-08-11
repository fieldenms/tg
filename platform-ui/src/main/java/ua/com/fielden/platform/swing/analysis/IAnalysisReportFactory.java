package ua.com.fielden.platform.swing.analysis;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;

/**
 * Factory interface that allows one to obtain specific analysis reports.
 * 
 * @author oleh
 * 
 * @param <T>
 * @param <DAO>
 */
public interface IAnalysisReportFactory<T extends AbstractEntity, DAO extends IEntityDao<T>> {

    /**
     * Returns {@link AbstractAnalysisReportModel} for specified {@link EntityQueryCriteria} and name of the entity center.
     * 
     * @param centerModel
     *            - Model of the entity center to which analysis report model belongs.
     * @param popUpWindowCache
     *            - the cache for details frame.
     * @param persistentObject
     *            - saved analysis report configuration.
     * @param name
     *            - name of entity center to which analysis report belongs.
     * @param reportName
     *            - name of the analysis report.
     * 
     * @return
     */
    AbstractAnalysisReportModel<T, DAO> getAnalysisReportModel(EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> popUpWindowCache, final IAnalysisReportPersistentObject persistentObject, final String name, final String reportName);

    /**
     * Returns {@link AbstractAnalysisReportView} instance for the given {@link AbstractAnalysisReportModel} and saved analysis report configuration.
     * 
     * @param reportModel
     * @param persistentObject
     * @return
     */
    AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> getAnalysisReportView(final AbstractAnalysisReportModel<T, DAO> reportModel, BlockingIndefiniteProgressLayer blockingPane, final IAnalysisReportPersistentObject persistentObject);

}
