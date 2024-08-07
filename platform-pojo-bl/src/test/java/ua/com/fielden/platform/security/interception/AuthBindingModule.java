package ua.com.fielden.platform.security.interception;

import com.google.inject.AbstractModule;
import ua.com.fielden.platform.ioc.AuthorisationModule;
import ua.com.fielden.platform.security.IAuthorisationModel;

/**
 * A test authorisation module for binding of IAuthorisationModel.
 * 
 * @author TG Team
 * 
 */
final class AuthBindingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IAuthorisationModel.class).toInstance(new AuthorisationModel());
        install(new AuthorisationModule());
    }

}
