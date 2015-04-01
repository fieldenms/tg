package ua.com.fielden.platform.web.resources.webui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

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
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * The web resource for criteria serves as a back-end mechanism of criteria retrieval. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CriteriaResource<CRITERIA_TYPE extends AbstractEntity<?>> extends ServerResource {
    public static final String DIFFERENCIES_CENTRE_NAME = "__________DIFFERENCIES_CENTRE_NAME";
    public static final String FRESH_CENTRE_NAME = "__________FRESH_CENTRE_NAME";
    private final static Logger logger = Logger.getLogger(CriteriaResource.class);

    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;

    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,

            final EntityCentre centre,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;
        this.companionFinder = companionFinder;

        miType = centre.getMenuItemType();
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
    }

    ///////////////////////////////// CUSTOM OBJECTS /////////////////////////////////
    private static Map<String, Object> createCriteriaMetaValuesCustomObject(final Map<String, Map<String, Object>> criteriaMetaValues, final boolean isCentreChanged) {
        final Map<String, Object> customObject = new LinkedHashMap<>();
        customObject.put("isCentreChanged", isCentreChanged);
        customObject.put("metaValues", criteriaMetaValues);
        return customObject;
    }

    private static Pair<Map<String, Object>, ArrayList<?>> createCriteriaMetaValuesCustomObjectWithResult(final Map<String, Object> modifiedPropertiesHolder, final Map<String, Map<String, Object>> criteriaMetaValues, final AbstractEntity<?> applied, final boolean isCentreChanged) {
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
     * Determines the entity type for which criteria entity will be generated.
     *
     * @param miType
     * @return
     */
    public static Class<AbstractEntity<?>> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType entityTypeAnnotation = miType.getAnnotation(EntityType.class);
        if (entityTypeAnnotation == null) {
            throw new IllegalStateException(String.format("The menu item type [%s] must be annotated with EntityType annotation", miType.getName()));
        }
        return (Class<AbstractEntity<?>>) entityTypeAnnotation.value();
    }

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
    public static Class<? extends MiWithConfigurationSupport<?>> getMiType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final MiType annotation = AnnotationReflector.getAnnotation(criteriaType, MiType.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("The criteria type [%s] should be annotated with MiType annotation.", criteriaType.getName()));
        }
        return annotation.value();
    }

    /**
     * Determines the master type for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    public static Class<? extends AbstractEntity<?>> getMasterType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final Class<? extends MiWithConfigurationSupport<?>> miType = getMiType(criteriaType);
        final EntityType annotation = miType.getAnnotation(EntityType.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("The menu item type [%s] must be annotated with EntityType annotation.", miType.getName()));
        }
        return (Class<AbstractEntity<?>>) annotation.value();
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

    public static ICentreDomainTreeManagerAndEnhancer getCurrentPrincipleCentreManager(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, null) == null) {
            globalManager.initEntityCentreManager(miType, null);
        }

        if (globalManager.isChangedEntityCentreManager(miType, null)) {
            throw new IllegalStateException("Should be not changed (after init).");
        }

        final ICentreDomainTreeManagerAndEnhancer principleCentre = globalManager.getEntityCentreManager(miType, null);
        return principleCentre;
    }

    /**
     * Returns the current version of principle centre manager (initialises it in case if it is not created yet).
     *
     * @param globalManager
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer getCurrentCentreManager(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, FRESH_CENTRE_NAME) == null) {
            final ICentreDomainTreeManagerAndEnhancer principleCentre = getCurrentPrincipleCentreManager(globalManager, miType);
            final ICentreDomainTreeManagerAndEnhancer differenciesCentre = getCurrentDifferenciesCentreManager(globalManager, miType);
            final ICentreDomainTreeManagerAndEnhancer freshCentre = applyDifferencies(((GlobalDomainTreeManager) globalManager).copyCentre(principleCentre), differenciesCentre, getEntityType(miType));

            ((GlobalDomainTreeManager) globalManager).init(miType, FRESH_CENTRE_NAME, freshCentre, true);
        }
        return globalManager.getEntityCentreManager(miType, FRESH_CENTRE_NAME);

        //        if (globalManager.getEntityCentreManager(miType, null) == null) {
        //            globalManager.initEntityCentreManager(miType, null);
        //
        //            if (globalManager.isChangedEntityCentreManager(miType, null)) {
        //                throw new IllegalStateException("Should be not changed (after init).");
        //            }
        //
        //            final ICentreDomainTreeManagerAndEnhancer cdtmae = globalManager.getEntityCentreManager(miType, null);
        //            final Class<AbstractEntity<?>> root = getEntityType(miType);
        //            for (final String property : cdtmae.getFirstTick().checkedProperties(root)) {
        //                final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        //                final Class<?> propertyType = isEntityItself ? managedType(root, cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(root, cdtmae), property);
        //                final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType(root, cdtmae), property);
        //                final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());
        //
        //                if (!EntityUtils.isBoolean(propertyType) && !(EntityUtils.isEntityType(propertyType) && !single)) {
        //                    cdtmae.getFirstTick().setValue(root, property, null);
        //                    if (AbstractDomainTree.isDoubleCriterion(managedType(root, cdtmae), property)) {
        //                        cdtmae.getFirstTick().setValue2(root, property, null);
        //                    }
        //                }
        //            }
        //
        //            globalManager.saveEntityCentreManager(miType, null);
        //
        //            if (globalManager.isChangedEntityCentreManager(miType, null)) {
        //                throw new IllegalStateException("Should be not changed (after initial save).");
        //            }
        //        }
        //        return globalManager.getEntityCentreManager(miType, null);
    }

    private static ICentreDomainTreeManagerAndEnhancer applyDifferencies(final ICentreDomainTreeManagerAndEnhancer targetCentre, final ICentreDomainTreeManagerAndEnhancer differenciesCentre, final Class<AbstractEntity<?>> root) {
        for (final String property : differenciesCentre.getFirstTick().checkedProperties(root)) {
            if (AbstractDomainTree.isDoubleCriterion(managedType(root, differenciesCentre), property)) {
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE, root, property)) {
                    targetCentre.getFirstTick().setExclusive(root, property, differenciesCentre.getFirstTick().getExclusive(root, property));
                }
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property)) {
                    targetCentre.getFirstTick().setExclusive2(root, property, differenciesCentre.getFirstTick().getExclusive2(root, property));
                }
            }
            final Class<?> propertyType = StringUtils.isEmpty(property) ? managedType(root, differenciesCentre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, differenciesCentre), property);
            if (EntityUtils.isDate(propertyType)) {
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_PREFIX, root, property)) {
                    targetCentre.getFirstTick().setDatePrefix(root, property, differenciesCentre.getFirstTick().getDatePrefix(root, property));
                }
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property)) {
                    targetCentre.getFirstTick().setDateMnemonic(root, property, differenciesCentre.getFirstTick().getDateMnemonic(root, property));
                }
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.AND_BEFORE, root, property)) {
                    targetCentre.getFirstTick().setAndBefore(root, property, differenciesCentre.getFirstTick().getAndBefore(root, property));
                }
            }

            if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.OR_NULL, root, property)) {
                targetCentre.getFirstTick().setOrNull(root, property, differenciesCentre.getFirstTick().getOrNull(root, property));
            }
            if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.NOT, root, property)) {
                targetCentre.getFirstTick().setNot(root, property, differenciesCentre.getFirstTick().getNot(root, property));
            }

            if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE, root, property)) {
                targetCentre.getFirstTick().setValue(root, property, differenciesCentre.getFirstTick().getValue(root, property));
            }
            if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType(root, differenciesCentre), property)) {
                if (differenciesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE2, root, property)) {
                    targetCentre.getFirstTick().setValue2(root, property, differenciesCentre.getFirstTick().getValue2(root, property));
                }
            }
        }
        return targetCentre;
    }

    private static ICentreDomainTreeManagerAndEnhancer getCurrentDifferenciesCentreManager(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME) == null) {
            try {
                globalManager.initEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME);
            } catch (final IllegalArgumentException e) {
                if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                    getCurrentPrincipleCentreManager(globalManager, miType);

                    globalManager.saveAsEntityCentreManager(miType, null, DIFFERENCIES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }

                    applyDefaultValues(globalManager.getEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME), getEntityType(miType));

                    globalManager.saveEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }
                } else {
                    throw e;
                }
            }

            if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME)) {
                throw new IllegalStateException("Should be not changed.");
            }
        }
        final ICentreDomainTreeManagerAndEnhancer differenciesCentre = globalManager.getEntityCentreManager(miType, DIFFERENCIES_CENTRE_NAME);
        return differenciesCentre;
    }

    private static void applyDefaultValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root) {
        for (final String property : cdtmae.getFirstTick().checkedProperties(root)) {
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            final Class<?> propertyType = isEntityItself ? managedType(root, cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(root, cdtmae), property);
            final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType(root, cdtmae), property);
            final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());

            if (!EntityUtils.isBoolean(propertyType) && !(EntityUtils.isEntityType(propertyType) && !single)) {
                cdtmae.getFirstTick().setValue(root, property, null);
                if (AbstractDomainTree.isDoubleCriterion(managedType(root, cdtmae), property)) {
                    cdtmae.getFirstTick().setValue2(root, property, null);
                }
            }
        }
    }

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = getCurrentCentreManager(gdtm, miType);
        return restUtil.rawListJSONRepresentation(createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, -1L), createCriteriaMetaValuesCustomObject(createCriteriaMetaValues(originalCdtmae, getEntityType(miType)), isCentreChanged(miType, gdtm)));
    }

    /**
     * Handles POST request resulting resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = getCurrentCentreManager(gdtm, miType);
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        applyMetaValues(originalCdtmae, getEntityType(miType), modifiedPropertiesHolder);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        final AbstractEntity<?> applied = EntityResourceUtils.constructEntityAndResetMetaValues(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        return restUtil.rawListJSONRepresentation(applied, createCriteriaMetaValuesCustomObject(createCriteriaMetaValues(originalCdtmae, getEntityType(miType)), isCentreChanged(miType, gdtm)));
    }

    //    private boolean isChanged(final ICentreDomainTreeManagerAndEnhancer originalCdtmae, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
    //        return !EntityUtils.equalsEx(originalCdtmae, cdtmae);
    //    }

    /**
     * Handles PUT request resulting from tg-selection-criteria <code>run()</code> method.
     */
    @Put
    @Override
    public Representation put(final Representation envelope) throws ResourceException {
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = getCurrentCentreManager(gdtm, miType);
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        applyMetaValues(originalCdtmae, getEntityType(miType), modifiedPropertiesHolder);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        final AbstractEntity<?> applied = EntityResourceUtils.constructEntityAndResetMetaValues(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        final Pair<Map<String, Object>, ArrayList<?>> pair = createCriteriaMetaValuesCustomObjectWithResult(modifiedPropertiesHolder, createCriteriaMetaValues(originalCdtmae, getEntityType(miType)), applied, isCentreChanged(miType, gdtm));
        if (pair.getValue() == null) {
            return restUtil.rawListJSONRepresentation(applied, pair.getKey());
        }

        final ArrayList<Object> list = new ArrayList<Object>();
        list.add(applied);
        list.add(pair.getKey());

        list.addAll(pair.getValue()); // TODO why is this needed for serialisation to perform without problems?!

        return restUtil.rawListJSONRepresentation(list.toArray());
    }

    /**
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded), <code>false</code> otherwise.
     *
     * @param miType
     * @param gdtm
     * @return
     */
    private static boolean isCentreChanged(final Class<? extends MiWithConfigurationSupport<?>> miType, final IGlobalDomainTreeManager gdtm) {
        // getCurrentCentreManager(gdtm, miType);

        final boolean isCentreChanged = gdtm.isChangedEntityCentreManager(miType, FRESH_CENTRE_NAME);
        logger.debug("isCentreChanged == " + isCentreChanged);
        return isCentreChanged;
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
    public static Map<String, Map<String, Object>> createCriteriaMetaValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root) {
        final Map<String, Map<String, Object>> metaValues = new LinkedHashMap<>();
        final DefaultValueContract dvc = new DefaultValueContract();
        for (final String checkedProp : cdtmae.getFirstTick().checkedProperties(root)) {
            metaValues.put(checkedProp, createMetaValuesFor(root, checkedProp, cdtmae, dvc));
        }
        return metaValues;
    }

    public static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        return cdtmae.getEnhancer().getManagedType(root);
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
    private static void applyMetaValues(final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<AbstractEntity<?>> root, final Map<String, Object> modifiedPropertiesHolder) {
        final Map<String, Map<String, Object>> metaValues = (Map<String, Map<String, Object>>) modifiedPropertiesHolder.get("@@metaValues");

        for (final Entry<String, Map<String, Object>> propAndMetaValues : metaValues.entrySet()) {
            final String prop = propAndMetaValues.getKey();
            // cdtmae.getFirstTick().setOrNull(root, property, orNull)

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
    public static EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> createCriteriaValidationPrototype(
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
