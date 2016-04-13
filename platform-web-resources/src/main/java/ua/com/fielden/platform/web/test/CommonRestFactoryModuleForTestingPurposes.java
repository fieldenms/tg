package ua.com.fielden.platform.web.test;

import com.google.inject.Scopes;

import ua.com.fielden.platform.ioc.CommonRestFactoryModule;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;

/**
 * Module for REST test clients.
 *
 * @author TG Team
 *
 */
public class CommonRestFactoryModuleForTestingPurposes extends CommonRestFactoryModule {

    private final ISerialisationClassProvider serialisationClassProvider;

    public CommonRestFactoryModuleForTestingPurposes(final RestClientUtil restUtil, final ISerialisationClassProvider serialisationClassProvider) {
        super(restUtil);
        this.serialisationClassProvider = serialisationClassProvider;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(ISerialisationClassProvider.class).toInstance(serialisationClassProvider);
        bind(ISerialiser0.class).to(Serialiser0.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(Serialiser.class).in(Scopes.SINGLETON);
    }
}
