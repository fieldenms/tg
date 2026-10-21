package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import org.junit.Test;
import ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.Outcome;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.BLOCK_TIME_SECONDS;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.MAX_OTHER_USERNAMES;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.Outcome.*;

/// Tests for the tracking of login attempts by [LoginAttemptTracker].
///
public class LoginAttemptTrackerTest {

    private static final Duration BLOCK_TIME = Duration.ofSeconds(BLOCK_TIME_SECONDS);

    private final ManualTicker ticker = new ManualTicker();
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(ticker);

    @Test
    public void the_first_two_failed_attempts_are_denied_and_the_third_one_blocks_the_username() {
        assertEquals(DENIED, fail("user1"));
        assertEquals(DENIED, fail("user1"));
        assertEquals(BLOCKED, fail("user1"));
    }

    @Test
    public void attempts_while_the_username_is_blocked_are_not_authenticated_and_not_counted() {
        failUntilBlocked("user1");

        for (int i = 0; i < 10; i++) {
            assertEquals(BLOCKED, tracker.attempt("user1", true, () -> { throw new AssertionError("Authentication of a blocked attempt."); }));
        }
        // Had the blocked attempts been counted, the next attempt would result in a lockout.
        ticker.advance(BLOCK_TIME);
        assertEquals(BLOCKED, fail("user1"));
    }

    @Test
    public void the_sixth_counted_failed_attempt_results_in_a_lockout() {
        failUntilBlocked("user1");

        ticker.advance(BLOCK_TIME);
        assertEquals(BLOCKED, fail("user1"));
        ticker.advance(BLOCK_TIME);
        assertEquals(BLOCKED, fail("user1"));
        ticker.advance(BLOCK_TIME);
        assertEquals(LOCKOUT, fail("user1"));
    }

    @Test
    public void attempt_after_a_lockout_starts_counting_afresh() {
        failUntilLockout("user1", true);

        ticker.advance(BLOCK_TIME);
        assertEquals(DENIED, fail("user1"));
    }

    @Test
    public void successful_attempt_resets_the_failed_attempts() {
        assertEquals(DENIED, fail("user1"));
        assertEquals(DENIED, fail("user1"));
        assertEquals(AUTHENTICATED, tracker.attempt("user1", true, () -> true));

        assertEquals(DENIED, fail("user1"));
        assertEquals(DENIED, fail("user1"));
    }

    @Test
    public void usernames_are_tracked_case_insensitively() {
        assertEquals(DENIED, fail("User1"));
        assertEquals(DENIED, fail("USER1"));
        assertEquals(BLOCKED, fail("user1"));
    }

    @Test
    public void attempts_with_other_usernames_do_not_evict_the_attempts_of_accounts() {
        assertEquals(DENIED, fail("user1"));
        assertEquals(DENIED, fail("user1"));

        for (int i = 0; i < 2 * MAX_OTHER_USERNAMES; i++) {
            assertEquals(DENIED, tracker.attempt("other" + i, false, () -> false));
        }

        assertEquals(BLOCKED, fail("user1"));
    }

    @Test
    public void attempts_with_other_usernames_are_tracked_in_the_same_way_as_attempts_of_accounts() {
        failUntilLockout("unknown", false);
    }

    /// A failed attempt of an account that can sign in with a password.
    ///
    private Outcome fail(final String username) {
        return tracker.attempt(username, true, () -> false);
    }

    private void failUntilBlocked(final String username) {
        assertEquals(DENIED, fail(username));
        assertEquals(DENIED, fail(username));
        assertEquals(BLOCKED, fail(username));
    }

    private void failUntilLockout(final String username, final boolean passwordAccount) {
        final Outcome[] expected = {DENIED, DENIED, BLOCKED, BLOCKED, BLOCKED, LOCKOUT};
        for (final Outcome outcome : expected) {
            assertEquals(outcome, tracker.attempt(username, passwordAccount, () -> false));
            ticker.advance(BLOCK_TIME);
        }
    }

    /// A [Ticker] advanced explicitly by tests.
    ///
    private static final class ManualTicker extends Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(final Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }

}
