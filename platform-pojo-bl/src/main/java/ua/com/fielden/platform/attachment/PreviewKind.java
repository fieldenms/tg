package ua.com.fielden.platform.attachment;

import jakarta.annotation.Nullable;

import java.util.Optional;

/// Represents the kind of preview available for an [attachment][Attachment].
///
/// This enum is the single source of truth for two related pieces of policy:
/// - the mapping from MIME types to preview kinds (see [#fromMime]), used by the preview producer;
/// - whether a given kind is rendered inline and therefore needs `?inline=true` on its download URL (see [#servesInline]), consulted by both the producer (when building URLs) and the download resource (when honouring the inline flag).
///
/// Adding a new previewable MIME is a single edit in this file — producer and resource read from here, so they can't drift.
///
public enum PreviewKind {
    /// A raster or vector image (e.g. PNG, JPEG, SVG) — rendered via `<img>`.
    ///
    IMAGE,
    /// A PDF document — rendered inline via `<object>`.
    ///
    PDF {
        @Override
        public boolean servesInline() {
            return true;
        }
    },
    /// A hyperlink — opened in a new tab or verified before navigating.
    ///
    HYPERLINK;

    public static Optional<PreviewKind> of(final @Nullable String name) {
        return name == null ? Optional.empty() : Optional.of(PreviewKind.valueOf(name));
    }

    /// Resolves the preview kind for a file MIME type (e.g. `image/png` → `IMAGE`, `application/pdf` → `PDF`).
    /// Returns empty when the MIME has no inline preview — the caller should fall back to a download button.
    ///
    /// [#HYPERLINK] is NOT returned from this method: hyperlink attachments are identified by title, not MIME.
    ///
    public static Optional<PreviewKind> fromMime(final @Nullable String mime) {
        if (mime == null) {
            return Optional.empty();
        }
        if (mime.startsWith("image/")) {
            return Optional.of(IMAGE);
        }
        if ("application/pdf".equals(mime)) {
            return Optional.of(PDF);
        }
        return Optional.empty();
    }

    /// Whether a download URL for this kind should carry `?inline=true` so the browser renders the file inline rather than offering a download.
    /// Also consulted by the download resource as the security gate for `Content-Disposition: inline`: only kinds that return `true` here may be served inline.
    /// Defaults to `false`; kinds that need inline rendering override it at the constant declaration (see [#PDF]).
    ///
    public boolean servesInline() {
        return false;
    }

}
