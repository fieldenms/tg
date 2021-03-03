package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.successful;

import com.google.inject.Singleton;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * Default implementation for {@link ICentreConfigSharingModel}.
 * 
 * @author TG Team
 *
 */
@Singleton
public class DefaultCentreConfigSharingModel implements ICentreConfigSharingModel {
    
    /**
     * Returns 'shared by USER' message in italic.
     */
    @Override
    public String sharedByMessage(final User sharedBy) {
        return format("<i>shared by %s</i>", sharedBy);
    }
    
    /**
     * In this implementation, everyone who has configuration URI can load that configuration and update from its changes.
     */
    @Override
    public Result isSharedWith(final String configUuid, final User user) {
        return successful("Ok");
    }
    
}