package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

/// Tracks login attempts per username, blocking repeated failed attempts and determining when an account is to be locked out.
///
/// The outcome of an attempt, which determines the response, is decided by the attempts of the username:
///
/// - The first [#FAILED_ATTEMPTS_BEFORE_BLOCK] failed attempts are denied.
/// - Each further failed attempt blocks the username for [#BLOCK_TIME_SECONDS].
///   An attempt made within that time of the previous one, while the username is blocked, is blocked again for that time without authentication, and is not counted.
/// - The [#LOCKOUT_THRESHOLD]-th counted failed attempt is denied, and counting starts afresh.
/// - A successful attempt resets the attempts of the username.
///
/// Attempts with all usernames are tracked in the same way, and in the same cache, bounded by [#MAX_TRACKED_USERNAMES], so that neither the outcomes nor their eviction from the cache disclose which usernames identify accounts.
///
/// Whether an account is locked out is decided separately, and is not reflected in the outcome.
/// For a username that identifies an account that can sign in with a password, failed authentications are counted, and the [#LOCKOUT_THRESHOLD]-th of them within [#LOCKOUT_WINDOW_HOURS] results in a lockout.
/// These counts are bounded by the number of such accounts, and are not evicted, so that attempts with other usernames cannot reset them, which would circumvent the lockout.
/// Eviction of the attempts of a username only allows further attempts to be authenticated, and each of them is counted towards the lockout.
///
/// Time is measured with a [Ticker], which is monotonic and does not depend on the time-zone of a request.
///
@Singleton
public class LoginAttemptTracker {

    public static final int
            FAILED_ATTEMPTS_BEFORE_BLOCK = 2,
            BLOCK_TIME_SECONDS = 15,
            LOCKOUT_THRESHOLD = 6,
            LOCKOUT_WINDOW_HOURS = 24,
            MAX_TRACKED_USERNAMES = 10_000;

    /// The outcome of a login attempt, which determines the response.
    ///
    public enum Outcome {
        /// Authentication succeeded.
        AUTHENTICATED,
        /// Authentication failed.
        DENIED,
        /// The username is blocked, either by this failed attempt or by previous ones, in which case authentication was not performed.
        BLOCKED
    }

    /// The result of a login attempt: its `outcome`, and whether the account is to be locked out (`lockout`).
    ///
    public record AttemptResult(Outcome outcome, boolean lockout) {}

    private static final long
            BLOCK_TIME_NANOS = SECONDS.toNanos(BLOCK_TIME_SECONDS),
            LOCKOUT_WINDOW_NANOS = HOURS.toNanos(LOCKOUT_WINDOW_HOURS);

    private final Ticker ticker;
    /// The attempts of all usernames, which determine the outcomes.
    private final ConcurrentMap<String, Attempts> attempts;
    /// The times of failed authentications within [#LOCKOUT_WINDOW_HOURS], per username of an account that can sign in with a password.
    private final ConcurrentMap<String, long[]> accountFailures = new ConcurrentHashMap<>();

    @Inject
    public LoginAttemptTracker() {
        this(Ticker.systemTicker());
    }

    /// Creates a tracker that measures time with `ticker`, which facilitates testing.
    ///
    LoginAttemptTracker(final Ticker ticker) {
        this.ticker = ticker;
        this.attempts = CacheBuilder.newBuilder().maximumSize(MAX_TRACKED_USERNAMES).<String, Attempts>build().asMap();
    }

    /// Makes a login attempt with `username`, performing `authenticate` unless the username is blocked.
    ///
    /// @param username        the username of the attempt
    /// @param passwordAccount whether `username` identifies an account that can sign in with a password, which can be locked out
    /// @param authenticate    performs authentication, returning `true` if it succeeds
    ///
    public AttemptResult attempt(final String username, final boolean passwordAccount, final BooleanSupplier authenticate) {
        final String key = username.toLowerCase(Locale.ROOT);
        final Attempts usernameAttempts = attempts.computeIfAbsent(key, _ -> new Attempts(ticker.read()));
        synchronized (usernameAttempts) {
            final long now = ticker.read();
            final boolean withinBlockTime = now - usernameAttempts.lastAttemptTime < BLOCK_TIME_NANOS;
            usernameAttempts.lastAttemptTime = now;
            if (withinBlockTime && (usernameAttempts.blockedUntil - now > 0 || usernameAttempts.failedCount > FAILED_ATTEMPTS_BEFORE_BLOCK)) {
                usernameAttempts.blockedUntil = now + BLOCK_TIME_NANOS;
                return new AttemptResult(Outcome.BLOCKED, false);
            }

            // If the user was locked out, authentication is expected to return false.
            if (authenticate.getAsBoolean()) {
                attempts.remove(key, usernameAttempts);
                accountFailures.remove(key);
                return new AttemptResult(Outcome.AUTHENTICATED, false);
            }

            final boolean lockout = passwordAccount && countAccountFailure(key, now);
            usernameAttempts.failedCount++;
            if (usernameAttempts.failedCount <= FAILED_ATTEMPTS_BEFORE_BLOCK) {
                return new AttemptResult(Outcome.DENIED, lockout);
            } else if (usernameAttempts.failedCount < LOCKOUT_THRESHOLD) {
                usernameAttempts.blockedUntil = now + BLOCK_TIME_NANOS;
                return new AttemptResult(Outcome.BLOCKED, lockout);
            } else {
                attempts.remove(key, usernameAttempts);
                return new AttemptResult(Outcome.DENIED, lockout);
            }
        }
    }

    /// Counts a failed authentication at time `now` for the account identified by `key`, returning `true` if it results in a lockout.
    /// The count restarts after a lockout.
    ///
    /// The count is updated atomically, as attempts with the same username may be synchronised on different instances of [Attempts] if one is evicted while in use.
    ///
    private boolean countAccountFailure(final String key, final long now) {
        final var lockout = new AtomicBoolean();
        accountFailures.compute(key, (_, times) -> {
            final long[] recentTimes = times == null ? new long[0] : Arrays.stream(times).filter(time -> now - time < LOCKOUT_WINDOW_NANOS).toArray();
            if (recentTimes.length + 1 >= LOCKOUT_THRESHOLD) {
                lockout.set(true);
                return null;
            }
            final long[] newTimes = Arrays.copyOf(recentTimes, recentTimes.length + 1);
            newTimes[recentTimes.length] = now;
            return newTimes;
        });
        return lockout.get();
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
