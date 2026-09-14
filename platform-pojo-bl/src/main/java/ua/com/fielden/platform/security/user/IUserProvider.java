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

    /// Clears the current user from this provider.
    ///
    /// Implementations backed by thread-confined storage (e.g. a [ThreadLocal]) must remove the stored value, so that a worker thread returned to a pool does not retain a previous request's user.
    /// This should be invoked once a unit of work (such as an HTTP request) completes.
    ///
    void clearUser();

}
