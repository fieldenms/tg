package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.entity.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.ioc.AuthorisationIocModule;
import ua.com.fielden.platform.security.IAuthorisationModel;

/**
 * A test authorisation module for binding of IAuthorisationModel.
 * 
 * @author TG Team
 * 
 */
final class AuthenticationTestIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(IAuthorisationModel.class).toInstance(new AuthorisationModelForTests());
        install(new AuthorisationIocModule());
    }

}
