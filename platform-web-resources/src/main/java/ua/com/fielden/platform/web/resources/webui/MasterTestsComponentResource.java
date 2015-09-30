package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Represents web server resource that returns entity master component for specified entity type to the client (for testing).
 *
 * @author TG Team
 *
 */
public class MasterTestsComponentResource extends ServerResource {
    private final EntityMaster<? extends AbstractEntity<?>> master;

    /**
     * Creates {@link MasterTestsComponentResource} and initialises it with master instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public MasterTestsComponentResource(
            final EntityMaster<? extends AbstractEntity<?>> master,
            final Context context,
            final Request request,
            final Response response //
    ) {
        init(context, request, response);
        this.master = master;
    }

    @Override
    protected Representation get() throws ResourceException {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(master.render().render().toString().getBytes(Charsets.UTF_8))));
    }
}