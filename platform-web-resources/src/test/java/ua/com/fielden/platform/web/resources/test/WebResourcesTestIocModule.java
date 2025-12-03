package ua.com.fielden.platform.web.resources.test;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;

import java.util.List;
import java.util.Properties;

class WebResourcesTestIocModule extends PlatformTestServerIocModule {

    public WebResourcesTestIocModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Properties props)
    {
        super(applicationDomainProvider, domainEntityTypes, props);
    }

}
