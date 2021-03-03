package ua.com.fielden.platform.web.centre;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * API for centre configuration sharing.
 * Together with {@code IWebUiConfig.centreConfigShareActions} it defines how centre configurations are shared and how sharing information is displayed.
 * 
 * @author TG Team
 *
 */
@ImplementedBy(DefaultCentreConfigSharingModel.class)
public interface ICentreConfigSharingModel {
    
    /**
     * Returns custom indication of the fact that configuration is shared by {@code sharedBy} user.
     */
    String sharedByMessage(final User sharedBy);
    
    /**
     * Returns custom result indicating whether configuration with {@code configUuid} is shared with {@code user}.
     * <p>
     * If result is successful then configuration can be loaded for {@code user} (other than creator) and it can be updated multiple times from upstream configuration.<br>
     * If result is not successful then configuration can not be loaded for {@code user} (other than creator) and the message from this result appears to the user trying to load configuration.<br>
     *   If configuration has already been loaded from shared but was unshared then it would not be updated afterwards from upstream configuration -- it acts like own save-as configuration.
     */
    Result isSharedWith(final String configUuid, final User user);
    
}