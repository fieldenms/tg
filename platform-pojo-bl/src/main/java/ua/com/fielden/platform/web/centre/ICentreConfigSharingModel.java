package ua.com.fielden.platform.web.centre;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

@ImplementedBy(DefaultCentreConfigSharingModel.class)
public interface ICentreConfigSharingModel {
    
    String sharedByMessage(final User sharedBy);
    
    Result isSharedTo(final String configUuid, final User user);
    
}