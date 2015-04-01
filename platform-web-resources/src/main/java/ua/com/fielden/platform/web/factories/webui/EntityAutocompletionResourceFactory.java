package ua.com.fielden.platform.web.factories.webui;

import java.util.LinkedHashMap;
import java.util.Map;

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
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreResourceUtils;
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
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> masters;

    /**
     * Instantiates a factory for entity validation resources.
     *
     * @param masters
     *            -- a list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityAutocompletionResourceFactory(final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> masters, final Injector injector) {
        this.masters = new LinkedHashMap<>(masters.size());
        this.masters.putAll(masters);
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            final String entityTypeString = (String) request.getAttributes().get("entityType");
            final String propertyName = (String) request.getAttributes().get("property");

            final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
            final EntityMaster<? extends AbstractEntity<?>> master = this.masters.get(entityType);
            final ICompanionObjectFinder coFinder = injector.getInstance(ICompanionObjectFinder.class);
            final IGlobalDomainTreeManager gdtm = injector.getInstance(IGlobalDomainTreeManager.class);
            final ICriteriaGenerator critGenerator = injector.getInstance(ICriteriaGenerator.class);

            final IValueMatcherWithContext<?, ?> valueMatcher;
            final IEntityProducer<? extends AbstractEntity<?>> entityProducer;
            if (master != null) {
                valueMatcher = master.createValueMatcher(propertyName);
                entityProducer = master.createEntityProducer();
            } else { // in case of generated entities like EntityQueryCriteria -- there is no corresponding master registered. So -- use default producer and value matcher
                if (EntityQueryCriteria.class.isAssignableFrom(entityType)) {
                    final Class<? extends AbstractEntity<?>> originalType = CentreResourceUtils.getOriginalType(entityType);
                    final String originalPropertyName = CentreResourceUtils.getOriginalPropertyName(entityType, propertyName);
                    // final DynamicPropertyAnalyser dpa = new DynamicPropertyAnalyser(originalType, originalPropertyName);
                    // logger.error("entityType = " + entityType + " propertyName = " + propertyName + " originalType = " + originalType + " originalPropertyName = " + originalPropertyName + " dpa.getCriteriaFullName() = " + dpa.getCriteriaFullName());
                    valueMatcher = EntityMaster.createDefaultValueMatcher(originalPropertyName, originalType, coFinder);
                    entityProducer = EntityMaster.createDefaultEntityProducer(factory, entityType);
                } else {
                    valueMatcher = EntityMaster.createDefaultValueMatcher(propertyName, entityType, coFinder);
                    entityProducer = EntityMaster.createDefaultEntityProducer(factory, entityType);
                }
            }

            final EntityAutocompletionResource resource = new EntityAutocompletionResource(entityType, propertyName, entityProducer, factory, valueMatcher, coFinder, gdtm, critGenerator, restUtil, getContext(), request, response);
            resource.handle();
        }
    }
}
