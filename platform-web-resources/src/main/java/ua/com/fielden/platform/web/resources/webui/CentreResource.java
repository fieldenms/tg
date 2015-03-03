package ua.com.fielden.platform.web.resources.webui;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that retrievers the entity centre configuration and returns it to the client..
 *
 * @author TG Team
 *
 */
public class CentreResource extends ServerResource {

    private final EntityCentre centre;
    private final IGlobalDomainTreeManager gdtm;
    private final ISerialiser serialiser;
    private final ICriteriaGenerator critGenerator;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link CentreResource} and initialises it with {@link EntityCentre} instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     * @param gdtm
     */
    public CentreResource(//
    final EntityCentre centre,//
            final Context context, //
            final Request request, //
            final Response response, //
            final IGlobalDomainTreeManager gdtm,//
            final ICriteriaGenerator critGenerator, //
            final RestServerUtil restUtil,//
            final ISerialiser serialiser) {
        init(context, request, response);
        this.centre = centre;
        this.gdtm = gdtm;
        this.serialiser = serialiser;
        this.critGenerator = critGenerator;
        this.restUtil = restUtil;
    }

    @Override
    protected Representation get() throws ResourceException {
        gdtm.initEntityCentreManager(centre.getMenuItemType(), null);
        final ICentreDomainTreeManagerAndEnhancer cdtmae = gdtm.getEntityCentreManager(centre.getMenuItemType(), null);
        final EntityType entityTypeAnnotation = centre.getMenuItemType().getAnnotation(EntityType.class);
        if (entityTypeAnnotation != null) {
            final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) entityTypeAnnotation.value();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> criteria = critGenerator.generateCentreQueryCriteria(entityType, cdtmae);
            return restUtil.singleJSONRepresentation(criteria);
        }
        throw new IllegalStateException("The menu item type must be annotated with EntityType annotation");
    }
}
