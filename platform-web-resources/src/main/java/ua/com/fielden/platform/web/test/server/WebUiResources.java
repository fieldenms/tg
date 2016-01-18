package ua.com.fielden.platform.web.test.server;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;
import ua.com.fielden.platform.web.factories.webui.FileProcessingResourceFactory;
import ua.com.fielden.platform.web.sse.resources.EventSourcingResourceFactory;
import ua.com.fielden.platform.web.test.eventsources.TgPersistentEntityWithPropertiesEventSrouce;

/**
 * Custom {@link AbstractWebUiResources} descendant for Web UI Testing Server. Provided in order to configure entity centres, masters and other client specific stuff.
 *
 * @author TG Team
 *
 */
public class WebUiResources extends AbstractWebUiResources {

    /**
     * Creates an instance of {@link WebUiResources} (for more information about the meaning of all this arguments see {@link AbstractWebUiResources#AbstractWebApp}
     *
     * @param context
     * @param injector
     * @param resourcePaths
     * @param name
     * @param desc
     * @param owner
     * @param author
     * @param username
     */
    public WebUiResources(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author,
            final IWebUiConfig webApp) {
        super(context, injector, name, desc, owner, author, webApp);
    }
    
    @Override
    protected void registerDomainWebResources(final Router router) {
        // register some file processors
        final FileProcessingResourceFactory factory = new FileProcessingResourceFactory(injector);
        router.attach("/file-processing/{processor-type}", factory);
        
        // register some server-side eventing
        // router.attach("/events",  new _EventSourcingResourceFactory()); -- some experimental stuff, which should be kept here for the moment
        router.attach("/entity-centre-events",  new EventSourcingResourceFactory(injector, TgPersistentEntityWithPropertiesEventSrouce.class));

    }
}
