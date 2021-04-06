package ua.com.fielden.platform.entity.query.generation.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.DefaultUniversalConstants;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * This is a helper IoC module that should be used (and enhanced if needed) with dependency bindings of named values for testing purposes that do not belong elsewhere.   
 * 
 * @author TG Team
 *
 */
public class HelperIocModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("app.name")).to("Test");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("non-existing-server");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tg@fielden.com.au");
        bindConstant().annotatedWith(Names.named("independent.time.zone")).to(false);
        bind(IDates.class).to(DefaultDates.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(DefaultUniversalConstants.class).in(Scopes.SINGLETON);
    }

}
