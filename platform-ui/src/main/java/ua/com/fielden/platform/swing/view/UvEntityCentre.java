package ua.com.fielden.platform.swing.view;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DefaultAnalysisReportFactoryProvider;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportFactoryProvider;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.UmEntityCentre;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewWithTabs;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * A common view for entity centres providing some basic common functionality such as handling of closing based on the model's list of open masters.
 * 
 * @author TG Team
 * 
 */
public class UvEntityCentre<T extends AbstractEntity, DAO extends IEntityDao<T>, F extends BaseFrame, M extends UmEntityCentre<T, DAO, F>> extends DynamicEntityReviewWithTabs<T, DAO, T> implements IUmViewOwner {

    private static final long serialVersionUID = 1L;

    public UvEntityCentre(//
	    final M model, //
	    final boolean loadRecordByDefault, //
	    final boolean isPrinciple, //
	    final DynamicCriteriaModelBuilder<T, DAO, T> modelBuilder, //
	    final IAnalysisReportFactoryProvider<T, DAO> reportFactoryProvider, final String reportName,//
	    final Map<String, Map<Object, DetailsFrame>> detailsCache) {
	super(model, loadRecordByDefault, isPrinciple, modelBuilder, reportFactoryProvider, reportName, detailsCache);
	getEntityReviewModel().setView(this);

	//	OpenMasterClickAction.enhanceWithClickAction(getEntityGridInspector().getActualModel().getPropertyColumnMappings(),//
	//		model.getEntityType(), //
	//		model.getEntityMasterFactory(), //
	//		this);
    }

    public UvEntityCentre(//
	    final M model, //
	    final boolean loadRecordByDefault, //
	    final boolean isPrinciple, //
	    final DynamicCriteriaModelBuilder<T, DAO, T> modelBuilder, //
	    final String reportName,//
	    final Map<String, Map<Object, DetailsFrame>> detailsCache) {
	this(model, loadRecordByDefault, isPrinciple, modelBuilder, new DefaultAnalysisReportFactoryProvider<T, DAO>(), reportName, detailsCache);
    }

    public UvEntityCentre(//
	    final M model, //
	    final DynamicCriteriaModelBuilder<T, DAO, T> modelBuilder, //
	    final boolean isPrinciple, //
	    final String reportName,//
	    final Map<String, Map<Object, DetailsFrame>> detailsCache) {
	this(model, false, isPrinciple, modelBuilder, reportName, detailsCache);
    }

    @Override
    public M getEntityReviewModel() {
	return (M) super.getEntityReviewModel();
    }

    @Override
    public <E extends AbstractEntity> void notifyEntityChange(final E entity) {
	if (entity.isPersisted()) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    getEntityGridInspector().getActualModel().refresh((T) entity);
		    getProgressLayer().setLocked(false);
		}
	    });
	}
    }

    @Override
    public String getInfo() {
	return "default info";
    }
}
