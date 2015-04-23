package ua.com.fielden.platform.web.test.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.annotations.PasswordHashingKey;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.ioc.BasicWebApplicationServerModule;

/**
 * Guice injector module for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationServerModule extends BasicWebApplicationServerModule {

    public ApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, props, new WebApp());
    }

    public ApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Class<? extends IUniversalConstants> universalConstantsType, final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, universalConstantsType, props, new WebApp());
    }

    @Override
    protected void configure() {
        super.configure();

        // TODO rectify whether this logic is appropriate
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(PasswordHashingKey.class).to("This is a hasing key, which is used to hash user passwords in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes
    }

}
