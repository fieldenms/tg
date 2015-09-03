package ua.com.fielden.platform.web.resources.webui;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for criteria serves as a back-end mechanism of centre management. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>save centre -- POST request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CentreResource<CRITERIA_TYPE extends AbstractEntity<?>> extends ServerResource {
    private final static Logger logger = Logger.getLogger(CentreResource.class);

    private final RestServerUtil restUtil;

    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final IGlobalDomainTreeManager gdtm;

    public CentreResource(
            final RestServerUtil restUtil,

            final EntityCentre centre,
            final IGlobalDomainTreeManager gdtm,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;

        miType = centre.getMenuItemType();
        this.gdtm = gdtm;
    }

    /**
     * Handles POST request resulting from tg-entity-centre <code>save()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            //            // NOTE: the following line can be the example how 'centre saving' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during centre saving.");
            // gets the fresh centre (that was created from the chain 'default centre' + 'saved diff centre' + 'current user diff' := 'fresh centre')
            final ICentreDomainTreeManagerAndEnhancer freshCentre = CentreResourceUtils.freshCentre(gdtm, miType);
            // removes the fresh centre -- to be later re-populated
            CentreResourceUtils.removeFreshCentre(gdtm, miType);

            final ICentreDomainTreeManagerAndEnhancer defaultCentre = CentreResourceUtils.getDefaultCentre(gdtm, miType);
            // creates differences centre from the differences between 'default centre' and 'fresh centre'
            final ICentreDomainTreeManagerAndEnhancer differencesCentre = CentreUtils.createDifferencesCentre(freshCentre, defaultCentre, CentreResourceUtils.getEntityType(miType), gdtm);

            // override old 'diff centre' with recently created one and save it
            CentreResourceUtils.overrideAndSaveDifferencesCentre(gdtm, miType, differencesCentre);

            return restUtil.rawListJSONRepresentation("OK");
        }, restUtil);
    }

    /**
     * Handles DELETE request resulting from tg-entity-centre <code>discard()</code> method.
     */
    @Delete
    @Override
    public Representation delete() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            //            // NOTE: the following line can be the example how 'centre discarding' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during centre discarding.");
            // discards fresh centre's changes (here fresh centre should have changes -- otherwise the exception will be thrown)
            CentreResourceUtils.discardFreshCentre(gdtm, miType);
            return restUtil.rawListJSONRepresentation("OK");
        }, restUtil);
    }
}
