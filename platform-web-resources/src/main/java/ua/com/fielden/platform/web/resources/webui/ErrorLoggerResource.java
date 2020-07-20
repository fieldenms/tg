package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Logs client side errors.
 *
 * @author TG Team
 *
 */
public class ErrorLoggerResource extends AbstractWebResource {
    private final static Logger LOGGER = Logger.getLogger(ErrorLoggerResource.class);
    private final IUserProvider userProvider;

    public ErrorLoggerResource(
            final Context context,
            final Request request,
            final Response response,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates) {
        super(context, request, response, deviceProvider, dates);
        this.userProvider = userProvider;
    }

    /**
     * Handles PUT request resulting from error sender ajax on client.
     */
    @Put
    @Override
    public Representation put(final Representation envelope) {
        try {
            final User user = userProvider.getUser();
            final String logHeader = format("Client-side [%s] error for user [%s]:\n", device().name(), user.getKey()); 
            final String loggerDetails = envelope.getText();
            LOGGER.error(logHeader + loggerDetails);
            return new StringRepresentation("success");
        } catch (final Exception e) {
            LOGGER.debug(e);
            return new StringRepresentation("failure");
        }
        
    }
}
