package ua.com.fielden.platform.web.test.server;

import java.util.Properties;

import org.restlet.Component;

import com.google.inject.Injector;

import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.LoginCompleteResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginInitiateResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LogoutResourceFactory;
import ua.com.fielden.platform.web.resources.OldVersionResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Configuration point for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class TgTestApplicationConfiguration extends Component {
    private final Injector injector;

    public TgTestApplicationConfiguration(final Properties props) {
        // /////////////////////////////////////////////////////
        // ////// configure Hibernate and Guice injector ///////
        // /////////////////////////////////////////////////////
        try {
            // create application IoC module and injector
            final TgTestApplicationDomain applicationDomainProvider = new TgTestApplicationDomain();
            final TgTestWebApplicationServerModule module = new TgTestWebApplicationServerModule(HibernateSetup.getHibernateTypes(), applicationDomainProvider, applicationDomainProvider.domainTypes(), SerialisationClassProvider.class, NoDataFilter.class, props);
            injector = new ApplicationInjectorFactory().add(module).getInjector();

            // create and configure REST server utility
            final RestServerUtil serverRestUtil = injector.getInstance(RestServerUtil.class);

            // //////////////////////////////////////////////////////////////
            // ///// Create a component with an HTTP server connector ///////
            // //////////////////////////////////////////////////////////////
            // Attach applications to the default host
            final OldVersionResource oldVersionResource = new OldVersionResource(serverRestUtil);
            getDefaultHost().attach("/v0", oldVersionResource);

            // attach system resources, which should be beyond the version scope
            // the interactive login page resource is considered one of the system resources, which does not require guarding
            getDefaultHost().attach("/login", new LoginResourceFactory(injector.getInstance(RestServerUtil.class), injector));
            getDefaultHost().attach("/forgotten", new LoginInitiateResetResourceFactory(injector.getInstance(RestServerUtil.class), injector));
            getDefaultHost().attach("/reset_password/{uuid}", new LoginCompleteResetResourceFactory(injector.getInstance(RestServerUtil.class), injector));
            getDefaultHost().attach("/logout", new LogoutResourceFactory(injector));
            
            // FIXME The old resources need to be completely removed from the platform
//            getDefaultHost().attach(
//                    "/system",//
//                    new SystemResources(//
//                            getContext().createChildContext(),//
//                            new UserAuthResourceFactory(injector, serverRestUtil), //
//                            injector,//
//                            injector.getInstance(EntityFactory.class),//
//                            serverRestUtil,//
//                            "/login",//
//                            props.getProperty("dependencies.location"),//
//                            "<h3>Web UI Testing Server</h3> This is a testing server for Trident Genesis platform Web UI module."
//                    )
//            );

            // FIXME The old resources need to be completely removed from the platform
            // attach application specific resources, which are versioned
//            getDefaultHost().attach("/v1",//
//                    new TgTestServerApplication(
//                            "Web UI Testing Server", //
//                            getContext().createChildContext(),//
//                            injector,//
//                            injector.getInstance(EntityFactory.class),//
//                            serverRestUtil,//
//                            props.getProperty("attachments.location"),//
//                            BasicServerApplication.companionObjectTypes(applicationDomainProvider.domainTypes()))
//                    );

            final IWebUiConfig webApp = injector.getInstance(IWebUiConfig.class);
            getDefaultHost().attach( // TODO potentially versioning is desirable "/v1"
                    new WebUiResources(
                            getContext().createChildContext(), injector,
                            "TG Web UI Testing Server", "The testing server for Trident Genesis platform Web UI module", "FMS", "R&D Team", webApp
                    ));

        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public Injector injector() {
        return injector;
    }
}
