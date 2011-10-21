package ua.com.fielden.platform.swing.analysis;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.ndec.NDecAnalysisModel;
import ua.com.fielden.platform.swing.analysis.ndec.NDecAnalysisReview;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReviewModel;

public class NDecReportFactory<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IAnalysisReportFactory<T, DAO> {

    @Override
    public AbstractAnalysisReportModel<T, DAO> getAnalysisReportModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> popUpWindowCache, final IAnalysisReportPersistentObject persistentObject, final String name, final String reportName) {
	return new NDecAnalysisModel<T, DAO>(centerModel, popUpWindowCache, name, reportName);
    }

    @Override
    public AbstractAnalysisReportView<T, DAO, ? extends IAnalysisWizardModel, ? extends IAnalysisReportModel> getAnalysisReportView(final AbstractAnalysisReportModel<T, DAO> reportModel, final BlockingIndefiniteProgressLayer blockingPane, final IAnalysisReportPersistentObject persistentObject) {
	return new NDecAnalysisReview<T, DAO>((NDecAnalysisModel<T, DAO>)reportModel, blockingPane, persistentObject);
    }

}
