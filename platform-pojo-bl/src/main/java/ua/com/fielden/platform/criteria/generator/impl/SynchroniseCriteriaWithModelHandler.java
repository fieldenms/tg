package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
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
        final CDTME cdtmae = entity.getCentreDomainTreeMangerAndEnhancer();
        final IAddToCriteriaTickManager ftm = cdtmae.getFirstTick();
        final Class<T> root = entity.getEntityClass();
        final boolean isSecond = CriteriaReflector.isSecondParam(entity.getType(), property.getName());
        final String propertyName = CriteriaReflector.getCriteriaProperty(entity.getType(), property.getName());
        final Object currValue = isSecond ? ftm.getValue2(root, propertyName) : ftm.getValue(root, propertyName);

        //        final boolean isEntityItself = "".equals(property.getName()); // empty property means "entity itself"
        //        final Class<?> propertyType = isEntityItself ? entity.getType() : PropertyTypeDeterminator.determinePropertyType(entity.getType(), property.getName());
        //        final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, entity.getType(), property.getName());
        //        final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());

        // final Object newV = newValue == null ? DynamicQueryBuilder.getEmptyValue(propertyType, single) : newValue;
        if (!EntityUtils.equalsEx(currValue, newValue)) {
            if (isSecond) {
                ftm.setValue2(root, propertyName, newValue);
            } else {
                ftm.setValue(root, propertyName, newValue);
            }
        }
    }
}
