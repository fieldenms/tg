package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns entity centre component for the specified 'miType' to the client.
 *
 * @author TG Team
 *
 */
public class CentreComponentResource extends DeviceProfileDifferentiatorResource {
    private final String mitypeString;
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;
    
    /**
     * Creates {@link CentreComponentResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public CentreComponentResource(
            final ISourceController sourceController,//
            final RestServerUtil restUtil,
            final IUserProvider userProvider,
            final Context context, //
            final Request request, //
            final Response response) {
        super(context, request, response, userProvider);
        this.mitypeString = (String) request.getAttributes().get("mitype");
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }

    @Override
    protected Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController.loadSource("/centre_ui/" + this.mitypeString, device());
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil);
    }
}
