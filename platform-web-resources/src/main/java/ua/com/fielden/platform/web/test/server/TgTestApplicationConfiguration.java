package ua.com.fielden.platform.web.test.server;

import java.util.Properties;

import org.restlet.Component;

import com.google.inject.Injector;

import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierBindingModule;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.DefaultUniversalConstants;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.LoginCompleteResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginInitiateResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LogoutResourceFactory;
import ua.com.fielden.platform.web.resources.OldVersionResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginCompleteResetResource;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;
import ua.com.fielden.platform.web.resources.webui.LoginResource;
import ua.com.fielden.platform.web.resources.webui.LogoutResource;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

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
            final ApplicationDomain applicationDomainProvider = new ApplicationDomain();
            final TgTestWebApplicationServerModule module = new TgTestWebApplicationServerModule(HibernateSetup.getHibernateTypes(), applicationDomainProvider, applicationDomainProvider.domainTypes(), SerialisationClassProvider.class, ExampleDataFilter.class, DefaultUniversalConstants.class, DefaultDates.class, props);
            injector = new ApplicationInjectorFactory().add(module).add(new NewUserEmailNotifierBindingModule()).getInjector();

            // create and configure REST server utility
            final RestServerUtil serverRestUtil = injector.getInstance(RestServerUtil.class);

            // //////////////////////////////////////////////////////////////
            // ///// Create a component with an HTTP server connector ///////
            // //////////////////////////////////////////////////////////////
            // Attach applications to the default host
            final OldVersionResource oldVersionResource = new OldVersionResource(serverRestUtil);
            getDefaultHost().attach("/v0", oldVersionResource);

            // application configuration
            final IWebUiConfig webApp = injector.getInstance(IWebUiConfig.class);
            // attach system resources, which should be beyond the version scope
            // the interactive login page resource is considered one of the system resources, which does not require guarding
            getDefaultHost().attach(LoginResource.BINDING_PATH, new LoginResourceFactory(injector.getInstance(RestServerUtil.class), injector));
            getDefaultHost().attach(LoginInitiateResetResource.BINDING_PATH, new LoginInitiateResetResourceFactory(injector));
            getDefaultHost().attach(LoginCompleteResetResource.BINDING_PATH, new LoginCompleteResetResourceFactory(injector, "Robust solutions serve the sociaty well."));
            getDefaultHost().attach(LogoutResource.BINDING_PATH, new LogoutResourceFactory(webApp.getDomainName(), webApp.getPath(), injector));

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
