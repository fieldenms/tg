package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;

/**
 * Utility methods for all centre config functional entities (producers, companions).
 * 
 * @author TG Team
 *
 */
public class CentreConfigUtils {
    
    /**
     * Applies modifHolder from <code>selectionCrit</code> against fresh centre.
     * 
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param selectionCrit
     * @return
     */
    public static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> applyCriteria(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        // get modifHolder and apply it against 'fresh' centre to be able to later identify validity of 'fresh' centre
        return selectionCrit.freshCentreApplier(selectionCrit.centreContextHolder().getModifHolder());
    }
    
    /**
     * Returns custom object with centre information in case where centre criteria is invalid, otherwise returns empty {@link Optional}. 
     * 
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @return
     */
    public static Optional<Map<String, Object>> invalidCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        // validate criteriaEntity
        final Result validationResult = appliedCriteriaEntity.isValid();
        if (!validationResult.isSuccessful()) { // if applied criteria entity is invalid then return corresponding custom object
            return of(getCustomObject(selectionCrit, appliedCriteriaEntity));
        }
        return empty();
    }
    
    /**
     * Creates custom object with centre information for concrete <code>appliedCriteriaEntity</code>.
     * 
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @return
     */
    static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity) {
        return getCustomObject(selectionCrit, appliedCriteriaEntity, selectionCrit.saveAsName());
    }
    
    /**
     * Creates custom object with centre information for concrete <code>appliedCriteriaEntity</code>.
     * <p>
     * Contains <code>centreChanged</code> flag which is calculated comparing <code>appliedCriteriaEntity</code>'s centre against saved version of <code>saveAsNameToCompare</code>'s centre.
     * 
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @param saveAsNameToCompare
     * @return
     */
    public static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final Optional<String> saveAsNameToCompare) {
        return selectionCrit.centreCustomObject(appliedCriteriaEntity, saveAsNameToCompare);
    }
    
    /**
     * Prepares default centre before its loading. This is applicable to both {@link CentreConfigNewAction} and {@link CentreConfigDeleteAction}.
     * 
     * @param selectionCrit
     * @return
     */
    public static Map<String, Object> prepareDefaultCentre(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        selectionCrit.clearDefaultCentre(); // clear it first
        selectionCrit.makePreferredConfig(empty()); // then make it preferred
        return getCustomObject(selectionCrit, selectionCrit.createCriteriaValidationPrototype(empty()), empty()); // return corresponding custom object
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents default configuration,
     * otherwise <code>false</code>.
     * 
     * @param saveAsName
     * @return
     */
    public static boolean isDefault(final Optional<String> saveAsName) {
        return !saveAsName.isPresent();
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents default configuration or inherited from base user configuration,
     * otherwise <code>false</code>.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isDefaultOrInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return isDefault(saveAsName) || isInherited(saveAsName, () -> selectionCrit.loadableCentreConfigs().stream());
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents inherited from base user configuration, <code>false</code> otherwise.
     * 
     * @param saveAsName
     * @param streamLoadableConfigurations -- a function to stream loadable configurations for current user
     * @return
     */
    public static boolean isInherited(final Optional<String> saveAsName, final Supplier<Stream<LoadableCentreConfig>> streamLoadableConfigurations) throws Result {
        return saveAsName.isPresent() &&
            streamLoadableConfigurations.get()
            .filter(lcc -> lcc.getKey().equals(saveAsName.get()))
            .findAny().map(lcc -> lcc.isInherited()).orElseThrow(() -> failure("Configuration has been deleted."));
    }
    
}