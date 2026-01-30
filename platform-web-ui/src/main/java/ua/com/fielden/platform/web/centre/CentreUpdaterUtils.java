package ua.com.fielden.platform.web.centre;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedPropertyInfo;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;
import static ua.com.fielden.platform.web.centre.CentreDiffSerialiser.CENTRE_DIFF_SERIALISER;

/// This utility class contains additional methods applicable to [CentreUpdater].
///
public class CentreUpdaterUtils extends CentreUpdater {
    private final static Logger logger = getLogger(CentreUpdaterUtils.class);
    public static final fetch<EntityCentreConfig> FETCH_CONFIG_AND_INSTRUMENT = fetchKeyAndDescOnlyAndInstrument(EntityCentreConfig.class);
    public static final fetch<EntityCentreConfig> FETCH_CONFIG = fetchKeyAndDescOnly(EntityCentreConfig.class);
    
    /// Protected default constructor to prevent instantiation.
    ///
    protected CentreUpdaterUtils() {
    }
    
    ///////////////////////////// CENTRE CREATION /////////////////////////////

    /// Creates default centre for concrete menu item type. Looks for Centre DSL config in [IWebUiConfig].
    ///
    protected static ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final Class<?> menuItemType, final IWebUiConfig webUiConfig) {
        final EntityCentre<?> entityCentre = webUiConfig.getCentres().get(menuItemType);
        if (entityCentre != null) {
            return entityCentre.createDefaultCentre();
        } else {
            throw new CentreUpdaterException(format("EntityCentre instance could not be found for [%s] menu item type.", menuItemType.getSimpleName()));
        }
    }
    
    /// Creates empty centre manager with calculated and custom properties.
    ///
    public static ICentreDomainTreeManagerAndEnhancer createEmptyCentre(final Class<?> root, final EntityFactory entityFactory, final T2<Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>> calculatedAndCustomProperties, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final CentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(entityFactory, setOf(root), calculatedAndCustomProperties);
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        centre.getFirstTick().checkedProperties(root);
        centre.getSecondTick().checkedProperties(root);
        return centre;
    }
    
    ///////////////////////////// CENTRE MAINTENANCE /////////////////////////////

    /// Retrieves diff instance from database if there is any.
    /// Restores it from binary representation.
    ///
    public static Optional<Map<String, Object>> retrieveDiff(
            final Class<?> menuItemType,
            final User user,
            final String name,
            final ICompanionObjectFinder coFinder)
    {
        final var co$EntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        return ofNullable(co$EntityCentreConfig.getEntity(from(modelFor(user, menuItemType.getName(), name)).model()))
                .map(ecc -> restoreDiffFrom(ecc, coFinder, format("for type [%s] with name [%s] for user [%s]", menuItemType.getSimpleName(), name, user)));
    }
    
    /// Restores diff instance from [EntityCentreConfig] instance's binary data.
    /// In case of failure, initialises new empty instance, saves it and returns as a result.
    ///
    private static Map<String, Object> restoreDiffFrom(
            // params for actual deserialisation
            final EntityCentreConfig ecc,
            // params for: deserialisation failed -- create empty and save
            final ICompanionObjectFinder coFinder,
            // params for: deserialisation failed -- logging
            final String loggingSuffix) {
        try {
            return CENTRE_DIFF_SERIALISER.deserialise(ecc.getConfigBody());
        } catch (final Exception deserialisationException) {
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED ============================================");
            logger.error(format("Unable to deserialise entity centre instance %s. Exception:", loggingSuffix), deserialisationException);
            logger.error(format("Creating and saving of empty diff %s...", loggingSuffix));
            final Map<String, Object> emptyDiff = createEmptyDifferences();
            ecc.setConfigBody(CENTRE_DIFF_SERIALISER.serialise(emptyDiff));
            final EntityCentreConfigCo co$EntityCentreConfig = coFinder.find(EntityCentreConfig.class);
            co$EntityCentreConfig.saveWithRetry(ecc); // this rare saving case should never be conflicted -- however, we still use saveWithRetry here
            logger.error(format("Creating and saving of empty diff %s...done", loggingSuffix));
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED [END] ============================================");
            return emptyDiff;
        }
    }
    
    /// Saves new [EntityCentreConfig] instance with serialised diff inside.
    ///
    public static Map<String, Object> saveNewEntityCentreManager(
        final Map<String, Object> differences,
        final Class<?> menuItemType,
        final User user,
        final String newName,
        final String newDesc,
        final ICompanionObjectFinder coFinder,
        final Function<EntityCentreConfig, EntityCentreConfig> adjustConfig
    ) {
        saveNewEntityCentreManager(CENTRE_DIFF_SERIALISER.serialise(differences), menuItemType, user, newName, newDesc, coFinder, adjustConfig);
        return differences;
    }

    /// Saves new [EntityCentreConfig] instance with `serialisedDifferences` inside.
    ///
    /// @param adjustConfig  function to adjust newly created centre config just before saving
    ///
    public static Long saveNewEntityCentreManager(
        final byte[] serialisedDifferences,
        final Class<?> menuItemType,
        final User user,
        final String newName,
        final String newDesc,
        final ICompanionObjectFinder coFinder,
        final Function<EntityCentreConfig, EntityCentreConfig> adjustConfig
    ) {
        final var co$MainMenuItem = coFinder.find(MainMenuItem.class);
        final MainMenuItem menuItem = co$MainMenuItem.findByKeyOptional(menuItemType.getName()).orElseGet(() -> {
            final MainMenuItem newMainMenuItem = co$MainMenuItem.new_();
            newMainMenuItem.setKey(menuItemType.getName());
            return co$MainMenuItem.save(newMainMenuItem);
        });
        final EntityCentreConfigCo co$EntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        final EntityCentreConfig ecc = adjustConfig.apply(co$EntityCentreConfig.new_().setOwner(user).setTitle(newName).setMenuItem(menuItem).setConfigBody(serialisedDifferences).setDesc(newDesc));
        return co$EntityCentreConfig.saveWithRetry(ecc);
    }
    
    /// Overrides existing [EntityCentreConfig] instance with new serialised diff.
    /// Otherwise, in case where there is no such instance in database, creates and saves new [EntityCentreConfig] instance with serialised diff inside.
    ///
    public static Map<String, Object> saveEntityCentreManager(
        final Map<String, Object> differences,
        final Class<?> menuItemType,
        final User user,
        final String name,
        final String newDesc,
        final ICompanionObjectFinder coFinder,
        final Function<EntityCentreConfig, EntityCentreConfig> adjustConfig)
    {
        final EntityCentreConfigCo co$EntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        final EntityCentreConfig config = co$EntityCentreConfig.getEntity(from(modelFor(user, menuItemType.getName(), name)).model());
        if (config == null) {
            saveNewEntityCentreManager(differences, menuItemType, user, name, newDesc, coFinder, adjustConfig);
        } else {
            if (newDesc != null) {
                config.setDesc(newDesc);
            }
            config.setConfigBody(CENTRE_DIFF_SERIALISER.serialise(differences));
            co$EntityCentreConfig.saveWithRetry(adjustConfig.apply(config));
        }
        return differences;
    }
    
    /// Finds [EntityCentreConfig] instance to be sufficient for changing 'preferred' / 'title' / 'desc' / 'configUuid' properties.
    ///
    protected static EntityCentreConfig findConfig(final Class<?> miType, final User user, final String deviceSpecificDiffName, final ICompanionObjectFinder coFinder) {
        final EntityCentreConfigCo coEntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        return coEntityCentreConfig.getEntity(
            from(modelFor(user, miType.getName(), deviceSpecificDiffName)).with(fetchWithKeyAndDesc(EntityCentreConfig.class, true).with("preferred").with("configUuid").with("dashboardable").with("dashboardableDate").with("dashboardRefreshFrequency").with("runAutomatically").fetchModel()).model()
        );
    }
    
    /// Finds optional configuration for `user`, `miType` and `deviceSpecificDiffName` with custom `fetch`.
    ///
    public static Optional<EntityCentreConfig> findConfigOpt(
            final Class<?> miType,
            final User user,
            final String deviceSpecificDiffName,
            final ICompanionObjectFinder coFinder,
            final fetch<EntityCentreConfig> fetch)
    {
        final EntityCentreConfigCo coEntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        return coEntityCentreConfig.getEntityOptional(
            from(modelFor(user, miType.getName(), deviceSpecificDiffName)).with(fetch).model()
        );
    }
    
    /// Finds optional configuration for `model` and `uuid` with predefined fetch model, sufficient for most situations.
    ///
    private static Optional<EntityCentreConfig> findConfigOptByUuid(final ICompoundCondition0<EntityCentreConfig> model, final String uuid, final ICompanionObjectFinder coFinder) {
        final EntityCentreConfigCo coEntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        return coEntityCentreConfig.getEntityOptional(from(model
            .and().prop("configUuid").eq().val(uuid).model()
        ).with(fetchWithKeyAndDesc(EntityCentreConfig.class, true).with("preferred").with("configUuid").with("owner.base").with("configBody").with("runAutomatically").fetchModel()).model());
    }
    
    /// Finds optional configuration for `uuid`, `miType`, `device` and `surrogateName` with predefined fetch model, sufficient for most situations.
    ///
    public static Optional<EntityCentreConfig> findConfigOptByUuid(
            final String uuid,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final DeviceProfile device,
            final String surrogateName,
            final ICompanionObjectFinder coFinder)
    {
        return findConfigOptByUuid(centreConfigQueryFor(miType, device, surrogateName), uuid, coFinder);
    }
    
    /// Finds optional configuration for `uuid`, `user`, `miType`, `device` and `surrogateName` with predefined fetch model, sufficient for most situations.
    ///
    public static Optional<EntityCentreConfig> findConfigOptByUuid(
            final String uuid,
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final DeviceProfile device,
            final String surrogateName,
            final ICompanionObjectFinder coFinder)
    {
        return findConfigOptByUuid(centreConfigQueryFor(user, miType, device, surrogateName), uuid, coFinder);
    }
    
    /// Removes centre configurations from persistent storage.
    ///
    public static void removeCentres(final User user, final Class<?> menuItemType, final ICompanionObjectFinder coFinder, final String ... names) {
        final var coEntityCentreConfig = coFinder.find(EntityCentreConfig.class);
        coEntityCentreConfig.delete(multiModelFor(user, menuItemType.getName(), names));
    }
    
    ///////////////////////////// EQL MODELS /////////////////////////////

    /// Creates partial model to retrieve [EntityCentreConfig] instances for specified <code>user</code> and <code>menuItemTypeName</code>.
    ///
    private static ICompoundCondition0<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName) {
        return select(EntityCentreConfig.class).where()
            .prop("owner").eq().val(user).and() // look for entity-centres for only current user
            .prop("menuItem.key").eq().val(menuItemTypeName);
    }
    
    /// Creates a model to retrieve [EntityCentreConfig] instances for specified <code>user</code>, <code>title</code> and <code>menuItemTypeName</code>.
    ///
    static EntityResultQueryModel<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName, final String title) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").eq().val(title).model();
    }
    
    /// Creates a model to retrieve [EntityCentreConfig] instances for specified <code>user</code>, <code>titles</code> and <code>menuItemTypeName</code>.
    ///
    private static EntityResultQueryModel<EntityCentreConfig> multiModelFor(final User user, final String menuItemTypeName, final String... titles) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").in().values(titles).model();
    }
    
}
