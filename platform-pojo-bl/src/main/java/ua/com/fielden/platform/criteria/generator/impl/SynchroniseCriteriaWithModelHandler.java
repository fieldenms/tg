package ua.com.fielden.platform.criteria.generator.impl;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.generateCriteriaPropertyName;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.getCriteriaProperty;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.isSecondParam;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.isMockNotFoundEntity;

import java.util.stream.Stream;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;

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
    public static final Long CRITERIA_ENTITY_ID = 333L;
    // private static final Logger LOGGER = Logger.getLogger(SynchroniseCriteriaWithModelHandler.class);
    
    @Override
    public void handle(final MetaProperty<Object> property, final Object newValue) {
        // criteria entity and property
        final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteriaEntity = (EntityQueryCriteria<CDTME, T, IEntityDao<T>>) property.getEntity();
        final Class<?> criteriaType = criteriaEntity.getType();
        final String criteriaPropName = property.getName();
        
        // real entity and property from which criteria entity and property were generated
        final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) criteriaEntity.getEntityClass();
        final String propName = getCriteriaProperty(criteriaType, criteriaPropName);
        // crit-only single property processing differs from any other property processing
        if (isCritOnlySingle(entityType, propName)) {
            // LOGGER.error(format("\t\tACE started for [%s]...", criteriaPropName));
            // LOGGER.error(format("\t\t\toriginal property [%s] is ...", propName));
            // set corresponding critOnlySinglePrototype's property which will trigger all necessary validations / definers and dependent properties processing
            criteriaEntity.critOnlySinglePrototypeInit(entityType, CRITERIA_ENTITY_ID).set(propName, newValue);
            
            // LOGGER.error(format("\tclearing requiredness..."));
            // Need to clear requiredness errors on each application of criteria entity property (crit-only single), not just on initial creation of critOnlySinglePrototype.
            // This is required to mimic 'new entity' application which permits empty required values.
            // The following code is inspired by EntityResourceUtils.disregardCritOnlyRequiredProperties method and its siblings.
            criteriaEntity.critOnlySinglePrototype().nonProxiedProperties().filter(mp -> mp.isRequired() && isCritOnlySingle(entityType, mp.getName())).forEach(mp -> {
                // It is not sufficient enough just to clear requiredness validation result by setRequiredValidationResult(successful("ok")).
                // We need to perform re-validation by making the property non-required first. This makes empty attempted value to become 'actual' one.
                // LOGGER.error(format("\t\tclearing requiredness... property [%s]", mp.getName()));
                mp.setRequired(false);
                // And then we need to return the property to required state.
                mp.setRequired(true);
            });
            // LOGGER.error(format("\tclearing requiredness...done"));
            
            // take a snapshot of all needed crit-only single prop information to be applied back against criteriaEntity
            final Stream<MetaProperty<?>> snapshot = criteriaEntity.critOnlySinglePrototype().nonProxiedProperties().filter(metaProp -> isCritOnlySingle(entityType, metaProp.getName()));
            // apply the snapshot against criteriaEntity
            applySnapshot(criteriaEntity, snapshot);
            // LOGGER.error(format("\t\t\toriginal property [%s] is crit-only single...done", propName));
            // LOGGER.error(format("\t\tACE started for [%s]...done", criteriaPropName));
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
        /*if (isCritOnlySingle(entityType, propName)) {
            if (!equalsEx(currValue, newValue)) {
                LOGGER.error(format("\t\t\t\tupdateTreeManagerProperty: propName = [%s] current -> new = [%s] -> [%s]...", propName, currValue, newValue));
            } else {
                LOGGER.error(format("\t\t\t\tupdateTreeManagerProperty: propName = [%s] current value unchanged [%s]...", propName, currValue));
            }
        }*/
        final IAddToCriteriaTickManager v = !areDifferent(currValue, newValue) ? criteriaTick : 

               isSecond                      ? criteriaTick.setValue2(entityType, propName, newValue) 
                                             : criteriaTick.setValue(entityType, propName, newValue);
        /*if (isCritOnlySingle(entityType, propName)) {
            if (!equalsEx(currValue, newValue)) {
                LOGGER.error(format("\t\t\t\tupdateTreeManagerProperty: propName = [%s] current -> new = [%s] -> [%s]...done", propName, currValue, newValue));
            } else {
                LOGGER.error(format("\t\t\t\tupdateTreeManagerProperty: propName = [%s] current value unchanged [%s]...done", propName, currValue));
            }
        }*/
        return v;
    }
    
    /**
     * Indicates whether two values are different including the case of 'mock not found entity' values.
     * 
     * @param value1
     * @param value2
     * @return
     */
    public static boolean areDifferent(final Object value1, final Object value2) {
        return !equalsEx(value1, value2) || isMockNotFoundEntity(value1) && isMockNotFoundEntity(value2) && !equalsEx(((AbstractEntity) value1).getDesc(), ((AbstractEntity) value2).getDesc());
    }
    
    /**
     * Applies the <code>snapshot</code> of crit-only meta-properties from critOnlySinglePrototype entity against <code>criteriaEntity</code>.
     * 
     * @param criteriaEntity
     * @param snapshot
     */
    public static <CDTME extends ICentreDomainTreeManagerAndEnhancer, T extends AbstractEntity<?>> void applySnapshot(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteriaEntity, final Stream<MetaProperty<?>> snapshot) {
        final Class<?> criteriaType = criteriaEntity.getType();
        final Class<?> entityType = criteriaEntity.getEntityClass();
        
        // all validations / definers need to be turned off
        criteriaEntity.beginInitialising();
        snapshot.forEach(metaProp -> {
            final String criteriaPropName = generateCriteriaPropertyName(metaProp.getEntity().getType(), metaProp.getName());
            criteriaEntity.getPropertyOptionally(criteriaPropName).ifPresent(mp -> { // several crit-only single properties of original entity could be NOT added to selection criteria: need to ignore them
                final MetaProperty<Object> criteriaMetaProp = (MetaProperty<Object>) mp;
                // the order of meta-info application is synced with EntityJsonDeserialiser; all properties are copied excluding prevValue, valueChangeCount and visible -- it is believed that these props are not relevant for critOnlySinglePrototype lifecycle 
                final Result firstFailure = metaProp.getFirstFailure();
                criteriaMetaProp.setDomainValidationResult(firstFailure == null ? metaProp.getFirstWarning() : firstFailure);
                criteriaMetaProp.setOriginalValue(metaProp.getOriginalValue());
                criteriaMetaProp.setLastInvalidValue(metaProp.getLastInvalidValue());
                criteriaMetaProp.setAssigned(metaProp.isAssigned());
                criteriaMetaProp.setEditable(metaProp.isEditable());
                criteriaMetaProp.setRequired(metaProp.isRequired());
                if (firstFailure != null) {
                    // In case where property is invalid there is a pair of values: valid and attempted.
                    // Valid value usually represents the value, that was previously set.
                    // In some cases valid and attempted values could even be the same: this occurs in case of valid->invalid @dependent property revalidation.
                    // In both cases valid value will be different from 'null' value that appears in critOnlySinglePrototype entity during its creation (values in property definitions must be NOT set).
                    // This will confuse conflict resolution mechanism.
                    // We need to sync the value with initial values in critOnlySinglePrototype.
                    criteriaMetaProp.setValue(null);
                    // LOGGER.error(firstFailure);
                } else {
                    criteriaMetaProp.setValue(metaProp.getValue());
                }
                // It is very important to copy 'lastAttemptedValue' into centre manager, not just 'metaProp.getValue()'.
                // This is needed to correctly assign initial values (and perform their validation) in the next round of criteriaEntity creation (this includes, for example, next selection criteria validation round-trip).
                updateTreeManagerProperty(criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getFirstTick(), entityType, getCriteriaProperty(criteriaType, criteriaPropName), metaProp.getLastAttemptedValue(), criteriaType, criteriaPropName);
            });
        });
        // turn on all validations / definers again
        criteriaEntity.endInitialising();
    }
}
