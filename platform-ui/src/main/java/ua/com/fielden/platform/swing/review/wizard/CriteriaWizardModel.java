package ua.com.fielden.platform.swing.review.wizard;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;

/**
 * Model for criteria wizard. "Totals" parameters used for second checkbox of criteria tree.
 * 
 * @author TG Team
 * 
 */
public class CriteriaWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends AbstractWizardModel<T, DAO, R> {
    private CriteriaTreeModel criteriaTreeModel;

    public CriteriaWizardModel(final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Class<R> resultantEntityClass, final DynamicCriteriaPersistentObjectUi persistentObject, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	super(dynamicCriteria, resultantEntityClass, persistentObject, modelBuilder);
    }

    @Override
    public CriteriaTreeModel createTreeModel() {
	return this.criteriaTreeModel = new CriteriaTreeModel(getEntityClass(), getPropertyFilter(), getPersistentObject() == null ? null : getPersistentObject().getTotals());
    }

    @Override
    public DynamicCriteriaPersistentObjectUi createPersistentObject() {
	final DynamicCriteriaPersistentObjectUi previousObjectUi = super.createPersistentObject();
	return new DynamicCriteriaPersistentObjectUi(previousObjectUi.getLocatorPersistentObject(), previousObjectUi.getTableHeaders(), previousObjectUi.getPersistentProperties(), previousObjectUi.getExcludeProperties(), previousObjectUi.getPropertyColumnMappings(), previousObjectUi.getCriteriaMappings(), previousObjectUi.getColumnsCount(), previousObjectUi.isProvideSuggestions(), previousObjectUi.getAnalysis(), criteriaTreeModel.getCorrectTreeTotals(getSelectedTableHeaders()), isAutoRun());
    }

    @Override
    public DynamicEntityReview<T, DAO, R> getEntityReview(final IEntityReviewFactory<T, DAO, R> entityReviewModelFactory, final ActionChangerBuilder actionChangerBuilder, final ActionPanelBuilder panelBuilder, final boolean isPrinciple) {
	getDynamicCriteria().setTotals(criteriaTreeModel.getCorrectTreeTotals(getSelectedTableHeaders()));
	final DynamicEntityReview<T, DAO, R> review = super.getEntityReview(entityReviewModelFactory, actionChangerBuilder, panelBuilder, isPrinciple);
	return review;
    }
}
