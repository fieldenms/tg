package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedPropertyInfo;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;

/**
 * This utility class contains additional methods applicable to {@link CentreUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreUpdaterUtils extends CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUpdaterUtils.class);
    
    /** Protected default constructor to prevent instantiation. */
    protected CentreUpdaterUtils() {
    }
    
    ///////////////////////////// CENTRE CREATION /////////////////////////////
    /**
     * Creates default centre for concrete menu item type. Looks for Centre DSL config in {@link IWebUiConfig}.
     * 
     * @param menuItemType
     * @param webUiConfig
     * @return
     */
    protected static ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final Class<?> menuItemType, final IWebUiConfig webUiConfig) {
        final EntityCentre<?> entityCentre = webUiConfig.getCentres().get(menuItemType);
        if (entityCentre != null) {
            return entityCentre.createDefaultCentre();
        } else {
            throw new CentreUpdaterException(format("EntityCentre instance could not be found for [%s] menu item type.", menuItemType.getSimpleName()));
        }
    }
    
    /**
     * Creates empty centre manager with calculated and custom properties.
     * 
     * @param root
     * @param serialiser
     * @param domainTreeEnhancerCache
     * @param calculatedAndCustomProperties
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer createEmptyCentre(final Class<?> root, final ISerialiser serialiser, final IDomainTreeEnhancerCache domainTreeEnhancerCache, final T2<Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>> calculatedAndCustomProperties, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final CentreDomainTreeManagerAndEnhancer centre = new CentreDomainTreeManagerAndEnhancer(serialiser, domainTreeEnhancerCache, setOf(root), calculatedAndCustomProperties, miType);
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        centre.getFirstTick().checkedProperties(root);
        centre.getSecondTick().checkedProperties(root);
        return centre;
    }
    
    ///////////////////////////// CENTRE MAINTENANCE /////////////////////////////
    /**
     * Retrieves diff instance from database if there is any.
     * Restores it from binary representation.
     * 
     * @param menuItemType
     * @param user
     * @param name
     * @param serialiser
     * @param eccCompanion
     * @return
     */
    public static Optional<Map<String, Object>> retrieveDiff(
            final Class<?> menuItemType,
            final User user,
            final String name,
            final ISerialiser serialiser,
            final IEntityCentreConfig eccCompanion) {
        return ofNullable(eccCompanion.getEntity(from(modelFor(user, menuItemType.getName(), name)).model()))
                .map(ecc -> restoreDiffFrom(ecc, serialiser, eccCompanion, format("for type [%s] with name [%s] for user [%s]", menuItemType.getSimpleName(), name, user)));
    }
    
    /**
     * Restores diff instance from {@link EntityCentreConfig} instance's binary data.
     * In case of failure, initialises new empty instance, saves it and returns as a result.
     * 
     * @param ecc
     * @param serialiser
     * @param eccCompanion
     * @param loggingSuffix
     * @return
     */
    private static Map<String, Object> restoreDiffFrom(
            // params for actual deserialisation
            final EntityCentreConfig ecc,
            final ISerialiser serialiser,
            // params for: deserialisation failed -- create empty and save
            final IEntityCentreConfig eccCompanion,
            // params for: deserialisation failed -- logging
            final String loggingSuffix) {
        try {
            return serialiser.deserialise(ecc.getConfigBody(), LinkedHashMap.class, JACKSON);
        } catch (final Exception deserialisationException) {
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED ============================================");
            logger.error(format("Unable to deserialise entity centre instance %s. Exception:", loggingSuffix), deserialisationException);
            logger.error(format("Creating and saving of empty diff %s...", loggingSuffix));
            final Map<String, Object> emptyDiff = createEmptyDifferences();
            ecc.setConfigBody(serialiser.serialise(emptyDiff, JACKSON));
            eccCompanion.saveWithConflicts(ecc); // this rare saving case should never be conflicted -- it is safe to use saveWithConflicts here (also nesting inside bigger transaction scopes is required)
            logger.error(format("Creating and saving of empty diff %s...done", loggingSuffix));
            logger.error("============================================ CENTRE DESERIALISATION HAS FAILED [END] ============================================");
            return emptyDiff;
        }
    }
    
    /**
     * Saves new {@link EntityCentreConfig} instance with serialised diff inside.
     */
    public static Map<String, Object> saveNewEntityCentreManager(
            final Map<String, Object> differences,
            final Class<?> menuItemType,
            final User user,
            final String newName,
            final String newDesc,
            final ISerialiser serialiser,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion) {
        return saveNewEntityCentreManager(false, centre, menuItemType, user, newName, newDesc, serialiser, eccCompanion, mmiCompanion);
    }
    
    /**
     * Saves new {@link EntityCentreConfig} instance with serialised centre manager inside.
     * 
     * @param withoutConflicts -- <code>true</code> to avoid self-conflict checks, <code>false</code> otherwise; <code>true</code> only to be used NOT IN another SessionRequired transaction scope
     */
    private static ICentreDomainTreeManagerAndEnhancer saveNewEntityCentreManager(
            final boolean withoutConflicts,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final Class<?> menuItemType,
            final User user,
            final String newName,
            final String newDesc,
            final ISerialiser serialiser,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion) {
        final MainMenuItem menuItem = mmiCompanion.findByKeyOptional(menuItemType.getName()).orElseGet(() -> {
            final MainMenuItem newMainMenuItem = mmiCompanion.new_();
            newMainMenuItem.setKey(menuItemType.getName());
            return mmiCompanion.save(newMainMenuItem);
        });
        final EntityCentreConfig ecc = eccCompanion.new_();
        ecc.setOwner(user);
        ecc.setTitle(newName);
        ecc.setMenuItem(menuItem);
        ecc.setDesc(newDesc);
        ecc.setConfigBody(serialiser.serialise(differences, JACKSON));
        if (withoutConflicts) {
            eccCompanion.saveWithoutConflicts(ecc);
        } else {
            eccCompanion.saveWithConflicts(ecc);
        }
        return differences;
    }
    
    /**
     * Overrides existing {@link EntityCentreConfig} instance with new serialised diff.
     * Otherwise, in case where there is no such instance in database, creates and saves new {@link EntityCentreConfig} instance with serialised diff inside.
     * 
     * @param withoutConflicts -- <code>true</code> to avoid self-conflict checks, <code>false</code> otherwise; <code>true</code> only to be used NOT IN another SessionRequired transaction scope
     */
    public static Map<String, Object> saveEntityCentreManager(
            final boolean withoutConflicts,
            final Map<String, Object> differences,
            final Class<?> menuItemType,
            final User user,
            final String name,
            final String newDesc,
            final ISerialiser serialiser,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion) {
        final EntityCentreConfig config = eccCompanion.getEntity(from(modelFor(user, menuItemType.getName(), name)).model());
        if (config == null) {
            saveNewEntityCentreManager(withoutConflicts, differences, menuItemType, user, name, newDesc, serialiser, eccCompanion, mmiCompanion);
        } else {
            if (newDesc != null) {
                config.setDesc(newDesc);
            }
            config.setConfigBody(serialiser.serialise(differences, JACKSON));
            if (withoutConflicts) {
                eccCompanion.saveWithoutConflicts(config);
            } else {
                eccCompanion.saveWithConflicts(config);
            }
        }
        return differences;
    }
    
    /**
     * Finds {@link EntityCentreConfig} instance to be sufficient for changing 'preferred' property and 'title' / 'desc'.
     * 
     * @param menuItemType
     * @param user
     * @param name
     * @param eccCompanion
     * @return
     */
    protected static EntityCentreConfig findConfig(final Class<?> menuItemType, final User user, final String name, final IEntityCentreConfig eccCompanion) {
        return eccCompanion.getEntity(
            from(modelFor(user, menuItemType.getName(), name)).with(fetchWithKeyAndDesc(EntityCentreConfig.class, true).with("preferred").fetchModel()).model()
        );
    }
    
    /**
     * Removes centre configurations from persistent storage.
     * 
     * @param menuItemType
     * @param names
     */
    public static void removeCentres(final User user, final Class<?> menuItemType, final IEntityCentreConfig eccCompanion, final String ... names) {
        eccCompanion.delete(multiModelFor(user, menuItemType.getName(), names));
    }
    
    ///////////////////////////// EQL MODELS /////////////////////////////
    /**
     * Creates partial model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * 
     * @return
     */
    private static ICompoundCondition0<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName) {
        return select(EntityCentreConfig.class).where()
            .prop("owner").eq().val(user).and() // look for entity-centres for only current user
            .prop("menuItem.key").eq().val(menuItemTypeName);
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code>, <code>title</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * @param title
     * 
     * @return
     */
    private static EntityResultQueryModel<EntityCentreConfig> modelFor(final User user, final String menuItemTypeName, final String title) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").eq().val(title).model();
    }
    
    /**
     * Creates a model to retrieve {@link EntityCentreConfig} instances for specified <code>user</code>, <code>titles</code> and <code>menuItemTypeName</code>.
     *
     * @param user
     * @param menuItemTypeName
     * @param titles
     * 
     * @return
     */
    private static EntityResultQueryModel<EntityCentreConfig> multiModelFor(final User user, final String menuItemTypeName, final String... titles) {
        return modelFor(user, menuItemTypeName).and()
            .prop("title").in().values(titles).model();
    }
    
}