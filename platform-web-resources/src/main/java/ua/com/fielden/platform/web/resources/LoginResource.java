package ua.com.fielden.platform.web.resources;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {
    private final Logger logger = Logger.getLogger(LoginResource.class);

    private final IUserProvider userProvider;
    private final IUserEx coUserEx;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link LoginResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public LoginResource(//
    final IUserProvider userProvider,
            final IUserEx coUserEx,
            final IUserSession coUserSession,//
            final RestServerUtil restUtil,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.userProvider = userProvider;
        this.coUserEx = coUserEx;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;

        // TODO username can only come from an authentication cookie
        //final String username = (String) request.getAttributes().get("username");
        //injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.
            final Cookie cookie = getRequest().getCookies().getFirst(AUTHENTICATOR_COOKIE_NAME);
            if (cookie != null) {
                final String authenticator = cookie.getValue();
                if (!StringUtils.isEmpty(authenticator)) {
                    final Authenticator auth = fromString(authenticator);
                    userProvider.setUsername(auth.username, coUserEx);
                    final Optional<UserSession> session = coUserSession.currentSession(userProvider.getUser(), authenticator);
                    if (session.isPresent()) {
                        final CookieSetting newCookie = new CookieSetting(1, AUTHENTICATOR_COOKIE_NAME, session.get().getAuthenticator().get().toString(), "/", null);
                        newCookie.setAccessRestricted(true);
                        getResponse().getCookieSettings().clear();
                        getResponse().getCookieSettings().add(newCookie);

                        final byte[] body = "".getBytes("UTF-8");
                        getResponse().redirectSeeOther("/");
                        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
                    }
                }
            }

            // otherwise just load the login page for user to login in explicitly
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login.html").replaceAll("@title", "Login").getBytes("UTF-8");

            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }

    @Put
    @Override
    public Representation put(final Representation entity) throws ResourceException {
        try {

            final String username = getRequest().getResourceRef().getQueryAsForm().getFirstValue("username");
            final String password = getRequest().getResourceRef().getQueryAsForm().getFirstValue("passwd");
            final Boolean isDeviceTrusted = Boolean.parseBoolean(getRequest().getResourceRef().getQueryAsForm().getFirstValue("trusted-device"));

            System.out.println(format("Form data: %s, %s, %s", username, password, isDeviceTrusted));

            if (!"SU".equals(username)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                final byte[] body = "/login".getBytes("UTF-8");
                return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
            } else {
                final User user = coUserEx.findByKeyAndFetch(fetch(User.class).with("key").with("password"), username);
                final UserSession session = coUserSession.newSession(user, isDeviceTrusted);

                final CookieSetting newCookie = new CookieSetting(1, AUTHENTICATOR_COOKIE_NAME, session.getAuthenticator().get().toString(), "/", null);
                newCookie.setAccessRestricted(true);
                getResponse().getCookieSettings().clear();
                getResponse().getCookieSettings().add(newCookie);


                final byte[] body = "/".getBytes("UTF-8");
                return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
            }
        } catch (final UnsupportedEncodingException ex) {
            logger.fatal(ex);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorJSONRepresentation(ex);
        }
    }

}
