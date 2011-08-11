package ua.com.fielden.platform.swing.review.factory;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;
import ua.com.fielden.platform.swing.review.wizard.CriteriaWizard;
import ua.com.fielden.platform.swing.review.wizard.CriteriaWizardModel;

/**
 * A wizard model factory for dynamic entity query criteria.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class CriteriaWizardModelFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> implements IWizardModelFactory<T, DAO, R> {
    @Override
    public CriteriaWizardModel<T, DAO, R> createWizardModel(final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Class<R> resultantEntityClass, final DynamicCriteriaPersistentObjectUi persistentObject, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	return new CriteriaWizardModel<T, DAO, R>(dynamicCriteria, resultantEntityClass, persistentObject, modelBuilder);
    }

    @Override
    public CriteriaWizard createWizardPanel(final AbstractWizardModel wizardModel, final Action buildAction, final Action cancelAction) {
	return new CriteriaWizard((CriteriaWizardModel) wizardModel, buildAction, cancelAction);
    }
}
