package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.ALL_ORDERING;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.AND_BEFORE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_MNEMONIC;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_PREFIX;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE2;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.GROW_FACTOR;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.NOT;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.OR_NULL;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE2;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.WIDTH;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isBooleanCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDummyMarker;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.KRYO;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.copyCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.createDefaultCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfig;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.initEntityCentreManager;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.saveAsEntityCentreManager;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.saveEntityCentreManager;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.TgKryo;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.SaveAsNameAnnotation;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/**
 * Represents a set of utility methods for updating / committing of surrogate centres, for e.g. 'fresh', 'previouslyRun' etc.
 * <p>
 * Every surrogate centre has its own diff centre that, saves into the database during {@link #commitCentre(User, IUserProvider, Class, String, Optional, DeviceProfile, ICentreDomainTreeManagerAndEnhancer, IWebUiConfig, ISerialiser, IEntityCentreConfig, IMainMenuItem, IUser)} process.
 *
 * @author TG Team
 *
 */
public class CentreUpdater {
    private static final Logger logger = Logger.getLogger(CentreUpdater.class);
    private static final String DIFFERENCES_SUFFIX = "__________DIFFERENCES";
    
    public static final String FRESH_CENTRE_NAME = "__________FRESH";
    public static final String PREVIOUSLY_RUN_CENTRE_NAME = "__________PREVIOUSLY_RUN";
    public static final String SAVED_CENTRE_NAME = "__________SAVED";
    
    /** Protected default constructor to prevent instantiation. */
    protected CentreUpdater() {
    }
    
    /**
     * Returns device-specific surrogate name for the centre based on original <code>surrogateName</code>.
     * <p>
     * Every centre, defined by miType and surrogateName, when accessed through {@link CentreUpdater} API could have two counterparts: DESKTOP and MOBILE.
     * This is needed to differentiate between actual centres on different devices for the same user (for example, only a subset of columns could be visible in
     * MOBILE app, but a full set in DESKTOP app).
     * <p>
     * This need has arisen mainly from embedded [into actions] centres, because copying of miTypes and those actions (and their full hierarchy with invocation points)
     * seems heavily impractical.
     * 
     * @param surrogateName
     * @param device
     * @return
     */
    private static String deviceSpecific(final String surrogateName, final DeviceProfile device) {
        if (DESKTOP.equals(device)) {
            return surrogateName;
        } else if (MOBILE.equals(device)) {
            // Please note that in case where the need arise to 'use the same configuration for both MOBILE and DESKTOP apps' 
            // then it is quite trivial to support such functionality.
            // In that case we can provide annotation for menu item types like @TheSameForMobileAndDesktop and check here whether this annotation is present.
            // If yes then 'surrogateName' should be returned just like for DESKTOP device.
            return MOBILE.name() + surrogateName;
        } else {
            throw new CentreUpdaterException(format("Device [%s] is unknown.", device));
        }
    }
    
    private static String saveAsSpecific(final String name, final Optional<String> saveAsName) {
        return saveAsName.map(san -> format("%s[%s]", name, san)).orElse(name);
    }
    
    /**
     * Returns the current version of centre (initialises it in case if it is not created yet, updates it in case where it is stale).
     * <p>
     * Initialisation / updating goes through the following chain: 'default centre' + 'differences centre' := 'centre'.
     * <p>
     * Centre on its own is never saved, but it is used to create 'differences centre' (when committing is performed).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.);
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer updateCentre(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return updateCentre0(user, userProvider, miType, deviceSpecific(saveAsSpecific(name, saveAsName), device), saveAsName, device, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    private static ICentreDomainTreeManagerAndEnhancer updateCentre0(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        System.out.println();
        logger.debug(format("%s '%s' centre for miType [%s] for user %s...", "Retrieving", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre = updateDifferencesCentre(miType, user, userProvider, deviceSpecificName, saveAsName, device, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = loadCentreFromDefaultAndDiff(user, miType, deviceSpecificName, saveAsName, updatedDiffCentre, serialiser, webUiConfig);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Retrieved", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return loadedCentre;
    }
    
    
    /**
     * Updates (retrieves) current version of centre description.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static String updateCentreDesc(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final IEntityCentreConfig eccCompanion) {
        return updateCentreDesc0(user, miType, deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device), eccCompanion);
    }
    private static String updateCentreDesc0(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final String deviceSpecificName, final IEntityCentreConfig eccCompanion) {
        System.out.println();
        logger.debug(format("%s '%s' centre for miType [%s] for user %s...", "updateCentreDesc", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // find description of the centre configuration to be copied from
        final String freshDeviceSpecificDiffName = deviceSpecificName + DIFFERENCES_SUFFIX;
        final EntityCentreConfig eccWithDesc = CentreUpdaterUtils.findConfig(miType, user, freshDeviceSpecificDiffName, eccCompanion);
        final String result = eccWithDesc == null ? null : eccWithDesc.getDesc();
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "updateCentreDesc", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return result;
    }
    
    /**
     * Changes configuration title to <code>newTitle</code> and description to <code>newDesc</code> and saves these changes to persistent storage.
     * 
     * @param gdtm
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param newTitle -- new title for configuration (aka 'saveAsName')
     * @param newDesc -- new description for configuration
     */
    public static void editCentreTitleAndDesc(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final String newTitle,
            final String newDesc,
            final IEntityCentreConfig eccCompanion) {
        System.out.println();
        logger.debug(format("%s '%s' centre for miType [%s] for user %s...", "editCentreTitleAndDesc", saveAsName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        final Function<Optional<String>, Function<String, String>> nameOf = (saveAs) -> (surrogateName) -> deviceSpecific(saveAsSpecific(surrogateName, saveAs), device) + DIFFERENCES_SUFFIX;
        final Function<String, String> currentNameOf = nameOf.apply(saveAsName);
        final String currentNameFresh = currentNameOf.apply(FRESH_CENTRE_NAME);
        final String currentNameSaved = currentNameOf.apply(SAVED_CENTRE_NAME);
        final String currentNamePreviouslyRun = currentNameOf.apply(PREVIOUSLY_RUN_CENTRE_NAME);
        final EntityCentreConfig freshConfig = findConfig(miType, user, currentNameFresh, eccCompanion);
        final EntityCentreConfig savedConfig = findConfig(miType, user, currentNameSaved, eccCompanion);
        final EntityCentreConfig previouslyRunConfig = findConfig(miType, user, currentNamePreviouslyRun, eccCompanion);
        if (freshConfig == null || savedConfig == null) {
            throw failuref("Fresh or saved configuration for configuration [%s] does not exist.", saveAsName);
        }
        
        // newTitle
        final Function<String, String> newNameOf = nameOf.apply(of(newTitle));
        freshConfig.setTitle(newNameOf.apply(FRESH_CENTRE_NAME));
        savedConfig.setTitle(newNameOf.apply(SAVED_CENTRE_NAME));
        final String previouslyRunNewTitle = newNameOf.apply(PREVIOUSLY_RUN_CENTRE_NAME);
        if (previouslyRunConfig != null) { // previouslyRun centre may not exist
            previouslyRunConfig.setTitle(previouslyRunNewTitle);
        }
        // newDesc
        freshConfig.setDesc(newDesc);
        
        // clear all centres with the same name in the case where title has been changed -- new title potentially can be in conflict with another configuration and that another configuration should be deleted
        if (!equalsEx(saveAsName, of(newTitle))) {
            CentreUpdaterUtils.removeCentres(user, miType, eccCompanion, freshConfig.getTitle(), savedConfig.getTitle(), previouslyRunNewTitle);
        }
        
        // save
        eccCompanion.quickSave(freshConfig);
        eccCompanion.quickSave(savedConfig);
        if (previouslyRunConfig != null) { // previouslyRun centre may not exist
            eccCompanion.quickSave(previouslyRunConfig);
        }
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "editCentreTitleAndDesc", saveAsName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
    }
    
    /**
     * Removes centres from persistent storage (diffs) by their <code>names</code>.
     * <p>
     * Please be careful when removing centres for the purpose of later update: preferred state and custom description need to be maintained properly.
     *
     * @param user
     * @param miType
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param names -- surrogate names of the centres (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     */
    public static void removeCentres(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final Optional<String> saveAsName, final IEntityCentreConfig eccCompanion, final String ... names) {
        removeCentres0(user, miType, eccCompanion, stream(names).map(name -> deviceSpecific(saveAsSpecific(name, saveAsName), device)).toArray(String[]::new));
    }
    private static void removeCentres0(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final IEntityCentreConfig eccCompanion, final String ... deviceSpecificNames) {
        System.out.println();
        logger.debug(format("%s '%s' centres for miType [%s] for user %s...", "removeCentres0", deviceSpecificNames, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // remove corresponding diff centre instances from persistent storage
        final String [] deviceSpecificDiffNames = stream(deviceSpecificNames).map(name -> name + DIFFERENCES_SUFFIX).toArray(String[]::new);
        CentreUpdaterUtils.removeCentres(user, miType, eccCompanion, deviceSpecificDiffNames);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centres for miType [%s] for user %s... done in [%s].", "removeCentres0", deviceSpecificNames, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
    }
    
    /**
     * Commits the centre's diff to the database and removes it from cache (needs to be updated to be able to be used).
     *
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     */
    public static ICentreDomainTreeManagerAndEnhancer commitCentre(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return commitCentre0(user, userProvider, miType, deviceSpecific(saveAsSpecific(name, saveAsName), device), saveAsName, device, null, centre, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    private static ICentreDomainTreeManagerAndEnhancer commitCentre0(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final String newDesc,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        System.out.println();
        logger.debug(format("%s '%s' centre for miType [%s] for user %s...", "Committing", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, user, webUiConfig);
        // creates differences centre from the differences between 'default centre' and 'centre'
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = createDifferencesCentre(centre, defaultCentre, getEntityType(miType), serialiser);
        
        // override old 'diff centre' with recently created one and save it
        overrideAndSaveDifferencesCentreIfChanged(user, userProvider, miType, deviceSpecificName, saveAsName, device, differencesCentre, newDesc, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Committed", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return centre;
    }
    
    /**
     * Initialises and commits centre from the passed <code>centreToBeInitialisedAndCommitted</code> instance for surrogate centre with concrete <code>name</code>.
     * <p>
     * Please note that this operation is immutable in regard to the surrogate centre instance being copied.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param centreToBeInitialisedAndCommitted
     * @param newDesc -- new description to be saved into persistent storage
     */
    public static ICentreDomainTreeManagerAndEnhancer initAndCommit(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centreToBeInitialisedAndCommitted,
            final String newDesc,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return initAndCommit0(user, userProvider, miType, deviceSpecific(saveAsSpecific(name, saveAsName), device), saveAsName, device, centreToBeInitialisedAndCommitted, newDesc, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    private static ICentreDomainTreeManagerAndEnhancer initAndCommit0(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centreToBeInitialisedAndCommitted,
            final String newDesc,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        System.out.println();
        logger.debug(format("%s '%s' centre for miType [%s] for user %s...", "Initialising & committing", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // and then commit it to the database (save its diff)
        commitCentre0(user, userProvider, miType, deviceSpecificName, saveAsName, device, newDesc, centreToBeInitialisedAndCommitted, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Initialised & committed", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return centreToBeInitialisedAndCommitted;
    }
    
    /**
     * Finds loadable configurations for current user (defined in <code>gdtm</code>) and specified <code>miType; device</code>.
     * {@link LoadableCentreConfig} instances are sorted by title.
     * Inherited configurations receive appropriate {@link LoadableCentreConfig#isInherited()} flag.
     * <p>
     * Please note that inheritance is purely defined by 'saveAsName' -- if both configuration for user and its base user have the same 'saveAsName' then they are in inheritance relationship.
     * 
     * @param user
     * @param miType
     * @param device -- device profile (mobile or desktop) for which loadable centres
     * @param companionFinder
     * @return
     */
    public static List<LoadableCentreConfig> loadableConfigurations(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final DeviceProfile device,
            final ICompanionObjectFinder companionFinder) {
        final List<LoadableCentreConfig> loadableConfigurations = new ArrayList<>();
        
        final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final ILoadableCentreConfig lccCompanion = companionFinder.find(LoadableCentreConfig.class);
        
        final String surrogateNamePrefix = deviceSpecific(FRESH_CENTRE_NAME, device);
        final EntityResultQueryModel<EntityCentreConfig> queryForCurrentUser = centreConfigQueryFor(user, miType, device).model();
        final fetch<EntityCentreConfig> fetch = EntityUtils.fetchWithKeyAndDesc(EntityCentreConfig.class).fetchModel();
        if (user.isBase()) {
            try (final Stream<EntityCentreConfig> stream = eccCompanion.stream(from(queryForCurrentUser).with(fetch).model()) ) {
                stream.forEach(ecc -> {
                    loadableConfigurations.add(createLoadableCentreConfig(ecc, false, surrogateNamePrefix, lccCompanion));
                });
            }
        } else {
            final EntityResultQueryModel<EntityCentreConfig> queryForBaseUser = centreConfigQueryFor(user.getBasedOnUser(), miType, device).model();
            try (final Stream<EntityCentreConfig> streamForCurrentUser = eccCompanion.stream(from(queryForCurrentUser).with(fetch).model());
                 final Stream<EntityCentreConfig> streamForBaseUser = eccCompanion.stream(from(queryForBaseUser).with(fetch).model())) {
                streamForCurrentUser.forEach(ecc -> {
                    loadableConfigurations.add(createLoadableCentreConfig(ecc, false, surrogateNamePrefix, lccCompanion));
                });
                streamForBaseUser.forEach(ecc -> {
                    final LoadableCentreConfig lcc = createLoadableCentreConfig(ecc, true, surrogateNamePrefix, lccCompanion);
                    if (loadableConfigurations.contains(lcc)) {
                        final LoadableCentreConfig foundLcc = loadableConfigurations.stream().filter(item -> item.equals(lcc)).findAny().get();
                        foundLcc.setInherited(true); // description of specific config has a priority over base config
                    } else {
                        loadableConfigurations.add(lcc);
                    }
                });
            }
        }
        loadableConfigurations.remove(new LoadableCentreConfig().setKey(WebApiUtils.LINK_CONFIG_TITLE)); // exclude 'link' configuration from load dialog if it is present (aka centre 'link' with criteria parameters was loaded at least once)
        Collections.sort(loadableConfigurations);
        return loadableConfigurations;
    }
    
    /**
     * Returns {@link Stream} of preferred {@link EntityCentreConfig} configurations for specified <code>user</code>, <code>device</code> and concrete 
     * <code>miType</code>'ed menu item.
     * <p>
     * Please note that by design this stream should return single or none instance.
     * 
     * @param user
     * @param miType
     * @param device
     * @return
     */
    private static Stream<EntityCentreConfig> streamPreferredConfigs(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final ICompanionObjectFinder companionFinder) {
        final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final EntityResultQueryModel<EntityCentreConfig> queryForCurrentUser = centreConfigQueryFor(user, miType, device)
            .and().prop("preferred").eq().val(true).model();
        final fetch<EntityCentreConfig> fetch = fetchWithKeyAndDesc(EntityCentreConfig.class).with("preferred").fetchModel();
        return eccCompanion.stream(from(queryForCurrentUser).with(fetch).model());
    }
    
    /**
     * Determines the preferred configuration <code>saveAsName</code> for the current user (defined by <code>gdtm.getUserProvider().getUser()</code>), the specified <code>device</code> and concrete 
     * <code>miType</code>'ed menu item.
     * 
     * @param user
     * @param miType
     * @param device
     * @return
     */
    public static Optional<String> retrievePreferredConfigName(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final ICompanionObjectFinder companionFinder) {
        final String surrogateNamePrefix = deviceSpecific(FRESH_CENTRE_NAME, device);
        try (final Stream<EntityCentreConfig> stream = streamPreferredConfigs(user, miType, device, companionFinder) ) {
            return stream.findAny().map(ecc -> obtainTitleFrom(ecc.getTitle(), surrogateNamePrefix));
        }
    }
    
    /**
     * Makes <code>saveAsName</code> configuration preferred for the current user (defined by <code>gdtm.getUserProvider().getUser()</code>), the specified <code>device</code> and concrete 
     * <code>miType</code>'ed menu item.
     * 
     * @param user
     * @param miType
     * @param device
     * @return
     */
    public static void makePreferred(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final ICompanionObjectFinder companionFinder) {
        final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
        try (final Stream<EntityCentreConfig> stream = streamPreferredConfigs(user, miType, device, companionFinder) ) {
            stream.forEach(ecc -> eccCompanion.quickSave(ecc.setPreferred(false)));
        }
        if (saveAsName.isPresent()) {
            eccCompanion.quickSave(
                findConfig(miType, user, deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device) + DIFFERENCES_SUFFIX, eccCompanion)
                .setPreferred(true)
            );
        }
    }
    
    /**
     * Creates {@link LoadableCentreConfig} instance from <code>ecc</code>'s title and description.
     * 
     * @param ecc
     * @param inherited -- indicates whether {@link LoadableCentreConfig} instance being created needs to be 'inherited'
     * @param surrogateNamePrefix
     * @param lccCompanion
     * @return
     */
    private static LoadableCentreConfig createLoadableCentreConfig(final EntityCentreConfig ecc, final boolean inherited, final String surrogateNamePrefix, final ILoadableCentreConfig lccCompanion) {
        final LoadableCentreConfig lcc = lccCompanion.new_();
        lcc.setInherited(inherited).setKey(obtainTitleFrom(ecc.getTitle(), surrogateNamePrefix)).setDesc(ecc.getDesc());
        return lcc;
    }
    
    /**
     * Returns opposite device for the specified <code>device</code>.
     * 
     * @param device
     * @return
     */
    private static DeviceProfile opposite(final DeviceProfile device) {
        return DESKTOP.equals(device) ? MOBILE : DESKTOP;
    }
    
    /**
     * Receives actual title from surrogate name persisted inside {@link EntityCentreConfig#getTitle()} (<code>surrogateName</code>).
     * 
     * @param surrogateName
     * @param surrogateNamePrefix
     * @return
     */
    private static String obtainTitleFrom(final String surrogateName, final String surrogateNamePrefix) {
        final String surrogateWithSuffix = surrogateName.replaceFirst(surrogateNamePrefix, "");
        return surrogateWithSuffix.substring(1, surrogateWithSuffix.lastIndexOf("]"));
    }
    
    /**
     * Creates a query to find centre configurations persisted for <code>user</code>.
     * <p>
     * Looks only for named / link configurations, default configurations are avoided.
     * 
     * @param user
     * @param miType
     * @param device -- the device for which centre configurations are looked for
     * @return
     */
    private static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device) {
        final String escapedOpeningBracket = "[[]"; // need to provide escaping for opening bracket to find records with [, see https://stackoverflow.com/questions/439495/how-can-i-escape-square-brackets-in-a-like-clause
        return select(EntityCentreConfig.class).where().
            begin().prop("owner").eq().val(user).end().and().
            prop("title").like().val(deviceSpecific(FRESH_CENTRE_NAME, device) + escapedOpeningBracket + "%").and().
            prop("title").notLike().val(deviceSpecific(FRESH_CENTRE_NAME, opposite(device)) + escapedOpeningBracket + "%").and().
            prop("menuItem.key").eq().val(miType.getName());
    }
    
    /**
     * Loads centre through the following chain: 'default centre' + 'differences centre' := 'centre'.
     *
     * @param user
     * @param miType
     * @param deviceSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param updatedDiffCentre -- updated differences centre
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig) {
        logger.debug(format("\t%s '%s' centre for miType [%s] for user %s...", "loadCentreFromDefaultAndDiff", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, user, webUiConfig);
        // applies diffCentre on top of defaultCentreCopy to produce loadedCentre:
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = applyDifferences(defaultCentre, updatedDiffCentre, getEntityType(miType));
        // For all generated types on freshCentre (and on its derivatives like 'unchanged freshCentre', 'previouslyRun centre', 'unchanged previouslyRun centre' etc.) there is a need to
        //  provide miType information inside its generated type to be sent to the client application. This is done through the use of
        //  annotation miType and other custom annotations, for example @SaveAsName.
        // Please note that copyCentre method performs copying of all defined annotations to provide freshCentre's derivatives
        //  with such additional information too.
        logger.debug(format("\t\tadjustManagedTypeAnnotations: add @MiType(%s, %s)...", miType.getSimpleName(), saveAsName));
        final DateTime adjustAnnotationsStart = new DateTime();
        if (saveAsName.isPresent()) { // this is saveAs user-specific configuration
            logger.debug(format("\t\t\tadjustManagedTypeAnnotations: saveAsName [%s] is present...", saveAsName));
            // We need to provide a new MiType annotation with saveAsName there.
            // However, it should be done in a smart way, i.e. look for cached (by means of (user, miType, saveAsName)) DomainTreeEnhancer instance first.
            // If there is such an instance, just take generated type information and replace it inside loadedCentre.getEnhancer().
            // Otherwise, perform adjustManagedTypeAnnotations and cache adjusted DTE instance for future reference.
            final TgKryo tgKryo = (TgKryo) serialiser.getEngine(KRYO);
            final Class<?> cachedGeneratedType = tgKryo.getGeneratedTypeFor(miType, saveAsName.get(), user.getId());
            if (cachedGeneratedType != null) {
                logger.debug(format("\t\t\t\tadjustManagedTypeAnnotations: cachedGeneratedType present..."));
                for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
                    if (isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                        loadedCentre.getEnhancer().replaceManagedTypeBy(root, cachedGeneratedType);
                    }
                }
                logger.debug(format("\t\t\t\tadjustManagedTypeAnnotations: cachedGeneratedType present...done"));
            } else {
                logger.debug(format("\t\t\t\tadjustManagedTypeAnnotations: cachedGeneratedType not present..."));
                for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
                    if (isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                        final Class<?> newGeneratedType = loadedCentre.getEnhancer().adjustManagedTypeAnnotations(root, new SaveAsNameAnnotation().newInstance(saveAsName.get()));
                        tgKryo.putGeneratedTypeFor(miType, saveAsName.get(), user.getId(), newGeneratedType);
                    }
                }
                logger.debug(format("\t\t\t\tadjustManagedTypeAnnotations: cachedGeneratedType not present...done"));
            }
            logger.debug(format("\t\tadjustManagedTypeAnnotations: saveAsName [%s] is present...done", saveAsName));
        } else {
            // otherwise, annotation @MiType(MiType.class, "") must be present already -- no action is needed
        }
        logger.debug(format("\t\tadjustManagedTypeAnnotations: add @MiType(%s, %s)...done in [%s]", miType.getSimpleName(), saveAsName, new Period(adjustAnnotationsStart, new DateTime()).getMillis() + " ms"));
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "loadCentreFromDefaultAndDiff", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return loadedCentre;
    }
    
    /**
     * Returns the current version of default centre manager (initialises it in case if it is not created yet).
     * <p>
     * Currently it is created from Entity Centre DSL through the special gdtm, which knows about Centre DSL configuration.
     * <p>
     * IMPORTANT: this 'default centre' is used for constructing 'fresh centre', 'previouslyRun centre' and their 'diff centres', that is why it is very important to make it suitable for Web UI default values.
     * All other centres will reuse such Web UI specific default values.
     * <p>
     * Please note that 'default' centre is not user-specific and here we just initialise it inside user-specific gdtm for convenient retrieval later.
     *
     * @param gdtm
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final User user, final IWebUiConfig webUiConfig) {
        logger.debug(format("\t\t%s centre for miType [%s] for user %s...", "getDefaultCentre", miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // standard init (from Centre DSL config)
        final ICentreDomainTreeManagerAndEnhancer centre = createDefaultCentre(miType, webUiConfig);
        
        // Web UI default values application
        final ICentreDomainTreeManagerAndEnhancer defaultedCentre = applyWebUIDefaultValues(centre, getEntityType(miType));
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t\t%s the centre for miType [%s] for user %s... done in [%s].", "getDefaultCentre", miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        
        return defaultedCentre;
    }
    
    /**
     * Initialises 'differences centre' from the persistent storage, if it exists.
     * <p>
     * If no 'differences centre' exists -- the following steps are performed:
     * <p>
     * 1. make sure that 'default centre' exists in gdtm (with already applied Web UI default values!);<br>
     * 2. make saveAs from 'default centre' which will be 'diff centre' (this promotes the empty diff to the storage!)<br>
     *
     * @param miType
     * @param deviceSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentre(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final User user,
            final IUserProvider userProvider,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        logger.debug(format("\t%s '%s' centre for miType [%s] for user %s...", "updateDifferencesCentre", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // the name consists of 'deviceSpecificName' and 'DIFFERENCES_SUFFIX'
        final String deviceSpecificDiffName = deviceSpecificName + DIFFERENCES_SUFFIX;
        
        final ICentreDomainTreeManagerAndEnhancer resultantDiffCentre;
        // WILL BE UPDATED IN EVERY CALL OF updateDifferencesCentre!
        
        // init (or update) diff centre from persistent storage if exists
        final Optional<ICentreDomainTreeManagerAndEnhancer> centre = initEntityCentreManager(miType, user, deviceSpecificDiffName, serialiser, webUiConfig, eccCompanion);
        if (centre.isPresent()) {
            resultantDiffCentre = centre.get();
        } else {
            // Default centre is used as a 'base' for all centres; all diffs are created comparing to default centre.
            // Default centre is now needed for both cases: base or non-base user.
            if (user.isBase() || of(WebApiUtils.LINK_CONFIG_TITLE).equals(saveAsName) || empty().equals(saveAsName)) { // for non-base user 'link' and 'default' configurations need to be derived from default user-specific configuration instead of base configuration
                final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, user, webUiConfig);
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff (there potentially can be some values from 'default centre',
                //   but diff centre will be empty disregarding that fact -- no properties were marked as changed; but initialisation from 'default centre' is important --
                //   this makes diff centre nicely synchronised with Web UI default values)
                resultantDiffCentre = saveAsEntityCentreManager(defaultCentre, miType, user, deviceSpecificDiffName, null, serialiser, eccCompanion, mmiCompanion);
            } else { // non-base user
                // diff centre does not exist in persistent storage yet -- create a diff by comparing basedOnCentre (configuration created by base user) and default centre
                final User baseUser = beginBaseUserOperations(userProvider, user, userCompanion);
                final ICentreDomainTreeManagerAndEnhancer baseCentre = getBaseCentre(baseUser, userProvider, miType, saveAsName, device, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                // find description of the centre configuration to be copied from
                final String upstreamDesc = updateCentreDesc(baseUser, miType, saveAsName, device, eccCompanion);
                // creates differences centre from the differences between base user's 'default centre' (which can be user specific, see IValueAssigner for properties dependent on User) and 'baseCentre'
                final ICentreDomainTreeManagerAndEnhancer differencesCentre = createDifferencesCentre(baseCentre, getDefaultCentre(miType, baseUser, webUiConfig), getEntityType(miType), serialiser);
                endBaseUserOperations(user, userProvider);
                
                // promotes diff to local cache and saves it into persistent storage
                resultantDiffCentre = saveAsEntityCentreManager(differencesCentre, miType, user, deviceSpecificDiffName, upstreamDesc, serialiser, eccCompanion, mmiCompanion);
            }
        }
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "updateDifferencesCentre", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return resultantDiffCentre;
    }
    
    /**
     * Prepares environment for executing operations against base user. {@link IUserProvider} receives corresponding base user into it.
     * <p>
     * This method's call must be followed by {@link #endBaseUserOperations(User, IUserProvider)} call.
     * 
     * @param userProvider
     * @param user
     * @param userCompanion
     * @return
     */
    private static User beginBaseUserOperations(final IUserProvider userProvider, final User user, final IUser userCompanion) {
        // get base user
        final User baseUser = userCompanion.findByEntityAndFetch(fetch(User.class).with(LAST_UPDATED_BY), user.getBasedOnUser());
        // insert appropriate user into IUserProvider for a very brief period of time to facilitate any operations against baseUser
        userProvider.setUser(baseUser);
        return baseUser;
    }
    
    /**
     * Retrieves current version of base centre for <code>baseUser</code>.
     * <p>
     * This method's call must be enclosed into {@link #beginBaseUserOperations(IUserProvider, User, IUser)} and {@link #endBaseUserOperations(User, IUserProvider)} calls.
     * 
     * @param baseUser
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getBaseCentre(
            final User baseUser,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return updateCentre(baseUser, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    
    /**
     * Completes base user work: {@link IUserProvider} receives back <code>user</code>.
     * 
     * @param user
     * @param userProvider
     * @return
     */
    private static void endBaseUserOperations(final User user, final IUserProvider userProvider) {
        // return 'user' into user provider
        userProvider.setUser(user);
    }
    
    /**
     * Returns the centre from which the specified centre is derived from. Parameters <code>saveAsName</code>, <code>device</code> and current user (<code>gdtm.getUserProvider().getUser()</code>) identify the centre for which
     * base centre is looking for.
     * <p>
     * For non-base user the base centre is identified as SAVED_CENTRE_NAME version of <code>gdtm.basedOnManager()</code>'s centre of the same <code>saveAsName</code>. 
     * For base user the base centre is identified as <code>gdtm.basedOnManager()</code>'s <code>getDefaultCentre()</code>. 
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer baseCentre(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        if (user.isBase()) {
            return getDefaultCentre(miType, user, webUiConfig);
        } else {
            final User baseUser = beginBaseUserOperations(userProvider, user, userCompanion);
            final ICentreDomainTreeManagerAndEnhancer baseCentre = getBaseCentre(baseUser, userProvider, miType, saveAsName, device, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            endBaseUserOperations(user, userProvider);
            return baseCentre;
        }
    }
    
    /**
     * Applies the differences from 'differences centre' on top of 'target centre'.
     *
     * @param targetCentre
     * @param differencesCentre
     * @param root
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer applyDifferences(final ICentreDomainTreeManagerAndEnhancer targetCentre, final ICentreDomainTreeManagerAndEnhancer differencesCentre, final Class<AbstractEntity<?>> root) {
        logger.debug(format("\t\t%s centre...", "applyDifferences"));
        final DateTime start = new DateTime();
        
        final Class<?> diffManagedType = managedType(root, differencesCentre);
        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!isPlaceholder(property) && !propertyRemovedFromDomainType(diffManagedType, property)) {
                if (isDoubleCriterion(diffManagedType, property) && !isBooleanCriterion(diffManagedType, property)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(EXCLUSIVE, root, property)) {
                        targetCentre.getFirstTick().setExclusive(root, property, differencesCentre.getFirstTick().getExclusive(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(EXCLUSIVE2, root, property)) {
                        targetCentre.getFirstTick().setExclusive2(root, property, differencesCentre.getFirstTick().getExclusive2(root, property));
                    }
                }
                final Class<?> propertyType = isEmpty(property) ? diffManagedType : determinePropertyType(diffManagedType, property);
                if (isDate(propertyType)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(DATE_PREFIX, root, property)) {
                        targetCentre.getFirstTick().setDatePrefix(root, property, differencesCentre.getFirstTick().getDatePrefix(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(DATE_MNEMONIC, root, property)) {
                        targetCentre.getFirstTick().setDateMnemonic(root, property, differencesCentre.getFirstTick().getDateMnemonic(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(AND_BEFORE, root, property)) {
                        targetCentre.getFirstTick().setAndBefore(root, property, differencesCentre.getFirstTick().getAndBefore(root, property));
                    }
                }

                if (differencesCentre.getFirstTick().isMetaValuePresent(OR_NULL, root, property)) {
                    targetCentre.getFirstTick().setOrNull(root, property, differencesCentre.getFirstTick().getOrNull(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(NOT, root, property)) {
                    targetCentre.getFirstTick().setNot(root, property, differencesCentre.getFirstTick().getNot(root, property));
                }

                if (differencesCentre.getFirstTick().isMetaValuePresent(VALUE, root, property)) {
                    targetCentre.getFirstTick().setValue(root, property, differencesCentre.getFirstTick().getValue(root, property));
                }
                if (isDoubleCriterion(diffManagedType, property)
                        && differencesCentre.getFirstTick().isMetaValuePresent(VALUE2, root, property)) {
                    targetCentre.getFirstTick().setValue2(root, property, differencesCentre.getFirstTick().getValue2(root, property));
                }
            }
        }

        // Diff centre contains full information of checkedProperties and usedProperties.
        // Such information should be carefully merged with potentially updated targetCentre.
        final List<String> diffCheckedPropertiesWithoutSummaries = checkedPropertiesWithoutSummaries(differencesCentre.getSecondTick().checkedProperties(root), differencesCentre.getEnhancer().getManagedType(root));
        final List<String> diffUsedProperties = differencesCentre.getSecondTick().usedProperties(root);

        final List<String> targetCheckedPropertiesWithoutSummaries = checkedPropertiesWithoutSummaries(targetCentre.getSecondTick().checkedProperties(root), targetCentre.getEnhancer().getManagedType(root));

        // determine properties that were added into targetCentre (default config) comparing to differencesCentre (currently saved config)
        // final List<String> addedIntoTarget = minus(targetCheckedPropertiesWithoutSummaries, diffCheckedPropertiesWithoutSummaries);

        // determine properties that were removed from targetCentre (default config) comparing to differencesCentre (currently saved config)
        final List<String> removedFromTarget = minus(diffCheckedPropertiesWithoutSummaries, targetCheckedPropertiesWithoutSummaries);

        if (!equalsEx(diffCheckedPropertiesWithoutSummaries, diffUsedProperties)) {
            // remove removedFromTarget properties custom configuration (custom column order / visibility); this custom configuration was explicitly changed by the user, because it's different from diffCheckedProperties
            final List<String> diffUsedPropertiesWithoutRemovedProps = minus(diffUsedProperties, removedFromTarget);
            // apply resultant properties on top of targetCentre (default config)
            final List<String> targetUsedProperties = targetCentre.getSecondTick().usedProperties(root);
            for (final String targetUsedProperty: targetUsedProperties) { // remove (un-use) all previous props
                targetCentre.getSecondTick().use(root, targetUsedProperty, false);
            }
            for (final String newUsedProperty : diffUsedPropertiesWithoutRemovedProps) { // apply (use) all new props
                targetCentre.getSecondTick().use(root, newUsedProperty, true);
            }
        }

        // apply widths and grow factor that were marked as changed
        final List<String> diffCheckedPropertiesWithoutRemovedProps = minus(diffCheckedPropertiesWithoutSummaries, removedFromTarget);
        for (final String property : diffCheckedPropertiesWithoutRemovedProps) {
            if (!propertyRemovedFromDomainType(diffManagedType, property)) {
                if (differencesCentre.getFirstTick().isMetaValuePresent(WIDTH, root, property)) {
                    targetCentre.getSecondTick().setWidth(root, property, differencesCentre.getSecondTick().getWidth(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(GROW_FACTOR, root, property)) {
                    targetCentre.getSecondTick().setGrowFactor(root, property, differencesCentre.getSecondTick().getGrowFactor(root, property));
                }
            }
        }

        if (differencesCentre.getFirstTick().isMetaValuePresent(ALL_ORDERING, root, "")) {
            // need to clear all previous orderings:
            final List<Pair<String, Ordering>> orderedProperties = new ArrayList<>(targetCentre.getSecondTick().orderedProperties(root));
            for (final Pair<String, Ordering> orderedProperty: orderedProperties) {
                if (Ordering.ASCENDING == orderedProperty.getValue()) {
                    targetCentre.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
                }
                targetCentre.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
            }
            // and apply new ones from diff centre:
            final List<Pair<String, Ordering>> diffSortedPropertiesWithoutRemovedProps = minus(differencesCentre.getSecondTick().orderedProperties(root), removedFromTarget, propAndSorting -> propAndSorting.getKey());
            for (final Pair<String, Ordering> newOrderedProperty: diffSortedPropertiesWithoutRemovedProps) {
                final String property = newOrderedProperty.getKey();
                if (!propertyRemovedFromDomainType(diffManagedType, property)) {
                    targetCentre.getSecondTick().toggleOrdering(root, property);
                    if (Ordering.DESCENDING == newOrderedProperty.getValue()) {
                        targetCentre.getSecondTick().toggleOrdering(root, property);
                    }
                }
            }
        }
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t\t%s centre... done in [%s].", "applyDifferences", pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return targetCentre;
    }

    private static boolean propertyRemovedFromDomainType(final Class<?> diffManagedType, final String property) {
        // Check whether the 'property' has not been disappeared from domain type since last server restart.
        // In such case 'orderedProperties' will contain that property but 'managedType(root, differencesCentre)' will not contain corresponding field.
        // Such properties need to be silently ignored. During next diffCentre creation such properties will disappear from diffCentre fully.
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        if (!isEntityItself) {
            try {
                determinePropertyType(diffManagedType, property);
                return false;
            } catch (final Exception ex) {
                logger.warn(format("Property [%s] could not be found in type [%s] in diffCentre. It will be skipped. Most likely this property was deleted from domain type definition.", property, diffManagedType.getSimpleName()), ex);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Computes a list of items that contain in <code>from</code> list and do not contain in <code>to</code> list, preserving the order of <code>from</code> list inside resultant list.
     *
     * @param from
     * @param to
     * @return
     */
    private static List<String> minus(final List<String> from, final List<String> to) {
        return minus(from, to, Function.identity());
    }

    /**
     * Computes a list of items that contain in <code>from</code> list and their keys do not contain in <code>to</code> list, preserving the order of <code>from</code> list inside resultant list.
     *
     * @param from
     * @param to
     * @param keyRetriever -- mapping function to retrieve the key of the item
     * @return
     */
    private static <T> List<T> minus(final List<T> from, final List<String> to, final Function<T, String> keyRetriever) {
        final List<T> result = new ArrayList<>();
        for (final T fromItem: from) {
            if (!to.contains(keyRetriever.apply(fromItem))) {
                result.add(fromItem);
            }
        }
        return result;
    }

    /**
     * Creates 'diff centre' from 'centre' and 'originalCentre' with marked meta-values (only those that are different).
     *
     * @param centre
     * @param originalCentre
     * @param root
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer createDifferencesCentre(final ICentreDomainTreeManagerAndEnhancer centre, final ICentreDomainTreeManagerAndEnhancer originalCentre, final Class<AbstractEntity<?>> root, final ISerialiser serialiser) {
        logger.debug(format("\t%s centre...", "createDifferencesCentre"));
        final DateTime start = new DateTime();
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = copyCentre(centre, serialiser);
        
        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!isPlaceholder(property)) {
                if (isDoubleCriterion(managedType(root, differencesCentre), property) && !isBooleanCriterion(managedType(root, differencesCentre), property)) {
                    if (!equalsEx(differencesCentre.getFirstTick().getExclusive(root, property), originalCentre.getFirstTick().getExclusive(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(EXCLUSIVE, root, property);
                    }
                    if (!equalsEx(differencesCentre.getFirstTick().getExclusive2(root, property), originalCentre.getFirstTick().getExclusive2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(EXCLUSIVE2, root, property);
                    }
                }
                final Class<?> propertyType = isEmpty(property) ? managedType(root, differencesCentre) : determinePropertyType(managedType(root, differencesCentre), property);
                if (isDate(propertyType)) {
                    if (!equalsEx(differencesCentre.getFirstTick().getDatePrefix(root, property), originalCentre.getFirstTick().getDatePrefix(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(DATE_PREFIX, root, property);
                    }
                    if (!equalsEx(differencesCentre.getFirstTick().getDateMnemonic(root, property), originalCentre.getFirstTick().getDateMnemonic(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(DATE_MNEMONIC, root, property);
                    }
                    if (!equalsEx(differencesCentre.getFirstTick().getAndBefore(root, property), originalCentre.getFirstTick().getAndBefore(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(AND_BEFORE, root, property);
                    }
                }
                
                if (!equalsEx(differencesCentre.getFirstTick().getOrNull(root, property), originalCentre.getFirstTick().getOrNull(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(OR_NULL, root, property);
                }
                if (!equalsEx(differencesCentre.getFirstTick().getNot(root, property), originalCentre.getFirstTick().getNot(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(NOT, root, property);
                }
                
                if (!equalsEx(differencesCentre.getFirstTick().getValue(root, property), originalCentre.getFirstTick().getValue(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(VALUE, root, property);
                }
                if (isDoubleCriterion(managedType(root, differencesCentre), property)) {
                    if (!equalsEx(differencesCentre.getFirstTick().getValue2(root, property), originalCentre.getFirstTick().getValue2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(VALUE2, root, property);
                    }
                }
            }
        }
        
        // extract widths that are changed and mark them
        for (final String property : differencesCentre.getSecondTick().checkedProperties(root)) {
            if (!equalsEx(differencesCentre.getSecondTick().getWidth(root, property), originalCentre.getSecondTick().getWidth(root, property))) {
                differencesCentre.getFirstTick().markMetaValuePresent(WIDTH, root, property);
            }
            if (!equalsEx(differencesCentre.getSecondTick().getGrowFactor(root, property), originalCentre.getSecondTick().getGrowFactor(root, property))) {
                differencesCentre.getFirstTick().markMetaValuePresent(GROW_FACTOR, root, property);
            }
        }
        
        // need to determine whether orderedProperties have been changed (as a whole) and mark diff centre if true:
        if (!equalsEx(differencesCentre.getSecondTick().orderedProperties(root), originalCentre.getSecondTick().orderedProperties(root))) {
            differencesCentre.getFirstTick().markMetaValuePresent(ALL_ORDERING, root, "");
        }
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t%s centre... done in [%s].", "createDifferencesCentre", pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return differencesCentre;
    }

    /**
     * Applies correct default values to be in sync with Web UI ones on top of 'centre'.
     * <p>
     * Currently, only String-typed properties in Web UI have different default values (<code>null</code> instead of ""). This method traverses all String-typed properties and
     * provides correct default values for them.
     * <p>
     * Please, refer to method {@link DynamicQueryBuilder#getEmptyValue(Class, boolean)} for more details on standard default values.
     *
     * @param centre
     * @param root
     */
    private static ICentreDomainTreeManagerAndEnhancer applyWebUIDefaultValues(final ICentreDomainTreeManagerAndEnhancer centre, final Class<AbstractEntity<?>> root) {
        for (final String includedProperty : centre.getRepresentation().includedProperties(root)) {
            if (!isDummyMarker(includedProperty)) {
                final String property = reflectionProperty(includedProperty);
                final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType(root, centre) : determinePropertyType(managedType(root, centre), property);

                if (isString(propertyType)) {
                    centre.getRepresentation().getFirstTick().setValueByDefault(root, includedProperty, null);
                }
            }
        }
        return centre;
    }

    /**
     * Returns the 'managed type' for the 'centre' manager.
     *
     * @param root
     * @param centre
     * @return
     */
    protected static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getEnhancer().getManagedType(root);
    }

    /**
     * Overrides old 'differences centre' with new one and saves it if it was changed from previous 'differences centre'.
     *
     * @param user
     * @param miType
     * @param deviceSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param newDiffCentre
     *
     * @return
     */
    private static void overrideAndSaveDifferencesCentreIfChanged(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer newDiffCentre,
            final String newDesc,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        logger.debug(format("\t%s '%s' centre for miType [%s] for user %s...", "overrideAndSaveDifferencesCentre", deviceSpecificName, miType.getSimpleName(), user));
        final DateTime start = new DateTime();
        
        // the name consists of 'deviceSpecificName' and 'DIFFERENCES_SUFFIX'
        final String deviceSpecificDiffName = deviceSpecificName + DIFFERENCES_SUFFIX;
        
        saveEntityCentreManager(newDiffCentre, miType, user, deviceSpecificDiffName, newDesc, serialiser, eccCompanion, mmiCompanion);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "overrideAndSaveDifferencesCentre", deviceSpecificName, miType.getSimpleName(), user, pd.getSeconds() + " s " + pd.getMillis() + " ms"));
    }
    
}