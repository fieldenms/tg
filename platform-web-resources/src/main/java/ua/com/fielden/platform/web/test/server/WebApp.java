package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import org.restlet.Context;
import org.restlet.routing.Router;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesProducer;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.web.WebAppConfig;
import ua.com.fielden.platform.web.application.AbstractWebApp;
import ua.com.fielden.platform.web.master.EntityMaster;

import com.google.inject.Injector;

/**
 * Custom {@link AbstractWebApp} descendant for Web UI Testing Server. Provided in order to configure entity centres, masters and other client specific stuff.
 *
 * @author TG Team
 *
 */
public class WebApp extends AbstractWebApp {

    /**
     * Creates an instance of {@link WebApp} (for more information about the meaning of all this arguments see {@link AbstractWebApp#AbstractWebApp}
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
    public WebApp(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author,
            final String username) {
        super(context, injector, new String[0], name, desc, owner, author, username);
    }

    /**
     * Configures the {@link WebAppConfig} with custom entity centres.
     */
    @Override
    protected void initWebApplication(final WebAppConfig app) {
        // Add entity centres.
        //        app.addCentre(new EntityCentre(MiPerson.class, "Personnel"));
        //        app.addCentre(new EntityCentre(MiTimesheet.class, "Timesheet"));
        // Add custom views.
        //        app.addCustomView(new MyProfile(), true);
        //        app.addCustomView(new CustomWebView(new CustomWebModel()));

        // Add entity masters.
        app.addMaster(new EntityMaster<EntityWithInteger>(EntityWithInteger.class, fetchOnly(EntityWithInteger.class).with("prop")));
        app.addMaster(new EntityMaster<TgPersistentEntityWithProperties>(TgPersistentEntityWithProperties.class,
                fetchOnly(TgPersistentEntityWithProperties.class)
                        .with("integerProp")
                        .with("moneyProp")
                        .with("bigDecimalProp")
                        .with("stringProp")
                        .with("booleanProp")
                        .with("dateProp")
                        .with("entityProp", fetchOnly(TgPersistentEntityWithProperties.class).with("key")), TgPersistentEntityWithPropertiesProducer.class)
                );
    }

    @Override
    protected void attachFunctionalEntities(final Router router, final Injector injector) {
        // router.attach("/users/{username}/CustomFunction", new FunctionalEntityResourceFactory<CustomFunction, ICustomFunction>(ICustomFunction.class, injector));
    }

}
