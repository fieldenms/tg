package ua.com.fielden.platform.web.resources.test;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test.ioc.PlatformTestServerIocModule;
import ua.com.fielden.platform.web.resources.webui.test_entities.*;

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

    @Override
    protected void bindDomainCompanionObjects(final List<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        super.bindDomainCompanionObjects(domainEntityTypes);

        bind(Action1Co.class).to(Action1Dao.class);
        bind(Action2Co.class).to(Action2Dao.class);
        bind(Action3Co.class).to(Action3Dao.class);
    }

}
