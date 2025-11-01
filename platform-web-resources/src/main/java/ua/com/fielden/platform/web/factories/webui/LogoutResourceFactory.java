package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.apache.commons.lang3.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.annotations.SsoRedirectUriSignOut;
import ua.com.fielden.platform.security.session.ISsoSessionController;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.LogoutResource;

import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * A factory for a logout web resource.
 *
 * @author TG Team
 *
 */
public class LogoutResourceFactory extends Restlet {

    private final String domainName;
    private final String path;
    private final Injector injector;
    private final Optional<String> maybeSsoRedirectUriSignOut;

    public LogoutResourceFactory(final String domainName, final String path, final Injector injector) {
        this.domainName = domainName;
        this.path = path;
        this.injector = injector;
        // let's try to obtain SLO redirection URI and process by attaching and encoding the post_logout_redirect_uri parameter
        // the result could be an empty result, which is expected if either SSO or SLO are not configured
        this.maybeSsoRedirectUriSignOut = getSsoParam(SsoRedirectUriSignOut.class, injector)
                                          .map(uri -> {
                                              final String redirectUrl = "https://" + domainName + LogoutResource.BINDING_PATH;
                                              return uri + "?post_logout_redirect_uri=" + encodeUrl(redirectUrl);
                                          });
    }

    private static String encodeUrl(final String url) {
        return URLEncoder.encode(url, UTF_8);
    }
    
    /**
     * A helper method to obtain SSO parameters in a safe manner (e.i., without any exception).
     *
     * @param paramAnnotation
     * @param injector
     * @return
     */
    private static Optional<String> getSsoParam(final Class<? extends Annotation> paramAnnotation, final Injector injector) {
        try {
            final String uri = injector.getInstance(Key.get(String.class, paramAnnotation));
            return StringUtils.isBlank(uri) ? empty() : of(uri);
        } catch (final Exception ex) {
            return empty();            
        }
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            final ICompanionObjectFinder coFinder = injector.getInstance(ICompanionObjectFinder.class);
            final IUser coUser = coFinder.find(User.class, true);
            
            new LogoutResource(
                    injector.getInstance(IWebResourceLoader.class),
                    injector.getInstance(IUserProvider.class),
                    coUser,
                    injector.getInstance(IUserSession.class),
                    injector.getInstance(ISsoSessionController.class),
                    domainName,
                    path,
                    injector.getInstance(IDeviceProvider.class),
                    injector.getInstance(IDates.class),
                    maybeSsoRedirectUriSignOut,
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }

}
