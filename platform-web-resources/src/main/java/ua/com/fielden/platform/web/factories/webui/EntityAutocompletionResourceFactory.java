package ua.com.fielden.platform.web.factories.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import java.util.Optional;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CriteriaEntityAutocompletionResource;
import ua.com.fielden.platform.web.resources.webui.EntityAutocompletionResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * A factory for entity autocompletion resources which instantiate resources based on entity type and propertyName of the autocompletion property.
 *
 * The entity type information is a part of the URI: "/autocompletion/{type}/{property}".
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResourceFactory extends Restlet {
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder companionFinder;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    
    /**
     * Instantiates a factory for entity autocompletion resources (for centres and masters).
     *
     * @param webUiConfig
     * @param injector
     */
    public EntityAutocompletionResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.domainTreeEnhancerCache = injector.getInstance(IDomainTreeEnhancerCache.class);
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.POST == request.getMethod()) {
            final String typeAndSaveAsName = (String) request.getAttributes().get("type");
            final String[] splitted = typeAndSaveAsName.split(":"); 
            final String propertyName = (String) request.getAttributes().get("property");
            
            // the type represents 'autocompletion type', to which autocompleter was bound. It can be "miType" (the identifier of corresponding centre) or "entity master entity" (not generated)
            final Class<?> type = ClassesRetriever.findClass(splitted[0]);
            if (MiWithConfigurationSupport.class.isAssignableFrom(type)) {
                final Optional<String> saveAsName = splitted.length > 1 ? of(splitted[1].replace("%20", " ")) : empty();
                final String criterionPropertyName = propertyName;
                
                final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) type;
                final EntityCentre<? extends AbstractEntity<?>> centre = this.webUiConfig.getCentres().get(miType);
                
                new CriteriaEntityAutocompletionResource(
                        webUiConfig, 
                        companionFinder,
                        userProvider,
                        deviceProvider,
                        critGenerator, 
                        factory, 
                        miType,
                        saveAsName,
                        criterionPropertyName,
                        centre,
                        restUtil,
                        domainTreeEnhancerCache,
                        getContext(),
                        request,
                        response //
                ).handle();
            } else {
                final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) type;
                final IEntityProducer<? extends AbstractEntity<?>> entityProducer;
                final IValueMatcherWithContext<?, ?> valueMatcher;

                final EntityMaster<? extends AbstractEntity<?>> master = this.webUiConfig.getMasters().get(entityType);
                if (master != null) {
                    valueMatcher = master.createValueMatcher(propertyName);
                    entityProducer = master.createEntityProducer();
                } else { // in case where no master is registered for the type -- use default producer and value matcher
                    valueMatcher = EntityMaster.createDefaultValueMatcher(propertyName, entityType, companionFinder);
                    entityProducer = EntityMaster.createDefaultEntityProducer(factory, entityType, companionFinder);
                }
                new EntityAutocompletionResource(
                        entityType,
                        propertyName,
                        entityProducer,
                        valueMatcher,
                        companionFinder,
                        restUtil,
                        deviceProvider,
                        master,
                        getContext(),
                        request,
                        response //
                ).handle();
            }

        }
    }
}
