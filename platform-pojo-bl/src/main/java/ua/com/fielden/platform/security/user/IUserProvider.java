package ua.com.fielden.platform.security.user;

import java.util.Optional;

/// An abstraction for identifying the current application user in the scope of the running thread.
///
public interface IUserProvider {

    User getUser();

    default Optional<String> getUsername() {
        final var user = getUser();
        return user == null ? Optional.empty() : Optional.ofNullable(user.getKey());
    }

    IUserProvider setUsername(final String username, final IUser coUser);

    IUserProvider setUser(final User user);

}
