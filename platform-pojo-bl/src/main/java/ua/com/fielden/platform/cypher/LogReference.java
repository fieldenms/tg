package ua.com.fielden.platform.cypher;

import org.apache.commons.lang3.StringUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

/// A reference to a sensitive value for use in logs.
///
/// Some values must not be written to logs, as they are sufficient to act on behalf of a user or against one: session identifiers, authenticators, tokens.
/// Log messages still need to tell such values apart, and to relate messages about the same value.
/// A reference serves that purpose: it is the same for the same value, but the value cannot be obtained from it.
///
public final class LogReference {

    /// The number of characters of a reference.
    public static final int LENGTH = 10;

    /// The reference to a value that is `null` or empty.
    public static final String NONE = "none";

    private LogReference() {}

    /// Returns a reference to `value`: the first [#LENGTH] hexadecimal digits of its SHA-256 digest, or [#NONE] if there is no value.
    ///
    public static String of(final String value) {
        return StringUtils.isEmpty(value) ? NONE : Checksum.sha256(value.getBytes(UTF_8)).substring(0, LENGTH);
    }

}
