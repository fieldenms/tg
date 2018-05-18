package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.CRITERIA_ENTITY_ID;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isBooleanCriterion;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isDoubleCriterion;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_DESC;
import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isAndBeforeDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isDateMnemonicDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isDatePrefixDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isExclusive2Default;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isExclusiveDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isNotDefault;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.isOrNullDefault;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalManagedType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getVersion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
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
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
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
    private static final Logger logger = Logger.getLogger(CentreResourceUtils.class);
    
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
     * Creates the 'custom object' that contain 'critMetaValues' and 'isCentreChanged' flag.
     *
     * @param criteriaMetaValues
     * @param isCentreChanged
     * @return
     */
    static Map<String, Object> createCriteriaMetaValuesCustomObject(final Map<String, Map<String, Object>> criteriaMetaValues, final boolean isCentreChanged) {
        final Map<String, Object> customObject = new LinkedHashMap<>();
        customObject.put("isCentreChanged", isCentreChanged);
        customObject.put("metaValues", criteriaMetaValues);
        return customObject;
    }

    /**
     * Creates the 'custom object' that contain 'critMetaValues' and 'isCentreChanged' flag.
     *
     * @param criteriaMetaValues
     * @param isCentreChanged
     * @param staleCriteriaMessage
     *            -- if not <code>null</code> then the criteria is stale and the user will be informed about that ('orange' config button), otherwise (if <code>null</code>) -- the
     *            criteria were not changed and the user will be informed about that ('black' config button).
     *
     * @return
     */
    static Map<String, Object> createCriteriaMetaValuesCustomObject(final Map<String, Map<String, Object>> criteriaMetaValues, final boolean isCentreChanged, final String staleCriteriaMessage) {
        final Map<String, Object> customObject = createCriteriaMetaValuesCustomObject(criteriaMetaValues, isCentreChanged);
        customObject.put("staleCriteriaMessage", staleCriteriaMessage);
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
     * Creates the pair of 'custom object' (that contain 'critMetaValues', 'isCentreChanged' flag, 'resultEntities' and 'pageCount') and 'resultEntities' (query run is performed
     * inside).
     *
     * @param customObject
     * @param criteriaMetaValues
     * @param criteriaEntity
     * @param isCentreChanged
     * @param additionalFetchProvider
     * @param additionalFetchProviderForTooltipProperties
     * @param createdByUserConstraint -- if exists then constraints the query by equality to the property 'createdBy'
     * @return
     */
    static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Pair<Map<String, Object>, List<?>> createCriteriaMetaValuesCustomObjectWithResult(
            final Map<String, Object> customObject,
            final M criteriaEntity,
            final Optional<IFetchProvider<T>> additionalFetchProvider,
            final Optional<IFetchProvider<T>> additionalFetchProviderForTooltipProperties,
            final Optional<Pair<IQueryEnhancer<T>, Optional<CentreContext<T, ?>>>> queryEnhancerAndContext,
            final Optional<User> createdByUserConstraint) {
        final Map<String, Object> resultantCustomObject = new LinkedHashMap<>();

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
        IPage<T> page = null;
        final List<T> data;
        final Integer pageCapacity = (Integer) customObject.get("@@pageCapacity");
        final String action = (String) customObject.get("@@action");
        if (isRunning(customObject)) {
            page = criteriaEntity.run(pageCapacity);
            resultantCustomObject.put("summary", page.summary());
            data = page.data();
        } else if (RunActions.REFRESH.toString().equals(action)) {
            final Integer pageNumber = (Integer) customObject.get("@@pageNumber");
            final Pair<IPage<T>, T> refreshedData = criteriaEntity.getPageWithSummaries(pageNumber, pageCapacity);
            page = refreshedData.getKey();
            data = page.data();
            resultantCustomObject.put("summary", refreshedData.getValue());
        } else if (RunActions.NAVIGATE.toString().equals(action)) {
            final Integer pageNumber = (Integer) customObject.get("@@pageNumber");
            try {
                page = criteriaEntity.getPage(pageNumber, pageCapacity);
            } catch (final Exception e) {
                logger.error(e);
                final Pair<IPage<T>, T> navigatedData = criteriaEntity.getPageWithSummaries(pageNumber, pageCapacity);
                page = navigatedData.getKey();
                resultantCustomObject.put("summary", navigatedData.getValue());
            }
            data = page.data();
        } else {
            data = new ArrayList<>();
        }
        resultantCustomObject.put("resultEntities", data);
        resultantCustomObject.put("columnWidths", createColumnWidths(criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick(), criteriaEntity.getEntityClass()));
        resultantCustomObject.put("visibleColumnsWithOrder", criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().usedProperties(criteriaEntity.getEntityClass()));
        resultantCustomObject.put("pageNumber", page == null ? 0 /* TODO ? */: page.no());
        resultantCustomObject.put("pageCount", page == null ? 0 /* TODO ? */: page.numberOfPages());
        return new Pair<>(resultantCustomObject, data);
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

        final int fetchSize = adhocParams.get("fetchSize") != null ? (Integer) adhocParams.get("fetchSize") : 100;
        final Long[] ids = adhocParams.get("ids") != null ? (Long[]) adhocParams.get("ids") : new Long[]{};
        return criteriaEntity.streamEntities(fetchSize, ids);
    }

    ///////////////////////////////// CUSTOM OBJECTS [END] ///////////////////////////

    @SuppressWarnings("serial")
    private static Map<String, Map<String, Integer>> createColumnWidths(final IAddToResultTickManager secondTick, final Class<?> root) {
        final Map<String, Map<String, Integer>> columnWidths = secondTick.checkedProperties(root).stream()
        .map(property -> new Pair<String, Map<String, Integer>>(property, new HashMap<String, Integer>() {{
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
        }
    }
    
    /**
     * Creates the validation prototype for criteria entity of concrete [miType].
     * <p>
     * The entity creation process uses rigorous generation of criteria type and the instance every time (based on cdtmae of concrete miType).
     * <p>
     * IMPORTANT: Both methods {@link CentreResourceUtils#createCriteriaValidationPrototype(Class, ICentreDomainTreeManagerAndEnhancer, ICriteriaGenerator, Long, IGlobalDomainTreeManager)}
     * and {@link CentreResourceUtils#createCriteriaEntity(Map, ICompanionObjectFinder, ICriteriaGenerator, Class, IGlobalDomainTreeManager)} need synchronisation against this utility 
     * class ({@link CentreResourceUtils}). This is needed to avoid simultaneous write access to FRESH_CENTRE_NAMEd centre manager, which could happen for quick simultaneous validation 
     * requests (even though older validation request could be aborted on the client-side, still, the server-side continues processing).
     *
     * @param miType
     * @param gdtm
     * @param critGenerator
     * @return
     */
    static synchronized <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaValidationPrototype(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final ICentreDomainTreeManagerAndEnhancer cdtmae,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final Long previousVersion,
            final IGlobalDomainTreeManager gdtm,
            final DeviceProfile device) {
        final Class<T> entityType = getEntityType(miType);
        final M validationPrototype = (M) critGenerator.generateCentreQueryCriteria(entityType, cdtmae, miType, new MiTypeAnnotation().newInstance(miType, saveAsName));
        validationPrototype.setFreshCentreSupplier(() -> updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device));
        validationPrototype.setDefaultCentreSupplier(() -> getDefaultCentre(gdtm, miType));
        validationPrototype.setCentreAdjuster((centreConsumer) -> {
            centreConsumer.accept(updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device));
            commitCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device);
            
            centreConsumer.accept(updateCentre(gdtm, miType, SAVED_CENTRE_NAME, saveAsName, device));
            commitCentre(gdtm, miType, SAVED_CENTRE_NAME, saveAsName, device);
            
            centreConsumer.accept(updateCentre(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device));
            commitCentre(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device);
        });
        validationPrototype.setCentreDeleter(() -> {
            if (!saveAsName.isPresent()) {
                // default configuration will never be deleted; however it can be 'defaulted'
                throw failuref("%s cannot be deleted.", DEFAULT_CONFIG_TITLE);
            } else {
                // perform deletion of centre 'saveAs' configuration even if it is inherited from its base; still such config could loaded again from base config
                removeCentres(gdtm, miType, device, saveAsName, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME, PREVIOUSLY_RUN_CENTRE_NAME);
            }
        });
        validationPrototype.setFreshCentreApplier((modifHolder) -> {
            return createCriteriaEntity(modifHolder, companionFinder, critGenerator, miType, saveAsName, gdtm, device);
        });
        validationPrototype.setCentreTitleAndDescGetter(() -> {
            return saveAsName.map(name -> t2(name, "TODO")).orElse(t2(DEFAULT_CONFIG_TITLE, DEFAULT_CONFIG_DESC)); // TODO
        });
        validationPrototype.setCentreEditor((editKindAndNewName, newDesc) -> {
            if (EditKind.COPY.equals(editKindAndNewName._1)) {
                final ICentreDomainTreeManagerAndEnhancer freshCentre = updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device);
                final ICentreDomainTreeManagerAndEnhancer savedCentre = updateCentre(gdtm, miType, SAVED_CENTRE_NAME, saveAsName, device);
                
                initAndCommit(gdtm, miType, FRESH_CENTRE_NAME, editKindAndNewName._2, device, freshCentre, newDesc);
                initAndCommit(gdtm, miType, SAVED_CENTRE_NAME, editKindAndNewName._2, device, savedCentre, null);
            } else {
                throw failure("Not yet implemented."); // TODO
            }
        });
        validationPrototype.setLoadableCentresSupplier(() -> {
            return t2(loadableConfigurations(gdtm, miType, device, companionFinder), saveAsName);
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
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final M criteriaEntity,
            final Optional<CentreContextConfig> contextConfig,
            final String chosenProperty,
            final DeviceProfile device
    ) {
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
                context.setMasterEntity(EntityResource.restoreMasterFunctionalEntity(disregardOriginallyProducedEntities, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, entityFactory, centreContextHolder, 0, device));
            }
            if (config.withComputation()) {
                context.setComputation(config.computation.get());
            }
            context.setChosenProperty(chosenProperty);
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
            final String chosenProperty
    ) {
        final CentreContext<T, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectionCrit(criteriaEntity);
        context.setSelectedEntities((List<T>) selectedEntities);
        context.setMasterEntity(masterContext);
        if (config.isPresent() && config.get().context.isPresent() && config.get().context.get().withComputation()) {
            context.setComputation(config.get().context.get().computation.get());
        }
        context.setChosenProperty(chosenProperty);
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
        final IGlobalDomainTreeManager gdtm,
        final ICriteriaGenerator critGenerator,
        
        final IServerGlobalDomainTreeManager serverGdtm, 
        final IUserProvider userProvider, 
        final IWebUiConfig webUiConfig,
        final EntityFactory entityFactory,
        
        final DeviceProfile device
    ) {
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

        final M criteriaEntity = (M) createCriteriaEntityForPaginating(companionFinder, critGenerator, miType, saveAsName, gdtm, device).setCentreContextHolder(centreContextHolder);
        criteriaEntity.setExportQueryRunner(customObject -> stream(webUiConfig, serverGdtm, userProvider, entityFactory, companionFinder, critGenerator, centreContextHolder, criteriaEntity, customObject, device));
        return criteriaEntity;
    }

    /**
     * A method to stream entities based on the centre query, including various transformations such as custom properties etc.
     * It is used as part of initialisation for an export query runner.
     * 
     * @param webUiConfig
     * @param serverGdtm
     * @param userProvider
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
        final IServerGlobalDomainTreeManager serverGdtm, 
        final IUserProvider userProvider, 
        final EntityFactory entityFactory, 
        final ICompanionObjectFinder companionFinder, 
        final ICriteriaGenerator critGenerator, 
        
        final CentreContextHolder centreContextHolder, 
        final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity, 
        final Map<String, Object> adhocParams,
        final DeviceProfile device
    ) {
        final Class<? extends MiWithConfigurationSupport<?>> miType = EntityResourceUtils.getMiType((Class<EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>>) criteriaEntity.getClass());
        final EntityCentre<AbstractEntity<?>> centre = (EntityCentre<AbstractEntity<?>>) webUiConfig.getCentres().get(miType);
        adhocParams.putAll(centreContextHolder.getCustomObject());
        // at this stage (during exporting of centre data) appliedCriteriaEntity is valid, because it represents 'previouslyRun' centre criteria which is getting updated only if Run was initiated and selection criteria validation succeeded
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>) criteriaEntity;
        // if the export() invocation occurs on the centre that warrants data generation
        // then for an entity centre configuration check if a generator was provided
        final boolean createdByConstraintShouldOccur = centre.getGeneratorTypes().isPresent();

        final Stream<AbstractEntity<?>> stream =
                CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithStream(
                        adhocParams,
                        appliedCriteriaEntity,
                        centre.getAdditionalFetchProvider(),
                        centre.getAdditionalFetchProviderForTooltipProperties(),
                        CriteriaResource.createQueryEnhancerAndContext(
                                webUiConfig,
                                companionFinder,
                                serverGdtm,
                                userProvider,
                                critGenerator,
                                entityFactory,
                                centreContextHolder,
                                centre.getQueryEnhancerConfig(),
                                appliedCriteriaEntity,
                                device),
                        // There could be cases where the generated data and the queried data would have different types.
                        // For example, the queried data could be modelled by a synthesized entity that includes a subquery based on some generated data.
                        // In such cases, it is unpossible to enhance the final query with a user related condition automatically.
                        // This should be the responsibility of the application developer to properly construct a subquery that is based on the generated data.
                        // The query will be enhanced with condition createdBy=currentUser if createdByConstraintShouldOccur and generatorEntityType equal to the type of queried data (otherwise end-developer should do that itself by using queryEnhancer or synthesized model).
                        createdByConstraintShouldOccur && centre.getGeneratorTypes().get().getKey().equals(EntityResourceUtils.getEntityType(miType)) ? Optional.of(userProvider.getUser()) : Optional.empty());

        return CriteriaResource.enhanceResultEntitiesWithCustomPropertyValues(
                centre, 
                centre.getCustomPropertiesDefinitions(), 
                centre.getCustomPropertiesAsignmentHandler(), 
                stream);
    }

    /**
     * Creates selection criteria entity from {@link CentreContextHolder} entity (which contains modifPropsHolder).
     *
     * @param centreContextHolder
     * @param isPaginating
     *            -- returns <code>true</code> in case when this method is a part of 'Paginating Actions', <code>false</code> otherwise
     * @return
     */
    protected static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntityForPaginating(final ICompanionObjectFinder companionFinder, final ICriteriaGenerator critGenerator, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final IGlobalDomainTreeManager gdtm, final DeviceProfile device) {
        final ICentreDomainTreeManagerAndEnhancer updatedPreviouslyRunCentre = updateCentre(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device);
        return createCriteriaValidationPrototype(miType, saveAsName, updatedPreviouslyRunCentre, companionFinder, critGenerator, 0L, gdtm, device);
    }

    /**
     * Creates selection criteria entity from <code>modifPropsHolder</code>.
     * <p>
     * IMPORTANT: Both methods {@link CentreResourceUtils#createCriteriaValidationPrototype(Class, ICentreDomainTreeManagerAndEnhancer, ICriteriaGenerator, Long, IGlobalDomainTreeManager)}
     * and {@link CentreResourceUtils#createCriteriaEntity(Map, ICompanionObjectFinder, ICriteriaGenerator, Class, IGlobalDomainTreeManager)} need synchronisation against this utility 
     * class ({@link CentreResourceUtils}). This is needed to avoid simultaneous write access to FRESH_CENTRE_NAMEd centre manager, which could happen for quick simultaneous validation 
     * requests (even though older validation request could be aborted on the client-side, still, the server-side continues processing).
     *
     * @return
     */
    protected static synchronized <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> M createCriteriaEntity(final Map<String, Object> modifiedPropertiesHolder, final ICompanionObjectFinder companionFinder, final ICriteriaGenerator critGenerator, final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName, final IGlobalDomainTreeManager gdtm, final DeviceProfile device) {
        if (isEmpty(modifiedPropertiesHolder)) {
            throw new IllegalArgumentException("ModifiedPropertiesHolder should not be empty during invocation of fully fledged criteria entity creation.");
        }
        
        // load / update fresh centre if it is not loaded yet / stale
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device);
        applyMetaValues(originalCdtmae, getEntityType(miType), modifiedPropertiesHolder);
        final M validationPrototype = createCriteriaValidationPrototype(miType, saveAsName, originalCdtmae, companionFinder, critGenerator, getVersion(modifiedPropertiesHolder), gdtm, device);
        final M appliedCriteriaEntity = constructCriteriaEntityAndResetMetaValues(
                modifiedPropertiesHolder,
                validationPrototype,
                getOriginalManagedType(validationPrototype.getType(), originalCdtmae),
                companionFinder//
        ).getKey();
        
        // need to commit changed fresh centre after modifiedPropertiesHolder has been applied!
        commitCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device);
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
}
