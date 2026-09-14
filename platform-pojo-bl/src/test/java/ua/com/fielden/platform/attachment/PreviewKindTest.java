package ua.com.fielden.platform.attachment;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/// Unit tests for [PreviewKind#of] — pins both the happy path and the tolerant-of-unknown contract.
///
public class PreviewKindTest {

    @Test
    public void of_resolves_known_names_to_their_kind() {
        assertEquals(Optional.of(PreviewKind.IMAGE), PreviewKind.of("IMAGE"));
        assertEquals(Optional.of(PreviewKind.PDF), PreviewKind.of("PDF"));
        assertEquals(Optional.of(PreviewKind.HYPERLINK), PreviewKind.of("HYPERLINK"));
    }

    @Test
    public void of_returns_empty_for_null() {
        assertEquals(Optional.empty(), PreviewKind.of(null));
    }

    /// Pins the deliberate change away from `valueOf`: stale, hand-edited, or case-mismatched payloads return empty instead of throwing `IllegalArgumentException`.
    ///
    @Test
    public void of_returns_empty_for_unknown_names_rather_than_throwing() {
        assertEquals(Optional.empty(), PreviewKind.of("UNKNOWN_VALUE"));
        assertEquals(Optional.empty(), PreviewKind.of(""));
        assertEquals(Optional.empty(), PreviewKind.of("pdf")); // case-sensitive — lowercase doesn't match
    }

}