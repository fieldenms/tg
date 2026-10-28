package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import org.junit.Test;
import ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.AttemptResult;
import ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.Outcome;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.*;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.Outcome.*;

/// Tests for the tracking of login attempts by [LoginAttemptTracker].
///
public class LoginAttemptTrackerTest {

    private static final Duration
            BLOCK_TIME = Duration.ofSeconds(BLOCK_TIME_SECONDS),
            LOCKOUT_WINDOW = Duration.ofHours(LOCKOUT_WINDOW_HOURS);

    private final ManualTicker ticker = new ManualTicker();
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(ticker);
    private int evictions;

    @Test
    public void the_first_two_failed_attempts_are_denied_and_the_third_one_blocks_the_username() {
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
    }

    @Test
    public void attempts_while_the_username_is_blocked_are_not_authenticated_and_not_counted() {
        failUntilBlocked("user1");

        for (int i = 0; i < 10; i++) {
            assertEquals(new AttemptResult(BLOCKED, false), tracker.attempt("user1", true, () -> { throw new AssertionError("Authentication of a blocked attempt."); }));
        }
        // Had the blocked attempts been counted, the next attempt would result in a lockout.
        ticker.advance(BLOCK_TIME);
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
    }

    @Test
    public void the_sixth_counted_failed_attempt_of_an_account_is_denied_and_results_in_a_lockout() {
        failUntilBlocked("user1");

        ticker.advance(BLOCK_TIME);
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
        ticker.advance(BLOCK_TIME);
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
        ticker.advance(BLOCK_TIME);
        assertEquals(new AttemptResult(DENIED, true), fail("user1"));
    }

    @Test
    public void attempts_with_other_usernames_have_the_same_outcomes_as_attempts_of_accounts_but_do_not_result_in_a_lockout() {
        assertEquals(new AttemptResult(DENIED, true), failUntilLockout("user1", true));
        assertEquals(new AttemptResult(DENIED, false), failUntilLockout("unknown", false));
    }

    @Test
    public void attempt_after_a_lockout_starts_counting_afresh() {
        failUntilLockout("user1", true);

        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
    }

    @Test
    public void successful_attempt_resets_the_failed_attempts_and_the_count_towards_a_lockout() {
        for (int i = 0; i < LOCKOUT_THRESHOLD - 1; i++) {
            assertFalse(fail("user1").lockout());
            ticker.advance(BLOCK_TIME);
        }
        assertEquals(new AttemptResult(AUTHENTICATED, false), tracker.attempt("user1", true, () -> true));

        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
    }

    @Test
    public void usernames_are_tracked_case_insensitively() {
        assertEquals(new AttemptResult(DENIED, false), fail("User1"));
        assertEquals(new AttemptResult(DENIED, false), fail("USER1"));
        assertEquals(new AttemptResult(BLOCKED, false), fail("user1"));
    }

    @Test
    public void eviction_of_attempts_does_not_disclose_which_usernames_identify_accounts() {
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(DENIED, false), fail("user1"));
        assertEquals(new AttemptResult(DENIED, false), failOther("unknown"));
        assertEquals(new AttemptResult(DENIED, false), failOther("unknown"));

        evictAttempts();

        // Without eviction, both attempts would be blocked.
        assertEquals(DENIED, fail("user1").outcome());
        assertEquals(DENIED, failOther("unknown").outcome());
    }

    @Test
    public void eviction_of_attempts_does_not_reset_the_count_towards_a_lockout() {
        // Each round of 2 failed attempts is followed by eviction, so that no attempt is blocked, and each is counted towards a lockout.
        for (int i = 1; i <= LOCKOUT_THRESHOLD; i++) {
            final AttemptResult result = fail("user1");
            assertEquals(DENIED, result.outcome());
            assertEquals("Attempt %s".formatted(i), i == LOCKOUT_THRESHOLD, result.lockout());
            if (i % FAILED_ATTEMPTS_BEFORE_BLOCK == 0) {
                evictAttempts();
            }
        }
    }

    @Test
    public void failed_attempts_within_the_lockout_window_result_in_a_lockout() {
        for (int i = 0; i < LOCKOUT_THRESHOLD - 1; i++) {
            assertFalse(fail("user1").lockout());
            ticker.advance(BLOCK_TIME);
        }

        ticker.advance(LOCKOUT_WINDOW.minus(BLOCK_TIME.multipliedBy(LOCKOUT_THRESHOLD)));
        assertTrue(fail("user1").lockout());
    }

    @Test
    public void failed_attempts_older_than_the_lockout_window_are_not_counted_towards_a_lockout() {
        for (int i = 0; i < LOCKOUT_THRESHOLD - 1; i++) {
            assertFalse(fail("user1").lockout());
            ticker.advance(BLOCK_TIME);
        }

        // The first failed attempt has just fallen out of the window, while the second one has not.
        ticker.advance(LOCKOUT_WINDOW.minus(BLOCK_TIME.multipliedBy(LOCKOUT_THRESHOLD - 1)));
        assertFalse(fail("user1").lockout());
        ticker.advance(Duration.ofSeconds(1));
        assertTrue(fail("user1").lockout());
    }

    /// A failed attempt of an account that can sign in with a password.
    ///
    private AttemptResult fail(final String username) {
        return tracker.attempt(username, true, () -> false);
    }

    /// A failed attempt with a username that does not identify an account that can sign in with a password.
    ///
    private AttemptResult failOther(final String username) {
        return tracker.attempt(username, false, () -> false);
    }

    /// Evicts the attempts of all usernames, by making attempts with twice [#MAX_TRACKED_USERNAMES] new usernames, as the cache bounds its size per segment.
    ///
    private void evictAttempts() {
        evictions++;
        for (int i = 0; i < 2 * MAX_TRACKED_USERNAMES; i++) {
            assertEquals(new AttemptResult(DENIED, false), failOther("other-%s-%s".formatted(evictions, i)));
        }
    }

    private void failUntilBlocked(final String username) {
        assertEquals(new AttemptResult(DENIED, false), fail(username));
        assertEquals(new AttemptResult(DENIED, false), fail(username));
        assertEquals(new AttemptResult(BLOCKED, false), fail(username));
    }

    /// Makes failed attempts with `username`, each after the block time of the previous one, asserting their outcomes up to the [#LOCKOUT_THRESHOLD]-th, and returning its result.
    ///
    private AttemptResult failUntilLockout(final String username, final boolean passwordAccount) {
        final Outcome[] expected = {DENIED, DENIED, BLOCKED, BLOCKED, BLOCKED};
        for (final Outcome outcome : expected) {
            assertEquals(new AttemptResult(outcome, false), tracker.attempt(username, passwordAccount, () -> false));
            ticker.advance(BLOCK_TIME);
        }
        final AttemptResult result = tracker.attempt(username, passwordAccount, () -> false);
        ticker.advance(BLOCK_TIME);
        return result;
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
