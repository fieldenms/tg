package ua.com.fielden.platform.web.test.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.web.ioc.IBasicWebApplicationServerModule;

import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * Guice injector module for TG Testing Server (WebApp-enabled).
 *
 * @author TG Team
 *
 */
public class TgTestWebApplicationServerModule extends TgTestApplicationServerModule implements IBasicWebApplicationServerModule {

    public TgTestWebApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, props);
    }

    @Override
    protected void configure() {
        super.configure();
        bindWebAppResources(new WebUiConfig());
    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        initWebApp(injector);
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bindType(final Class<T> clazz) {
        return bind(clazz);
    }
}
