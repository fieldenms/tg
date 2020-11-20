package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;

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
     * Creates custom object with centre information for concrete <code>appliedCriteriaEntity</code>.
     * 
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @return
     */
    static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final Optional<Optional<String>> configUuid) {
        return getCustomObject(selectionCrit, appliedCriteriaEntity, selectionCrit.saveAsName(), configUuid);
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
    public static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final Optional<String> saveAsNameToCompare, final Optional<Optional<String>> configUuid) {
        return selectionCrit.centreCustomObject(appliedCriteriaEntity, saveAsNameToCompare, configUuid);
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
        return getCustomObject(selectionCrit, selectionCrit.createCriteriaValidationPrototype(empty()), empty(), of(empty()) /* update with empty uuid indicating default config */); // return corresponding custom object
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
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents default / link configuration or inherited from base user configuration or inherited from shared configuration,
     * otherwise <code>false</code>.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isDefaultOrLinkOrInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return isDefault(saveAsName) || LINK_CONFIG_TITLE.equals(saveAsName.get()) || isInherited(saveAsName, () -> selectionCrit.loadableCentreConfigs().stream());
    }
    
    /**
     * Returns <code>true</code> in case where <code>saveAsName</code>d configuration represents inherited from base user configuration or inherited from shared configuration, <code>false</code> otherwise.
     * <p>
     * Default and link configurations are not inherited.
     * 
     * @param saveAsName
     * @param streamLoadableConfigurations -- a function to stream loadable configurations for current user
     * @return
     * @throws Result failure for non-default and non-link configurations, that have been deleted
     */
    public static boolean isInherited(final Optional<String> saveAsName, final Supplier<Stream<LoadableCentreConfig>> streamLoadableConfigurations) throws Result {
        return saveAsName.isPresent() && !LINK_CONFIG_TITLE.equals(saveAsName.get()) &&
            streamLoadableConfigurations.get()
            .filter(lcc -> lcc.getKey().equals(saveAsName.get()))
            .findAny().map(lcc -> lcc.isInherited()).orElseThrow(() -> failure("Configuration has been deleted."));
    }
    
}