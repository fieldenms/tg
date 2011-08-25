package ua.com.fielden.platform.web.test;

import ua.com.fielden.platform.ioc.CommonRestFactoryModule;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * Module for REST test clients.
 *
 * @author TG Team
 *
 */
public class CommonRestFactoryModuleForTestingPurposes extends CommonRestFactoryModule {

    public CommonRestFactoryModuleForTestingPurposes(final RestClientUtil restUtil) {
	super(restUtil);
    }

    @Override
    protected void configure() {
	super.configure();

	bind(ISerialiser.class).to(ClientSerialiser.class);
    }
}
