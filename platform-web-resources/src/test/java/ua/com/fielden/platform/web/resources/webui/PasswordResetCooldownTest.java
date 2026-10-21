package ua.com.fielden.platform.web.resources.webui;

import com.google.common.base.Ticker;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MUNUTES;
import static ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource.findOrAssignPasswordResetUuid;
import static ua.com.fielden.platform.web.resources.webui.PasswordResetCooldown.COOLDOWN_IN_MINUTES;

/// Tests for the cooldown of password resets and the reuse of reset links, as applied by [LoginInitiateResetResource].
///
public class PasswordResetCooldownTest extends AbstractWebResourceWithDaoTestCase {

    private final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
    private final ManualTicker ticker = new ManualTicker();
    private final PasswordResetCooldown cooldown = new PasswordResetCooldown(ticker);

    @Before
    public void setNow() {
        constants.setNow(dateTime("2026-10-21 10:00:00"));
    }

    @Test
    public void password_reset_is_initiated_for_a_user_out_of_cooldown() {
        final Optional<UserSecret> secret = reset("USER1");
        assertTrue(secret.isPresent());
        assertNotNull(secret.get().getResetUuid());
    }

    @Test
    public void password_reset_within_cooldown_is_not_initiated_and_the_reset_link_sent_before_remains_valid() {
        final String resetUuid = reset("USER1").orElseThrow().getResetUuid();

        // Keep the user within the cooldown period.
        ticker.advance(Duration.ofMinutes(COOLDOWN_IN_MINUTES).minusSeconds(1));

        // The cooldown applies to the user, however identified -- either by their username or email address.
        assertTrue(reset("USER1").isEmpty());
        assertTrue(reset("user1@company.com").isEmpty());
        assertTrue(co$User().isPasswordResetUuidValid(resetUuid, constants.now().toDate()));
    }

    @Test
    public void password_reset_after_the_cooldown_sends_the_link_sent_before_while_it_remains_valid_long_enough() {
        // The link expires at 10:15, 15 minutes after it is sent.
        final String resetUuid = reset("USER1").orElseThrow().getResetUuid();

        // Move the user beyond the cooldown period.
        ticker.advance(Duration.ofMinutes(COOLDOWN_IN_MINUTES));

        // At 10:09:59, the link remains valid for more than 5 minutes (MIN_REUSED_LINK_VALIDITY_IN_MINUTES).
        constants.setNow(dateTime("2026-10-21 10:09:59"));
        assertEquals(resetUuid, reset("USER1").orElseThrow().getResetUuid());
    }

    @Test
    public void password_reset_after_the_cooldown_replaces_the_link_sent_before_if_it_expires_soon() {
        // The link expires at 10:15, 15 minutes after it is sent.
        final String resetUuid = reset("USER1").orElseThrow().getResetUuid();

        // Move the user beyond the cooldown period.
        ticker.advance(Duration.ofMinutes(COOLDOWN_IN_MINUTES));

        // At 10:10, the link remains valid for only 5 minutes (MIN_REUSED_LINK_VALIDITY_IN_MINUTES).
        constants.setNow(dateTime("2026-10-21 10:10:00"));
        final String newResetUuid = reset("USER1").orElseThrow().getResetUuid();
        assertNotEquals(resetUuid, newResetUuid);
        assertTrue(co$User().isPasswordResetUuidValid(newResetUuid, constants.now().toDate()));
    }

    @Test
    public void cooldown_of_one_user_does_not_affect_another() {
        assertTrue(reset("USER1").isPresent());
        assertFalse(reset("USER1").isPresent()); // cooldown started for USER1
        assertTrue(reset("USER2").isPresent());
    }

    @Test
    public void password_reset_for_an_unknown_user_is_not_initiated() {
        assertTrue(reset("UNKNOWN").isEmpty());
    }

    /// Requests a password reset at the current time of [#constants], with a reset link that is valid for [RESET_UUID_EXPIRATION_IN_MUNUTES], if a new one is assigned.
    ///
    private Optional<UserSecret> reset(final String usernameOrEmail) {
        final var now = constants.now();
        return findOrAssignPasswordResetUuid(co$User(), cooldown, usernameOrEmail, now.toDate(), now.plusMinutes(RESET_UUID_EXPIRATION_IN_MUNUTES).toDate());
    }

    private IUser co$User() {
        return co$(User.class);
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final IUser coUser = co$User();
        coUser.save(new_(User.class, "USER1").setBase(true).setEmail("USER1@company.com").setActive(true));
        coUser.save(new_(User.class, "USER2").setBase(true).setEmail("USER2@company.com").setActive(true));

        // Saving a new user assigns a reset UUID for the initial password, which these users are assumed to have used.
        final UserSecretCo coUserSecret = co$(UserSecret.class);
        for (final String username : List.of("USER1", "USER2")) {
            coUserSecret.save(coUserSecret.findByUsername(username).orElseThrow().setResetUuid(null));
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
