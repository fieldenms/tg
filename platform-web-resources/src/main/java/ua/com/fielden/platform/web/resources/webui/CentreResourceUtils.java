package ua.com.fielden.platform.web.resources.webui;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.min;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.CRITERIA_ENTITY_ID;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isBooleanCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isAndBeforeDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isDateMnemonicDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isDatePrefixDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isExclusive2Default;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isExclusiveDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isNotDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isOrGroupDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isOrNullDefault;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.centre.AbstractCentreConfigAction.APPLIED_CRITERIA_ENTITY_NAME;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.AUTO_RUN;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLink;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isInherited;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.FETCH_CONFIG_AND_INSTRUMENT;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOpt;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.enhanceResultEntitiesWithCustomPropertyValues;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.enhanceResultEntitiesWithDynamicPropertyValues;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.restoreMasterFunctionalEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalManagedType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getVersion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.MetaPropertyFull;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiType;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.SaveAsNameAnnotation;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * This utility class contains the methods that are shared across {@link CentreResource} and {@link CriteriaResource}.
 *
 * @author TG Team
 *
 */
public class CentreResourceUtils<T extends AbstractEntity<?>> extends CentreUtils<T> {
    private static final Logger logger = LogManager.getLogger(CentreResourceUtils.class);
    
    /**
     * The key for customObject's value containing save-as name.
     */
    public static final String SAVE_AS_NAME = "saveAsName";
    /**
     * The key for customObject's value containing save-as description.
     */
    private static final String SAVE_AS_DESC = "saveAsDesc";
    /**
     * The key for customObject's value containing configuration uuid.
     */
    private static final String CONFIG_UUID = "configUuid";
    /**
     * The key for customObject's value indicating whether centre configuration is dirty meaning it is changed or of [default, link, inherited] kind.
     */
    static final String CENTRE_DIRTY = "centreDirty";
    /**
     * The key for customObject's value containing meta values e.g. mnemonics.
     */
    static final String META_VALUES = "metaValues";
    /**
     * The key for customObject's value, which either contains a message for stale criteria or is empty if no criteria are stale.
     */
    static final String STALE_CRITERIA_MESSAGE = "staleCriteriaMessage";
    /**
     * The key for customObject's value containing the preferred view index.
     */
    static final String PREFERRED_VIEW = "preferredView";

    /** Private default constructor to prevent instantiation. */
    private CentreResourceUtils() {
    }

    private enum RunActions {
        RUN("run"),
        REFRESH("refresh"),
        NAVIGATE("navigate");

        private final String action;

        private RunActions(final String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    ///////////////////////////////// CUSTOM OBJECTS /////////////////////////////////
    /**
     * Creates the 'custom object' that contains 'critMetaValues' and 'centreDirty'.
     *
     * @param criteriaMetaValues
     * @param centreDirty
     * @return
     */
    static Map<String, Object> createCriteriaMetaValuesCustomObject(final Map<String, Map<String, Object>> criteriaMetaValues, final boolean centreDirty) {
        final Map<String, Object> customObject = new LinkedHashMap<>();
        customObject.put(CENTRE_DIRTY, centreDirty);
        customObject.put(META_VALUES, criteriaMetaValues);
        return customObject;
    }

    /**
     * Creates the 'custom object' that contains 'critMetaValues', 'centreDirty' flag (see {@link #createCriteriaMetaValuesCustomObject(Map, boolean, int)},
     * optional 'saveAsName' value, optional 'saveAsDesc' value and optional 'configUuid' value.
     *
     * @param criteriaMetaValues
     * @param centreDirty
     * @param saveAsName -- represents a configuration title to be updated in UI after returning to client application (if present; otherwise nothing will be updated)
     * @param configUuid -- represents uuid of configuration to be updated in UI after returning to client application (if present; otherwise nothing will be updated)
     * @param autoRun -- represents autoRun parameter of configuration to be updated in UI after returning to client application (if present; otherwise nothing will be updated)
     * @param saveAsDesc -- represents a configuration title's tooltip to be updated in UI after returning to client application (if present; otherwise nothing will be updated)
     * @param staleCriteriaMessage
     * @param preferredView -- represents a configuration preferred view index to be updated in UI after returning to client application (if present; otherwise nothing will be updated)
     *
     * @return
     */
    static Map<String, Object> createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
        final Map<String, Map<String, Object>> criteriaMetaValues,
        final boolean centreDirty,
        final Optional<Optional<String>> saveAsName,
        final Optional<Optional<String>> configUuid,
        final Optional<Boolean> autoRun,
        final Optional<Optional<String>> saveAsDesc,
        final Optional<Optional<String>> staleCriteriaMessage,
        final Optional<Integer> preferredView
    ) {
        final Map<String, Object> customObject = createCriteriaMetaValuesCustomObject(criteriaMetaValues, centreDirty);
        saveAsName.ifPresent(name -> {
            customObject.put(SAVE_AS_NAME, name.orElse(""));
        });
        configUuid.ifPresent(uuid -> {
            customObject.put(CONFIG_UUID, uuid.orElse(""));
        });
        autoRun.ifPresent(autoRunning -> {
            customObject.put(AUTO_RUN, autoRunning);
        });
        saveAsDesc.ifPresent(desc -> {
            customObject.put(SAVE_AS_DESC, desc.orElse(""));
        });
        staleCriteriaMessage.ifPresent(message -> {
            customObject.put(STALE_CRITERIA_MESSAGE, message.orElse(null));
        });
        preferredView.ifPresent(prefView -> {
            customObject.put(PREFERRED_VIEW, prefView);
        });
        return customObject;
    }

    /**
     * Creates the 'custom object' that contain 'critMetaValues', 'centreDirty' flag (see {@link #createCriteriaMetaValuesCustomObject(Map, boolean, int)}) and 'staleCriteriaMessage'.
     *
     * @param criteriaMetaValues
     * @param centreDirty
     * @param staleCriteriaMessage
     *            -- if not <code>null</code> then the criteria is stale and the user will be informed about that ('orange' config button), otherwise (if <code>null</code>) -- the
     *            criteria were not changed and the user will be informed about that ('black' config button).
     *
     * @return
     */
    static Map<String, Object> createCriteriaMetaValuesCustomObject(final Map<String, Map<String, Object>> criteriaMetaValues, final boolean centreDirty, final String staleCriteriaMessage) {
        final Map<String, Object> customObject = createCriteriaMetaValuesCustomObject(criteriaMetaValues, centreDirty);
        customObject.put(STALE_CRITERIA_MESSAGE, staleCriteriaMessage);
        return customObject;
    }

    /**
     * Returns <code>true</code> if 'Run' action is performed represented by specified <code>customObject</code>, otherwise <code>false</code>.
     *
     * @param customObject
     * @return
     */
    public static boolean isRunning(final Map<String, Object> customObject) {
        return RunActions.RUN.toString().equals(customObject.get("@@action"));
    }

    /**
     * Returns <code>true</code> if 'Sorting' action is performed, otherwise <code>false</code>.
     *
     * @param customObject
     * @return
     */
    public static boolean isSorting(final Map<String, Object> customObject) {
        return customObject.containsKey("@@sortingAction");
    }

    /**
     * Returns <code>true</code> if 'autoRunning' action is performed, otherwise <code>false</code>.
     *
     * @param customObject
     * @return
     */
    public static boolean isAutoRunning(final Map<String, Object> customObject) {
        return customObject.containsKey("@@autoRunning");
    }

    /**
     * Creates the pair of 'custom object' (that contain 'resultEntities' and 'pageCount') and 'resultEntities' (query run is performed
     * inside).
     *
     * @param customObject
     * @param updatedPreviouslyRunCriteriaEntity -- criteria entity created from PREVIOUSLY_RUN surrogate centre, which was potentially updated from FRESH (in case of "running" action), but not yet actually used for running
     * @param additionalFetchProvider
     * @param additionalFetchProviderForTooltipProperties
     * @param createdByUserConstraint -- if exists then constraints the query by equality to the property 'createdBy'
     * @return
     */
    static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Pair<Map<String, Object>, List<?>> createCriteriaMetaValuesCustomObjectWithResult(
            final Map<String, Object> customObject,
            final M updatedPreviouslyRunCriteriaEntity,
            final Optional<IFetchProvider<T>> additionalFetchProvider,
            final Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties,
            final Optional<Pair<IQueryEnhancer<T>, Optional<CentreContext<T, ?>>>> queryEnhancerAndContext,
            final Optional<User> createdByUserConstraint) {
        final Map<String, Object> resultantCustomObject = new LinkedHashMap<>();

        updatedPreviouslyRunCriteriaEntity.getGeneratedEntityController().setEntityType(updatedPreviouslyRunCriteriaEntity.getEntityClass());
        if (additionalFetchProvider.isPresent()) {
            updatedPreviouslyRunCriteriaEntity.setAdditionalFetchProvider(additionalFetchProvider.get());
        }
        if (additionalFetchProviderForTooltipProperties.isPresent()) {
            updatedPreviouslyRunCriteriaEntity.setAdditionalFetchProviderForTooltipProperties(additionalFetchProviderForTooltipProperties.get());
        }
        if (queryEnhancerAndContext.isPresent()) {
            final IQueryEnhancer<T> queryEnhancer = queryEnhancerAndContext.get().getKey();
            updatedPreviouslyRunCriteriaEntity.setAdditionalQueryEnhancerAndContext(queryEnhancer, queryEnhancerAndContext.get().getValue());
        }
        if (createdByUserConstraint.isPresent()) {
            updatedPreviouslyRunCriteriaEntity.setCreatedByUserConstraint(createdByUserConstraint.get());
        }
        IPage<T> page = null;
        final List<T> data;
        // At this stage all the necessary validations have succeeded and actual running is about to be performed.
        // The 'updatedPreviouslyRunCriteriaEntity' instance represents the criteria entity created from PREVIOUSLY_RUN surrogate centre.
        // For refresh / navigate action this instance was not changed from previous run / refresh / navigate action.
        // For run action this instance was already updated from FRESH surrogate centre and includes "fresh" pageCapacity for the purposes of running
        //  (the only way to make FRESH pageCapacity different from PREVIOUSLY_RUN is to change it using Customise Columns and press DISCARD on selection criteria).
        final Integer pageCapacity = updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().getPageCapacity();
        final String action = (String) customObject.get("@@action");
        if (isRunning(customObject)) {
            page = updatedPreviouslyRunCriteriaEntity.run(pageCapacity);
            resultantCustomObject.put("summary", page.summary());
            data = page.data();
        } else if (RunActions.REFRESH.toString().equals(action)) {
            final Integer pageNumber = (Integer) customObject.get("@@pageNumber");
            final Pair<IPage<T>, T> refreshedData = updatedPreviouslyRunCriteriaEntity.getPageWithSummaries(pageNumber, pageCapacity);
            page = refreshedData.getKey();
            data = page.data();
            resultantCustomObject.put("summary", refreshedData.getValue());
        } else if (RunActions.NAVIGATE.toString().equals(action)) {
            final Integer pageNumber = (Integer) customObject.get("@@pageNumber");
            try {
                page = updatedPreviouslyRunCriteriaEntity.getPage(pageNumber, pageCapacity);
            } catch (final Exception e) {
                logger.error(e);
                final Pair<IPage<T>, T> navigatedData = updatedPreviouslyRunCriteriaEntity.getPageWithSummaries(pageNumber, pageCapacity);
                page = navigatedData.getKey();
                resultantCustomObject.put("summary", navigatedData.getValue());
            }
            data = page.data();
        } else {
            data = new ArrayList<>();
        }
        resultantCustomObject.put("resultEntities", data);
        resultantCustomObject.put("columnWidths", createColumnWidths(updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick(), updatedPreviouslyRunCriteriaEntity.getEntityClass()));
        resultantCustomObject.put("resultConfig", createResultConfigObject(updatedPreviouslyRunCriteriaEntity));
        resultantCustomObject.put("pageNumber", page == null ? 0 /* TODO ? */: page.no());
        resultantCustomObject.put("pageCount", page == null ? 0 /* TODO ? */: page.numberOfPages());
        return new Pair<>(resultantCustomObject, data);
    }

    /**
     * Creates custom resultant object containing running configuration (e.g. pageCapacity, visibleRowsCount, visibleColumnsWithOrder etc.) to be sent to the client-side application.
     * 
     * @param updatedPreviouslyRunCriteriaEntity -- criteria entity created from PREVIOUSLY_RUN surrogate centre, which was potentially updated from FRESH (in case of "running" action)
     * @return
     */
    private static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Map<String, Object> createResultConfigObject(final M updatedPreviouslyRunCriteriaEntity) {
        final Map<String, Object> resultConfigObject = new LinkedHashMap<>();
        resultConfigObject.put("visibleColumnsWithOrder", updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().usedProperties(updatedPreviouslyRunCriteriaEntity.getEntityClass()));
        resultConfigObject.put("orderingConfig", createOrderingProperties(updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().orderedProperties(updatedPreviouslyRunCriteriaEntity.getEntityClass())));
        resultConfigObject.put("pageCapacity", updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().getPageCapacity());
        resultConfigObject.put("visibleRowsCount", updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().getVisibleRowsCount());
        resultConfigObject.put("numberOfHeaderLines", updatedPreviouslyRunCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().getNumberOfHeaderLines());
        return resultConfigObject;
    }

    private static List<Map<String, String>> createOrderingProperties(final List<Pair<String, Ordering>> orderedProperties) {
        return orderedProperties.stream().map(pair -> {
            final Map<String, String> prop = new HashMap<>();
            prop.put("property", pair.getKey());
            prop.put("sorting", pair.getValue().name());
            return prop;
        }).collect(Collectors.toList());
    }

    /**
     * This method is similar to {@link #createCriteriaMetaValuesCustomObjectWithResult(Map, EnhancedCentreEntityQueryCriteria, Optional, Optional, Optional)}, but instead of returning a list of entities,
     * it returns a stream.
     *
     * @param adhocParams
     * @param criteriaEntity
     * @param additionalFetchProvider
     * @param additionalFetchProviderForTooltipProperties
     * @param queryEnhancerAndContext
     * @param createdByUserConstraint
     * @return
     */
    static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Stream<T> createCriteriaMetaValuesCustomObjectWithStream(
            final Map<String, Object> adhocParams,
            final M criteriaEntity,
            final Optional<IFetchProvider<T>> additionalFetchProvider,
            final Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties,
            final Optional<Pair<IQueryEnhancer<T>, Optional<CentreContext<T, ?>>>> queryEnhancerAndContext,
            final List<List<DynamicColumnForExport>> dynamicProperties,
            final Optional<User> createdByUserConstraint) {
        criteriaEntity.getGeneratedEntityController().setEntityType(criteriaEntity.getEntityClass());
        if (additionalFetchProvider.isPresent()) {
            criteriaEntity.setAdditionalFetchProvider(additionalFetchProvider.get());
        }
        if (additionalFetchProviderForTooltipProperties.isPresent()) {
            criteriaEntity.setAdditionalFetchProviderForTooltipProperties(additionalFetchProviderForTooltipProperties.get());
        }
        if (queryEnhancerAndContext.isPresent()) {
            final IQueryEnhancer<T> queryEnhancer = queryEnhancerAndContext.get().getKey();
            criteriaEntity.setAdditionalQueryEnhancerAndContext(queryEnhancer, queryEnhancerAndContext.get().getValue());
        }
        if (createdByUserConstraint.isPresent()) {
            criteriaEntity.setCreatedByUserConstraint(createdByUserConstraint.get());
        }
        criteriaEntity.setDynamicProperties(dynamicProperties);

        final int fetchSize = min(ofNullable((Integer) adhocParams.get("fetchSize")).orElse(100), 100);
        final Long[] ids = adhocParams.get("ids") != null ? (Long[]) adhocParams.get("ids") : new Long[]{};
        return criteriaEntity.streamEntities(fetchSize, ids);
    }

    ///////////////////////////////// CUSTOM OBJECTS [END] ///////////////////////////

    @SuppressWarnings("serial")
    private static Map<String, Map<String, Integer>> createColumnWidths(final IAddToResultTickManager secondTick, final Class<?> root) {
        final Map<String, Map<String, Integer>> columnWidths = secondTick.checkedProperties(root).stream()
        .map(property -> new Pair<>(property, new HashMap<String, Integer>() {{
                            put("newWidth", secondTick.getWidth(root, property));
                            put("newGrowFactor", secondTick.getGrowFactor(root, property));
                        }}))
        .collect(Collectors.toMap(pair -> pair.getKey(), pair -> pair.getValue()));
        return columnWidths;
    }

    /**
     * Creates the holder of meta-values (missingValue, not, exclusive etc.) for criteria of concrete [miType].
     * <p>
     * The holder should contain only those meta-values, which are different from default values, that are defined in {@link DefaultValueContract}. Client-side logic should also be
     * smart-enough to understand which values are currently relevant, even if they do not exist in transferred object.
     *
     * @param miType
     * @param gdtm
     * @return
     */
    static Map<String, Map<String, Object>> createCriteriaMetaValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root) {
        final Map<String, Map<String, Object>> metaValues = new LinkedHashMap<>();
        for (final String checkedProp : cdtmae.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(checkedProp)) {
                metaValues.put(checkedProp, createMetaValuesFor(root, checkedProp, cdtmae));
            }
        }
        return metaValues;
    }

    /**
     * Creates the map of meta-values for the specified property based on cdtmae configuration.
     *
     * @param root
     * @param prop
     * @param cdtmae
     * @param dvc
     * @return
     */
    private static Map<String, Object> createMetaValuesFor(final Class<AbstractEntity<?>> root, final String prop, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final IAddToCriteriaTickManager tickManager = cdtmae.getFirstTick();

        final Map<String, Object> metaValues = new LinkedHashMap<>();

        if (isDoubleCriterion(managedType(root, cdtmae), prop) && !isBooleanCriterion(managedType(root, cdtmae), prop)) {
            final Boolean exclusive = tickManager.getExclusive(root, prop);
            if (!isExclusiveDefault(exclusive)) {
                metaValues.put("exclusive", exclusive);
            }
            final Boolean exclusive2 = tickManager.getExclusive2(root, prop);
            if (!isExclusive2Default(exclusive2)) {
                metaValues.put("exclusive2", exclusive2);
            }
        }
        final Class<?> propertyType = StringUtils.isEmpty(prop) ? managedType(root, cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(root, cdtmae), prop);
        if (EntityUtils.isDate(propertyType)) {
            final DateRangePrefixEnum datePrefix = tickManager.getDatePrefix(root, prop);
            if (!isDatePrefixDefault(datePrefix)) {
                metaValues.put("datePrefix", datePrefix);
            }
            final MnemonicEnum dateMnemonic = tickManager.getDateMnemonic(root, prop);
            if (!isDateMnemonicDefault(dateMnemonic)) {
                metaValues.put("dateMnemonic", dateMnemonic);
            }
            final Boolean andBefore = tickManager.getAndBefore(root, prop);
            if (!isAndBeforeDefault(andBefore)) {
                metaValues.put("andBefore", andBefore);
            }
        }
        final Boolean orNull = tickManager.getOrNull(root, prop);
        if (!isOrNullDefault(orNull)) {
            metaValues.put("orNull", orNull);
        }
        final Boolean not = tickManager.getNot(root, prop);
        if (!isNotDefault(not)) {
            metaValues.put("not", not);
        }
        final Integer orGroup = tickManager.getOrGroup(root, prop);
        if (!isOrGroupDefault(orGroup)) {
            metaValues.put("orGroup", orGroup);
        }

        return metaValues;
    }

    /**
     * Applies all meta values, that are located in 'modifiedPropertiesHolder', to cdtmae (taken from 'miType' and 'gdtm').
     *
     * @param miType
     * @param gdtm
     * @param modifiedPropertiesHolder
     */
    private static void applyMetaValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root, final Map<String, Object> modifiedPropertiesHolder) {
        final Map<String, Map<String, Object>> metaValues = (Map<String, Map<String, Object>>) modifiedPropertiesHolder.get("@@metaValues");

        for (final Entry<String, Map<String, Object>> propAndMetaValues : metaValues.entrySet()) {
            final String prop = propAndMetaValues.getKey();

            final Map<String, Object> mValues = propAndMetaValues.getValue();
            if (isDoubleCriterion(managedType(root, cdtmae), prop) && !isBooleanCriterion(managedType(root, cdtmae), prop)) {
                cdtmae.getFirstTick().setExclusive(root, prop, mValues.get("exclusive") != null ? (Boolean) mValues.get("exclusive") : null);
                cdtmae.getFirstTick().setExclusive2(root, prop, mValues.get("exclusive2") != null ? (Boolean) mValues.get("exclusive2") : null);
            }
            final Class<?> propertyType = StringUtils.isEmpty(prop) ? managedType(root, cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(root, cdtmae), prop);
            if (EntityUtils.isDate(propertyType)) {
                cdtmae.getFirstTick().setDatePrefix(root, prop, mValues.get("datePrefix") != null ? DateRangePrefixEnum.valueOf(mValues.get("datePrefix").toString()) : null);
                cdtmae.getFirstTick().setDateMnemonic(root, prop, mValues.get("dateMnemonic") != null ? MnemonicEnum.valueOf(mValues.get("dateMnemonic").toString()) : null);
                cdtmae.getFirstTick().setAndBefore(root, prop, mValues.get("andBefore") != null ? (Boolean) mValues.get("andBefore") : null);
            }

            cdtmae.getFirstTick().setOrNull(root, prop, mValues.get("orNull") != null ? (Boolean) mValues.get("orNull") : null);
            cdtmae.getFirstTick().setNot(root, prop, mValues.get("not") != null ? (Boolean) mValues.get("not") : null);
            cdtmae.getFirstTick().setOrGroup(root, prop, mValues.get("orGroup") != null ? (Integer) mValues.get("orGroup") : null);
        }
    }

    /**
     * Creates the validation prototype for criteria entity of concrete [miType].
     * <p>
     * The entity creation process uses rigorous generation of criteria type and the instance every time (based on cdtmae of concrete miType).
     *
     * @param miType
     * @param critGenerator
     * @return
     */
    static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaValidationPrototype(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final ICentreDomainTreeManagerAndEnhancer cdtmae,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final Long previousVersion,
            final User user,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        // generates validation prototype
        final MiType miTypeAnnotation = new MiTypeAnnotation().newInstance(miType);
        final Annotation [] annotations = saveAsName.isPresent() ? new Annotation[] {miTypeAnnotation, new SaveAsNameAnnotation().newInstance(saveAsName.get())} : new Annotation[] {miTypeAnnotation};
        final M validationPrototype = (M) critGenerator.generateCentreQueryCriteria((Class<T>) getEntityType(miType), cdtmae, miType, annotations);

        validationPrototype.setMiType(miType);
        validationPrototype.setDevice(device);

        // Functions for companion implementations:

        // returns an updated version of centre
        validationPrototype.setFreshCentreSupplier(() -> updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder));
        validationPrototype.setSavedCentreSupplier(() -> updateCentre(user, miType, SAVED_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder));
        validationPrototype.setPreviouslyRunCentreSupplier(() -> updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder));
        
        // returns whether centre (defined by 'specificSaveAsName, freshCentreSupplier' arguments) is changed from previously saved (or the very original) configuration version or it is New (aka default, link or inherited)
        validationPrototype.setCentreDirtyCalculator(specificSaveAsName -> freshCentreSupplier ->
            isDefaultOrLink(specificSaveAsName) // this is very cheap operation
            || isFreshCentreChanged( // this operation has been chosen to be calculated before isInherited as it is assumed to be slightly cheaper than isInherited
                freshCentreSupplier.get(),
                updateCentre(user, miType, SAVED_CENTRE_NAME, specificSaveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)
            )
            || isInherited(specificSaveAsName, validationPrototype)
        );
        
        // returns whether centre is changed from previously saved (or the very original) configuration version or it is New (aka default, link or inherited)
        validationPrototype.setCentreDirtyGetter(() -> validationPrototype.centreDirtyCalculator().apply(saveAsName).apply(() -> updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)));
        // creates criteria validation prototype for concrete saveAsName
        validationPrototype.setCriteriaValidationPrototypeCreator(validationPrototypeSaveAsName ->
            createCriteriaValidationPrototype(
                miType,
                validationPrototypeSaveAsName,
                updateCentre(user, miType, FRESH_CENTRE_NAME, validationPrototypeSaveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                companionFinder, critGenerator, -1L,
                user,
                device,
                domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel
            )
        );
        // creates custom object representing centre information for concrete criteriaEntity and saveAsName
        validationPrototype.setCentreCustomObjectGetter(appliedCriteriaEntity -> customObjectSaveAsName -> configUuid -> preferredView -> {
            final ICentreDomainTreeManagerAndEnhancer freshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
            // In both cases (criteria entity valid or not) create customObject with criteriaEntity to be returned and bound into tg-entity-centre after save.
            final Map<String, Object> customObject = createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
                createCriteriaMetaValues(freshCentre, getEntityType(miType)),
                validationPrototype.centreDirtyCalculator().apply(customObjectSaveAsName).apply(() -> freshCentre),
                of(customObjectSaveAsName),
                configUuid,
                of(false), // even though configuration can be runAutomatically, do not perform auto-running on any action (except Load, see CentreConfigLoadActionDao)
                of(validationPrototype.centreTitleAndDesc(customObjectSaveAsName).map(titleDesc -> titleDesc._2)),
                empty(),
                preferredView
            );
            customObject.put(APPLIED_CRITERIA_ENTITY_NAME, appliedCriteriaEntity);
            return customObject;
        });
        // performs mutation function centreConsumer against FRESH and PREVIOUSLY_RUN centres and saves them into persistent storage
        validationPrototype.setCentreAdjuster(centreConsumer -> {
            final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            centreConsumer.accept(freshCentre);
            commitCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, freshCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

            final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            centreConsumer.accept(previouslyRunCentre);
            commitCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, previouslyRunCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        });
        // performs mutation function centreConsumer (column widths adjustments) against PREVIOUSLY_RUN centre and copies column widths / grow factors directly to FRESH centre; saves them both into persistent storage
        validationPrototype.setCentreColumnWidthsAdjuster(centreConsumer -> {
            // we have diffs that need to be applied against 'previouslyRun' centre
            final ICentreDomainTreeManagerAndEnhancer centre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            centreConsumer.accept(centre);
            commitCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, centre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

            // however those diffs are not applicable to 'fresh' centre due to ability of 'fresh' centre to differ from 'previouslyRun' centre
            // the only way to get such mismatch is to press Discard on selection criteria
            // that's why we need to carefully override only widths and grow factors of 'fresh' centre from 'previouslyRun' centre
            // all other unrelated to CentreColumnWidthConfigUpdater information should remain 'as is'
            final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            freshCentre.getSecondTick().setWidthsAndGrowFactors(previouslyRunCentre. getSecondTick().getWidthsAndGrowFactors());
            commitCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, freshCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        });
        // performs deletion of current owned configuration
        validationPrototype.setCentreDeleter(() ->
            // removes the associated surrogate centres
            removeCentres(user, miType, device, saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME, PREVIOUSLY_RUN_CENTRE_NAME)
        );
        // overrides SAVED centre configuration by FRESH one -- 'saves' centre
        validationPrototype.setFreshCentreSaver(() -> {
            final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            commitCentreWithoutConflicts(user, miType, SAVED_CENTRE_NAME, saveAsName, device, freshCentre, null, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        });
        // overrides FRESH default centre configuration by FRESH current centre configuration; makes default config as preferred -- 'duplicates' centre
        validationPrototype.setConfigDuplicateAction(() -> {
            final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            commitCentreWithoutConflicts(user, miType, FRESH_CENTRE_NAME, empty(), device, freshCentre, null, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            findConfigOpt(miType, user, NAME_OF.apply(FRESH_CENTRE_NAME).apply(empty()).apply(device), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("runAutomatically")).ifPresent(config -> {
                eccCompanion.saveWithoutConflicts(config.setRunAutomatically(validationPrototype.centreRunAutomatically(saveAsName))); // copy runAutomatically from current configuration (saveAsName) into default configuration (empty())
            });
            // when switching to default configuration we need to make it preferred
            validationPrototype.makePreferredConfig(empty()); // 'default' kind -- can be preferred; only 'link / inherited from shared' can not be preferred
        });
        // updates inherited centre with title 'saveAsNameToLoad' from upstream base user's configuration -- just before LOAD action
        validationPrototype.setInheritedFromBaseCentreUpdater(saveAsNameToLoad -> {
            // determine current preferred configuration
            final Optional<String> preferredConfigName = retrievePreferredConfigName(user, miType, device, companionFinder, webUiConfig);
            // determine whether inherited configuration is changed
            final boolean centreChanged = isFreshCentreChanged(
                updateCentre(user, miType, FRESH_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                updateCentre(user, miType, SAVED_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)
            );
            if (centreChanged) { // if there are some user changes, only SAVED surrogate must be updated; if such centre will be discarded the base user changes will be loaded immediately
                removeCentres(user, miType, device, of(saveAsNameToLoad), eccCompanion, SAVED_CENTRE_NAME);
            } else { // otherwise base user changes will be loaded immediately after centre loading
                removeCentres(user, miType, device, of(saveAsNameToLoad), eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
            }
            updateCentre(user, miType, FRESH_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            updateCentre(user, miType, SAVED_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder); // do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly

            if (equalsEx(preferredConfigName, of(saveAsNameToLoad))) { // if inherited configuration being updated was preferred
                makePreferred(user, miType, of(saveAsNameToLoad), device, companionFinder, webUiConfig); // then must leave it preferred after deletion
            }
        });
        // updates inherited centre with title 'saveAsNameToLoad' from upstream shared configuration -- just before LOAD action
        validationPrototype.setInheritedFromSharedCentreUpdater(saveAsNameToLoad -> configUuid -> {
            return updateInheritedFromShared(configUuid, miType, device, of(saveAsNameToLoad), user, eccCompanion, of(() -> isFreshCentreChanged(
                updateCentre(user, miType, FRESH_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                updateCentre(user, miType, SAVED_CENTRE_NAME, of(saveAsNameToLoad), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)
            )))
            .map(upstreamConfig -> of(obtainTitleFrom(upstreamConfig.getTitle(), SAVED_CENTRE_NAME, device)))
            .orElseGet(() -> of(saveAsNameToLoad));
        });
        // clears default centre and fully prepares it for usage
        validationPrototype.setDefaultCentreClearer(() -> {
            removeCentres(user, miType, device, empty(), eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME, PREVIOUSLY_RUN_CENTRE_NAME);
            updateCentre(user, miType, FRESH_CENTRE_NAME, empty(), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            updateCentre(user, miType, SAVED_CENTRE_NAME, empty(), device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder); // do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly
        });
        // applies new criteria from client application against FRESH centre and returns respective criteria entity
        validationPrototype.setFreshCentreApplier(modifHolder -> {
            return createCriteriaEntityWithoutConflicts(modifHolder, companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        });
        // returns title / desc for named (inherited or owned) configuration and empty optional for unnamed (default) configuration
        validationPrototype.setCentreTitleAndDescGetter(saveAsNameForTitleAndDesc -> {
            return saveAsNameForTitleAndDesc.map(name -> t2(name, updateCentreDesc(user, miType, of(name), device, eccCompanion)));
        });
        // returns runAutomatically from Centre DSL config
        validationPrototype.setDefaultRunAutomaticallySupplier(() -> defaultRunAutomatically(miType, webUiConfig));
        // returns runAutomatically for named (inherited or owned, also link) configuration and unnamed (default) configuration
        validationPrototype.setCentreRunAutomaticallyGetter(saveAsNameForRunAutomatically -> {
            return updateCentreRunAutomatically(user, miType, saveAsNameForRunAutomatically, device, eccCompanion, webUiConfig, validationPrototype);
        });
        // returns dashboardable indicator for named (inherited or owned) configuration and false for unnamed (default) configuration
        validationPrototype.setCentreDashboardableGetter(saveAsNameForDashboardable -> {
            return saveAsNameForDashboardable.map(name -> updateCentreDashboardable(user, miType, of(name), device, eccCompanion)).orElse(false);
        });
        // returns dashboard refresh frequency for named (inherited or owned) configuration and null for unnamed (default) configuration
        validationPrototype.setCentreDashboardRefreshFrequencyGetter(saveAsNameForDashboardable -> {
            return saveAsNameForDashboardable.map(name -> updateCentreDashboardRefreshFrequency(user, miType, of(name), device, eccCompanion)).orElse(null);
        });
        // returns configUuid for named (inherited, owned or link) configuration and empty optional for unnamed (default) configuration
        validationPrototype.setCentreConfigUuidGetter(saveAsNameForConfigUuid -> {
            return updateCentreConfigUuid(user, miType, saveAsNameForConfigUuid, device, eccCompanion);
        });
        // changes title / desc for current saveAsName'd configuration; returns custom object containing centre information
        validationPrototype.setCentreEditor(newName -> newDesc -> dashboardable -> dashboardRefreshFrequency -> {
            editCentreTitleAndDesc(user, miType, saveAsName, device, newName, newDesc, dashboardable, dashboardRefreshFrequency, eccCompanion);
            // currently loaded configuration should remain preferred -- no action is required
            return validationPrototype.centreCustomObject(
                createCriteriaEntity(validationPrototype.centreContextHolder().getModifHolder(), companionFinder, critGenerator, miType, of(newName), user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel),
                of(newName),
                empty(), // no need to update already existing client-side configUuid
                empty() // no need to update already loaded preferredView
            );
        });
        // changes runAutomatically for current saveAsName'd configuration; returns custom object containing centre information
        validationPrototype.setCentreConfigurator(runAutomatically -> {
            configureCentre(user, miType, saveAsName, device, runAutomatically, eccCompanion);
            // currently loaded configuration should remain preferred -- no action is required
            return validationPrototype.centreCustomObject(
                createCriteriaEntity(validationPrototype.centreContextHolder().getModifHolder(), companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel),
                saveAsName,
                empty(), // no need to update already existing client-side configUuid
                empty() // no need to update already loaded preferredView
            );
        });
        // performs copying of current configuration with the specified title / desc; makes it preferred; returns custom object containing centre information
        validationPrototype.setCentreSaver(newName -> newDesc -> dashboardable -> dashboardRefreshFrequency -> {
            final Optional<String> newSaveAsName = of(newName);
            final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            // save 'freshCentre' with a new name into FRESH / SAVED -- button SAVE will be disabled
            final String newConfigUuid = randomUUID().toString();
            final Function<String, Consumer<String>> createAndOverrideUuid = newDescription -> surrogateName -> {
                commitCentre(user, miType, surrogateName, newSaveAsName, device, freshCentre, newDescription, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                findConfigOpt(miType, user, NAME_OF.apply(surrogateName).apply(newSaveAsName).apply(device), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("configUuid").with("dashboardable").with("dashboardableDate").with("dashboardRefreshFrequency").with("runAutomatically"))
                    .ifPresent(config -> {
                        if (FRESH_CENTRE_NAME.equals(surrogateName)) {
                            config.setDashboardable(dashboardable);
                            config.setDashboardRefreshFrequency(dashboardRefreshFrequency);
                            config.setRunAutomatically(validationPrototype.centreRunAutomatically(saveAsName)); // copy runAutomatically from currently loaded centre configuration being copied
                        }
                        eccCompanion.saveWithConflicts(config.setConfigUuid(newConfigUuid));
                    }); // update with newConfigUuid
            };
            createAndOverrideUuid.apply(newDesc).accept(FRESH_CENTRE_NAME);
            createAndOverrideUuid.apply(null).accept(SAVED_CENTRE_NAME);
            
            // when switching to new configuration we need to make it preferred
            makePreferred(user, miType, newSaveAsName, device, companionFinder, webUiConfig); // it is of 'own save-as' kind -- can be preferred; only 'link / inherited from shared' can not be preferred
            return validationPrototype.centreCustomObject(
                createCriteriaEntity(validationPrototype.centreContextHolder().getModifHolder(), companionFinder, critGenerator, miType, newSaveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel),
                newSaveAsName,
                of(of(newConfigUuid)),
                empty() // do not update preferredView on client when copying centre configuration
            );
        });
        // returns ordered alphabetically list of 'loadable' configurations for current user
        validationPrototype.setLoadableCentresSupplier(saveAsNameOpt -> () -> loadableConfigurations(user, miType, device, companionFinder, sharingModel).apply(saveAsNameOpt));
        // returns currently loaded configuration's saveAsName
        validationPrototype.setSaveAsNameSupplier(() -> saveAsName);
        // makes 'saveAsNameToBecomePreferred' configuration preferred in case where it differs from currently loaded configuration; does nothing otherwise
        validationPrototype.setPreferredConfigMaker(saveAsNameToBecomePreferred -> {
            if (!equalsEx(saveAsNameToBecomePreferred, saveAsName)) {
                // please note currently loaded configuration can be preferred (default, own save-as, base) or not (link, shared);
                // for embedded centres only default configuration can be preferred, but still named configurations may exist
                makePreferred(user, miType, saveAsNameToBecomePreferred, device, companionFinder, webUiConfig);
            }
        });

        final Field idField = Finder.getFieldByName(validationPrototype.getType(), AbstractEntity.ID);
        final boolean idAccessible = idField.isAccessible();
        idField.setAccessible(true);
        try {
            idField.set(validationPrototype, CRITERIA_ENTITY_ID); // here the fictional id is populated to mark the entity as persisted!
            idField.setAccessible(idAccessible);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            idField.setAccessible(idAccessible);
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        final Field versionField = Finder.getFieldByName(validationPrototype.getType(), AbstractEntity.VERSION);
        final boolean accessible = versionField.isAccessible();
        versionField.setAccessible(true);
        try {
            // Here the version of validation prototype is set to be increased comparing to previousVersion.
            // This action is necessary to indicate that the criteria entity was changed (by other fictional user) since new modifications arrive.
            // But to be clear -- the criteria entity is 'changed' because it is originated from ICDTMAE, which has been changed during previous validation cycle.
            versionField.set(validationPrototype, previousVersion + 1);
            versionField.setAccessible(accessible);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            versionField.setAccessible(accessible);
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        // IMPORTANT: after the creation of criteria validation prototype there can exist an 'required' validation errors.
        //     But, why? It seems, that just newly created entity should be empty.. But this is not the case --
        //     the entity has been already applied the values from 'cdtmae' during CriteriaGenerator generation process.
        //     So, we potentially have the 'required' errors -- need to disregard all of them!
        //     (for e.g., in TridentFleet it fixes the errors if no DdsStationAssigner is specified)
        return EntityResourceUtils.disregardUntouchedRequiredProperties(
                resetMetaStateForCriteriaValidationPrototype(
                        validationPrototype,
                        EntityResourceUtils.getOriginalManagedType(validationPrototype.getType(), cdtmae)//
                ), new LinkedHashSet<>()//
        );
    }

    /**
     * Resets the meta state for the specified criteria entity.
     * <p>
     * The meta state reset is necessary to make the criteria entity like 'just saved and retrieved from the database' (originalValues should be equal to values).
     * <p>
     * UPDATE: resetting of the values has been enhanced (comparing to just invoking resetMetaState() on entity) with the functionality, for which the detailed comment is inside
     * the method implementation.
     *
     *
     * @param criteriaValidationPrototype
     * @param originalManagedType
     * @return
     */
    private static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M resetMetaStateForCriteriaValidationPrototype(final M criteriaValidationPrototype, final Class<?> originalManagedType) {
        // standard resetting of meta-values: copies values into originalValues for all properties:
        criteriaValidationPrototype.resetMetaState();

        final Class<M> criteriaType = (Class<M>) criteriaValidationPrototype.getType();
        // For non-single entity-typed criteria properties (the properties with type List<String>, which were originated from entity-typed properties)
        //   there is a need to populate 'value' into 'originalValue' manually.
        // This is necessary due to the following:
        //     1) these properties have type List<String> and are treated as collectional in MetaProperty (see AbstractEntity's setMetaPropertyFactory method);
        //     2) originalValue is not set for collectional properties during resetMetaState() method (see MetaProperty's setOriginalValue method);
        //     3) but originalValue is still necessary for criteria entity validation.
        final List<Field> propFields = Finder.findProperties(criteriaType);
        for (final Field propField : propFields) {
            final String originalProperty = CriteriaReflector.getCriteriaProperty(criteriaType, propField.getName());
            if (List.class.isAssignableFrom(propField.getType()) && isMultiEntityTypedProperty(originalProperty, originalManagedType)) { // only List<String> is needed
                final MetaProperty<List<String>> metaProperty = criteriaValidationPrototype.getProperty(propField.getName());

                final Field originalValueField = Finder.findFieldByName(MetaPropertyFull.class, "originalValue");
                final boolean originalValueAccessible = originalValueField.isAccessible();
                originalValueField.setAccessible(true);
                try {
                    originalValueField.set(metaProperty, metaProperty.getValue()); // here 'value' is populated into 'originalValue' for non-single entity-typed criteria property
                    originalValueField.setAccessible(originalValueAccessible);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    originalValueField.setAccessible(originalValueAccessible);
                    logger.error(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            }
        }
        return criteriaValidationPrototype;
    }

    /**
     * Determines whether the property represents a) entity-typed property or b) crit-only 'multi' entity-typed property.
     * <p>
     * if <code>true</code> -- this means that criterion, generated for that property, will be 'multi', if <code>false</code> -- it will be 'single'.
     *
     * @param property
     * @param managedType
     * @return
     */
    private static boolean isMultiEntityTypedProperty(final String property, final Class<?> managedType) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, property);
        final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, property);
        final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());
        return EntityUtils.isEntityType(propertyType) && !single;
    }

    //////////////////////////////////////////////////// CREATE CENTRE CONTEXT FOR CENTRE RUN METHOD ETC. (context config is available) ////////////////////////////////////////////////////

    /**
     * Creates centre context based on serialisation {@link CentreContextHolder} entity.
     * <p>
     * Note: the control of which centreContext's parts should be initialised is provided by the server (using 'contextConfig'), but the client also filters out the context parts
     * that are not needed (the 'centreContextHolder' will contain only desirable context parts).
     *
     * @param centreContextHolder
     * @return
     */
    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Optional<CentreContext<T, ?>> createCentreContext(
            final boolean disregardOriginallyProducedEntities,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final M criteriaEntity,
            final Optional<CentreContextConfig> contextConfig,
            final String chosenProperty,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        if (contextConfig.isPresent()) {
            final CentreContext<T, AbstractEntity<?>> context = new CentreContext<>();
            final CentreContextConfig config = contextConfig.get();
            if (config.withSelectionCrit) {
                context.setSelectionCrit(criteriaEntity);
            }
            if (config.withAllSelectedEntities || config.withCurrentEtity) {
                context.setSelectedEntities(!centreContextHolder.proxiedPropertyNames().contains("selectedEntities") ? (List<T>) centreContextHolder.getSelectedEntities() : new ArrayList<>());
            }
            if (config.withMasterEntity) {
                context.setMasterEntity(restoreMasterFunctionalEntity(disregardOriginallyProducedEntities, webUiConfig, companionFinder, user, critGenerator, entityFactory, centreContextHolder, 0, device, domainTreeEnhancerCache, eccCompanion, mmiCompanion, userCompanion, sharingModel));
            }
            if (config.withComputation()) {
                context.setComputation(config.computation.get());
            }
            context.setChosenProperty(chosenProperty);
            context.setCustomObject(centreContextHolder != null && !centreContextHolder.proxiedPropertyNames().contains("customObject") ? centreContextHolder.getCustomObject() : new HashMap<>());
            return Optional.of(context);
        } else {
            return Optional.empty();
        }
    }

    //////////////////////////////////////////////////// CREATE CENTRE CONTEXT FOR CENTRE-DEPENDENT FUNCTIONAL ENTITIES ////////////////////////////////////////////////////

    /**
     * Creates centre context based on serialisation {@link CentreContextHolder} entity.
     * <p>
     * Note: the control of which centreContext's parts should be initialised is provided by the client (there are generated meta-information like 'requireSelectedEntities',
     * 'requireMasterEntity').
     *
     * @param actionConfig - the configuration of action for which this context is restored (used to restore computation function). It is not mandatory to
     *  specify this parameter as non-empty -- at this stage only centre actions are enabled with 'computation' part of the context.
     * @param centreContextHolder
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> CentreContext<T, AbstractEntity<?>> createCentreContext(
            final AbstractEntity<?> masterContext,
            final List<AbstractEntity<?>> selectedEntities,
            final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity,
            final Optional<EntityActionConfig> config,
            final String chosenProperty,
            final Map<String, Object> customObject
    ) {
        final CentreContext<T, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectionCrit(criteriaEntity);
        context.setSelectedEntities((List<T>) selectedEntities);
        context.setMasterEntity(masterContext);
        if (config.isPresent() && config.get().context.isPresent() && config.get().context.get().withComputation()) {
            context.setComputation(config.get().context.get().computation.get());
        }
        context.setChosenProperty(chosenProperty);
        context.setCustomObject(customObject);
        return context;
    }

    //////////////////////////////////////////// CRITERIA ENTITY CREATION ////////////////////////////////////////////

    /**
     * Creates selection criteria entity from {@link CentreContextHolder} entity (which contains modifPropsHolder).
     *
     * @param centreContextHolder
     * @return
     */
    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntityForContext(
        final CentreContextHolder centreContextHolder,
        final ICompanionObjectFinder companionFinder,
        final User user,
        final ICriteriaGenerator critGenerator,
        final IWebUiConfig webUiConfig,
        final EntityFactory entityFactory,
        final DeviceProfile device,
        final IDomainTreeEnhancerCache domainTreeEnhancerCache,
        final EntityCentreConfigCo eccCompanion,
        final MainMenuItemCo mmiCompanion,
        final IUser userCompanion,
        final ICentreConfigSharingModel sharingModel) {

        if (centreContextHolder.getCustomObject().get("@@miType") == null || isEmpty(!centreContextHolder.proxiedPropertyNames().contains("modifHolder") ? centreContextHolder.getModifHolder() : new HashMap<String, Object>())) {
            return null;
        }
        final Class<? extends MiWithConfigurationSupport<?>> miType;
        final Optional<String> saveAsName;
        try {
            miType = (Class<? extends MiWithConfigurationSupport<?>>) Class.forName((String) centreContextHolder.getCustomObject().get("@@miType"));
            final String saveAsNameString = (String) centreContextHolder.getCustomObject().get("@@saveAsName");
            saveAsName = "".equals(saveAsNameString) ? empty() : of(saveAsNameString);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        final M criteriaEntity = (M) createCriteriaEntityForPaginating(companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel).setCentreContextHolder(centreContextHolder);
        criteriaEntity.setExportQueryRunner(customObject -> stream(webUiConfig, user, entityFactory, companionFinder, critGenerator, centreContextHolder, criteriaEntity, customObject, device, domainTreeEnhancerCache, eccCompanion, mmiCompanion, userCompanion, sharingModel));
        return criteriaEntity;
    }

    /**
     * A method to stream entities based on the centre query, including various transformations such as custom properties etc.
     * It is used as part of initialisation for an export query runner.
     *
     * @param webUiConfig
     * @param serverGdtm
     * @param companionFinder
     * @param critGenerator
     * @param centreContextHolder
     * @param criteriaEntity
     * @param adhocParams
     * @param utils
     *
     * @return
     */
    private static <T extends AbstractEntity<?>> Stream<AbstractEntity<?>> stream(
        final IWebUiConfig webUiConfig,
        final User user,
        final EntityFactory entityFactory,
        final ICompanionObjectFinder companionFinder,
        final ICriteriaGenerator critGenerator,

        final CentreContextHolder centreContextHolder,
        final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity,
        final Map<String, Object> adhocParams,
        final DeviceProfile device,
        final IDomainTreeEnhancerCache domainTreeEnhancerCache,
        final EntityCentreConfigCo eccCompanion,
        final MainMenuItemCo mmiCompanion,
        final IUser userCompanion,
        final ICentreConfigSharingModel sharingModel) {

        final Class<? extends MiWithConfigurationSupport<?>> miType = EntityResourceUtils.getMiType((Class<EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>>) criteriaEntity.getClass());
        final EntityCentre<AbstractEntity<?>> centre = (EntityCentre<AbstractEntity<?>>) webUiConfig.getCentres().get(miType);
        adhocParams.putAll(centreContextHolder.getCustomObject());
        // at this stage (during exporting of centre data) appliedCriteriaEntity is valid, because it represents 'previouslyRun' centre criteria which is getting updated only if Run was initiated and selection criteria validation succeeded
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>) criteriaEntity;
        // if the export() invocation occurs on the centre that warrants data generation
        // then for an entity centre configuration check if a generator was provided
        final boolean createdByConstraintShouldOccur = centre.getGeneratorTypes().isPresent();

      //Build dynamic properties object
        final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext = CriteriaResource.getDynamicResultProperties(
                centre,
                webUiConfig,
                companionFinder,
                user,
                critGenerator,
                entityFactory,
                centreContextHolder,
                appliedCriteriaEntity,
                device,
                domainTreeEnhancerCache,
                eccCompanion,
                mmiCompanion,
                userCompanion,
                sharingModel);

        final Stream<AbstractEntity<?>> stream =
                CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithStream(
                        adhocParams,
                        appliedCriteriaEntity,
                        centre.getAdditionalFetchProvider(),
                        centre.getAdditionalFetchProviderForTooltipProperties(),
                        CriteriaResource.createQueryEnhancerAndContext(
                                webUiConfig,
                                companionFinder,
                                user,
                                critGenerator,
                                entityFactory,
                                centreContextHolder,
                                centre.getQueryEnhancerConfig(),
                                appliedCriteriaEntity,
                                device,
                                domainTreeEnhancerCache,
                                eccCompanion,
                                mmiCompanion,
                                userCompanion,
                                sharingModel),
                        createDynamicPropertiesForExport(centre, resPropsWithContext),
                        // There could be cases where the generated data and the queried data would have different types.
                        // For example, the queried data could be modelled by a synthesized entity that includes a subquery based on some generated data.
                        // In such cases, it is unpossible to enhance the final query with a user related condition automatically.
                        // This should be the responsibility of the application developer to properly construct a subquery that is based on the generated data.
                        // The query will be enhanced with condition createdBy=currentUser if createdByConstraintShouldOccur and generatorEntityType equal to the type of queried data (otherwise end-developer should do that itself by using queryEnhancer or synthesized model).
                        createdByConstraintShouldOccur && centre.getGeneratorTypes().get().getKey().equals(getEntityType(miType)) ? of(user) : empty());

        final Stream<AbstractEntity<?>> entities = enhanceResultEntitiesWithCustomPropertyValues(
                centre,
                centre.getCustomPropertiesDefinitions(),
                centre.getCustomPropertiesAsignmentHandler(),
                stream);

        return enhanceResultEntitiesWithDynamicPropertyValues(entities, resPropsWithContext);
    }


    private static List<List<DynamicColumnForExport>> createDynamicPropertiesForExport(final EntityCentre<AbstractEntity<?>> centre, final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext) {
        final List<List<DynamicColumnForExport>> dynamicColumns = new ArrayList<>();
        resPropsWithContext.forEach(resPropWithContext -> {
            centre.getDynamicColumnBuilderFor(resPropWithContext.getKey()).ifPresent(colBuilder -> {
                colBuilder.getColumnsConfig(resPropWithContext.getValue()).ifPresent(config -> dynamicColumns.add(config.buildToExport()));
            });
        });
        return dynamicColumns;
    }

    /**
     * Creates selection criteria entity from {@link CentreContextHolder} entity (which contains modifPropsHolder).
     *
     * @param centreContextHolder
     * @param isPaginating
     *            -- returns <code>true</code> in case when this method is a part of 'Paginating Actions', <code>false</code> otherwise
     * @return
     */
    protected static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntityForPaginating(
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        final ICentreDomainTreeManagerAndEnhancer updatedPreviouslyRunCentre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        return createCriteriaValidationPrototype(miType, saveAsName, updatedPreviouslyRunCentre, companionFinder, critGenerator, 0L, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
    }

    /**
     * Creates selection criteria entity from <code>modifPropsHolder</code>.
     *
     * @return
     */
    protected static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntity(
        final Map<String, Object> modifiedPropertiesHolder,
        final ICompanionObjectFinder companionFinder,
        final ICriteriaGenerator critGenerator,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final Optional<String> saveAsName,
        final User user,
        final DeviceProfile device,
        final IDomainTreeEnhancerCache domainTreeEnhancerCache,
        final IWebUiConfig webUiConfig,
        final EntityCentreConfigCo eccCompanion,
        final MainMenuItemCo mmiCompanion,
        final IUser userCompanion,
        final ICentreConfigSharingModel sharingModel) {
        return createCriteriaEntity(false, modifiedPropertiesHolder, companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
    }

    /**
     * Creates selection criteria entity from <code>modifPropsHolder</code>.
     * <p>
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     *
     * @return
     */
    protected static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntityWithoutConflicts(
        final Map<String, Object> modifiedPropertiesHolder,
        final ICompanionObjectFinder companionFinder,
        final ICriteriaGenerator critGenerator,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final Optional<String> saveAsName,
        final User user,
        final DeviceProfile device,
        final IDomainTreeEnhancerCache domainTreeEnhancerCache,
        final IWebUiConfig webUiConfig,
        final EntityCentreConfigCo eccCompanion,
        final MainMenuItemCo mmiCompanion,
        final IUser userCompanion,
        final ICentreConfigSharingModel sharingModel) {
        return createCriteriaEntity(true, modifiedPropertiesHolder, companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
    }

    /**
     * Creates selection criteria entity from <code>modifPropsHolder</code>.
     *
     * @param withoutConflicts -- <code>true</code> to avoid self-conflict checks, <code>false</code> otherwise; <code>true</code> only to be used NOT IN another SessionRequired transaction scope
     * @return
     */
    private static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntity(
            final boolean withoutConflicts,
            final Map<String, Object> modifiedPropertiesHolder,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        if (isEmpty(modifiedPropertiesHolder)) {
            throw new IllegalArgumentException("ModifiedPropertiesHolder should not be empty during invocation of fully fledged criteria entity creation.");
        }

        // load / update fresh centre if it is not loaded yet / stale
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        applyMetaValues(originalCdtmae, getEntityType(miType), modifiedPropertiesHolder);
        final M validationPrototype = createCriteriaValidationPrototype(miType, saveAsName, originalCdtmae, companionFinder, critGenerator, getVersion(modifiedPropertiesHolder), user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        final M appliedCriteriaEntity = constructCriteriaEntityAndResetMetaValues(
                modifiedPropertiesHolder,
                validationPrototype,
                getOriginalManagedType(validationPrototype.getType(), originalCdtmae),
                companionFinder//
        ).getKey();

        // need to commit changed fresh centre after modifiedPropertiesHolder has been applied!
        commitCentre(withoutConflicts, user, miType, FRESH_CENTRE_NAME, saveAsName, device, originalCdtmae, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        return appliedCriteriaEntity;
    }

    /**
     * Returns <code>true</code> in case when 'modifiedPropertiesHolder' is empty, and should not be used for 'criteriaValidationPrototype' application, <code>false</code>
     * otherwise.
     *
     * @param modifiedPropertiesHolder
     * @return
     */
    public static boolean isEmpty(final Map<String, Object> modifiedPropertiesHolder) {
        // TODO improve implementation?
        return !modifiedPropertiesHolder.containsKey(AbstractEntity.VERSION);
    }

    /**
     * Constructs the criteria entity from the client envelope and resets the original values of the entity to be equal to the values.
     * <p>
     * The envelope contains special version of entity called 'modifiedPropertiesHolder' which has only modified properties and potentially some custom stuff with '@' sign as the
     * prefix. All custom properties will be disregarded, but can be used later from the returning map.
     * <p>
     * All normal properties will be applied in 'validationPrototype'.
     *
     * @return applied validationPrototype and modifiedPropertiesHolder map
     */
    private static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Pair<M, Map<String, Object>> constructCriteriaEntityAndResetMetaValues(final Map<String, Object> modifiedPropertiesHolder, final M validationPrototype, final Class<?> originalManagedType, final ICompanionObjectFinder companionFinder) {
        return new Pair<>(
                resetMetaStateForCriteriaValidationPrototype(
                        EntityResourceUtils.apply(modifiedPropertiesHolder, validationPrototype, companionFinder),
                        originalManagedType
                ),
                modifiedPropertiesHolder//
        );
    }

    /**
     * Updates FRESH / SAVED / PREVIOUSLY_RUN versions of configuration for {@code user} from upstream configuration if it exists.<br>
     * Returns upstream configuration.
     * <p>
     * This method safely checks whether FRESH / SAVED / PREVIOUSLY_RUN configs exist.<br>
     * They should (FRESH / SAVED), if prior isInherited check was true.<br>
     * However in highly concurrent system {@code user} could have performed deletion of current configuration (very unlikely).<br>
     * PREVIOUSLY_RUN may not be present, and this is checked also.<br>
     * <p>
     * IMPORTANT: In this method saveAsName of inherited configuration may be changed to the one from upstream configuration.<br>
     *   In this case this new saveAsName must be taken from returning argument and used for further calculations and for returning to the client application.
     * 
     * @param checkChanges -- optional function to check whether there are local changes; if they are -- not update FRESH from upstream; if no such check is needed i.e. empty function is passed (e.g. when discarding) -- force FRESH centre updating
     */
    public static Optional<EntityCentreConfig> updateInheritedFromShared(
        final String configUuid,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final DeviceProfile device,
        final Optional<String> saveAsName,
        final User user,
        final EntityCentreConfigCo eccCompanion,
        final Optional<Supplier<Boolean>> checkChanges
    ) {
        return findConfigOptByUuid(configUuid, miType, device, SAVED_CENTRE_NAME, eccCompanion)
               .map(upstreamConfig -> updateInheritedFromShared(upstreamConfig, miType, device, saveAsName, user, eccCompanion, checkChanges));
    }

    /**
     * Updates FRESH / SAVED / PREVIOUSLY_RUN versions of configuration for {@code user} from upstream configuration.<br>
     * Returns upstream configuration back.
     * <p>
     * This method safely checks whether FRESH / SAVED / PREVIOUSLY_RUN configs exist.<br>
     * They should (FRESH / SAVED), if prior isInherited check was true.<br>
     * However in highly concurrent system {@code user} could have performed deletion of current configuration (very unlikely).<br>
     * PREVIOUSLY_RUN may not be present, and this is checked also.<br>
     * <p>
     * IMPORTANT: In this method saveAsName of inherited configuration may be changed to the one from upstream configuration.<br>
     *   In this case this new saveAsName must be taken from returning argument and used for further calculations and for returning to the client application.
     * 
     * @param checkChanges -- optional function to check whether there are local changes; if they are -- not update FRESH from upstream; if no such check is needed i.e. empty function is passed (e.g. when discarding) -- force FRESH centre updating
     */
    public static EntityCentreConfig updateInheritedFromShared(final EntityCentreConfig upstreamConfig, final Class<? extends MiWithConfigurationSupport<?>> miType, final DeviceProfile device, final Optional<String> saveAsName, final User user, final EntityCentreConfigCo eccCompanion, final Optional<Supplier<Boolean>> checkChanges) {
        final String upstreamTitle = obtainTitleFrom(upstreamConfig.getTitle(), SAVED_CENTRE_NAME, device);
        final Optional<String> changedTitle = !equalsEx(upstreamTitle, saveAsName.get()) ? of(upstreamTitle) : empty();
        final Function<String, Function<Supplier<Optional<Boolean>>, Consumer<Supplier<String>>>> overrideConfigBodyFor = name -> calcRunAutomaticallyOpt -> calcDesc ->
            findConfigOpt(miType, user, NAME_OF.apply(name).apply(saveAsName).apply(device), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("configBody").with("runAutomatically")) // contains 'title' / 'desc' inside fetch model
            .ifPresent(config -> {
                final String desc = calcDesc.get();
                if (desc != null) {
                    config.setDesc(desc);
                }
                calcRunAutomaticallyOpt.get().ifPresent(runAutomatically -> config.setRunAutomatically(runAutomatically));
                changedTitle.ifPresent(ct -> config.setTitle(NAME_OF.apply(name).apply(of(ct)).apply(device))); // update title of configuration from upstream if it has changed
                eccCompanion.saveWithConflicts(config.setConfigBody(upstreamConfig.getConfigBody()));
            });
        final Function<String, Consumer<String>> overrideConfigTitleFor = name -> ct -> findConfigOpt(miType, user, NAME_OF.apply(name).apply(saveAsName).apply(device), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT /*contains 'title' inside fetch model*/).ifPresent(config ->
            eccCompanion.saveWithConflicts(config.setTitle(NAME_OF.apply(name).apply(of(ct)).apply(device)))
        );
        final boolean notUpdateFresh = checkChanges.map(check -> check.get()).orElse(FALSE);
        // update SAVED surrogate configuration; always
        overrideConfigBodyFor.apply(SAVED_CENTRE_NAME)
            .apply(() -> empty())
            .accept(() -> null);
        if (!notUpdateFresh) {
            // update FRESH surrogate configuration; if there are no local changes or if local changes are irrelevant (DISCARD)
            overrideConfigBodyFor.apply(FRESH_CENTRE_NAME)
                // upstreamConfig exists; its FRESH counterpart too -- no need to provide webUiConfig (first 'null') for getting default runAutomatically values;
                // also no need to provide selectionCrit (second 'null') for checking whether upstreamConfig is inherited - it can never be inherited transitively
                .apply(() -> of(updateCentreRunAutomatically(upstreamConfig.getOwner(), miType, of(upstreamTitle), device, eccCompanion, null, null))) 
                .accept(() -> updateCentreDesc(upstreamConfig.getOwner(), miType, of(upstreamTitle), device, eccCompanion));
        } else {
            // update FRESH surrogate configuration; only if upstream title has been changed
            changedTitle.ifPresent(ct -> overrideConfigTitleFor.apply(FRESH_CENTRE_NAME).accept(ct));
        }
        // update PREVIOUSLY_RUN surrogate configuration; only if upstream title has been changed and if PREVIOUSLY_RUN surrogate configuration does exist
        changedTitle.ifPresent(ct -> overrideConfigTitleFor.apply(PREVIOUSLY_RUN_CENTRE_NAME).accept(ct)); // update title of configuration from upstream if it has changed
        return upstreamConfig;
    }

}