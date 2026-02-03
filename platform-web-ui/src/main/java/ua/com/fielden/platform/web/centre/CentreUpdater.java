package ua.com.fielden.platform.web.centre;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangePrefixEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.MnemonicEnum;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.*;
import static java.util.function.Function.identity;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.*;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.valueOf;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isLink;
import static ua.com.fielden.platform.web.centre.CentreUpdater.MetaValueType.*;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.*;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.*;

/// Represents a set of utility methods for updating / committing of surrogate centres, for e.g. 'fresh', 'previouslyRun' etc.
///
/// Every surrogate centre has its own diff centre that is saved into the database during
/// [#commitCentreWithoutConflicts(User, Class, String, Optional, DeviceProfile, ICentreDomainTreeManagerAndEnhancer, String, IWebUiConfig, EntityCentreConfigCo, MainMenuItemCo, IUser)]
/// and
/// [#commitCentreDiffWithoutConflicts(User, Class, String, Optional, DeviceProfile, ICentreDomainTreeManagerAndEnhancer, Map, String, EntityCentreConfigCo, MainMenuItemCo, ICompanionObjectFinder, Function)].
///
public class CentreUpdater {
    private static final Logger logger = getLogger(CentreUpdater.class);
    
    /**
     * Message that marks own save-as configuration when it was inherited from base / shared but was disconnected; this occurs if upstream config is deleted, base upstream config is renamed or shared upstream config is made unshared.
     */
    private static final String UPSTREAM_CONFIG_DISCONNECTED_MESSAGE = "<i>was shared, removed by user</i>";
    
    private static final String DIFFERENCES_SUFFIX = "__________DIFFERENCES";
    
    public static final String FRESH_CENTRE_NAME = "__________FRESH";
    public static final String PREVIOUSLY_RUN_CENTRE_NAME = "__________PREVIOUSLY_RUN";
    public static final String SAVED_CENTRE_NAME = "__________SAVED";
    
    /**
     * A type of meta values.
     *
     * @author TG Team
     *
     */
    enum MetaValueType {
        VALUE, VALUE2, EXCLUSIVE, EXCLUSIVE2, OR_NULL, NOT, OR_GROUP, DATE_PREFIX, DATE_MNEMONIC, AND_BEFORE, WIDTH, GROW_FACTOR, AUTOCOMPLETE_ACTIVE_ONLY
    }
    
    /**
     * Granular property-based key of diff values those include values pertaining to both selection-criteria (value and value2, exclusive, date mnemonics etc.) and result-set (column widths, grow-factors etc.). This key always exists.
     */
    private static final String PROPERTIES = "PROPERTIES";
    /**
     * Key of diff pertaining to result-set column visibility and order. Contains all snapshot and exists only if visibility / order is changed.
     */
    private static final String VISIBILITY_AND_ORDER = "VISIBILITY_AND_ORDER";
    /**
     * Key of diff pertaining to result-set sorting. Result-set can be sorted by 'invisible' columns. Contains all snapshot and exists only if sorting is changed.
     */
    private static final String SORTING = "SORTING";
    /**
     * Key of diff pertaining to result-set page capacity.
     */
    private static final String PAGE_CAPACITY = "PAGE_CAPACITY";
    /**
     * Key of diff pertaining to result-set visible rows count.
     */
    private static final String VISIBLE_ROWS_COUNT = "VISIBLE_ROWS_COUNT";
    /**
     * Key of diff pertaining to result-set number of header lines.
     */
    private static final String NUMBER_OF_HEADER_LINES = "NUMBER_OF_HEADER_LINES";
    
    /**
     * Key of diff pertaining to entity centre's preferred view.
     */
    private static final String PREFERRED_VIEW = "PREFERRED_VIEW";
    
    /* Following functions are used for conversion of criteria values to / from strings. Some implementations could have been used directly but left here for clarity and consistency. */
    static final String ID_PREFIX = "__________ID__________";
    private static final Function<DateRangePrefixEnum, String> DATE_PREFIX_TO_STRING = DateRangePrefixEnum::name;
    private static final Function<String, DateRangePrefixEnum> STRING_TO_DATE_PREFIX = DateRangePrefixEnum::valueOf;
    private static final Function<MnemonicEnum, String> DATE_MNEMONIC_TO_STRING = MnemonicEnum::name;
    private static final Function<String, MnemonicEnum> STRING_TO_DATE_MNEMONIC = MnemonicEnum::valueOf;
    private static final Function<Long, Date> LONG_TO_DATE = Date::new;
    private static final Function<Date, Long> DATE_TO_LONG = Date::getTime;
    private static final Function<Object, String> TO_STRING = Object::toString;
    private static final Function<String, Integer> STRING_TO_INTEGER = Integer::valueOf;
    private static final Function<String, Long> STRING_TO_LONG = Long::valueOf;
    private static final Function<String, BigDecimal> STRING_TO_BIG_DECIMAL = BigDecimal::new;
    private static final Function<String, Currency> STRING_TO_CURRENCY = Currency::getInstance;
    private static final Function<Money, Map<String, Object>> MONEY_TO_MAP = money ->
        money.getTaxPercent() != null
            ? mapOf(t2("amount", TO_STRING.apply(money.getAmount())), t2("taxPercent", TO_STRING.apply(money.getTaxPercent())), t2("currency", TO_STRING.apply(money.getCurrency())))
            : mapOf(t2("amount", TO_STRING.apply(money.getAmount())), t2("currency", TO_STRING.apply(money.getCurrency())));
    private static final Function<Map<String, Object>, Money> MAP_TO_MONEY = map -> 
        map.containsKey("taxPercent") 
            ? new Money(STRING_TO_BIG_DECIMAL.apply((String) map.get("amount")), STRING_TO_INTEGER.apply((String) map.get("taxPercent")), STRING_TO_CURRENCY.apply((String) map.get("currency")))
            : new Money(STRING_TO_BIG_DECIMAL.apply((String) map.get("amount")), STRING_TO_CURRENCY.apply((String) map.get("currency")));
    private static final Function<AbstractEntity<?>, String> ENTITY_TO_STRING = entity -> entityWithMocksToString((ent) -> {
        if (isPersistentEntityType(ent.getType()) || isSyntheticBasedOnPersistentEntityType(ent.getType()) || isUnionEntityType(ent.getType())) {
            if (ent.getId() == null) {
                // Usually persistent (or synthetic based on persistent) entities should have IDs when conversion to diff object is performed.
                // However we have two edge-cases here.
                // The first edge-case is where old configurations convert to new ones:
                //     old 'not found mocks' are not recognised (in method entityWithMocksToString above) and fall into category as UNKNOWN not found mock.
                // Other edge-case is much more important: consider @SkipEntityExists property that creates ad-hoc inside overridden findByKeyAndFetch method.
                //     We need to treat this entity as new by converting its key toString and saving it in diff object. When diff object is used for centre restoration,
                //     this string will be passed back to findByKeyAndFetch method and, again, the entity will be created ad-hoc (convertFrom method).
                try {
                    final String entString = ent.toString();
                    if (!ent.isIdOnlyProxy() && !KEY_NOT_ASSIGNED.equals(entString)) {
                        return entString;
                    }
                } catch (final Exception ex) {
                    logger.warn("Ad-hoc created entity with empty ID, that is used in crit-only single criterion, can not be converted to string. UNKNOWN 'not found mock' will be used.", ex);
                }
                return createNotFoundMockString("UNKNOWN");
            }
            return ID_PREFIX + ent.getId().toString();
        } else {
            return ent.getKey().toString();
        }
    }, entity);
    /**
     * Function to get title of surrogate configuration from surrogate name, save-as name and device.
     */
    public static final Function<String, Function<Optional<String>, Function<DeviceProfile, String>>> NAME_OF = surrogateName -> saveAs -> device -> deviceSpecific(saveAsSpecific(surrogateName, saveAs), device) + DIFFERENCES_SUFFIX;
    public static final Function<String, Function<DeviceProfile, String>> PREFIX_OF = surrogateName -> device -> deviceSpecific(surrogateName, device) + "[%";

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
     * Centre on its own is never saved, but it is used to create 'differences' (when committing is performed).
     *
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.);
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param companionFinder
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer updateCentre(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(name, saveAsName), device);
        final Map<String, Object> updatedDiff = updateDifferences(miType, user, deviceSpecificName, name, saveAsName, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        return loadCentreFromDefaultAndDiff(miType, updatedDiff, webUiConfig, companionFinder);
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
    public static String updateCentreDesc(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final EntityCentreConfigCo eccCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device);
        final EntityCentreConfig eccWithDesc = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
        return eccWithDesc == null ? null : eccWithDesc.getDesc();
    }
    
    /**
     * Updates (retrieves) current version of centre dashboardable indicator.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static boolean updateCentreDashboardable(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final EntityCentreConfigCo eccCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device);
        final EntityCentreConfig config = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
        return config != null && config.isDashboardable();
    }
    
    /**
     * Updates (retrieves) current version of centre runAutomatically.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param webUiConfig -- only specify in case where there is possibility that FRESH configuration does not exist and default Centre DSL value for runAutomatically should be taken
     * @param selectionCrit -- only specify in case where there is possibility that configuration is inherited; if not specified, inheritness will not even be checked
     * @return
     */
    public static boolean updateCentreRunAutomatically(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final EntityCentreConfigCo eccCompanion, final IWebUiConfig webUiConfig, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        if (isLink(saveAsName)) { // link
            return true;
        }
        final Function<User, Function<Optional<String>, Boolean>> calcRunAutomaticallyFor = customUser -> customSaveAsName -> {
            final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, customSaveAsName), device);
            final EntityCentreConfig config = findConfig(miType, customUser, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
            return config != null ? config.isRunAutomatically() : webUiConfig != null ? defaultRunAutomatically(miType, webUiConfig) : false;
        };
        if (!saveAsName.isPresent()) {
            return calcRunAutomaticallyFor.apply(user).apply(saveAsName); // default
        }
        if (!user.isBase() && selectionCrit != null) {
            final Optional<LoadableCentreConfig> loadableConfigOpt = findLoadableConfig(saveAsName, selectionCrit);
            if (loadableConfigOpt.isPresent() && loadableConfigOpt.get().isInherited()) {
                if (loadableConfigOpt.get().isBase()) { // inherited from base
                    return calcRunAutomaticallyFor.apply(user.getBasedOnUser()).apply(saveAsName);
                } else { // inherited from shared
                    return calcRunAutomaticallyFor.apply(loadableConfigOpt.get().getSharedBy()).apply(of(loadableConfigOpt.get().getSaveAsName()));
                }
            }
        }
        return calcRunAutomaticallyFor.apply(user).apply(saveAsName); // own save-as (including orphaned)
    }

    /**
     * Updates (retrieves) current version of centre dashboard refresh frequency.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static DashboardRefreshFrequency updateCentreDashboardRefreshFrequency(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final EntityCentreConfigCo eccCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device);
        final EntityCentreConfig config = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
        return config != null ? config.getDashboardRefreshFrequency() : null;
    }
    
    /**
     * Updates (retrieves) current version of centre uuid.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @return
     */
    public static Optional<String> updateCentreConfigUuid(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final EntityCentreConfigCo eccCompanion) {
        return saveAsName.map(name -> {
            final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, of(name)), device);
            final EntityCentreConfig eccWithDesc = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
            return eccWithDesc == null ? null : eccWithDesc.getConfigUuid();
        });
    }
    
    /**
     * Changes configuration title to <code>newTitle</code> and description to <code>newDesc</code> and saves these changes to persistent storage.
     * 
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param newTitle -- new title for configuration (aka 'saveAsName')
     * @param newDashboardable -- parameter indicating whether edited centre configuration should be present on a dashboard
     * @param newDashboardRefreshFrequency -- refresh frequency for edited centre configuration on a dashboard
     * @param newDesc -- new description for configuration
     */
    public static void editCentreTitleAndDesc(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final String newTitle,
            final String newDesc,
            final boolean newDashboardable,
            final DashboardRefreshFrequency newDashboardRefreshFrequency,
            final EntityCentreConfigCo eccCompanion) {
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
        // newDesc / newDashboardable / newDashboardRefreshFrequency
        freshConfig.setDesc(newDesc);
        freshConfig.setDashboardable(newDashboardable);
        freshConfig.setDashboardRefreshFrequency(newDashboardRefreshFrequency);
        
        // clear all centres with the same name in the case where title has been changed -- new title potentially can be in conflict with another configuration and that another configuration should be deleted
        if (!equalsEx(saveAsName, of(newTitle))) {
            CentreUpdaterUtils.removeCentres(user, miType, eccCompanion, freshConfig.getTitle(), savedConfig.getTitle(), previouslyRunNewTitle);
        }
        
        // save
        eccCompanion.saveWithRetry(freshConfig); // editCentreTitleAndDesc is not used inside other transaction scopes (i.e. CentreConfigEditActionDao.performSave and AbstractCentreConfigCommitActionDao.save do not have @SessionRequired) -- saveWithRetry can be used
        eccCompanion.saveWithRetry(savedConfig);
        if (previouslyRunConfig != null) { // previouslyRun centre may not exist
            eccCompanion.saveWithRetry(previouslyRunConfig);
        }
    }
    
    /**
     * Changes configuration's runAutomatically and saves these changes to persistent storage.
     * 
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param newRunAutomatically -- new runAutomatically for configuration
     */
    public static void configureCentre(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final boolean newRunAutomatically,
            final EntityCentreConfigCo eccCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device);
        final EntityCentreConfig freshConfig = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX , eccCompanion);
        if (freshConfig != null) {
            freshConfig.setRunAutomatically(newRunAutomatically);
            eccCompanion.saveWithRetry(freshConfig); // configureCentre is not used inside other transaction scopes (i.e. CentreConfigConfigureActionDao.save has no @SessionRequired) -- saveWithRetry can be used
        }
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
    public static void removeCentres(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final Optional<String> saveAsName, final EntityCentreConfigCo eccCompanion, final String ... names) {
        // remove corresponding diff centre instances from persistent storage
        final String[] deviceSpecificDiffNames = stream(names).map(name -> deviceSpecific(saveAsSpecific(name, saveAsName), device) + DIFFERENCES_SUFFIX).toArray(String[]::new);
        CentreUpdaterUtils.removeCentres(user, miType, eccCompanion, deviceSpecificDiffNames);
    }
    
    /**
     * Initialises and commits centre from the passed <code>centreToBeInitialisedAndCommitted</code> instance for surrogate centre with concrete <code>name</code>.
     * <p>
     * Please note that this operation is immutable in regard to the surrogate centre instance being copied.
     * <p>
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param centre -- the centre manager to commit
     * @param newDesc -- new description to be saved into persistent storage
     */
    public static ICentreDomainTreeManagerAndEnhancer commitCentreWithoutConflicts(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final String newDesc,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(name, saveAsName), device);
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, webUiConfig);
        // override old 'diff' with recently created one and save it
        saveEntityCentreManager(createDifferences(centre, defaultCentre, getEntityType(miType)), miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, newDesc, eccCompanion, mmiCompanion, identity());
        return centre;
    }
    
    /**
     * Commits centre from the passed {@code diff} object for surrogate centre with concrete {@code name}. Constructs {@link ICentreDomainTreeManagerAndEnhancer} from that {@code diff}.
     * <p>
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param defaultCentre -- centre instance to be used for constructing desired centre manager from {@code diff} object
     * @param diff -- differences object being committed (diffs comparing to default centre)
     * @param newDesc -- new description to be saved into persistent storage
     * @param adjustConfig - function to adjust centre configuration ({@link EntityCentreConfig}) before save
     */
    public static ICentreDomainTreeManagerAndEnhancer commitCentreDiffWithoutConflicts(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer defaultCentre,
            final Map<String, Object> diff,
            final String newDesc,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final ICompanionObjectFinder companionFinder,
            final Function<EntityCentreConfig, EntityCentreConfig> adjustConfig) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(name, saveAsName), device);
        // override old 'diff' with recently created one and save it
        saveEntityCentreManager(diff, miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, newDesc, eccCompanion, mmiCompanion, adjustConfig);
        return applyDifferences(defaultCentre, diff, getEntityType(miType), companionFinder);
    }
    
    /**
     * Finds loadable configurations for current user and specified <code>miType; device</code>.
     * {@link LoadableCentreConfig} instances are sorted by title.
     * Inherited configurations receive appropriate {@link LoadableCentreConfig#isInherited()} flag.
     * Inherited from shared configurations receive appropriate {@link LoadableCentreConfig#getSharedBy()} user.
     * <p>
     * Please note that inheritance from base is purely defined by 'saveAsName' -- if both configuration for user and its base user have the same 'saveAsName' then they are in inheritance relationship.
     * 
     * @param user
     * @param miType
     * @param device -- device profile (mobile or desktop) for which loadable centres
     * @param companionFinder
     * @return
     */
    public static Function<Optional<Optional<String>>, List<LoadableCentreConfig>> loadableConfigurations(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final DeviceProfile device,
            final ICompanionObjectFinder companionFinder,
            final ICentreConfigSharingModel sharingModel) {
        return saveAsNameOpt -> {
            final List<LoadableCentreConfig> loadableConfigurations = new ArrayList<>();
            
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final LoadableCentreConfigCo lccCompanion = companionFinder.find(LoadableCentreConfig.class);
            
            final String surrogateNamePrefix = deviceSpecific(FRESH_CENTRE_NAME, device);
            final EntityResultQueryModel<EntityCentreConfig> queryForCurrentUser = findConfigsFunction(user, miType, device, eccCompanion).apply(saveAsNameOpt);
            final fetch<EntityCentreConfig> fetch = FETCH_CONFIG.with("configUuid");
            if (user.isBase()) {
                try (final Stream<EntityCentreConfig> stream = eccCompanion.stream(from(queryForCurrentUser).with(fetch).model()) ) {
                    stream.forEach(ecc -> {
                        loadableConfigurations.add(createLoadableCentreConfig(ecc, false, surrogateNamePrefix, lccCompanion));
                    });
                    loadableConfigurations.remove(new LoadableCentreConfig().setKey(LINK_CONFIG_TITLE)); // exclude 'link' configuration from load dialog if it is present (aka centre 'link' with criteria parameters was loaded at least once)
                }
            } else {
                final EntityResultQueryModel<EntityCentreConfig> queryForBaseUser = findConfigsFunction(user.getBasedOnUser(), miType, device, eccCompanion).apply(saveAsNameOpt);
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
                    loadableConfigurations.remove(new LoadableCentreConfig().setKey(LINK_CONFIG_TITLE)); // exclude 'link' configuration from load dialog if it is present (aka centre 'link' with criteria parameters was loaded at least once)
                    // collect uuids for not inherited from base configurations, aka own save-as configs or inherited from shared
                    final Set<String> notInheritedFromBaseUuids = loadableConfigurations.stream()
                        .filter(lcc -> lcc != null && !lcc.isInherited() && lcc.getConfig() != null)
                        .map(lcc -> lcc.getConfig().getConfigUuid())
                        .collect(toSet());
                    
                    // find config creators not being equal to current user...
                    if (!notInheritedFromBaseUuids.isEmpty()) {
                        eccCompanion.getAllEntities(
                            from(centreConfigQueryFor(miType, device, SAVED_CENTRE_NAME)
                                .and().prop("configUuid").in().values(notInheritedFromBaseUuids.toArray())
                                .and().prop("owner").ne().val(user)
                                .and().begin() // we look only for shared configs; base user could have changed the title of base config already loaded by current user; so we need to look for ...
                                    .prop("owner.base").eq().val(false) // ... owners that are not base users ...
                                    .or().prop("owner").ne().val(user.getBasedOnUser()) // ... or base users but not base for current user
                                .end().model()
                            )
                            .with(FETCH_CONFIG.with("configUuid").with("owner", fetch(User.class).with("key")))
                            .lightweight().model()
                        ).stream()
                        .forEach(ecc -> {
                            if (sharingModel.isSharedWith(ecc.getConfigUuid(), user).isSuccessful()) {
                                final LoadableCentreConfig foundLcc = loadableConfigurations.stream().filter(lcc -> lcc.getConfig() != null && ecc.getConfigUuid().equals(lcc.getConfig().getConfigUuid())).findAny().get();
                                foundLcc.setInherited(true); // ... and make corresponding configuration inherited (from shared) ...
                                foundLcc.setSharedByMessage(sharingModel.sharedByMessage(ecc.getOwner())); // ... with appropriate domain-specific message indication about that
                                foundLcc.setSharedBy(ecc.getOwner());
                                foundLcc.setSaveAsName(obtainTitleFrom(ecc.getTitle(), SAVED_CENTRE_NAME, device));
                            }
                        });
                    }
                    
                    // Mark own save-as configurations as orphaned for those being disconnected from base / shared (not created by current 'user'):
                    
                    // function for getting own save-as LoadableCentreConfigs
                    final Supplier<Stream<LoadableCentreConfig>> ownLoadableConfigurationsSupplier = () -> loadableConfigurations.stream()
                        .filter(lcc -> !lcc.isInherited()); // all non-inherited configurations, that were left after shared / base processing, are considered own save-as
                    
                    // collect uuids for own save-as configurations
                    final Set<String> ownSaveAsUuids = ownLoadableConfigurationsSupplier.get()
                        .filter(lcc -> lcc != null && lcc.getConfig() != null)
                        .map(lcc -> lcc.getConfig().getConfigUuid()) // all loadable configs always have configUuids present
                        .collect(toSet());
                    
                    if (!ownSaveAsUuids.isEmpty()) {
                        // find config creators for that uuids
                        final List<EntityCentreConfig> savedConfigsWithCreators = eccCompanion.getAllEntities(
                            from(centreConfigQueryFor(miType, device, SAVED_CENTRE_NAME)
                                .and().prop("configUuid").in().values(ownSaveAsUuids.toArray()).model()
                            )
                            .with(FETCH_CONFIG.with("configUuid").with("owner", fetch(User.class).with("key")))
                            .lightweight().model()
                        );
                        
                        // iterate through own loadable configurations and ...
                        ownLoadableConfigurationsSupplier.get().forEach(lcc -> {
                            final Optional<EntityCentreConfig> creatorConfigOpt = savedConfigsWithCreators.stream()
                                .filter(savedConfig -> lcc.getConfig() != null && savedConfig.getConfigUuid().equals(lcc.getConfig().getConfigUuid())) // savedConfig.getConfigUuid() always present, also lcc.getConfig() / lcc.getConfig().getConfigUuid() are always present too
                                .findAny();
                            if (!creatorConfigOpt.isPresent()) { // ... mark orphaned due to upstream config deleted (either base or shared)
                                lcc.setOrphanedSharingMessage(UPSTREAM_CONFIG_DISCONNECTED_MESSAGE);
                            } else {
                                final User creator = creatorConfigOpt.get().getOwner();
                                if (!areEqual(user, creator)) { // (consider only not own save-as)
                                    lcc.setOrphanedSharingMessage(UPSTREAM_CONFIG_DISCONNECTED_MESSAGE); // ... mark as orphaned from shared / based configuration
                                }
                            }
                        });
                    }
                    
                }
            }
            Collections.sort(loadableConfigurations);
            return loadableConfigurations;
        };
    }

    /**
     * Creates function for loading of FRESH configs -- either all or one based on function argument.
     */
    private static Function<Optional<Optional<String>>, EntityResultQueryModel<EntityCentreConfig>> findConfigsFunction(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final EntityCentreConfigCo eccCompanion) {
        return saveAsNameOpt -> {
            return saveAsNameOpt
                .map(saveAsName -> modelFor(user, miType.getName(), NAME_OF.apply(FRESH_CENTRE_NAME).apply(saveAsName).apply(device)))
                .orElseGet(() -> centreConfigQueryFor(user, miType, device, FRESH_CENTRE_NAME).model());
        };
    }
    
    /**
     * Returns {@link List} of preferred {@link EntityCentreConfig} configurations for specified {@code user}, {@code device} and concrete
     * {@code miType}'ed menu item.
     * <p>
     * Please note that by design this list should return single or none instance.
     * 
     * @param user
     * @param miType
     * @param device
     * @return
     */
    private static List<EntityCentreConfig> getAllPreferredConfigs(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final ICompanionObjectFinder companionFinder) {
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final EntityResultQueryModel<EntityCentreConfig> queryForCurrentUser = centreConfigQueryFor(user, miType, device, FRESH_CENTRE_NAME)
            .and().prop("preferred").eq().val(true).model();
        final fetch<EntityCentreConfig> fetch = fetchWithKeyAndDesc(EntityCentreConfig.class).with("preferred").fetchModel();
        return eccCompanion.getAllEntities(from(queryForCurrentUser).with(fetch).model());
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
    public static Optional<String> retrievePreferredConfigName(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final ICompanionObjectFinder companionFinder, final IWebUiConfig webUiConfig) {
        if (webUiConfig.isEmbeddedCentreAndNotAllowCustomised(miType)) {
            return empty();
        }
        final String surrogateNamePrefix = deviceSpecific(FRESH_CENTRE_NAME, device);
        final List<EntityCentreConfig> prefConfigs = getAllPreferredConfigs(user, miType, device, companionFinder);
        return prefConfigs.stream().findAny().map(ecc -> obtainTitleFrom(ecc.getTitle(), surrogateNamePrefix));
    }
    
    /**
     * Makes {@code saveAsName}d configuration preferred for {@code user}, {@code device} and concrete {@code miType}'ed menu item.
     * <p>
     * Does nothing for embedded centres. This means that default configurations will always be preferred for them.
     * 
     * @param user
     * @param miType
     * @param saveAsName
     * @param device
     * @return
     */
    public static void makePreferred(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final ICompanionObjectFinder companionFinder, final IWebUiConfig webUiConfig) {
        if (!webUiConfig.isEmbeddedCentreAndNotAllowCustomised(miType)) { // standalone centres only, not embedded
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final List<EntityCentreConfig> prefConfigs = getAllPreferredConfigs(user, miType, device, companionFinder);
            prefConfigs.stream().forEach(ecc -> eccCompanion.saveWithRetry(ecc.setPreferred(false)));
            if (saveAsName.isPresent()) {
                findConfigOpt(
                    miType,
                    user,
                    deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device) + DIFFERENCES_SUFFIX,
                    eccCompanion,
                    fetchWithKeyAndDesc(EntityCentreConfig.class, true).with("preferred").with("configUuid").with("dashboardable").with("dashboardableDate").with("dashboardRefreshFrequency").with("runAutomatically").fetchModel()
                ).ifPresent(ecc ->
                    eccCompanion.saveWithRetry( // not used inside other transaction scopes (e.g. CentreConfigLoadActionDao->makePreferredConfig->makePreferred does not have @SessionRequired) -- saveWithRetry can be used
                        ecc.setPreferred(true)
                    )
                );
            }
        }
    }
    
    /**
     * Creates {@link LoadableCentreConfig} instance from <code>ecc</code>'s title and description.
     * 
     * @param ecc -- centre config entity with 'title', 'desc' and 'configUuid' fetched
     * @param inherited -- indicates whether {@link LoadableCentreConfig} instance being created needs to be 'inherited'
     * @param surrogateNamePrefix
     * @param lccCompanion
     * @return
     */
    private static LoadableCentreConfig createLoadableCentreConfig(final EntityCentreConfig ecc, final boolean inherited, final String surrogateNamePrefix, final LoadableCentreConfigCo lccCompanion) {
        final LoadableCentreConfig lcc = lccCompanion.new_();
        lcc.setInherited(inherited).setConfig(ecc).setKey(obtainTitleFrom(ecc.getTitle(), surrogateNamePrefix)).setDesc(ecc.getDesc());
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
     * Receives actual title from surrogate name persisted inside {@link EntityCentreConfig#getTitle()}.
     * 
     * @param title
     * @param surrogateName
     * @param device
     * @return
     */
    public static String obtainTitleFrom(final String title, final String surrogateName, final DeviceProfile device) {
        return obtainTitleFrom(title, deviceSpecific(surrogateName, device));
    }
    
    /**
     * Receives actual title from surrogate name persisted inside {@link EntityCentreConfig#getTitle()}.
     * 
     * @param title
     * @param surrogateNamePrefix
     * @return
     */
    private static String obtainTitleFrom(final String title, final String surrogateNamePrefix) {
        final String surrogateWithSuffix = title.replaceFirst(surrogateNamePrefix, "");
        return surrogateWithSuffix.substring(1, surrogateWithSuffix.lastIndexOf("]"));
    }

    private static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final DeviceProfile device, final String surrogateName) {
        return select(EntityCentreConfig.class)
            .where().prop("title").like().val(PREFIX_OF.apply(surrogateName).apply(device))
            .and().prop("title").notLike().val(PREFIX_OF.apply(surrogateName).apply(opposite(device)));
    }

    public static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final String uuid, final DeviceProfile device, final String surrogateName) {
        return centreConfigQueryFor(device, surrogateName)
            .and().condition(centreConfigCondFor(uuid));
    }

    public static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final String uuid, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final String surrogateName) {
        return centreConfigQueryFor(uuid, device, surrogateName)
            .and().condition(centreConfigCondFor(miType));
    }

    public static ConditionModel centreConfigCondFor(final String uuid) {
        return cond().prop("configUuid").eq().val(uuid).model();
    }

    /**
     * Creates a function that returns a query to find centre configurations persisted.
     * <p>
     * Looks only for named / link configurations, default configurations are avoided.
     * 
     * @param miType
     * @param device -- the device for which centre configurations are looked for
     * @param surrogateName -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @return
     */
    static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final String surrogateName) {
        return centreConfigQueryFor(device, surrogateName)
            .and().condition(centreConfigCondFor(miType)) ;
    }

    private static ConditionModel centreConfigCondFor(Class<? extends MiWithConfigurationSupport<?>> miType) {
        return cond().prop("menuItem.key").eq().val(miType.getName()).model();
    }

    /**
     * Creates a function that returns a query to find centre configurations persisted for <code>user</code>.
     * <p>
     * Looks only for named / link configurations, default configurations are avoided.
     * 
     * @param user
     * @param miType
     * @param device -- the device for which centre configurations are looked for
     * @param surrogateName -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @return
     */
    static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final String surrogateName) {
        return centreConfigQueryFor(miType, device, surrogateName)
            .and().condition(centreConfigCondFor(user));
    }

    static ConditionModel centreConfigCondFor(final User user) {
        return cond().prop("owner").eq().val(user).model();
    }
    
    /**
     * Loads centre through the following chain: 'default centre' + 'differences' := 'centre'.
     *
     * @param miType
     * @param updatedDiff -- updated differences
     * @param companionFinder
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Map<String, Object> updatedDiff,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder) {
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, webUiConfig);
        // applies diffCentre on top of defaultCentre to produce loadedCentre:
        return applyDifferences(defaultCentre, updatedDiff, getEntityType(miType), companionFinder);
    }
    
    /**
     * Creates user-specific (!) default centre manager from Centre DSL configuration.
     * <p>
     * IMPORTANT: this 'default centre' is used for constructing 'fresh centre', 'previouslyRun centre' and their 'diff centres', that is why it is very important to make it suitable for Web UI default values.
     * All other centres will reuse such Web UI specific default values.
     * <p>
     * Please note that 'default' centre is specific to the user on current thread ({@link IUserProvider}). All injector-based default values will be user-specific
     * if they are defined as user-specific in domain logic.
     *
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final IWebUiConfig webUiConfig) {
        return applyWebUIDefaultValues(createDefaultCentre(miType, webUiConfig), getEntityType(miType));
    }
    
    /**
     * Returns {@code runAutomatically} parameter for the Centre DSL configuration defined by {@code miType}.
     * <p>
     * Centres defined as {@code runAutomatically} not only runs automatically on loading; criteria for such centres will be cleared before auto-running.
     * 
     * @param miType
     * @param webUiConfig
     * @return
     */
    public static boolean defaultRunAutomatically(final Class<? extends MiWithConfigurationSupport<?>> miType, final IWebUiConfig webUiConfig) {
        return ofNullable(webUiConfig.getCentres().get(miType)) // additional safety in case if for some reason there is no EntityCentre instance for miType
            .map(EntityCentre::isRunAutomatically)
            .orElse(false);
    }
    
    /**
     * Initialises 'differences centre' from the persistent storage, if it exists.
     * <p>
     * If no 'differences centre' exists -- the following steps are performed:
     * <p>
     * 1. creates user-specific 'default centre';<br>
     * 2. saves 'default centre' as 'empty diff centre'<br>
     * <p>
     * In case of non-base user the diff is initialised from base user's corresponding SAVED_CENTRE_NAMEd diff centre.
     *
     * @param miType
     * @param deviceSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.);
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     *
     * @return
     */
    private static Map<String, Object> updateDifferences(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final User user,
            final String deviceSpecificName,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        // the name consists of 'deviceSpecificName' and 'DIFFERENCES_SUFFIX'
        final String deviceSpecificDiffName = deviceSpecificName + DIFFERENCES_SUFFIX;
        
        final Map<String, Object> resultantDiff;
        // WILL BE UPDATED IN EVERY CALL OF updateDifferencesCentre!
        
        // init (or update) diff centre from persistent storage if exists
        final Optional<Map<String, Object>> retrievedDiff = retrieveDiff(miType, user, deviceSpecificDiffName, eccCompanion);
        if (retrievedDiff.isPresent()) {
            resultantDiff = retrievedDiff.get();
        } else {
            // Default centre is used as a 'base' for all centres; all diffs are created comparing to default centre.
            // Default centre is now needed for both cases: base or non-base user.
            if (user.isBase() || of(LINK_CONFIG_TITLE).equals(saveAsName) || empty().equals(saveAsName)) { // for non-base user 'link' and 'default' configurations need to be derived from default user-specific configuration instead of base configuration
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff
                resultantDiff = saveNewEntityCentreManager(createEmptyDifferences(), miType, user, deviceSpecificDiffName, null, eccCompanion, mmiCompanion, identity());
                if (FRESH_CENTRE_NAME.equals(name)) { // configs have runAutomatically only in FRESH centre
                    findConfigOpt(miType, user, deviceSpecificDiffName, eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("runAutomatically"))
                        .ifPresent(freshConfig -> {
                            final boolean upstreamRunAutomatically = of(LINK_CONFIG_TITLE).equals(saveAsName) /* link: always runAutomatically */ || defaultRunAutomatically(miType, webUiConfig) /* default/base: runAutomatically as in Centre DSL */;
                            eccCompanion.saveWithRetry(freshConfig.setRunAutomatically(upstreamRunAutomatically));
                        });
                }
            } else { // non-base user
                // diff centre does not exist in persistent storage yet -- load diff from base user's configuration
                final User baseUser = userCompanion.findByEntityAndFetch(fetch(User.class).with(LAST_UPDATED_BY), user.getBasedOnUser());
                final Optional<Map<String, Object>> baseCentreDiffOpt = retrieveDiff(miType, baseUser, deviceSpecific(saveAsSpecific(SAVED_CENTRE_NAME, saveAsName), device) + DIFFERENCES_SUFFIX, eccCompanion);
                // find description of the centre configuration to be copied from
                final String upstreamDesc = baseCentreDiffOpt.isPresent() ? updateCentreDesc(baseUser, miType, saveAsName, device, eccCompanion) : null;
                final Optional<String> upstreamConfigUuid = baseCentreDiffOpt.isPresent() ? updateCentreConfigUuid(baseUser, miType, saveAsName, device, eccCompanion) : empty();
                // no need to provide selectionCrit ('null' parameter) for checking whether baseUser's configuration is inherited - it can never be inherited transitively
                final boolean upstreamRunAutomatically = baseCentreDiffOpt.isPresent() ? updateCentreRunAutomatically(baseUser, miType, saveAsName, device, eccCompanion, webUiConfig, null) : defaultRunAutomatically(miType, webUiConfig);
                // creates differences centre from the differences between base user's 'default centre' (which can be user specific, see IValueAssigner for properties dependent on User) and 'baseCentre'
                final Map<String, Object> differences = baseCentreDiffOpt.orElseGet(CentreUpdater::createEmptyDifferences);
                // promotes diff to persistent storage
                resultantDiff = saveNewEntityCentreManager(differences, miType, user, deviceSpecificDiffName, upstreamDesc, eccCompanion, mmiCompanion, identity());
                if (FRESH_CENTRE_NAME.equals(name)) { // inherited configs have uuid only in FRESH centre
                    if (upstreamConfigUuid.isPresent()) {
                        findConfigOpt(miType, user, deviceSpecificDiffName, eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("configUuid").with("runAutomatically"))
                            .ifPresent(freshConfig -> eccCompanion.saveWithRetry(freshConfig.setConfigUuid(upstreamConfigUuid.get()).setRunAutomatically(upstreamRunAutomatically)));
                    } else {
                        findConfigOpt(miType, user, deviceSpecificDiffName, eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("runAutomatically"))
                            .ifPresent(freshConfig -> eccCompanion.saveWithRetry(freshConfig.setRunAutomatically(upstreamRunAutomatically)));
                    }
                }
            }
        }
        return resultantDiff;
    }
    
    /**
     * Converts serialisable <code>value</code> representation to actual value, mostly from {@link String}. Two exceptions are {@link boolean} (from {@link boolean}) and {@link Money} (from {@link Map}).
     *  
     * @param value
     * @param root
     * @param managedTypeSupplier
     * @param property
     * @param companionFinder
     * @return
     */
    private static Object convertFrom(final Object value, final Class<AbstractEntity<?>> root, final Supplier<Class<?>> managedTypeSupplier, final String property, final ICompanionObjectFinder companionFinder) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> managedType = managedTypeSupplier.get();
        final Class<?> propertyType = isEntityItself ? managedType : determinePropertyType(managedType, property);
        if (Integer.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, STRING_TO_INTEGER);
        } else if (Long.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, STRING_TO_LONG);
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, STRING_TO_BIG_DECIMAL);
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, MAP_TO_MONEY);
        } else if (isDate(propertyType)) {
            return nullOrConvert(value, STRING_TO_LONG.andThen(LONG_TO_DATE));
        } else if (isEntityType(propertyType) && isCritOnlySingle(managedType, property)) {
            if (isPropertyDescriptor(propertyType)) {
                return nullOrConvert(value, PROPERTY_DESCRIPTOR_FROM_STRING);
            } else {
                return nullOrConvert(value, (final String str) -> entityWithMocksFromString(idOrKey -> {
                    final IEntityDao<AbstractEntity<?>> propertyCompanion = companionFinder.find((Class<AbstractEntity<?>>) propertyType, true);
                    final IEntityDao<AbstractEntity<?>> companion = companionFinder.find(root, true);
                    final fetch<AbstractEntity<?>> fetch = fetchForPropertyOrDefault(companion.getFetchProvider(), property).fetchModel();
                    if (idOrKey.startsWith(ID_PREFIX)) {
                        return (AbstractEntity<?>) propertyCompanion.findById(Long.valueOf(idOrKey.replaceFirst(quote(ID_PREFIX), "")), fetch);
                    } else {
                        return (AbstractEntity<?>) propertyCompanion.findByKeyAndFetch(fetch, idOrKey);
                    }
                }, str, (Class<AbstractEntity<?>>) propertyType));
            }
        } else {
            return value; // boolean and String values here
        }
    }
    
    /**
     * Converts <code>value</code> to serialisable representation, mostly into {@link String} format. Two exceptions are {@link boolean} (to {@link boolean}) and {@link Money} (to {@link Map}).
     * 
     * @param value
     * @param managedTypeSupplier
     * @param property
     * @return
     */
    private static Object convertTo(final Object value, final Supplier<Class<?>> managedTypeSupplier, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> managedType = managedTypeSupplier.get();
        final Class<?> propertyType = isEntityItself ? managedType : determinePropertyType(managedType, property);
        if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType) || BigDecimal.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, TO_STRING);
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return nullOrConvert(value, MONEY_TO_MAP);
        } else if (isDate(propertyType)) {
            return nullOrConvert(value, DATE_TO_LONG.andThen(TO_STRING));
        } else if (isEntityType(propertyType) && isCritOnlySingle(managedType, property)) {
            if (isPropertyDescriptor(propertyType)) {
                return nullOrConvert(value, PROPERTY_DESCRIPTOR_TO_STRING);
            } else {
                return nullOrConvert(value, ENTITY_TO_STRING);
            }
        } else {
            return value; // boolean and String values here
        }
    }
    
    /**
     * If <code>value</code> is <code>null</code> then returns <code>null</code>, otherwise treats the value as being of type <code>T</code> and converts using <code>conversionFunc</code> to value of type <code>M</code>.
     * 
     * @param value
     * @param conversionFunc
     * @return
     */
    private static <T> Object nullOrConvert(final Object value, final Function<T, ?> conversionFunc) {
        if (value == null) {
            return null;
        }
        try {
            return conversionFunc.apply((T) value);
        } catch (final Exception ex) {
            logger.warn("Error converting value.", ex);
            return null;
        }
    }
    
    /**
     * Creates 'diff' from 'centre' and 'defaultCentre' with meta-values (only those that are different) and other information.
     *
     * @param centre
     * @param defaultCentre
     * @param root
     * 
     * @return
     */
    static Map<String, Object> createDifferences(final ICentreDomainTreeManagerAndEnhancer centre, final ICentreDomainTreeManagerAndEnhancer defaultCentre, final Class<AbstractEntity<?>> root) {
        final Supplier<Class<?>> managedTypeSupplier = () -> centre.getEnhancer().getManagedType(root);
        
        final Map<String, Object> diff = createEmptyDifferences();
        final Map<String, Map<String, Object>> propertiesDiff = (Map<String, Map<String, Object>>) diff.get(PROPERTIES);
        
        final Class<?> managedType = managedTypeSupplier.get();
        for (final String property : centre.getFirstTick().checkedProperties(root)) { // lets go through centre's checked properties for the first tick (criteria) -- these will be fully synced to originalCentre's checked properties (due to the way centre is created: centre := defaultCentre + diff)
            if (!isPlaceholder(property)) { // placeholders were used in Swing UI to indicate empty places on selection criteria; need to be filtered out
                // when checking property type, we need to look for property in 'managedType' rather than in original 'root' type; this is due to ability to add adhoc calculated properties into selection criteria (like totalCost = quantity * price)
                if (isDoubleCriterion(managedType, property) && !isBooleanCriterion(managedType, property)) {
                    final Boolean exclusiveVal = centre.getFirstTick().getExclusive(root, property);
                    if (!equalsEx(exclusiveVal, defaultCentre.getFirstTick().getExclusive(root, property))) {
                        diff(property, propertiesDiff).put(EXCLUSIVE.name(), exclusiveVal);
                    }
                    final Boolean exclusive2Val = centre.getFirstTick().getExclusive2(root, property);
                    if (!equalsEx(exclusive2Val, defaultCentre.getFirstTick().getExclusive2(root, property))) {
                        diff(property, propertiesDiff).put(EXCLUSIVE2.name(), exclusive2Val);
                    }
                }
                final Class<?> propertyType = isEmpty(property) ? managedType : determinePropertyType(managedType, property);
                if (isDate(propertyType)) {
                    final DateRangePrefixEnum datePrefixVal = centre.getFirstTick().getDatePrefix(root, property);
                    if (!equalsEx(datePrefixVal, defaultCentre.getFirstTick().getDatePrefix(root, property))) {
                        diff(property, propertiesDiff).put(DATE_PREFIX.name(), nullOrConvert(datePrefixVal, DATE_PREFIX_TO_STRING));
                    }
                    final MnemonicEnum dateMnemonicVal = centre.getFirstTick().getDateMnemonic(root, property);
                    if (!equalsEx(dateMnemonicVal, defaultCentre.getFirstTick().getDateMnemonic(root, property))) {
                        diff(property, propertiesDiff).put(DATE_MNEMONIC.name(), nullOrConvert(dateMnemonicVal, DATE_MNEMONIC_TO_STRING));
                    }
                    final Boolean andBeforeVal = centre.getFirstTick().getAndBefore(root, property);
                    if (!equalsEx(andBeforeVal, defaultCentre.getFirstTick().getAndBefore(root, property))) {
                        diff(property, propertiesDiff).put(AND_BEFORE.name(), andBeforeVal);
                    }
                }
                
                final Boolean orNullVal = centre.getFirstTick().getOrNull(root, property);
                if (!equalsEx(orNullVal, defaultCentre.getFirstTick().getOrNull(root, property))) {
                    diff(property, propertiesDiff).put(OR_NULL.name(), orNullVal);
                }
                final Boolean notVal = centre.getFirstTick().getNot(root, property);
                if (!equalsEx(notVal, defaultCentre.getFirstTick().getNot(root, property))) {
                    diff(property, propertiesDiff).put(NOT.name(), notVal);
                }
                final Integer orGroupVal = centre.getFirstTick().getOrGroup(root, property);
                if (!equalsEx(orGroupVal, defaultCentre.getFirstTick().getOrGroup(root, property))) {
                    diff(property, propertiesDiff).put(OR_GROUP.name(), orGroupVal);
                }
                
                final Object valueVal = centre.getFirstTick().getValue(root, property);
                if (!equalsEx(valueVal, defaultCentre.getFirstTick().getValue(root, property))) {
                    diff(property, propertiesDiff).put(VALUE.name(), convertTo(valueVal, managedTypeSupplier, property));
                }
                if (isDoubleCriterion(managedType, property)) {
                    final Object value2Val = centre.getFirstTick().getValue2(root, property);
                    if (!equalsEx(value2Val, defaultCentre.getFirstTick().getValue2(root, property))) {
                        diff(property, propertiesDiff).put(VALUE2.name(), convertTo(value2Val, managedTypeSupplier, property));
                    }
                }
                if (isActivatableEntityOrUnionType(propertyType)) {
                    final boolean autocompleteActiveOnlyVal = centre.getFirstTick().getAutocompleteActiveOnly(root, property);
                    if (!equalsEx(autocompleteActiveOnlyVal, defaultCentre.getFirstTick().getAutocompleteActiveOnly(root, property))) {
                        diff(property, propertiesDiff).put(AUTOCOMPLETE_ACTIVE_ONLY.name(), autocompleteActiveOnlyVal);
                    }
                }
            }
        }
        
        extendDiffsWithNonIntrusiveDifferences(diff, centre.getSecondTick(), defaultCentre.getSecondTick(), root);
        
        // determine whether preferred view has been changed and add it to the diff if true
        final Integer preferredView = centre.getPreferredView();
        if (!equalsEx(preferredView, defaultCentre.getPreferredView())) {
            diff.put(PREFERRED_VIEW, preferredView);
        }
        
        return diff;
    }
    
    /**
     * Extends existing {@code diff} object with non-intrusive changes, taken from {@code secondTick}.
     * Non-intrusive changes remain existent for auto-runnable centres during auto-run -- unlike criteria and other changes, which are being cleared.
     * They contain sorting, visibility and order of columns, page capacity etc.
     * 
     * @param diff
     * @param secondTick - target result-set config
     * @param defaultSecondTick - base (default) result-set config, with which target config will be compared
     * @param root
     * @return
     */
    public static Map<String, Object> extendDiffsWithNonIntrusiveDifferences(final Map<String, Object> diff, final IAddToResultTickManager secondTick, final IAddToResultTickManager defaultSecondTick, final Class<AbstractEntity<?>> root) {
        final Map<String, Map<String, Object>> propertiesDiff = (Map<String, Map<String, Object>>) diff.get(PROPERTIES);
        
        // extract widths that are changed and add them to the diff
        for (final String property : secondTick.checkedProperties(root)) {
            final int widthVal = secondTick.getWidth(root, property);
            if (!equalsEx(widthVal, defaultSecondTick.getWidth(root, property))) {
                diff(property, propertiesDiff).put(WIDTH.name(), widthVal);
            }
            final int growFactorVal = secondTick.getGrowFactor(root, property);
            if (!equalsEx(growFactorVal, defaultSecondTick.getGrowFactor(root, property))) {
                diff(property, propertiesDiff).put(GROW_FACTOR.name(), growFactorVal);
            }
        }
        
        // determine whether usedProperties have been changed (as a whole) and add them to the diff if true
        final List<String> visibilityAndOrderPropertiesVal = secondTick.usedProperties(root);
        if (!equalsEx(visibilityAndOrderPropertiesVal, defaultSecondTick.usedProperties(root))) {
            diff.put(VISIBILITY_AND_ORDER, visibilityAndOrderPropertiesVal);
        }
        
        // determine whether orderedProperties have been changed (as a whole) and add them to the diff if true
        final List<Pair<String, Ordering>> sortingPropertiesVal = secondTick.orderedProperties(root);
        if (!equalsEx(sortingPropertiesVal, defaultSecondTick.orderedProperties(root))) {
            diff.put(SORTING, createSerialisableSortingProperties(sortingPropertiesVal));
        }
        
        // determine whether pageCapacity has been changed and add it to the diff if true
        final int pageCapacityVal = secondTick.getPageCapacity();
        if (!equalsEx(pageCapacityVal, defaultSecondTick.getPageCapacity())) {
            diff.put(PAGE_CAPACITY, pageCapacityVal);
        }
        
        // determine whether visibleRowsCount has been changed and add it to the diff if true
        final int visibleRowsCountVal = secondTick.getVisibleRowsCount();
        if (!equalsEx(visibleRowsCountVal, defaultSecondTick.getVisibleRowsCount())) {
            diff.put(VISIBLE_ROWS_COUNT, visibleRowsCountVal);
        }
        
        // determine whether numberOfHeaderLines has been changed and add it to the diff if true
        final int numberOfHeaderLinesVal = secondTick.getNumberOfHeaderLines();
        if (!equalsEx(numberOfHeaderLinesVal, defaultSecondTick.getNumberOfHeaderLines())) {
            diff.put(NUMBER_OF_HEADER_LINES, numberOfHeaderLinesVal);
        }
        return diff;
    }
    
    /**
     * Provides a kind warning about <code>property</code> disappearance from <code>from</code> and <code>valueKind</code> that was affected.
     * 
     * @param from
     * @param valueKind
     * @param property
     */
    private static void warnSubValueRemovalFrom(final String from, final String valueKind, final String property) {
        logger.warn(format("[%s] diff sub-value ignored. Property [%s] does not exist in %s (removed from Centre DSL config or even from domain type).", valueKind, property, from));
    }
    
    /**
     * Provides a kind warning about <code>property</code> disappearance from <code>from</code> and <code>valueKind</code> that was affected.
     * 
     * @param from
     * @param valueKind
     * @param diff
     * @param property
     */
    private static void warnPropRemovalFrom(final String from, final String valueKind, final Map<String, Object> diff, final String property) {
        logger.warn(format("Property [%s] diff value [%s] ignored. Property [%s] does not exist in %s (removed from Centre DSL config or even from domain type).", valueKind, diff.get(valueKind), property, from));
    }
    
    /**
     * Processes property value of <code>valueKind</code> taking it from <code>diff</code> and applying <code>valueApplier</code>.
     * If property was removed (<code>propertyPresent = false</code>) then warns about it. 
     * 
     * @param diff
     * @param valueKind
     * @param propertyPresent
     * @param removedFrom
     * @param valueApplier
     * @param property
     */
    private static void processValue(final Map<String, Object> diff, final String valueKind, final boolean propertyPresent, final String removedFrom, final Consumer<Object> valueApplier, final String property) {
        if (diff.containsKey(valueKind)) {
            if (propertyPresent) {
                valueApplier.accept(diff.get(valueKind));
            } else {
                warnPropRemovalFrom(removedFrom, valueKind, diff, property);
            }
        }
    }
    
    /**
     * Applies the differences from 'differences centre' on top of 'target centre'.
     *
     * @param targetCentre
     * @param root
     * @param companionFinder -- to process crit-only single entity-typed values
     * @return
     */
    static ICentreDomainTreeManagerAndEnhancer applyDifferences(final ICentreDomainTreeManagerAndEnhancer targetCentre, final Map<String, Object> differences, final Class<AbstractEntity<?>> root, final ICompanionObjectFinder companionFinder) {
        final Supplier<Class<?>> managedTypeSupplier = () -> targetCentre.getEnhancer().getManagedType(root);
        final Map<String, Map<String, Object>> propertiesDiff = (Map<String, Map<String, Object>>) differences.get(PROPERTIES);
        
        for (final Entry<String, Map<String, Object>> propertyDiff: propertiesDiff.entrySet()) {
            final String property = propertyDiff.getKey();
            final Map<String, Object> diff = propertyDiff.getValue();
            
            final boolean selectionCriteriaContains = targetCentre.getFirstTick().checkedProperties(root).contains(property);
            
            processValue(diff, EXCLUSIVE.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setExclusive(root, property, (Boolean) value), property);
            processValue(diff, EXCLUSIVE2.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setExclusive2(root, property, (Boolean) value), property);
            processValue(diff, DATE_PREFIX.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setDatePrefix(root, property, (DateRangePrefixEnum) nullOrConvert(value, STRING_TO_DATE_PREFIX)), property);
            processValue(diff, DATE_MNEMONIC.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setDateMnemonic(root, property, (MnemonicEnum) nullOrConvert(value, STRING_TO_DATE_MNEMONIC)), property);
            processValue(diff, AND_BEFORE.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setAndBefore(root, property, (Boolean) value), property);
            processValue(diff, OR_NULL.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setOrNull(root, property, (Boolean) value), property);
            processValue(diff, NOT.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setNot(root, property, (Boolean) value), property);
            processValue(diff, OR_GROUP.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setOrGroup(root, property, (Integer) value), property);
            processValue(diff, VALUE.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setValue(root, property, convertFrom(value, root, managedTypeSupplier, property, companionFinder)), property);
            processValue(diff, VALUE2.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setValue2(root, property, convertFrom(value, root, managedTypeSupplier, property, companionFinder)), property);
            processValue(diff, AUTOCOMPLETE_ACTIVE_ONLY.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setAutocompleteActiveOnly(root, property, (Boolean) value), property);
            
            final boolean resultSetContains = targetCentre.getSecondTick().checkedProperties(root).contains(property);
            
            processValue(diff, WIDTH.name(), resultSetContains, "result-set", (value) -> targetCentre.getSecondTick().setWidth(root, property, (int) value), property);
            processValue(diff, GROW_FACTOR.name(), resultSetContains, "result-set", (value) -> targetCentre.getSecondTick().setGrowFactor(root, property, (int) value), property);
        }
        
        // process EGI column visibility / order
        final List<String> visibilityAndOrder = (List<String>) differences.get(VISIBILITY_AND_ORDER);
        if (visibilityAndOrder != null) { // if it exists then it was explicitly changed by user; whole snapshot will be applied against target centre
            // apply resultant properties on top of targetCentre (default config)
            final List<String> targetUsedProperties = targetCentre.getSecondTick().usedProperties(root);
            for (final String targetUsedProperty: targetUsedProperties) { // remove (un-use) all previous props
                targetCentre.getSecondTick().use(root, targetUsedProperty, false);
            }
            for (final String newUsedProperty : visibilityAndOrder) { // apply (use) all new props
                if (targetCentre.getSecondTick().checkedProperties(root).contains(newUsedProperty)) {
                    targetCentre.getSecondTick().use(root, newUsedProperty, true);
                } else {
                    warnSubValueRemovalFrom("result-set", VISIBILITY_AND_ORDER, newUsedProperty);
                }
            }
        }
        
        // process EGI data sorting
        final ArrayList<LinkedHashMap<String, String>> sorting = (ArrayList<LinkedHashMap<String, String>>) differences.get(SORTING);
        if (sorting != null) { // if it exists then it was explicitly changed by user; whole snapshot will be applied against target centre
            // need to clear all previous orderings:
            final List<Pair<String, Ordering>> sortingProperties = new ArrayList<>(targetCentre.getSecondTick().orderedProperties(root));
            for (final Pair<String, Ordering> sortingProperty: sortingProperties) {
                if (ASCENDING == sortingProperty.getValue()) {
                    targetCentre.getSecondTick().toggleOrdering(root, sortingProperty.getKey());
                }
                targetCentre.getSecondTick().toggleOrdering(root, sortingProperty.getKey());
            }
            // and apply new ones from diff:
            for (final LinkedHashMap<String, String> propertyAndDirectionMapped: sorting) {
                final Entry<String, String> propertyAndDirection = propertyAndDirectionMapped.entrySet().iterator().next();
                final String property = propertyAndDirection.getKey();
                final Ordering direction = valueOf(propertyAndDirection.getValue());
                if (targetCentre.getSecondTick().checkedProperties(root).contains(property)) {
                    targetCentre.getSecondTick().toggleOrdering(root, property);
                    if (DESCENDING == direction) {
                        targetCentre.getSecondTick().toggleOrdering(root, property);
                    }
                } else {
                    warnSubValueRemovalFrom("result-set", SORTING, property);
                }
            }
        }
        
        // process EGI pageCapacity, visibleRowsCount and numberOfHeaderLines
        final Integer pageCapacity = (Integer) differences.get(PAGE_CAPACITY);
        if (pageCapacity != null) { // if it exists then it was explicitly changed by user; will be applied against target centre
            targetCentre.getSecondTick().setPageCapacity(pageCapacity);
        }
        final Integer visibleRowsCount = (Integer) differences.get(VISIBLE_ROWS_COUNT);
        if (visibleRowsCount != null) { // if it exists then it was explicitly changed by user; will be applied against target centre
            targetCentre.getSecondTick().setVisibleRowsCount(visibleRowsCount);
        }
        final Integer numberOfHeaderLines = (Integer) differences.get(NUMBER_OF_HEADER_LINES);
        if (numberOfHeaderLines != null) { // if it exists then it was explicitly changed by user; will be applied against target centre
            targetCentre.getSecondTick().setNumberOfHeaderLines(numberOfHeaderLines);
        }
        final Integer preferredView = (Integer) differences.get(PREFERRED_VIEW);
        if (preferredView != null) { // if it exists then it was explicitly changed by user; will be applied against target centre
            targetCentre.setPreferredView(preferredView);
        }
        return targetCentre;
    }
    
    /**
     * Takes property differences from <code>diff</code>. Creates empty property differences inside <code>diff</code> if they are empty. 
     * 
     * @param property
     * @param diff
     * @return
     */
    static Map<String, Object> propDiff(final String property, final Map<String, Object> diff) {
        final Map<String, Map<String, Object>> propertiesDiff = (Map<String, Map<String, Object>>) diff.get(PROPERTIES);
        return diff(property, propertiesDiff);
    }
    
    /**
     * Takes property differences from <code>propertiesDiff</code> part of overall diff. Creates empty property differences inside <code>propertiesDiff</code> if they are empty.
     * 
     * @param property
     * @param propertiesDiff
     * @return
     */
    private static Map<String, Object> diff(final String property, final Map<String, Map<String, Object>> propertiesDiff) {
        final Map<String, Object> propertyDiff = propertiesDiff.get(property);
        if (propertyDiff == null) {
            final Map<String, Object> newPropertyDiff = new LinkedHashMap<>();
            propertiesDiff.put(property, newPropertyDiff);
            return newPropertyDiff;
        }
        return propertyDiff;
    }
    
    /**
     * Creates empty diff.
     * 
     * @return
     */
    public static Map<String, Object> createEmptyDifferences() {
        final Map<String, Object> diff = new LinkedHashMap<>();
        final Map<String, Map<String, Object>> propertiesDiff = new LinkedHashMap<>();
        diff.put(PROPERTIES, propertiesDiff);
        return diff;
    }
    
    /**
     * Creates serialisable representation of sorting properties to be used in diff object.
     * This representation should be exactly the same as the representation returning after diff deserialisation.
     * This is because further restoring logic ({@link #applyDifferences(ICentreDomainTreeManagerAndEnhancer, Map, Class, ICompanionObjectFinder)} method)
     * requires <code>ArrayList<LinkedHashMap<String, String>></code> and when base configuration is loaded the diff object is created on-the-fly 
     * by {@link #createDifferences(ICentreDomainTreeManagerAndEnhancer, ICentreDomainTreeManagerAndEnhancer, Class)} method and not directly deserialised.
     * 
     * @param sortingPropertiesVal
     * @return
     */
    private static List<LinkedHashMap<String, String>> createSerialisableSortingProperties(final List<Pair<String, Ordering>> sortingPropertiesVal) {
        return sortingPropertiesVal.stream().map(CentreUpdater::pairToMap).collect(toCollection(ArrayList::new));
    }
    
    /**
     * Creates raw serialisable map from pair of property name and its {@link Ordering}.
     * 
     * @param pair
     * @return
     */
    private static LinkedHashMap<String, String> pairToMap(final Pair<String, Ordering> pair) {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // pair.getValue() should not be null, but added handling of nulls just in case to avoid potential risks in future
        if (pair.getValue() != null) {
            map.put(pair.getKey(), pair.getValue().name());
        } else {
            logger.warn(format("NULL value for [%s].", pair.getKey()));
        }
        return map;
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

                if (isString(propertyType) || isRichText(propertyType)) {
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
    
}
