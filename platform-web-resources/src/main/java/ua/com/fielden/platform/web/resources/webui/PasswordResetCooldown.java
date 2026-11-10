package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.security.user.User;

import java.util.concurrent.ConcurrentMap;

import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MINUTES;

/// Limits password resets to one per user within [#COOLDOWN_IN_MINUTES].
///
/// Each password reset sends an email and replaces the reset link of the user, invalidating the link sent before.
/// The cooldown prevents flooding a mailbox with password reset emails, and repeatedly invalidating a link that the user is about to use.
///
/// The cooldown is held in memory: it applies per application server, and starts afresh when the server restarts.
///
@Singleton
public class PasswordResetCooldown {

    public static final int COOLDOWN_IN_MINUTES = 5;

    private final ConcurrentMap<Long, Boolean> usersInCooldown;

    @Inject
    public PasswordResetCooldown() {
        this(Ticker.systemTicker());
    }

    /// Creates a cooldown that measures time with `ticker`, which facilitates testing.
    ///
    PasswordResetCooldown(final Ticker ticker) {
        this.usersInCooldown = CacheBuilder.newBuilder()
                .expireAfterWrite(COOLDOWN_IN_MINUTES, MINUTES)
                .ticker(ticker)
                .<Long, Boolean>build()
                .asMap();
    }

    /// Starts the cooldown for `user` and returns `true`, unless `user` is already in cooldown, in which case returns `false`.
    /// Concurrent calls for the same user start the cooldown once, so that only one of them returns `true`.
    ///
    public boolean tryStart(final User user) {
        return usersInCooldown.putIfAbsent(user.getId(), TRUE) == null;
    }

}
