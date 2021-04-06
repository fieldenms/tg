package ua.com.fielden.web.domain_driven.authetication;

import static java.lang.String.format;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.google.inject.Inject;
import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.security.AbstractWebResourceGuard;

/**
 * This is a web application specific to testing of {@link AbstractWebResourceGuard};
 *
 * @author TG Team
 *
 */
class WebResourceGuardTestWebApplication extends Application {

    private final Injector injector;
    private User currentUser;

    @Inject
    WebResourceGuardTestWebApplication(final Injector injector) {
        this.injector = injector;
    }

    public void setCurrUser(final User user) {
        this.currentUser = user;
    }

    public User getCurrUser() {
        return currentUser;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        // let's create a router
        final Router router = new Router(getContext());

        // and add some other resource to be accessed
        router.attach(format("/users/{username}/%s/{entity-id}", TgPerson.class.getSimpleName()), new TestResource());

        // setup resource guard for the whole router
        final AbstractWebResourceGuard guard = new AbstractWebResourceGuard(getContext(), "tgdev.com", "/", injector) {
            
            @Override
            protected boolean enforceUserSessionEvictionWhenDbSessionIsMissing() {
                return true;
            }
            
            @Override
            protected User getUser(final String username) {
                if (getCurrUser() == null) {
                    throw new IllegalStateException("The current user has not been specified for the test case!");
                }
                return getCurrUser();
            }
        };

        guard.setNext(router);

        return guard;
    }

}
