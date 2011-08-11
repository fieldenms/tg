package ua.com.fielden.platform.swing.review.factory;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.dynamicreportstree.AbstractTree;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizard;
import ua.com.fielden.platform.swing.review.wizard.AbstractWizardModel;

/**
 * A contract that abstract out concrete instantiation of {@link DynamicCriteriaWizardModel} and {@link DynamicCriteriaWizard}.
 * 
 * @author oleh
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public interface IWizardModelFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> {

    /**
     * 
     * @param dynamicCriteria
     * @param resultantEntityClass
     * @param persistentObject
     * @param propertyFilter
     * @param modelBuilder
     * @return
     */
    AbstractWizardModel<T, DAO, R> createWizardModel(final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria,//
    final Class<R> resultantEntityClass,//
    final DynamicCriteriaPersistentObjectUi persistentObject,//
    final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder);

    /**
     * 
     * 
     * @param wizardModel
     * @param buildAction
     * @param cancelAction
     * @return
     */
    AbstractWizard<? extends AbstractWizardModel, ? extends AbstractTree> createWizardPanel(final AbstractWizardModel<T, DAO, R> wizardModel, final Action buildAction, final Action cancelAction);
}
