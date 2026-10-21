package ua.com.fielden.platform.test_config;

import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;

import java.util.Properties;

/// A test runner like [H2OrPostgreSqlOrSqlServerContextSelector], which sets the authentication mode to SSO.
///
public class SsoAuthModeContextSelector extends H2OrPostgreSqlOrSqlServerContextSelector {

    public SsoAuthModeContextSelector(final Class<?> klass) throws Exception {
        super(klass);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties props = super.mkDbProps(dbUri);
        props.setProperty("auth.mode", AuthMode.SSO.name());
        return props;
    }

}
