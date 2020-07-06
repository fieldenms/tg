package ua.com.fielden.platform.web.resources.webui;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

public class ErrorLoggerResource extends AbstractWebResource {
    private final static Logger LOGGER = Logger.getLogger(ErrorLoggerResource.class);

    public ErrorLoggerResource(final Context context, final Request request, final Response response, final IDeviceProvider deviceProvider, final IDates dates) {
        super(context, request, response, deviceProvider, dates);
    }

    /**
     * Handles POST request resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Put
    @Override
    public Representation put(final Representation envelope) {
        try {
            LOGGER.error("Error happened on " + device().name() + " device:\n" + envelope.getText());
        } catch (final IOException e) {
            LOGGER.debug(e);
        }
        return new StringRepresentation("ok");
    }
}
