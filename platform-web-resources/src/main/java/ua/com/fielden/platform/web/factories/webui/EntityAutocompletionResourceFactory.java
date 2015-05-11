package ua.com.fielden.platform.web.factories.webui;

import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.provider.IUserEx;
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
 * The entity type information is a part of the URI: "/users/{username}/validation/{entityType}/{property}".
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResourceFactory extends Restlet {
    private final Logger logger = Logger.getLogger(getClass());
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final IWebUiConfig webApp;

    /**
     * Instantiates a factory for entity validation resources.
     *
     * @param masters
     *            -- a list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityAutocompletionResourceFactory(final IWebUiConfig webApp, final Injector injector) {
        this.webApp = webApp;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

            final String entityTypeString = (String) request.getAttributes().get("entityType");
            final String propertyName = (String) request.getAttributes().get("property");

            final ICompanionObjectFinder coFinder = injector.getInstance(ICompanionObjectFinder.class);

            // the type represents 'autocompletion type', to which autocompleter was bound. It can be "miType" (the identificator of corresponding centre) or "entity master entity" (not generated)
            final Class<?> type = ClassesRetriever.findClass(entityTypeString);
            if (MiWithConfigurationSupport.class.isAssignableFrom(type)) {
                final String criterionPropertyName = propertyName;

                final IGlobalDomainTreeManager gdtm = injector.getInstance(IGlobalDomainTreeManager.class);
                final ICriteriaGenerator critGenerator = injector.getInstance(ICriteriaGenerator.class);
                final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) type; // CentreUtils.getMiType(criteriaType);
                final EntityCentre<? extends AbstractEntity<?>> centre = this.webApp.getCentres().get(miType);

                final CriteriaEntityAutocompletionResource resource = new CriteriaEntityAutocompletionResource(miType, criterionPropertyName, centre, coFinder, gdtm, critGenerator, restUtil, getContext(), request, response);
                resource.handle();
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
                    entityProducer = EntityMaster.createDefaultEntityProducer(factory, entityType);
                }
                final EntityAutocompletionResource resource = new EntityAutocompletionResource(entityType, propertyName, entityProducer, factory, valueMatcher, coFinder, restUtil, getContext(), request, response);
                resource.handle();
            }

        }
    }
}
