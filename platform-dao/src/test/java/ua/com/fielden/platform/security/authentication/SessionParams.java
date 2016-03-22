package ua.com.fielden.platform.security.authentication;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;

import com.google.inject.Inject;

/**
 * This is just a convenience class to capture session creation parameters for testing purposes.
 *
 */
class SessionParams {
    final String hashingKey;
    final int trustedDurationMins;
    final int untrustedDurationMins;
    final SessionIdentifierGenerator crypto;

    @Inject
    public SessionParams(
            final @SessionHashingKey String hashingKey,
            final @TrustedDeviceSessionDuration int trustedDurationMins,
            final @UntrustedDeviceSessionDuration int untrustedDurationMins,
            final SessionIdentifierGenerator crypto) {
        this.hashingKey = hashingKey;
        this.trustedDurationMins = trustedDurationMins;
        this.untrustedDurationMins = untrustedDurationMins;
        this.crypto = crypto;
    }

}
