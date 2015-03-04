package ua.com.fielden.platform.web.resources.webui;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
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
public class CriteriaResource extends ServerResource {
    private final Logger logger = Logger.getLogger(getClass());

    private final RestServerUtil restUtil;

    private final EntityCentre centre;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final EntityCentre centre,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.centre = centre;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;

        this.restUtil = restUtil;
    }

    /**
     * Handles GET requests resulting from tg-entity-master <code>retrieve()</code> method (new or persisted entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        gdtm.initEntityCentreManager(centre.getMenuItemType(), null);
        final ICentreDomainTreeManagerAndEnhancer cdtmae = gdtm.getEntityCentreManager(centre.getMenuItemType(), null);
        final EntityType entityTypeAnnotation = centre.getMenuItemType().getAnnotation(EntityType.class);
        if (entityTypeAnnotation != null) {
            final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) entityTypeAnnotation.value();

            //    TODO     entityProducer = new DefaultEntityProducer<AbstractEntity<?>>(entityFactory, criteriaType);

            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> criteria = critGenerator.generateCentreQueryCriteria(entityType, cdtmae);
            return restUtil.singleJSONRepresentation(criteria);
        }
        throw new IllegalStateException("The menu item type must be annotated with EntityType annotation");
    }
}
