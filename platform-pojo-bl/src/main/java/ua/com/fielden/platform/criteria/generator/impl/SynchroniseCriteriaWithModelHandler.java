package ua.com.fielden.platform.criteria.generator.impl;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.generateCriteriaPropertyName;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.getCriteriaProperty;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.isSecondParam;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;

import java.util.stream.Stream;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * {@link IAfterChangeEventHandler} that synchronises entity query criteria values with domain tree model values.
 * <p>
 * In case where this {@link IAfterChangeEventHandler} is actioned for crit-only single property, then validation process 
 * will be kicked in through 'critOnlySinglePrototype'.
 *
 * @author TG Team
 *
 * @param <CDTME>
 * @param <T>
 */
public class SynchroniseCriteriaWithModelHandler<CDTME extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>> implements IAfterChangeEventHandler<Object> {
    
    @Override
    public void handle(final MetaProperty<Object> property, final Object newValue) {
        // criteria entity and property
        final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteriaEntity = (EntityQueryCriteria<CDTME, T, IEntityDao<T>>) property.getEntity();
        final Class<?> criteriaType = criteriaEntity.getType();
        final String criteriaPropName = property.getName();
        
        // real entity and property from which criteria entity and property were generated
        final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) criteriaEntity.getEntityClass(); // getEntityType(getAnnotation(criteriaType, MiType.class).value());
        final String propName = getCriteriaProperty(criteriaType, criteriaPropName);
        
        // crit-only single property processing differs from any other property processing
        if (isCritOnlySingle(entityType, propName)) {
            // set corresponding critOnlySinglePrototype's property which will trigger all necessary validations / definers and dependent properties processing
            criteriaEntity.critOnlySinglePrototypeInit(entityType, /* criteriaEntity.getId() */ 333L).set(propName, newValue);
            // take a snapshot of all needed crit-only single prop information to be applied back against criteriaEntity
            final Stream<MetaProperty<?>> snapshot = criteriaEntity.critOnlySinglePrototype().nonProxiedProperties().filter(metaProp -> isCritOnlySingle(entityType, metaProp.getName()));
            // apply the snapshot against criteriaEntity
            applySnapshot(criteriaEntity, snapshot);
        } else {
            updateTreeManagerProperty(criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getFirstTick(), entityType, propName, newValue, criteriaType, criteriaPropName);
        }
    }
    
    /**
     * Updates domain tree criteria tick with the <code>newValue</code>.
     * 
     * @param criteriaTick
     * @param entityType
     * @param propName
     * @param newValue
     * @param criteriaType
     * @param criteriaPropName
     * @return
     */
    private static IAddToCriteriaTickManager updateTreeManagerProperty(final IAddToCriteriaTickManager criteriaTick, final Class<?> entityType, final String propName, final Object newValue, final Class<?> criteriaType, final String criteriaPropName) {
        final boolean isSecond = isSecondParam(criteriaType, criteriaPropName);
        final Object currValue = isSecond    ? criteriaTick.getValue2(entityType, propName)
                                             : criteriaTick.getValue(entityType, propName);
        return equalsEx(currValue, newValue) ? criteriaTick : 
               isSecond                      ? criteriaTick.setValue2(entityType, propName, newValue) 
                                             : criteriaTick.setValue(entityType, propName, newValue);
    }
    
    /**
     * Applies the <code>snapshot</code> of crit-only meta-properties from critOnlySinglePrototype entity against <code>criteriaEntity</code>.
     * 
     * @param criteriaEntity
     * @param snapshot
     */
    private static <CDTME extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>> void applySnapshot(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteriaEntity, final Stream<MetaProperty<?>> snapshot) {
        final Class<?> criteriaType = criteriaEntity.getType();
        final Class<?> entityType = criteriaEntity.getEntityClass();
        
        // all validations / definers need to be turned off
        criteriaEntity.beginInitialising();
        snapshot.forEach(metaProp -> {
            final String nameWithoutSuffix = generateCriteriaPropertyName(metaProp.getEntity().getType(), metaProp.getName());
            final String criteriaPropName = isBoolean(PropertyTypeDeterminator.determinePropertyType(metaProp.getEntity().getType(), metaProp.getName())) ? is(nameWithoutSuffix) : nameWithoutSuffix;
            final MetaProperty<Object> criteriaMetaProp = criteriaEntity.getProperty(criteriaPropName);
            
            // the order of meta-info application is synced with EntityJsonDeserialiser; all properties are copied excluding prevValue, valueChangeCount and visible -- it is believed that these props are not relevant for critOnlySinglePrototype lifecycle 
            final Result firstFailure = metaProp.getFirstFailure();
            criteriaMetaProp.setDomainValidationResult(firstFailure == null ? metaProp.getFirstWarning() : firstFailure); // TODO do we need to clear required validation result?
            criteriaMetaProp.setOriginalValue(metaProp.getOriginalValue());
            criteriaMetaProp.setLastInvalidValue(metaProp.getLastInvalidValue());
            criteriaMetaProp.setAssigned(metaProp.isAssigned());
            criteriaMetaProp.setEditable(metaProp.isEditable());
            criteriaMetaProp.setRequired(metaProp.isRequired());
            criteriaMetaProp.setValue(metaProp.getValue());
            updateTreeManagerProperty(criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getFirstTick(), entityType, getCriteriaProperty(criteriaType, criteriaPropName), metaProp.getValue(), criteriaType, criteriaPropName);
        });
        // turn on all validations / definers again
        criteriaEntity.endInitialising();
    }
}
