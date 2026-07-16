package ua.com.fielden.platform.web.layout.grid;

/// The automatic-repeat options of a column track's `repeat()` function, mirroring CSS Grid.
/// Both create as many tracks of the given size as fit across the available width; the difference is how empty tracks are treated.
///
public enum AutoRepeat {

    /// `repeat(auto-fit, size)` — empty tracks collapse, so the present items stretch to fill the row.
    ///
    AUTO_FIT("auto-fit"),

    /// `repeat(auto-fill, size)` — empty trailing tracks are preserved, so items keep their intrinsic track width.
    ///
    AUTO_FILL("auto-fill");

    private final String keyword;

    AutoRepeat(final String keyword) {
        this.keyword = keyword;
    }

    /// The CSS keyword (`auto-fit` or `auto-fill`) emitted into the track's `repeat()` function.
    ///
    public String keyword() {
        return keyword;
    }
}