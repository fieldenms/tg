package ua.com.fielden.platform.web.resources.webui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiTypeAnnotation;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * This utility class contains the methods that are shared across {@link CentreResource} and {@link CriteriaResource}.
 *
 * @author TG Team
 *
 */
public class CentreResourceUtils<T extends AbstractEntity<?>> extends CentreUtils<T> {
    private final static Logger logger = Logger.getLogger(CentreResourceUtils.class);

    public CentreResourceUtils() {
    }

    /**
     * Creates 'diff centre' from 'centre' and 'originalCentre' with marked meta-values (only those that are different).
     *
     * @param centre
     * @param originalCentre
     * @param root
     * @return
     */
    static ICentreDomainTreeManagerAndEnhancer createDifferencesCentre(final ICentreDomainTreeManagerAndEnhancer centre, final ICentreDomainTreeManagerAndEnhancer originalCentre, final Class<AbstractEntity<?>> root, final IGlobalDomainTreeManager gdtm) {
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = ((GlobalDomainTreeManager) gdtm).copyCentre(centre);

        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(property)) {
                if (AbstractDomainTree.isDoubleCriterion(managedType(root, differencesCentre), property)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive(root, property), originalCentre.getFirstTick().getExclusive(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive2(root, property), originalCentre.getFirstTick().getExclusive2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property);
                    }
                }
                final Class<?> propertyType = StringUtils.isEmpty(property) ? managedType(root, differencesCentre) : PropertyTypeDeterminator.determinePropertyType(CentreResourceUtils.managedType(root, differencesCentre), property);
                if (EntityUtils.isDate(propertyType)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getDatePrefix(root, property), originalCentre.getFirstTick().getDatePrefix(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_PREFIX, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getDateMnemonic(root, property), originalCentre.getFirstTick().getDateMnemonic(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getAndBefore(root, property), originalCentre.getFirstTick().getAndBefore(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.AND_BEFORE, root, property);
                    }
                }

                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getOrNull(root, property), originalCentre.getFirstTick().getOrNull(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.OR_NULL, root, property);
                }
                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getNot(root, property), originalCentre.getFirstTick().getNot(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.NOT, root, property);
                }

                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getValue(root, property), originalCentre.getFirstTick().getValue(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE, root, property);
                }
                if (AbstractDomainTree.isDoubleCriterionOrBoolean(CentreResourceUtils.managedType(root, differencesCentre), property)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getValue2(root, property), originalCentre.getFirstTick().getValue2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE2, root, property);
                    }
                }
            }
        }

        return differencesCentre;
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
     * Creates the pair of 'custom object' (that contain 'critMetaValues', 'isCentreChanged' flag, 'resultEntities' and 'pageCount') and 'resultEntities' (query run is performed
     * inside).
     *
     * @param customObject
     * @param criteriaMetaValues
     * @param criteriaEntity
     * @param isCentreChanged
     * @param additionalFetchProvider
     * @return
     */
    static Pair<Map<String, Object>, ArrayList<?>> createCriteriaMetaValuesCustomObjectWithResult(final Map<String, Object> customObject, final Map<String, Map<String, Object>> criteriaMetaValues, final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> criteriaEntity, final boolean isCentreChanged, final Optional<IFetchProvider<AbstractEntity<?>>> additionalFetchProvider, final Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> queryEnhancerAndContext) {
        final Map<String, Object> resultantCustomObject = new LinkedHashMap<>();
        resultantCustomObject.put("isCentreChanged", isCentreChanged);
        resultantCustomObject.put("metaValues", criteriaMetaValues);

        if (criteriaEntity.isValid().isSuccessful()) {
            // final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> resultingCriteria = applied;
            criteriaEntity.getGeneratedEntityController().setEntityType(criteriaEntity.getEntityClass());
            if (additionalFetchProvider.isPresent()) {
                criteriaEntity.setAdditionalFetchProvider(additionalFetchProvider.get());
            }
            if (queryEnhancerAndContext.isPresent()) {
                final IQueryEnhancer<AbstractEntity<?>> queryEnhancer = queryEnhancerAndContext.get().getKey();
                criteriaEntity.setAdditionalQueryEnhancerAndContext(queryEnhancer, queryEnhancerAndContext.get().getValue());
            }
            final IPage<AbstractEntity<?>> page;
            final Integer pageCapacity = (Integer) customObject.get("@@pageCapacity");
            customObject.remove("@@pageCapacity");
            if (customObject.get("@@pageNumber") == null) {
                page = criteriaEntity.run(pageCapacity);
            } else {
                page = criteriaEntity.getPage((Integer) customObject.get("@@pageNumber"), (Integer) customObject.get("@@pageCount"), pageCapacity);
                customObject.remove("@@pageNumber");
                customObject.remove("@@pageCount");
            }
            final boolean isNotRefreshingConcreteEntities = customObject.get("@@idsToRefresh") == null;
            final ArrayList<Object> resultEntities = new ArrayList<Object>(isNotRefreshingConcreteEntities
                    ? page.data()
                    : selectEntities(page.data(), convertToListWithLongValues((List) customObject.get("@@idsToRefresh"))));

            resultantCustomObject.put("resultEntities", resultEntities);
            resultantCustomObject.put("pageCount", page.numberOfPages());
            if (!isNotRefreshingConcreteEntities) {
                // mark customObject with a special property, that indicates the process of concrete entities refreshing (potentially this can be removed
                // and resolved purely on the client side)
                resultantCustomObject.put("isRefreshingConcreteEntities", "yes");
            }
            return new Pair<>(resultantCustomObject, resultEntities);
        }
        return new Pair<>(resultantCustomObject, null);
    }

    /**
     * Selects the entities from resulting <code>data</code> to contain only those, that have specified <code>longIds</code>.
     *
     * @param data
     * @param ids
     * @return
     */
    private static List<AbstractEntity<?>> selectEntities(final List<AbstractEntity<?>> data, final List<Long> longIds) {
        final List<AbstractEntity<?>> list = new ArrayList<>();
        if (longIds.isEmpty()) {
            list.addAll(data);
        } else {
            for (final AbstractEntity<?> retrievedEntity : data) {
                if (longIds.contains(retrievedEntity.getId())) {
                    list.add(retrievedEntity);
                }
            }
        }
        return list;
    }

    private static List<Long> convertToListWithLongValues(final List ids) {
        final List<Long> longIds = new ArrayList<>();
        for (final Object id : ids) {
            if (id != null) {
                longIds.add(id instanceof Integer ? ((Integer) id).longValue() : (Long) id);
            }
        }
        return longIds;
    }

    ///////////////////////////////// CUSTOM OBJECTS [END] ///////////////////////////

    /**
     * Generates annotation with mi type.
     *
     * @param miType
     * @return
     */
    private static MiType createMiTypeAnnotation(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return new MiTypeAnnotation().newInstance(miType);
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
        final DefaultValueContract dvc = new DefaultValueContract();
        for (final String checkedProp : cdtmae.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(checkedProp)) {
                metaValues.put(checkedProp, createMetaValuesFor(root, checkedProp, cdtmae, dvc));
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
    private static Map<String, Object> createMetaValuesFor(final Class<AbstractEntity<?>> root, final String prop, final ICentreDomainTreeManagerAndEnhancer cdtmae, final DefaultValueContract dvc) {
        final IAddToCriteriaTickManager tickManager = cdtmae.getFirstTick();

        final Map<String, Object> metaValues = new LinkedHashMap<>();

        if (AbstractDomainTree.isDoubleCriterion(managedType(root, cdtmae), prop)) {
            final Boolean exclusive = tickManager.getExclusive(root, prop);
            if (!dvc.isExclusiveDefault(exclusive)) {
                metaValues.put("exclusive", exclusive);
            }
            final Boolean exclusive2 = tickManager.getExclusive2(root, prop);
            if (!dvc.isExclusive2Default(exclusive2)) {
                metaValues.put("exclusive2", exclusive2);
            }
        }
        final Class<?> propertyType = StringUtils.isEmpty(prop) ? managedType(root, cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(root, cdtmae), prop);
        if (EntityUtils.isDate(propertyType)) {
            final DateRangePrefixEnum datePrefix = tickManager.getDatePrefix(root, prop);
            if (!dvc.isDatePrefixDefault(datePrefix)) {
                metaValues.put("datePrefix", datePrefix);
            }
            final MnemonicEnum dateMnemonic = tickManager.getDateMnemonic(root, prop);
            if (!dvc.isDateMnemonicDefault(dateMnemonic)) {
                metaValues.put("dateMnemonic", dateMnemonic);
            }
            final Boolean andBefore = tickManager.getAndBefore(root, prop);
            if (!dvc.isAndBeforeDefault(andBefore)) {
                metaValues.put("andBefore", andBefore);
            }
        }
        final Boolean orNull = tickManager.getOrNull(root, prop);
        if (!dvc.isOrNullDefault(orNull)) {
            metaValues.put("orNull", orNull);
        }
        final Boolean not = tickManager.getNot(root, prop);
        if (!dvc.isNotDefault(not)) {
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
    static void applyMetaValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root, final Map<String, Object> modifiedPropertiesHolder) {
        final Map<String, Map<String, Object>> metaValues = (Map<String, Map<String, Object>>) modifiedPropertiesHolder.get("@@metaValues");

        for (final Entry<String, Map<String, Object>> propAndMetaValues : metaValues.entrySet()) {
            final String prop = propAndMetaValues.getKey();

            final Map<String, Object> mValues = propAndMetaValues.getValue();
            if (AbstractDomainTree.isDoubleCriterion(managedType(root, cdtmae), prop)) {
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
     *
     * @param miType
     * @param gdtm
     * @param critGenerator
     * @return
     */
    static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> createCriteriaValidationPrototype(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final ICentreDomainTreeManagerAndEnhancer cdtmae,
            final ICriteriaGenerator critGenerator,
            final Long previousVersion) {
        final Class<AbstractEntity<?>> entityType = getEntityType(miType);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = critGenerator.generateCentreQueryCriteria(entityType, cdtmae, createMiTypeAnnotation(miType));

        final Field idField = Finder.getFieldByName(validationPrototype.getType(), AbstractEntity.ID);
        final boolean idAccessible = idField.isAccessible();
        idField.setAccessible(true);
        try {
            idField.set(validationPrototype, 333L); // here the fictional id is populated to mark the entity as persisted!
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

        return resetMetaStateForCriteriaValidationPrototype(
                validationPrototype,
                CentreUtils.getOriginalManagedType(validationPrototype.getType(), cdtmae)//
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
    public static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> resetMetaStateForCriteriaValidationPrototype(final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> criteriaValidationPrototype, final Class<?> originalManagedType) {
        // standard resetting of meta-values: copies values into originalValues for all properties:
        criteriaValidationPrototype.resetMetaState();

        final Class<? extends AbstractEntity<?>> criteriaType = criteriaValidationPrototype.getType();
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

                final Field originalValueField = Finder.findFieldByName(MetaProperty.class, "originalValue");
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

    public static <T extends AbstractEntity<?>> Optional<CentreContext<T, AbstractEntity<?>>> createCentreContext(final CentreContextHolder centreContextHolder, final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity, final Optional<CentreContextConfig> contextConfig) {
        final CentreContext<T, AbstractEntity<?>> context = new CentreContext<>();
        if (contextConfig.isPresent()) {
            final CentreContextConfig config = contextConfig.get();
            if (config.withSelectionCrit) {
                context.setSelectionCrit(criteriaEntity);
            }
            if (config.withAllSelectedEntities) {
                context.setSelectedEntities((List<T>) centreContextHolder.getSelectedEntities());
            } else if (config.withCurrentEtity) {
                context.setSelectedEntities((List<T>) centreContextHolder.getSelectedEntities());
            }
            if (config.withMasterEntity) {
                context.setMasterEntity(centreContextHolder.getMasterEntity());
            }
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
     * @param centreContextHolder
     * @return
     */
    public static <T extends AbstractEntity<?>> CentreContext<T, AbstractEntity<?>> createCentreContext(final CentreContextHolder centreContextHolder, final ICompanionObjectFinder companionFinder, final IGlobalDomainTreeManager gdtm, final ICriteriaGenerator critGenerator) {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectionCrit(createSelectionCriteriaEntity(centreContextHolder, companionFinder, gdtm, critGenerator));
        context.setSelectedEntities(centreContextHolder.getSelectedEntities());
        context.setMasterEntity(centreContextHolder.getMasterEntity());
        return (CentreContext<T, AbstractEntity<?>>) context;
    }

    /**
     * Creates selection criteria entity from {@link CentreContextHolder} entity (which contains modifPropsHolder).
     *
     * @param centreContextHolder
     * @return
     */
    private static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> createSelectionCriteriaEntity(final CentreContextHolder centreContextHolder, final ICompanionObjectFinder companionFinder, final IGlobalDomainTreeManager gdtm, final ICriteriaGenerator critGenerator) {
        Class<? extends MiWithConfigurationSupport<?>> miType;
        try {
            miType = (Class<? extends MiWithConfigurationSupport<?>>) Class.forName((String) centreContextHolder.getCustomObject().get("@@miType"));
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);

        final Map<String, Object> modifiedPropertiesHolder = centreContextHolder.getModifHolder();

        CentreResourceUtils.applyMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType), modifiedPropertiesHolder);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype =
                CentreResourceUtils.createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));

        return EntityResourceUtils.constructCriteriaEntityAndResetMetaValues(
                modifiedPropertiesHolder,
                validationPrototype,
                CentreUtils.getOriginalManagedType(validationPrototype.getType(), originalCdtmae),
                companionFinder//
        ).getKey();
    }

}
