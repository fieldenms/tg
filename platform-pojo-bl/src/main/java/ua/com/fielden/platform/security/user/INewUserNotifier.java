package ua.com.fielden.platform.security.user;

/**
 * A contract to notify newly created users of their registration as application users.
 * <p>
 * This contract is required to support application specific customization as well as the ability to provide test and production specific variations. 
 * 
 * @author TG Team
 *
 */
public interface INewUserNotifier {
    
    /**
     * Notifies the specified user of their registration of a new application user.
     * @param user
     */
    void notify(final User user);
    
}
