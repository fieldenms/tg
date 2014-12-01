package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.web.centre.EntityCentre;

import com.google.common.base.Charsets;

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
            final ISerialiser serialiser) {
        init(context, request, response);
        this.centre = centre;
        this.gdtm = gdtm;
        this.serialiser = serialiser;
    }

    @Override
    protected Representation get() throws ResourceException {
        gdtm.initEntityCentreManager(centre.getMenuItemType(), null);
        final ICentreDomainTreeManagerAndEnhancer cdtmae = gdtm.getEntityCentreManager(centre.getMenuItemType(), null);

        final String centreString = new String(serialiser.serialise(cdtmae, SerialiserEngines.JACKSON), Charsets.UTF_8);
        return new JsonRepresentation(centreString);
    }
}
