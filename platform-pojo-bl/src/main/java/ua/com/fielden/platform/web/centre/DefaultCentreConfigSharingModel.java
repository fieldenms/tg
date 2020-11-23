package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.successful;

import com.google.inject.Singleton;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

@Singleton
public class DefaultCentreConfigSharingModel implements ICentreConfigSharingModel {
    
    @Override
    public String sharedByMessage(final User sharedBy) {
        return format("<i>shared by %s</i>", sharedBy);
    }
    
    @Override
    public Result isSharedTo(final String configUuid, final User user) {
        return successful("Ok");
    }
    
}