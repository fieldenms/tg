package ua.com.fielden.platform.web.test;

import ua.com.fielden.platform.ioc.CommonRestFactoryModule;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.google.inject.Scopes;

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
	bind(ISerialiser.class).to(TgKryo.class).in(Scopes.SINGLETON);
    }
}
