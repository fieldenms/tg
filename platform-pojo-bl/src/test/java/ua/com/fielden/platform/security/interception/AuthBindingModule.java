package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.security.IAuthorisationModel;

import com.google.inject.AbstractModule;

/**
 * A test authorisation module for binding of IAuthorisationModel.
 * 
 * @author TG Team
 * 
 */
public class AuthBindingModule extends AbstractModule {

    @Override
    protected void configure() {
	bind(IAuthorisationModel.class).toInstance(new AuthorisationModel());
    }

}
