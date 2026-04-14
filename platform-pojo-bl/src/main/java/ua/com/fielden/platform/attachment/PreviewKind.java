package ua.com.fielden.platform.attachment;

import jakarta.annotation.Nullable;

import java.util.Optional;

/// Represents the kind of preview available for an [attachment][Attachment].
///
public enum PreviewKind {
    /// A raster or vector image (e.g. PNG, JPEG, SVG) — rendered via `<img>`.
    ///
    IMAGE,
    /// A PDF document — rendered inline via `<object>`.
    ///
    PDF,
    /// A hyperlink — opened in a new tab or verified before navigating.
    ///
    HYPERLINK;

    public static Optional<PreviewKind> of(final @Nullable String name) {
        return name == null ? Optional.empty() : Optional.of(PreviewKind.valueOf(name));
    }

}
