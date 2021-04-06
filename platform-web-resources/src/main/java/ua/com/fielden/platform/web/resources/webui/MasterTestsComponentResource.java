package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Represents web server resource that returns entity master component for specified entity type to the client (for testing).
 *
 * @author TG Team
 *
 */
public class MasterTestsComponentResource extends AbstractWebResource {
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
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response //
    ) {
        super(context, request, response, deviceProvider, dates);
        this.master = master;
    }

    @Get
    @Override
    public Representation get() {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(master.render().toString().getBytes(Charsets.UTF_8)), MediaType.TEXT_HTML));
    }
}