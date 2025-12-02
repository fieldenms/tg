package ua.com.fielden.platform.security.interception;

import com.google.inject.name.Names;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.ioc.AuthorisationIocModule;
import ua.com.fielden.platform.security.IAuthorisationModel;

/**
 * A test authorisation module for binding of IAuthorisationModel.
 * 
 * @author TG Team
 * 
 */
public final class AuthenticationTestIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(IAuthorisationModel.class).toInstance(new AuthorisationModelForTests());
        install(new AuthorisationIocModule());

        bindConstant().annotatedWith(Names.named("tokens.path")).to("../platform-pojo-bl/target/test-classes");
        bindConstant().annotatedWith(Names.named("tokens.package")).to("ua.com.fielden.platform.security.tokens");
        requestStaticInjection(EntityQueryCriteriaUtils.class);
    }

}
