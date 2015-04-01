package ua.com.fielden.platform.web.resources.webui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiTypeAnnotation;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * This utility class contains the methods that are shared across {@link CentreResource} and {@link CriteriaResource}.
 *
 * @author TG Team
 *
 */
public class CentreResourceUtils<T extends AbstractEntity<?>> {
    private final static Logger logger = Logger.getLogger(CentreResourceUtils.class);
    private static final String DIFFERENCES_CENTRE_NAME = "__________DIFFERENCES_CENTRE_NAME";
    private static final String FRESH_CENTRE_NAME = "__________FRESH_CENTRE_NAME";

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
            if (AbstractDomainTree.isDoubleCriterion(CentreResourceUtils.managedType(root, differencesCentre), property)) {
                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive(root, property), originalCentre.getFirstTick().getExclusive(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE, root, property);
                }
                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive2(root, property), originalCentre.getFirstTick().getExclusive2(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property);
                }
            }
            final Class<?> propertyType = StringUtils.isEmpty(property) ? CentreResourceUtils.managedType(root, differencesCentre) : PropertyTypeDeterminator.determinePropertyType(CentreResourceUtils.managedType(root, differencesCentre), property);
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

        return differencesCentre;
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
        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (AbstractDomainTree.isDoubleCriterion(managedType(root, differencesCentre), property)) {
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE, root, property)) {
                    targetCentre.getFirstTick().setExclusive(root, property, differencesCentre.getFirstTick().getExclusive(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property)) {
                    targetCentre.getFirstTick().setExclusive2(root, property, differencesCentre.getFirstTick().getExclusive2(root, property));
                }
            }
            final Class<?> propertyType = StringUtils.isEmpty(property) ? managedType(root, differencesCentre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, differencesCentre), property);
            if (EntityUtils.isDate(propertyType)) {
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_PREFIX, root, property)) {
                    targetCentre.getFirstTick().setDatePrefix(root, property, differencesCentre.getFirstTick().getDatePrefix(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property)) {
                    targetCentre.getFirstTick().setDateMnemonic(root, property, differencesCentre.getFirstTick().getDateMnemonic(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.AND_BEFORE, root, property)) {
                    targetCentre.getFirstTick().setAndBefore(root, property, differencesCentre.getFirstTick().getAndBefore(root, property));
                }
            }

            if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.OR_NULL, root, property)) {
                targetCentre.getFirstTick().setOrNull(root, property, differencesCentre.getFirstTick().getOrNull(root, property));
            }
            if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.NOT, root, property)) {
                targetCentre.getFirstTick().setNot(root, property, differencesCentre.getFirstTick().getNot(root, property));
            }

            if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE, root, property)) {
                targetCentre.getFirstTick().setValue(root, property, differencesCentre.getFirstTick().getValue(root, property));
            }
            if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType(root, differencesCentre), property)) {
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE2, root, property)) {
                    targetCentre.getFirstTick().setValue2(root, property, differencesCentre.getFirstTick().getValue2(root, property));
                }
            }
        }
        return targetCentre;
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
     * @param modifiedPropertiesHolder
     * @param criteriaMetaValues
     * @param applied
     * @param isCentreChanged
     * @return
     */
    static Pair<Map<String, Object>, ArrayList<?>> createCriteriaMetaValuesCustomObjectWithResult(final Map<String, Object> modifiedPropertiesHolder, final Map<String, Map<String, Object>> criteriaMetaValues, final AbstractEntity<?> applied, final boolean isCentreChanged) {
        final Map<String, Object> customObject = new LinkedHashMap<>();
        customObject.put("isCentreChanged", isCentreChanged);
        customObject.put("metaValues", criteriaMetaValues);

        if (applied.isValid().isSuccessful()) {
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> resultingCriteria = (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) applied;
            resultingCriteria.getGeneratedEntityController().setEntityType(resultingCriteria.getEntityClass());
            final IPage<AbstractEntity<?>> page;
            final Integer pageCapacity = (Integer) modifiedPropertiesHolder.get("@@pageCapacity");
            modifiedPropertiesHolder.remove("@@pageCapacity");
            if (modifiedPropertiesHolder.get("@@pageNumber") == null) {
                page = resultingCriteria.run(pageCapacity);
            } else {
                page = resultingCriteria.getPage((Integer) modifiedPropertiesHolder.get("@@pageNumber"), (Integer) modifiedPropertiesHolder.get("@@pageCount"), pageCapacity);
                modifiedPropertiesHolder.remove("@@pageNumber");
                modifiedPropertiesHolder.remove("@@pageCount");
            }
            final ArrayList<Object> resultEntities = new ArrayList<Object>(page.data());

            customObject.put("resultEntities", resultEntities);
            customObject.put("pageCount", page.numberOfPages());
            return new Pair<>(customObject, resultEntities);
        }
        return new Pair<>(customObject, null);
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
     * Determines the miType for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    static Class<? extends MiWithConfigurationSupport<?>> getMiType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final MiType annotation = AnnotationReflector.getAnnotation(criteriaType, MiType.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("The criteria type [%s] should be annotated with MiType annotation.", criteriaType.getName()));
        }
        return annotation.value();
    }

    /**
     * Determines the entity type for which criteria entity will be generated.
     *
     * @param miType
     * @return
     */
    static Class<AbstractEntity<?>> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType entityTypeAnnotation = miType.getAnnotation(EntityType.class);
        if (entityTypeAnnotation == null) {
            throw new IllegalStateException(String.format("The menu item type [%s] must be annotated with EntityType annotation", miType.getName()));
        }
        return (Class<AbstractEntity<?>>) entityTypeAnnotation.value();
    }

    /**
     * Determines the master type for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    public static Class<? extends AbstractEntity<?>> getOriginalType(final Class<? extends AbstractEntity<?>> criteriaType) {
        return getEntityType(getMiType(criteriaType));
    }

    /**
     * Determines the property name of the property from which the criteria property was generated. This is only applicable for entity typed properties.
     *
     * @param propertyName
     * @return
     */
    public static String getOriginalPropertyName(final Class<?> criteriaClass, final String propertyName) {
        return CriteriaReflector.getCriteriaProperty(criteriaClass, propertyName);
    }

    /**
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded), <code>false</code> otherwise.
     *
     * @param miType
     * @param gdtm
     * @return
     */
    static boolean isFreshCentreChanged(final Class<? extends MiWithConfigurationSupport<?>> miType, final IGlobalDomainTreeManager gdtm) {
        final boolean isCentreChanged = gdtm.isChangedEntityCentreManager(miType, FRESH_CENTRE_NAME);
        logger.debug("isCentreChanged == " + isCentreChanged);
        return isCentreChanged;
    }

    /**
     * Returns the current version of default centre manager (initialises it in case if it is not created yet).
     * <p>
     * Currently it is created from default configuration in Mi types (CDTMAE is used), but later it should be initialised from Entity Centre DSL.
     *
     * @param globalManager
     * @param miType
     * @return
     */
    static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, null) == null) {
            globalManager.initEntityCentreManager(miType, null);
        }

        if (globalManager.isChangedEntityCentreManager(miType, null)) {
            throw new IllegalStateException("Should be not changed (after init).");
        }

        return globalManager.getEntityCentreManager(miType, null);
    }

    /**
     * Returns the current version of edited by the user centre manager (initialises it in case if it is not created yet).
     * <p>
     * Initialisation goes through the following chain: 'default centre' + 'differences centre' := 'initial fresh centre'. Later the user make its own diffs on top of the 'initial
     * fresh centre'.
     * <p>
     * Fresh centre is never saved, but it is used to create 'differences centre' (when saving is performed) and then it is removed.
     *
     * @param gdtm
     * @param miType
     * @return
     */
    static ICentreDomainTreeManagerAndEnhancer getFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME) == null) {
            final ICentreDomainTreeManagerAndEnhancer freshCentre =
                    applyDifferences(
                            ((GlobalDomainTreeManager) gdtm).copyCentre(getDefaultCentre(gdtm, miType)),
                            getDifferencesCentre(gdtm, miType),
                            getEntityType(miType)
                    );

            ((GlobalDomainTreeManager) gdtm).init(miType, FRESH_CENTRE_NAME, freshCentre, true);
        }
        return freshCentre(gdtm, miType);
    }

    /**
     * Returns the current version of principle centre manager (it assumes that it should be initialised!).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    static ICentreDomainTreeManagerAndEnhancer freshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME) == null) {
            throw new IllegalStateException("The 'fresh centre' should be initialised.");
        }
        return gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME);
    }

    /**
     * Removes fresh centre (to be able later to re-populate it automatically).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    static void removeFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        ((GlobalDomainTreeManager) gdtm).removeCentre(miType, CentreResourceUtils.FRESH_CENTRE_NAME);
    }

    /**
     * Overrides old 'differences centre' with new one and saves it.
     *
     * @param gdtm
     * @param miType
     * @return
     */
    static void overrideAndSaveDifferencesCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final ICentreDomainTreeManagerAndEnhancer newDifferencesCentre) {
        ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, CentreResourceUtils.DIFFERENCES_CENTRE_NAME, newDifferencesCentre);
        gdtm.saveEntityCentreManager(miType, CentreResourceUtils.DIFFERENCES_CENTRE_NAME);
    }

    /**
     * Discards fresh centre (throws an exception if it was not changed).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    static void discardFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.isChangedEntityCentreManager(miType, CentreResourceUtils.FRESH_CENTRE_NAME)) {
            gdtm.discardEntityCentreManager(miType, CentreResourceUtils.FRESH_CENTRE_NAME);
        } else {
            final String message = "Can not discard the centre that was not changed.";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Initialises 'differences centre' from the persistent storage, if it exists.
     * <p>
     * If no 'differences centre' exists -- the following steps are performed:
     * <p>
     * 1. make sure that 'defalt centre' exists in gdtm;<br>
     * 2. make saveAs from 'default centre' which will be 'diff centre' (this promotes the empty diff to the storage!)<br>
     * 3. applies correct default values to be in sync with Web UI ones (on top of 'diff centre');<br>
     * 4. saves the 'diff centre's changes;<br>
     *
     * @param globalManager
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getDifferencesCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME) == null) {
            try {
                globalManager.initEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);
            } catch (final IllegalArgumentException e) {
                if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                    getDefaultCentre(globalManager, miType);

                    globalManager.saveAsEntityCentreManager(miType, null, DIFFERENCES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }

                    applyDefaultValues(globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME), getEntityType(miType));

                    globalManager.saveEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }
                } else {
                    throw e;
                }
            }

            if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                throw new IllegalStateException("Should be not changed.");
            }
        }
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);
        return differencesCentre;
    }

    /**
     * Applies correct default values to be in sync with Web UI ones on top of 'centre'.
     *
     * @param centre
     * @param root
     */
    private static void applyDefaultValues(final ICentreDomainTreeManagerAndEnhancer centre, final Class<AbstractEntity<?>> root) {
        for (final String property : centre.getFirstTick().checkedProperties(root)) {
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            final Class<?> propertyType = isEntityItself ? managedType(root, centre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, centre), property);
            final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType(root, centre), property);
            final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());

            if (!EntityUtils.isBoolean(propertyType) && !(EntityUtils.isEntityType(propertyType) && !single)) {
                centre.getFirstTick().setValue(root, property, null);
                if (AbstractDomainTree.isDoubleCriterion(managedType(root, centre), property)) {
                    centre.getFirstTick().setValue2(root, property, null);
                }
            }
        }
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
            metaValues.put(checkedProp, createMetaValuesFor(root, checkedProp, cdtmae, dvc));
        }
        return metaValues;
    }

    /**
     * Returns the 'managed type' for the 'centre' manager.
     *
     * @param root
     * @param centre
     * @return
     */
    private static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getEnhancer().getManagedType(root);
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

        // The meta state reset is necessary to make the entity like 'just saved and retrieved from the database' (originalValues should be equal to values)
        validationPrototype.resetMetaState();

        return validationPrototype;
    }

}
