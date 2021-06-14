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
    private static final String CONFIGURATION_HAS_BEEN_DELETED = "Configuration has been deleted.";
    /**
     * The key for customObject's value containing configuration autoRun.
     */
    public static final String AUTO_RUN = "autoRun";

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
     * @param configUuid -- empty not to update current config uuid on the client; {@code of(empty())} or {@code of("a1b2c3")} to update config uuid on the client for default and named configs respectively
     * @return
     */
    static Map<String, Object> getCustomObject(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity, final Optional<Optional<String>> configUuid) {
        return getCustomObject(selectionCrit, appliedCriteriaEntity, selectionCrit.saveAsName(), configUuid);
    }
    
    /**
     * Creates custom object with centre information for concrete <code>appliedCriteriaEntity</code>.
     * <p>
     * Contains <code>centreDirty</code> flag which is calculated comparing <code>appliedCriteriaEntity</code>'s centre against saved version of <code>saveAsNameToCompare</code>'s centre or it is true if configuration is New (aka default, link or inherited).
     * 
     * @param selectionCrit
     * @param appliedCriteriaEntity
     * @param saveAsNameToCompare
     * @param configUuid -- empty not to update current config uuid on the client; {@code of(empty())} or {@code of("a1b2c3")} to update config uuid on the client for default and named configs respectively
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
     * Returns {@code true} in case where {@code saveAsName}d configuration represents default configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @return
     */
    public static boolean isDefault(final Optional<String> saveAsName) {
        return !saveAsName.isPresent();
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents link configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @return
     */
    public static boolean isLink(final Optional<String> saveAsName) {
        return !isDefault(saveAsName) && LINK_CONFIG_TITLE.equals(saveAsName.get());
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents default or link configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @return
     */
    public static boolean isDefaultOrLink(final Optional<String> saveAsName) {
        return isDefault(saveAsName) || LINK_CONFIG_TITLE.equals(saveAsName.get());
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents link configuration or inherited from base user configuration or inherited from shared configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isLinkOrInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return isLink(saveAsName) || isInherited(saveAsName, selectionCrit);
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents default / link configuration or inherited from base user configuration or inherited from shared configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isDefaultOrLinkOrInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return isDefaultOrLink(saveAsName) || isInherited(saveAsName, selectionCrit);
    }
    
    /**
     * Returns {@code loadableConfig} for non-empty inherited {@code loadableConfig}, empty optional otherwise.
     * 
     * @param loadableConfig
     * @return
     */
    public static Optional<LoadableCentreConfig> inherited(final Optional<LoadableCentreConfig> loadableConfig) {
        return loadableConfig.filter(LoadableCentreConfig::isInherited);
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents inherited from base user configuration or inherited from shared configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isInherited(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return inherited(findLoadableConfig(saveAsName, selectionCrit)).isPresent();
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents inherited from base user configuration or inherited from shared configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @param streamLoadableConfigurations -- function to stream loadable configurations from which currently analysed configuration will be taken
     * @return
     */
    public static boolean isInherited(final Optional<String> saveAsName, final Supplier<Stream<LoadableCentreConfig>> streamLoadableConfigurations) {
        return inherited(findLoadableConfig(saveAsName, streamLoadableConfigurations)).isPresent();
    }
    
    /**
     * Returns {@code loadableConfig} for non-empty 'inherited from base' {@code loadableConfig}, empty optional otherwise.
     * 
     * @param loadableConfig
     * @return
     */
    public static Optional<LoadableCentreConfig> inheritedFromBase(final Optional<LoadableCentreConfig> loadableConfig) {
        return inherited(loadableConfig).filter(LoadableCentreConfig::isBase);
    }
    
    /**
     * Returns {@code true} in case where {@code saveAsName}d configuration represents inherited from base user configuration,
     * otherwise {@code false}.
     * 
     * @param saveAsName
     * @param selectionCrit
     * @return
     */
    public static boolean isInheritedFromBase(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return inheritedFromBase(findLoadableConfig(saveAsName, selectionCrit)).isPresent();
    }
    
    /**
     * Finds {@link LoadableCentreConfig} instance for concrete {@code saveAsName}. Default or link configurations are not loadable and empty {@link Optional} is returned.
     * 
     * @param saveAsName
     * @param selectionCrit -- selection criteria being able to stream loadable configurations
     * @return
     * @throws Result if configuration is not present aka deleted
     */
    public static Optional<LoadableCentreConfig> findLoadableConfig(final Optional<String> saveAsName, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) throws Result {
        return findLoadableConfig(saveAsName, () -> selectionCrit.loadableCentreConfigs().apply(of(saveAsName)).stream());
    }
    
    /**
     * Finds {@link LoadableCentreConfig} instance for concrete {@code saveAsName}. Default or link configurations are not loadable and empty {@link Optional} is returned.
     * 
     * @param saveAsName
     * @param streamLoadableConfigurations -- function to stream loadable configurations
     * @return
     * @throws Result if configuration is not present aka deleted
     */
    public static Optional<LoadableCentreConfig> findLoadableConfig(final Optional<String> saveAsName, final Supplier<Stream<LoadableCentreConfig>> streamLoadableConfigurations) throws Result {
        return isDefaultOrLink(saveAsName)
            ? empty()
            : of(
                streamLoadableConfigurations.get()
                .filter(lcc -> lcc.getKey().equals(saveAsName.get()))
                .findAny()
                .orElseThrow(() -> failure(CONFIGURATION_HAS_BEEN_DELETED))
            );
    }
    
}