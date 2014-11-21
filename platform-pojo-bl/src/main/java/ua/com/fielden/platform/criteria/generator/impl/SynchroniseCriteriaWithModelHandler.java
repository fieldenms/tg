package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * {@link IAfterChangeEventHandler} that synchronises entity query criteria values with domain tree model values.
 *
 * @author TG Team
 *
 * @param <CDTME>
 * @param <T>
 */
public class SynchroniseCriteriaWithModelHandler<CDTME extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>> implements IAfterChangeEventHandler<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public void handle(final MetaProperty<Object> property, final Object newValue) {
        final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity = (EntityQueryCriteria<CDTME, T, IEntityDao<T>>) property.getEntity();
        final IAddToCriteriaTickManager ftm = entity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        final Class<T> root = entity.getEntityClass();
        final boolean isSecond = CriteriaReflector.isSecondParam(entity.getType(), property.getName());
        final String propertyName = CriteriaReflector.getCriteriaProperty(entity.getType(), property.getName());
        final Object currValue = isSecond ? ftm.getValue2(root, propertyName) : ftm.getValue(root, propertyName);
        final IAddToCriteriaTickRepresentation ftr = entity.getCentreDomainTreeMangerAndEnhancer().getRepresentation().getFirstTick();
        if (!EntityUtils.equalsEx(currValue, newValue)) {
            if (isSecond) {
                ftm.setValue2(root, propertyName, newValue);
            } else {
                ftm.setValue(root, propertyName, newValue);
            }
        }
    }

}
