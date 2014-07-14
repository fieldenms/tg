package ua.com.fielden.platform.web.resources;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;

public class CentreResource extends ServerResource {

    private final String menuItemType;
    private final ISerialiser serialiser;
    private final IEntityCentreConfigController eccc;
    private final String username;

    public CentreResource(//
    final IEntityCentreConfigController eccc, //
            final ISerialiser serialiser, //
            final Context context, //
            final Request request, //
            final Response response, //
            final String username) {
        this.username = username;
        init(context, request, response);
        this.eccc = eccc;
        this.serialiser = serialiser;
        this.menuItemType = (String) request.getAttributes().get("centreName");
    }

    @Override
    protected Representation get() throws ResourceException {
        final EntityResultQueryModel<EntityCentreConfig> model = modelSystemUser();
        return retrieveAndInit(model);
    }

    private Representation retrieveAndInit(final EntityResultQueryModel<EntityCentreConfig> model) {
        try {
            final EntityCentreConfig entityCentre = eccc.getEntity(from(model).model());
            final CentreDomainTreeManagerAndEnhancer cdtmae = serialiser.deserialise(entityCentre.getConfigBody(), CentreDomainTreeManagerAndEnhancer.class);
            final String centreString = new TgObjectMapper().writeValueAsString(cdtmae);
            return new JsonRepresentation(centreString);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Couldn't deserialise the entity centre.");
        }
    }

    private EntityResultQueryModel<EntityCentreConfig> modelSystemUser() {
        final EntityResultQueryModel<EntityCentreConfig> model =
        /*    */select(EntityCentreConfig.class).where().//
        /*    */prop("owner.key").eq().val(username).and().// look for entity-centres for both users (current and its base)
        /*    */prop("title").eq().val(menuItemType).and().//
        /*    */prop("menuItem.key").eq().val(menuItemType).model();
        return model;
    }
}
