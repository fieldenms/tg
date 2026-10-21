package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/// Tracks login attempts per username, blocking repeated failed attempts and determining when an account is to be locked out.
///
/// - The first [#FAILED_ATTEMPTS_BEFORE_BLOCK] failed attempts are denied.
/// - Each further failed attempt blocks the username for [#BLOCK_TIME_SECONDS].
///   An attempt made within that time of the previous one, while the username is blocked, is blocked again without authentication, and is not counted.
/// - The [#LOCKOUT_THRESHOLD]-th counted failed attempt results in a lockout.
/// - A successful attempt resets the attempts of the username.
///
/// Usernames are tracked case-insensitively, as users are identified by username case-insensitively.
/// Attempts with usernames of accounts that can sign in with a password are tracked separately from attempts with other usernames, which are unknown, inactive, or restricted to SSO.
/// The former are bounded by the number of such accounts, so that attempts with other usernames cannot evict them; the latter are bounded by [#MAX_OTHER_USERNAMES].
/// Attempts with both kinds of usernames are treated the same, so that the outcomes do not disclose which usernames identify accounts that can sign in with a password.
///
/// Time is measured with a [Ticker], which is monotonic and does not depend on the time-zone of a request.
///
@Singleton
public class LoginAttemptTracker {

    public static final int
            FAILED_ATTEMPTS_BEFORE_BLOCK = 2,
            BLOCK_TIME_SECONDS = 15,
            LOCKOUT_THRESHOLD = 6,
            MAX_OTHER_USERNAMES = 1000;

    /// The outcome of a login attempt.
    ///
    public enum Outcome {
        /// Authentication succeeded.
        AUTHENTICATED,
        /// Authentication failed.
        DENIED,
        /// The username is blocked, either by this failed attempt or by previous ones, in which case authentication was not performed.
        BLOCKED,
        /// Authentication failed for the [#LOCKOUT_THRESHOLD]-th time, and the account is to be locked out.
        LOCKOUT
    }

    private static final long BLOCK_TIME_NANOS = SECONDS.toNanos(BLOCK_TIME_SECONDS);

    private final Ticker ticker;
    private final ConcurrentMap<String, Attempts> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Attempts> otherUsernames;

    @Inject
    public LoginAttemptTracker() {
        this(Ticker.systemTicker());
    }

    /// Creates a tracker that measures time with `ticker`, which facilitates testing.
    ///
    LoginAttemptTracker(final Ticker ticker) {
        this.ticker = ticker;
        this.otherUsernames = CacheBuilder.newBuilder().maximumSize(MAX_OTHER_USERNAMES).<String, Attempts>build().asMap();
    }

    /// Makes a login attempt with `username`, performing `authenticate` unless the username is blocked.
    ///
    /// @param username        the username of the attempt
    /// @param passwordAccount whether `username` identifies an account that can sign in with a password
    /// @param authenticate    performs authentication, returning `true` if it succeeds
    ///
    public Outcome attempt(final String username, final boolean passwordAccount, final BooleanSupplier authenticate) {
        final ConcurrentMap<String, Attempts> tracked = passwordAccount ? accounts : otherUsernames;
        final String key = username.toLowerCase(Locale.ROOT);
        final Attempts attempts = tracked.computeIfAbsent(key, _ -> new Attempts(ticker.read()));
        synchronized (attempts) {
            final long now = ticker.read();
            final boolean withinBlockTime = now - attempts.lastAttemptTime < BLOCK_TIME_NANOS;
            attempts.lastAttemptTime = now;
            if (withinBlockTime && (attempts.blockedUntil - now > 0 || attempts.failedCount > FAILED_ATTEMPTS_BEFORE_BLOCK)) {
                attempts.blockedUntil = now + BLOCK_TIME_NANOS;
                return Outcome.BLOCKED;
            }

            if (authenticate.getAsBoolean()) {
                tracked.remove(key, attempts);
                return Outcome.AUTHENTICATED;
            }

            attempts.failedCount++;
            if (attempts.failedCount <= FAILED_ATTEMPTS_BEFORE_BLOCK) {
                return Outcome.DENIED;
            } else if (attempts.failedCount < LOCKOUT_THRESHOLD) {
                attempts.blockedUntil = now + BLOCK_TIME_NANOS;
                return Outcome.BLOCKED;
            } else {
                tracked.remove(key, attempts);
                return Outcome.LOCKOUT;
            }
        }
    }

    /// The attempts of a username, which are mutable and synchronised on.
    /// Times are values of [Ticker#read()].
    ///
    private static final class Attempts {
        private int failedCount;
        private long lastAttemptTime;
        private long blockedUntil;

        private Attempts(final long time) {
            this.lastAttemptTime = time;
            this.blockedUntil = time;
        }
    }

}
