package ua.com.fielden.platform.web.resources.webui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
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

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        final List<Object> criteriaEntityAndMetaValues = new ArrayList<>();
        criteriaEntityAndMetaValues.add(createCriteriaValidationPrototype(miType, gdtm, critGenerator, -1L));
        criteriaEntityAndMetaValues.add(createCriteriaMetaValues(miType, gdtm));
        return restUtil.rawListJSONRepresentation(criteriaEntityAndMetaValues);
    }

    /**
     * Determines the entity type for which criteria entity will be generated.
     *
     * @param miType
     * @return
     */
    private static Class<AbstractEntity<?>> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
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

    /**
     * Returns the current version of principle centre manager (initialises it in case if it is not created yet).
     *
     * @param globalManager
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getCurrentCentreManager(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final String centreName = null; // indicates that the entity centre is principle (TODO saveAsses are not supported yet)
        if (globalManager.getEntityCentreManager(miType, centreName) == null) {
            globalManager.initEntityCentreManager(miType, centreName);
        }
        return globalManager.getEntityCentreManager(miType, centreName);
    }

    /**
     * Handles POST request resulting resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = createCriteriaValidationPrototype(miType, gdtm, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        final AbstractEntity<?> applied = EntityResourceUtils.constructEntityAndResetMetaValues(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        return restUtil.singleJSONRepresentation(applied);
    }

    /**
     * Handles PUT request resulting from tg-selection-criteria <code>run()</code> method.
     */
    @Put
    @Override
    public Representation put(final Representation envelope) throws ResourceException {
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = createCriteriaValidationPrototype(miType, gdtm, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        final AbstractEntity<?> applied = EntityResourceUtils.constructEntityAndResetMetaValues(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        final List<Object> criteriaAndResultEntities = new ArrayList<>();
        criteriaAndResultEntities.add(applied);
        criteriaAndResultEntities.add(createCriteriaMetaValues(miType, gdtm));
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
            final List<AbstractEntity<?>> resultEntities = page.data();
            criteriaAndResultEntities.addAll(resultEntities);

            criteriaAndResultEntities.add(page.numberOfPages()); // pageCount
        }
        return restUtil.rawListJSONRepresentation(criteriaAndResultEntities);
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
    public static Map<String, Map<String, Object>> createCriteriaMetaValues(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final IGlobalDomainTreeManager gdtm) {

        final ICentreDomainTreeManagerAndEnhancer cdtmae = getCurrentCentreManager(gdtm, miType);
        final Class<AbstractEntity<?>> entityType = getEntityType(miType);

        final Map<String, Map<String, Object>> metaValues = new LinkedHashMap<>();
        final DefaultValueContract dvc = new DefaultValueContract();
        for (final String checkedProp : cdtmae.getFirstTick().checkedProperties(entityType)) {
            metaValues.put(checkedProp, createMetaValuesFor(entityType, checkedProp, cdtmae, dvc));
        }
        return metaValues;
    }

    private static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
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
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final Long previousVersion) {
        final ICentreDomainTreeManagerAndEnhancer cdtmae = getCurrentCentreManager(gdtm, miType);
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
