package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CriteriaEntityAutocompletionResource;
import ua.com.fielden.platform.web.resources.webui.EntityAutocompletionResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import com.google.inject.Injector;

/**
 * A factory for entity autocompletion resources which instantiate resources based on entity type and propertyName of the autocompletion property.
 *
 * The entity type information is a part of the URI: "/autocompletion/{type}/{property}".
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final IWebUiConfig webApp;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder coFinder;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;


    /**
     * Instantiates a factory for entity autocompletion resources (for centres and masters).
     *
     * @param webUiConfig
     * @param injector
     */
    public EntityAutocompletionResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webApp = webUiConfig;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final String typeString = (String) request.getAttributes().get("type");
            final String propertyName = (String) request.getAttributes().get("property");

            // the type represents 'autocompletion type', to which autocompleter was bound. It can be "miType" (the identifier of corresponding centre) or "entity master entity" (not generated)
            final Class<?> type = ClassesRetriever.findClass(typeString);
            if (MiWithConfigurationSupport.class.isAssignableFrom(type)) {
                final String criterionPropertyName = propertyName;

                final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) type;
                final EntityCentre<? extends AbstractEntity<?>> centre = this.webApp.getCentres().get(miType);

                new CriteriaEntityAutocompletionResource(
                        webApp, 
                        coFinder, 
                        serverGdtm, 
                        userProvider, 
                        critGenerator, 
                        factory, 
                        miType,
                        criterionPropertyName,
                        centre,
                        restUtil,
                        getContext(),
                        request,
                        response //
                ).handle();
            } else {
                final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) type;
                final IEntityProducer<? extends AbstractEntity<?>> entityProducer;
                final IValueMatcherWithContext<?, ?> valueMatcher;

                final EntityMaster<? extends AbstractEntity<?>> master = this.webApp.getMasters().get(entityType);
                if (master != null) {
                    valueMatcher = master.createValueMatcher(propertyName);
                    entityProducer = master.createEntityProducer();
                } else { // in case when no master is registered for the type -- use default producer and value matcher
                    valueMatcher = EntityMaster.createDefaultValueMatcher(propertyName, entityType, coFinder);
                    entityProducer = EntityMaster.createDefaultEntityProducer(factory, entityType, coFinder);
                }
                new EntityAutocompletionResource(
                        entityType,
                        propertyName,
                        entityProducer,
                        factory,
                        valueMatcher,
                        coFinder,
                        restUtil,
                        getContext(),
                        request,
                        response //
                ).handle();
            }

        }
    }
}
