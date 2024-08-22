package ua.com.fielden.platform.security.user;

import java.util.Optional;

/**
 * An abstraction for accessing a logged in application user.
 * 
 * @author TG Team
 * 
 */
public interface IUserProvider {

    User getUser();

    default Optional<String> getUsername() {
        final var user = getUser();
        return user == null ? Optional.empty() : Optional.ofNullable(user.getKey());
    }

    IUserProvider setUsername(final String username, final IUser coUser);

    IUserProvider setUser(final User user);

}
