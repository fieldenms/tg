package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ioc.IModuleWithInjector;
import ua.com.fielden.platform.entity.validation.CanBuildReferenceHierarchyForEveryEntityValidator;
import ua.com.fielden.platform.entity.validation.ICanBuildReferenceHierarchyForEntityValidator;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.ioc.IBasicWebApplicationServerModule;

import java.util.List;
import java.util.Properties;

/**
 * Guice injector module for TG Testing Server (WebApp-enabled).
 *
 * @author TG Team
 *
 */
public class TgTestWebApplicationServerModule extends TgTestApplicationServerModule implements IBasicWebApplicationServerModule, IModuleWithInjector {

    private final Properties props;

    public TgTestWebApplicationServerModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Class<? extends IUniversalConstants> universalConstantsImplType,
            final Class<? extends IDates> datesImplType,
            final Properties props)
    {
        super(applicationDomainProvider, domainEntityTypes, universalConstantsImplType, datesImplType, props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ICanBuildReferenceHierarchyForEntityValidator.class).to(CanBuildReferenceHierarchyForEveryEntityValidator.class);
        bindWebAppResources(new WebUiConfig(props));
    }

    @Override
    public void setInjector(final Injector injector) {
        initWebApp(injector);
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bindType(final Class<T> clazz) {
        return bind(clazz);
    }
}
