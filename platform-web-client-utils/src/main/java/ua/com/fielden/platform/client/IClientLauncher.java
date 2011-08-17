package ua.com.fielden.platform.client;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.client.session.AppSessionController;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.login.StyledLoginScreen;

import com.google.inject.Injector;

/**
 * A contract for implementing application specific launching logic.
 *
 * @author TG Team
 *
 */
public interface IClientLauncher {
    void launch(final SplashController splash,//
	    final StyledLoginScreen loginScreen,//
	    final RestClientUtil restUtil,//
	    final Injector injector, //
	    final boolean autoudate, //
	    final AppSessionController sessionController,//
	    final Logger logger);
}
