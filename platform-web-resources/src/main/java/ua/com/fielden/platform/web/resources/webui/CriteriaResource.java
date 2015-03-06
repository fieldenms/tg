package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiTypeAnnotation;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;

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
    private final Logger logger = Logger.getLogger(getClass());

    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final ICompanionObjectFinder companionFinder;

    private final EntityCentre centre;
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,

            final EntityCentre centre,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;
        this.factory = factory;
        this.companionFinder = companionFinder;

        this.centre = centre;
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
        return restUtil.singleJSONRepresentation(createCriteriaValidationPrototype(miType, gdtm, critGenerator));
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
     * Generates marker with mi type.
     *
     * @param miType
     * @return
     */
    private static NewProperty createMiTypeNewProperty(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return new NewProperty("________________masterTypeMarker", String.class, false, "Master Type Marker", "Master Type Marker Desc", new MiTypeAnnotation().newInstance(miType));
    }

    /**
     * Determines the miType for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    public static Class<? extends MiWithConfigurationSupport<?>> getMiType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final MiType annotation = AnnotationReflector.getPropertyAnnotation(MiType.class, criteriaType, "________________masterTypeMarker"); // criteriaType.getAnnotation(MasterEntityType.class);
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
    public static String getOriginalPropertyName(final String propertyName) {
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO  propertyName.substring(propertyName.lastIndexOf("_") + 1); VERY BAD APPROACH!
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        return propertyName.substring(propertyName.lastIndexOf("_") + 1);
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
        // final Class<AbstractEntity<?>> criteriaType = (Class<AbstractEntity<?>>) validationPrototype.getType();
        // final EntityResourceUtils<AbstractEntity<?>> utils = new EntityResourceUtils<AbstractEntity<?>>(criteriaType, new DefaultEntityProducer<AbstractEntity<?>>(factory, criteriaType), factory, restUtil, companionFinder);
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = createCriteriaValidationPrototype(miType, gdtm, critGenerator);
        final AbstractEntity<?> applied = EntityResourceUtils.constructEntity(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        return restUtil.singleJSONRepresentation(applied);
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
            final ICriteriaGenerator critGenerator) {
        final ICentreDomainTreeManagerAndEnhancer cdtmae = getCurrentCentreManager(gdtm, miType);
        final Class<AbstractEntity<?>> entityType = getEntityType(miType);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype = critGenerator.generateCentreQueryCriteria(entityType, cdtmae, createMiTypeNewProperty(miType));
        return validationPrototype;
    }
}
