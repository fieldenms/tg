package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toCollection;
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
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.valueOf;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isBooleanCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDummyMarker;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isPlaceholder;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.reflectionProperty;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.createDefaultCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfig;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.retrieveDiff;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.saveEntityCentreManager;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.saveNewEntityCentreManager;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.centre.WebApiUtils.checkedPropertiesWithoutSummaries;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.PROPERTY_DESCRIPTOR_FROM_STRING;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.PROPERTY_DESCRIPTOR_TO_STRING;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createNotFoundMockString;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.entityWithMocksFromString;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.entityWithMocksToString;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.fetchForPropertyOrDefault;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
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
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.SaveAsNameAnnotation;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

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
        if (isPersistedEntityType(ent.getType()) || isSyntheticBasedOnPersistentEntityType(ent.getType())) {
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
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(name, saveAsName), device);
        final Map<String, Object> updatedDiff = updateDifferences(miType, user, userProvider, deviceSpecificName, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        return loadCentreFromDefaultAndDiff(user, miType, saveAsName, updatedDiff, webUiConfig, domainTreeEnhancerCache, companionFinder);
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
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(FRESH_CENTRE_NAME, saveAsName), device);
        final EntityCentreConfig eccWithDesc = findConfig(miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, eccCompanion);
        return eccWithDesc == null ? null : eccWithDesc.getDesc();
    }
    
    /**
     * Changes configuration title to <code>newTitle</code> and description to <code>newDesc</code> and saves these changes to persistent storage.
     * 
     * @param user
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
        eccCompanion.saveWithConflicts(freshConfig); // editCentreTitleAndDesc is used inside other transaction scopes (e.g. CentreConfigEditActionDao.performSave and AbstractCentreConfigCommitActionDao.save) -- saveWithConflicts must be used
        eccCompanion.saveWithConflicts(savedConfig);
        if (previouslyRunConfig != null) { // previouslyRun centre may not exist
            eccCompanion.saveWithConflicts(previouslyRunConfig);
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
    public static void removeCentres(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final Optional<String> saveAsName, final IEntityCentreConfig eccCompanion, final String ... names) {
        // remove corresponding diff centre instances from persistent storage
        final String[] deviceSpecificDiffNames = stream(names).map(name -> deviceSpecific(saveAsSpecific(name, saveAsName), device) + DIFFERENCES_SUFFIX).toArray(String[]::new);
        CentreUpdaterUtils.removeCentres(user, miType, eccCompanion, deviceSpecificDiffNames);
    }
    
    /**
     * Initialises and commits centre from the passed <code>centreToBeInitialisedAndCommitted</code> instance for surrogate centre with concrete <code>name</code>.
     * <p>
     * Please note that this operation is immutable in regard to the surrogate centre instance being copied.
     *
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param centre -- the centre manager to commit
     * @param newDesc -- new description to be saved into persistent storage
     */
    public static ICentreDomainTreeManagerAndEnhancer commitCentre(
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final String newDesc,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return commitCentre(false, user, userProvider, miType, name, saveAsName, device, centre, newDesc, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
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
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final String newDesc,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return commitCentre(true, user, userProvider, miType, name, saveAsName, device, centre, newDesc, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    
    /**
     * Initialises and commits centre from the passed <code>centreToBeInitialisedAndCommitted</code> instance for surrogate centre with concrete <code>name</code>.
     * <p>
     * Please note that this operation is immutable in regard to the surrogate centre instance being copied.
     *
     * @param withoutConflicts -- <code>true</code> to avoid self-conflict checks, <code>false</code> otherwise; <code>true</code> only to be used NOT IN another SessionRequired transaction scope
     * @param user
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.); can be {@link CentreUpdater#deviceSpecific(String, DeviceProfile)}.
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     * @param centre -- the centre manager to commit
     * @param newDesc -- new description to be saved into persistent storage
     */
    protected static ICentreDomainTreeManagerAndEnhancer commitCentre(
            final boolean withoutConflicts,
            final User user,
            final IUserProvider userProvider,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String name,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final ICentreDomainTreeManagerAndEnhancer centre,
            final String newDesc,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        final String deviceSpecificName = deviceSpecific(saveAsSpecific(name, saveAsName), device);
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, webUiConfig);
        // override old 'diff' with recently created one and save it
        saveEntityCentreManager(withoutConflicts, createDifferences(centre, defaultCentre, getEntityType(miType)), miType, user, deviceSpecificName + DIFFERENCES_SUFFIX, newDesc, eccCompanion, mmiCompanion);
        return centre;
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
        loadableConfigurations.remove(new LoadableCentreConfig().setKey(LINK_CONFIG_TITLE)); // exclude 'link' configuration from load dialog if it is present (aka centre 'link' with criteria parameters was loaded at least once)
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
     * @param saveAsName
     * @param device
     * @return
     */
    public static void makePreferred(final User user, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final DeviceProfile device, final ICompanionObjectFinder companionFinder) {
        final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
        try (final Stream<EntityCentreConfig> stream = streamPreferredConfigs(user, miType, device, companionFinder) ) { // stream has its own transaction scope -- saveWithConflicts must be used
            stream.forEach(ecc -> eccCompanion.saveWithConflicts(ecc.setPreferred(false)));
        }
        if (saveAsName.isPresent()) {
            eccCompanion.saveWithConflicts( // used inside other transaction scopes (e.g. CentreConfigLoadActionDao->makePreferredConfig->makePreferred) -- saveWithConflicts must be used
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
     * Loads centre through the following chain: 'default centre' + 'differences' := 'centre'.
     *
     * @param user
     * @param miType
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param updatedDiff -- updated differences
     * @param companionFinder
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(
            final User user,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final Map<String, Object> updatedDiff,
            final IWebUiConfig webUiConfig,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final ICompanionObjectFinder companionFinder) {
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(miType, webUiConfig);
        // applies diffCentre on top of defaultCentre to produce loadedCentre:
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = applyDifferences(defaultCentre, updatedDiff, getEntityType(miType), companionFinder);
        // For all generated types on freshCentre (and on its derivatives like 'unchanged freshCentre', 'previouslyRun centre', 'unchanged previouslyRun centre' etc.) there is a need to
        //  provide miType information inside its generated type to be sent to the client application. This is done through the use of
        //  annotation miType and other custom annotations, for example @SaveAsName.
        if (saveAsName.isPresent()) { // this is saveAs user-specific configuration
            // We need to provide a new MiType annotation with saveAsName there.
            // However, it should be done in a smart way, i.e. look for cached (by means of (user, miType, saveAsName)) generated type.
            // If there is such a type, just replace generated type information inside loadedCentre.getEnhancer().
            // Otherwise, perform adjustManagedTypeAnnotations and cache adjusted generated type for future reference.
            final Class<?> cachedGeneratedType = domainTreeEnhancerCache.getGeneratedTypeFor(miType, saveAsName.get(), user.getId());
            if (cachedGeneratedType != null) {
                for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
                    if (isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                        loadedCentre.getEnhancer().replaceManagedTypeBy(root, cachedGeneratedType);
                    }
                }
            } else {
                for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
                    if (isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                        final Class<?> newGeneratedType = loadedCentre.getEnhancer().adjustManagedTypeAnnotations(root, new SaveAsNameAnnotation().newInstance(saveAsName.get()));
                        domainTreeEnhancerCache.putGeneratedTypeFor(miType, saveAsName.get(), user.getId(), newGeneratedType);
                    }
                }
            }
        }// otherwise, no need to add @SaveAsName annotation (or use cached type with that annotation)
        return loadedCentre;
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
     * @param gdtm
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final IWebUiConfig webUiConfig) {
        return applyWebUIDefaultValues(createDefaultCentre(miType, webUiConfig), getEntityType(miType));
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
     * @param saveAsName -- user-defined title of 'saveAs' centre configuration or empty {@link Optional} for unnamed centre
     * @param device -- device profile (mobile or desktop) for which the centre is accessed / maintained
     *
     * @return
     */
    private static Map<String, Object> updateDifferences(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final User user,
            final IUserProvider userProvider,
            final String deviceSpecificName,
            final Optional<String> saveAsName,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
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
            if (user.isBase() || of(WebApiUtils.LINK_CONFIG_TITLE).equals(saveAsName) || empty().equals(saveAsName)) { // for non-base user 'link' and 'default' configurations need to be derived from default user-specific configuration instead of base configuration
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff
                resultantDiff = saveNewEntityCentreManager(createEmptyDifferences(), miType, user, deviceSpecificDiffName, null, eccCompanion, mmiCompanion);
            } else { // non-base user
                // diff centre does not exist in persistent storage yet -- create a diff by comparing basedOnCentre (configuration created by base user) and default centre
                final User baseUser = beginBaseUserOperations(userProvider, user, userCompanion);
                final ICentreDomainTreeManagerAndEnhancer baseCentre = getBaseCentre(baseUser, userProvider, miType, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                // find description of the centre configuration to be copied from
                final String upstreamDesc = updateCentreDesc(baseUser, miType, saveAsName, device, eccCompanion);
                // creates differences centre from the differences between base user's 'default centre' (which can be user specific, see IValueAssigner for properties dependent on User) and 'baseCentre'
                final Map<String, Object> differences = createDifferences(baseCentre, getDefaultCentre(miType, webUiConfig), getEntityType(miType));
                endBaseUserOperations(user, userProvider);
                
                // promotes diff to local cache and saves it into persistent storage
                resultantDiff = saveNewEntityCentreManager(differences, miType, user, deviceSpecificDiffName, upstreamDesc, eccCompanion, mmiCompanion);
            }
        }
        return resultantDiff;
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
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        return updateCentre(baseUser, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
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
     * For non-base user the base centre is identified as SAVED_CENTRE_NAME version of the centre of the same <code>saveAsName</code> configured by <code>user</code>'s base user. 
     * For base user the base centre is identified as <code>getDefaultCentre()</code>. 
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
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        if (user.isBase()) {
            return getDefaultCentre(miType, webUiConfig);
        } else {
            final User baseUser = beginBaseUserOperations(userProvider, user, userCompanion);
            final ICentreDomainTreeManagerAndEnhancer baseCentre = getBaseCentre(baseUser, userProvider, miType, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            endBaseUserOperations(user, userProvider);
            return baseCentre;
        }
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
        return value == null ? null : conversionFunc.apply((T) value);
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
            }
        }
        
        // extract widths that are changed and add them to the diff
        for (final String property : centre.getSecondTick().checkedProperties(root)) {
            final int widthVal = centre.getSecondTick().getWidth(root, property);
            if (!equalsEx(widthVal, defaultCentre.getSecondTick().getWidth(root, property))) {
                diff(property, propertiesDiff).put(WIDTH.name(), widthVal);
            }
            final int growFactorVal = centre.getSecondTick().getGrowFactor(root, property);
            if (!equalsEx(growFactorVal, defaultCentre.getSecondTick().getGrowFactor(root, property))) {
                diff(property, propertiesDiff).put(GROW_FACTOR.name(), growFactorVal);
            }
        }
        
        // determine whether usedProperties have been changed (as a whole) and add them to the diff if true
        final List<String> visibilityAndOrderPropertiesVal = centre.getSecondTick().usedProperties(root);
        if (!equalsEx(visibilityAndOrderPropertiesVal, defaultCentre.getSecondTick().usedProperties(root))) {
            diff.put(VISIBILITY_AND_ORDER, visibilityAndOrderPropertiesVal);
        }
        
        // determine whether orderedProperties have been changed (as a whole) and add them to the diff if true
        final List<Pair<String, Ordering>> sortingPropertiesVal = centre.getSecondTick().orderedProperties(root);
        if (!equalsEx(sortingPropertiesVal, defaultCentre.getSecondTick().orderedProperties(root))) {
            diff.put(SORTING, createSerialisableSortingProperties(sortingPropertiesVal));
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
     * @param differencesCentre
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
            processValue(diff, VALUE.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setValue(root, property, convertFrom(value, root, managedTypeSupplier, property, companionFinder)), property);
            processValue(diff, VALUE2.name(), selectionCriteriaContains, "selection criteria", (value) -> targetCentre.getFirstTick().setValue2(root, property, convertFrom(value, root, managedTypeSupplier, property, companionFinder)), property);
            
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
     * Converts old <code>diffCentre</code> version of diff object to new Map-like diff.
     * 
     * @param differencesCentre
     * @return
     */
    public static Map<String, Object> createDiffFrom(final ICentreDomainTreeManagerAndEnhancer differencesCentre) {
        final BiFunction<Class<?>, String, Boolean> propertyRemovedFromDomainType = (final Class<?> diffManagedType, final String property) -> {
            // Check whether the 'property' has not been disappeared from domain type since last server restart.
            // In such case 'orderedProperties' will contain that property but 'managedType(root, differencesCentre)' will not contain corresponding field.
            // Such properties need to be silently ignored. During next diffCentre creation such properties will disappear from diffCentre fully.
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            if (!isEntityItself) {
                try {
                    determinePropertyType(diffManagedType, property);
                    return false;
                } catch (final Exception ex) {
                    // System.out.println();
                    // logger.warn(format("Property [%s] could not be found in type [%s] in diffCentre. It will be skipped. Most likely this property was deleted from domain type definition.", property, diffManagedType.getSimpleName()));
                    return true;
                }
            } else {
                return false;
            }
        };
        
        final Class<?> root = differencesCentre.getRepresentation().rootTypes().iterator().next();
        final Supplier<Class<?>> managedTypeSupplier = () -> differencesCentre.getEnhancer().getManagedType(root);
        
        final Map<String, Object> diff = createEmptyDifferences();
        final Map<String, Map<String, Object>> propertiesDiff = (Map<String, Map<String, Object>>) diff.get(PROPERTIES);
        
        final Class<?> managedType = managedTypeSupplier.get();
        
        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!isPlaceholder(property) && !propertyRemovedFromDomainType.apply(managedType, property)) {
                if (isDoubleCriterion(managedType, property) && !isBooleanCriterion(managedType, property)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(EXCLUSIVE, root, property)) {
                        final Boolean exclusiveVal = differencesCentre.getFirstTick().getExclusive(root, property);
                        diff(property, propertiesDiff).put(EXCLUSIVE.name(), exclusiveVal);
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(EXCLUSIVE2, root, property)) {
                        final Boolean exclusive2Val = differencesCentre.getFirstTick().getExclusive2(root, property);
                        diff(property, propertiesDiff).put(EXCLUSIVE2.name(), exclusive2Val);
                    }
                }
                final Class<?> propertyType = isEmpty(property) ? managedType : determinePropertyType(managedType, property);
                if (isDate(propertyType)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(DATE_PREFIX, root, property)) {
                        final DateRangePrefixEnum datePrefixVal = differencesCentre.getFirstTick().getDatePrefix(root, property);
                        diff(property, propertiesDiff).put(DATE_PREFIX.name(), nullOrConvert(datePrefixVal, DATE_PREFIX_TO_STRING));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(DATE_MNEMONIC, root, property)) {
                        final MnemonicEnum dateMnemonicVal = differencesCentre.getFirstTick().getDateMnemonic(root, property);
                        diff(property, propertiesDiff).put(DATE_MNEMONIC.name(), nullOrConvert(dateMnemonicVal, DATE_MNEMONIC_TO_STRING));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(AND_BEFORE, root, property)) {
                        final Boolean andBeforeVal = differencesCentre.getFirstTick().getAndBefore(root, property);
                        diff(property, propertiesDiff).put(AND_BEFORE.name(), andBeforeVal);
                    }
                }
                
                if (differencesCentre.getFirstTick().isMetaValuePresent(OR_NULL, root, property)) {
                    final Boolean orNullVal = differencesCentre.getFirstTick().getOrNull(root, property);
                    diff(property, propertiesDiff).put(OR_NULL.name(), orNullVal);
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(NOT, root, property)) {
                    final Boolean notVal = differencesCentre.getFirstTick().getNot(root, property);
                    diff(property, propertiesDiff).put(NOT.name(), notVal);
                }
                
                if (differencesCentre.getFirstTick().isMetaValuePresent(VALUE, root, property)) {
                    final Object valueVal = differencesCentre.getFirstTick().getValue(root, property);
                    diff(property, propertiesDiff).put(VALUE.name(), convertTo(valueVal, managedTypeSupplier, property));
                }
                if (isDoubleCriterion(managedType, property) && differencesCentre.getFirstTick().isMetaValuePresent(VALUE2, root, property)) {
                    final Object value2Val = differencesCentre.getFirstTick().getValue2(root, property);
                    diff(property, propertiesDiff).put(VALUE2.name(), convertTo(value2Val, managedTypeSupplier, property));
                }
            }
        }
        
        final List<String> diffCheckedPropertiesWithoutSummaries = checkedPropertiesWithoutSummaries(differencesCentre.getSecondTick().checkedProperties(root), differencesCentre.getEnhancer().getManagedType(root));
        final List<String> visibilityAndOrderPropertiesVal = differencesCentre.getSecondTick().usedProperties(root);
        if (!equalsEx(diffCheckedPropertiesWithoutSummaries, visibilityAndOrderPropertiesVal)) {
            diff.put(VISIBILITY_AND_ORDER, visibilityAndOrderPropertiesVal);
        }
        
        for (final String property : diffCheckedPropertiesWithoutSummaries) {
            if (!propertyRemovedFromDomainType.apply(managedType, property)) {
                if (differencesCentre.getFirstTick().isMetaValuePresent(WIDTH, root, property)) {
                    final int widthVal = differencesCentre.getSecondTick().getWidth(root, property);
                    diff(property, propertiesDiff).put(WIDTH.name(), widthVal);
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(GROW_FACTOR, root, property)) {
                    final int growFactorVal = differencesCentre.getSecondTick().getGrowFactor(root, property);
                    diff(property, propertiesDiff).put(GROW_FACTOR.name(), growFactorVal);
                }
            }
        }
        
        if (differencesCentre.getFirstTick().isMetaValuePresent(ALL_ORDERING, root, "")) {
            final List<Pair<String, Ordering>> sortingPropertiesVal = differencesCentre.getSecondTick().orderedProperties(root);
            diff.put(SORTING, createSerialisableSortingProperties(sortingPropertiesVal));
        }
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
    
}