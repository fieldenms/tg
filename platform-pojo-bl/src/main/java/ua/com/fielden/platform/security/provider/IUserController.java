package ua.com.fielden.platform.security.provider;

import java.util.List;

import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Controller that provides API for retrieving and saving user roles associated and users
 * 
 * @author TG Team
 * 
 */
public interface IUserController extends IUserDao {

    /**
     * Returns all available user roles
     * 
     * @return
     */
    List<? extends UserRole> findAllUserRoles();

    /**
     * Returns the list of users. Users must be associated with user roles.
     * 
     * @return
     */
    List<User> findAllUsers();

    /**
     * Updates association between user and a list of roles. Once completer user should be associated strictly with the specified list of roles.
     * 
     * @param user
     * @param userRoles
     */
    void updateUser(User user, List<UserRole> userRoles);

    /**
     * Finds user by name.
     * 
     * @param username
     * @return
     */
    User findUser(String username);

}
