package ua.com.fielden.platform.test.runners;

import java.util.Properties;

import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

/**
 * A test case runner for domain-driven unit tests extending {@link H2TgDomainDrivenTestCaseRunner}, which sets {@code auth.mode} to SSO.
 * 
 * @author TG Team
 *
 */
public class H2DomainDrivenTestCaseInSsoAuthModeRunner extends H2DomainDrivenTestCaseRunner {

    public H2DomainDrivenTestCaseInSsoAuthModeRunner(final Class<?> klass) throws Exception {
        super(klass);
    }
    
    public H2DomainDrivenTestCaseInSsoAuthModeRunner(final Class<?> klass, final IDomainDrivenTestCaseConfiguration config) throws Exception {
        super(klass, config);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties props = super.mkDbProps(dbUri);
        props.setProperty("auth.mode", AuthMode.SSO.name());
        return props;
    }

}