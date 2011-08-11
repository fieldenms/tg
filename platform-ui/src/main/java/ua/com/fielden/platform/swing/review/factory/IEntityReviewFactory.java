package ua.com.fielden.platform.swing.review.factory;

import java.util.Map;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewModel;
import ua.com.fielden.platform.swing.review.LocatorPersistentObject;
import ua.com.fielden.platform.swing.review.PropertyPersistentObject;
import ua.com.fielden.platform.swing.review.optionbuilder.ActionChangerBuilder;

/**
 * A contract that abstract out concrete instantiation of {@link DynamicEntityReviewModel} and {@link DynamicEntityReview}.
 * 
 * @author 01es
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public interface IEntityReviewFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> {
    DynamicEntityReviewModel<T, DAO, R> createModel(final DynamicEntityQueryCriteria<T, DAO> criteria,//
    final PropertyTableModelBuilder<T> builder, //
    final ActionChangerBuilder actionChangerBuilder,//
    final ActionPanelBuilder panelBuilder, //
    final int columns,//
    final Map<String, PropertyPersistentObject> criteriaProperties, final LocatorPersistentObject locatorPersistentObject, final Runnable... afterRunActions);

    DynamicEntityReview<T, DAO, R> createView(//
    final DynamicEntityReviewModel<T, DAO, R> model,//
    final boolean loadRecordByDefault,//
    final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder, boolean isPrinciple);
}
