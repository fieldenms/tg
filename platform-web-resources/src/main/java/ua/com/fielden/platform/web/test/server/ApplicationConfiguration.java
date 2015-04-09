package ua.com.fielden.platform.web.test.server;

import java.util.Properties;

import org.restlet.Component;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.application.BasicServerApplication;
import ua.com.fielden.platform.web.factories.UserAuthResourceFactory;
import ua.com.fielden.platform.web.resources.OldVersionResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.system.SystemResources;

import com.google.inject.Injector;

/**
 * Configuration point for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationConfiguration extends Component {

    public ApplicationConfiguration(final Properties props) {
        // /////////////////////////////////////////////////////
        // ////// configure Hibernate and Guice injector ///////
        // /////////////////////////////////////////////////////
        try {
            // create application IoC module and injector
            final ApplicationDomain applicationDomainProvider = new ApplicationDomain();
            final ApplicationServerModule module = new ApplicationServerModule(HibernateSetup.getHibernateTypes(), applicationDomainProvider, applicationDomainProvider.domainTypes(), SerialisationClassProvider.class, NoDataFilter.class, props);
            final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();

            // create and configure REST server utility
            final RestServerUtil serverRestUtil = injector.getInstance(RestServerUtil.class);
            serverRestUtil.setAppWidePrivateKey(props.getProperty("private-key"));
            serverRestUtil.setAppWidePublicKey(props.getProperty("public-key"));

            // //////////////////////////////////////////////////////////////
            // ///// Create a component with an HTTP server connector ///////
            // //////////////////////////////////////////////////////////////
            // Attach applications to the default host
            final OldVersionResource oldVersionResource = new OldVersionResource(serverRestUtil);
            getDefaultHost().attach("/v0", oldVersionResource);

            // attach system resources, which should be beyond the version scope
            getDefaultHost().attach(
                    "/system",//
                    new SystemResources(//
                    getContext().createChildContext(),//
                    new UserAuthResourceFactory(injector, serverRestUtil), //
                    injector,//
                    injector.getInstance(EntityFactory.class),//
                    serverRestUtil,//
                    "/login",//
                    props.getProperty("dependencies.location"),//
                    "<h3>Web UI Testing Server</h3> This is a testing server for Trident Genesis platform Web UI module.")
                    );

            // attach application specific resources, which are versioned
            getDefaultHost().attach("/v1",//
            new ServerApplication(
                    "Web UI Testing Server", //
                    getContext().createChildContext(),//
                    injector,//
                    injector.getInstance(EntityFactory.class),//
                    serverRestUtil,//
                    props.getProperty("attachments.location"),//
                    BasicServerApplication.companionObjectTypes(applicationDomainProvider.domainTypes()))
                    );

            final IWebApp webApp = injector.getInstance(IWebApp.class);

            getDefaultHost().attach(
                    new WebApplication(
                            getContext().createChildContext(), injector,
                            "TG Web UI Testing Server", "The testing server for Trident Genesis platform Web UI module", "FMS", "FMS", webApp
                    ));
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
