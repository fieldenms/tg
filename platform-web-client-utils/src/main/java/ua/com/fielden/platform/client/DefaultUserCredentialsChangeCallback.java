package ua.com.fielden.platform.client;

import ua.com.fielden.platform.client.session.AppSessionController;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.provider.IUserCredentialsChangeCallback;

/** Profile feedback, which updates configuration and REST client utility when user profile changes. */
public class DefaultUserCredentialsChangeCallback implements IUserCredentialsChangeCallback {
    private final RestClientUtil util;
    private final AppSessionController config;

    public DefaultUserCredentialsChangeCallback(final RestClientUtil util, final AppSessionController config) {
        this.util = util;
        this.config = config;
    }

    @Override
    public void changed(final String username, final String privateKey) {
        util.setUsername(username);
        util.setPrivateKey(privateKey);
        config.setUsername(username);
        config.setPrivateKey(privateKey);
        try {
            config.persist(username, privateKey);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new Result(new IllegalStateException("Could not persist user changes locally."));
        }
    }
}