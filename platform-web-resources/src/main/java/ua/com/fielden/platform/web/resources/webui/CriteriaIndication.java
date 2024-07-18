package ua.com.fielden.platform.web.resources.webui;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ua.com.fielden.platform.dom.StyleAttribute;

/**
 * Entity Centre's selection criteria indication in relation to loaded result-set.
 */
@JsonSerialize(using = CriteriaIndicationJsonSerialiser.class)
enum CriteriaIndication {
    /**
     * Indicates that selection criteria are in sync with result-set and are not changed from saved (default) values.
     */
    NONE("Show selection criteria, Ctrl&nbsp+&nbspe", new StyleAttribute()),
    /**
     * Indicates that selection criteria are not in sync with result-set and re-run should be performed for synchronisation.
     */
    STALE("Selection criteria have been changed, but not applied. Previously applied values are in effect. Please tap <b>RUN</b> to apply the updated selection criteria. Ctrl&nbsp+&nbspe",
        new StyleAttribute()
            .addStyle("color", "var(--paper-orange-500)")
            .addStyle("border-color", "var(--paper-orange-500)")
    ),
    /**
     * Indicates that selection criteria are in sync with result-set, but were changed from saved (default) values.
     */
    CHANGED("Selection criteria have been applied, but not saved. Tap <b>SAVE</b> to save the updated selection criteria if needed. Ctrl&nbsp+&nbspe",
        new StyleAttribute()
            .addStyle("color", "var(--paper-light-blue-600)")
            .addStyle("border-color", "var(--paper-light-blue-600)")
    );

    /**
     * User-facing message for criteria indication (tooltip for button).
     */
    public final String message;
    /**
     * User-facing CSS styles for criteria indication (CSS for button).
     */
    public final StyleAttribute style;

    CriteriaIndication(final String message, final StyleAttribute style) {
        this.message = message;
        this.style = style;
    }
}
