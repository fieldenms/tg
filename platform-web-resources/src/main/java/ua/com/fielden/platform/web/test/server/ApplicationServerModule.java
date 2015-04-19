package ua.com.fielden.platform.web.test.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
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

}
