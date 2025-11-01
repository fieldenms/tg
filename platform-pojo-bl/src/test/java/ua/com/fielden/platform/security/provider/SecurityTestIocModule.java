package ua.com.fielden.platform.security.provider;

import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

public final class SecurityTestIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        super.configure();

        bind(ISecurityTokenProvider.class).to(TestSecurityTokenProvider.class);
    }

}
