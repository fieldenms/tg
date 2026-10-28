package ua.com.fielden.platform.web.resources.webui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/// Locks out accounts on behalf of login attempts (see [IUser#lockoutUser(String)]), as determined by [LoginAttemptTracker].
///
/// A lockout is performed asynchronously, on a thread of its own, so that the response time of a login attempt does not depend on it.
/// Otherwise, the response time of an attempt that results in a lockout, which only an account can, would disclose that the username identifies an account.
/// Lockouts are performed one at a time, in the order they are requested.
/// They are queued in memory: a lockout that is pending when the application server stops is not performed.
///
/// A lockout requested for a username (case-insensitive) while one is queued or in progress for it is not queued again, as that lockout has the same effect.
/// Therefore, at most one lockout per account is queued or in progress, however many lockouts a coordinated attack causes to be requested.
/// A lockout requested after that one has completed is queued.
///
/// Until the lockout is performed, typically within milliseconds, further attempts with the username are tracked as usual by [LoginAttemptTracker],
/// and are authenticated unless the username is blocked.
///
@Singleton
public class AccountLockout {
    private static final Logger LOGGER = LogManager.getLogger(AccountLockout.class);

    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;
    private final Executor executor;
    /// Usernames (lower-case) with a lockout that is queued or in progress.
    private final Set<String> pending = ConcurrentHashMap.newKeySet();

    @Inject
    public AccountLockout(final ICompanionObjectFinder coFinder, final IUserProvider up) {
        this(coFinder, up, Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("account-lockout-%d").setDaemon(true).build()));
    }

    /// Creates an instance that performs lockouts with `executor`, which facilitates testing.
    ///
    AccountLockout(final ICompanionObjectFinder coFinder, final IUserProvider up, final Executor executor) {
        this.coFinder = coFinder;
        this.up = up;
        this.executor = executor;
    }

    /// Requests a lockout of the account of `username`, which is performed asynchronously, unless one is already queued or in progress for it.
    ///
    public void lockoutAsync(final String username) {
        final String key = username.toLowerCase(Locale.ROOT);
        if (!pending.add(key)) {
            LOGGER.debug(() -> "Lockout of user [%s] is already queued or in progress.".formatted(username));
            return;
        }
        try {
            executor.execute(() -> {
                try {
                    lockoutUser(up, coFinder.find(User.class, true), username);
                } catch (final Exception ex) {
                    LOGGER.error(() -> "Could not lock out user [%s].".formatted(username), ex);
                } finally {
                    // Removed even if the lockout fails, so that a later lockout of the account can be requested.
                    pending.remove(key);
                }
            });
        } catch (final RuntimeException ex) {
            // The task is rejected and will not remove the key.
            pending.remove(key);
            throw ex;
        }
    }

    /// Locks out the account of `username` (see [IUser#lockoutUser(String)]).
    ///
    /// The user making a login attempt is not logged in, and lockout saves the user.
    /// Therefore, SU is used as the current user, also for auditing purposes.
    /// The current user is cleared afterwards, so that the thread does not retain it.
    ///
    static void lockoutUser(final IUserProvider up, final IUser coUser, final String username) {
        up.setUsername(User.system_users.SU.name(), coUser);
        try {
            coUser.lockoutUser(username);
        } finally {
            up.clearUser();
        }
    }

}
