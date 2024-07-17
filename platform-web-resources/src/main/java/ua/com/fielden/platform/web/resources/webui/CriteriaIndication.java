package ua.com.fielden.platform.web.resources.webui;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ua.com.fielden.platform.dom.StyleAttribute;

/**
 * Entity Centre's selection criteria indication in relation to loaded result-set.
 */
@JsonSerialize(using = CriteriaIndicationJsonSerialiser.class)
enum CriteriaIndication {
    NONE("Show selection criteria, Ctrl&nbsp+&nbspe", new StyleAttribute()),
    STALE("Selection criteria have been changed, but not applied. Previously applied values are in effect. Please tap <b>RUN</b> to apply the updated selection criteria. Ctrl&nbsp+&nbspe",
        new StyleAttribute()
            .addStyle("color", "var(--paper-orange-500)")
            .addStyle("border-color", "var(--paper-orange-500)")
    ),
    CHANGED("Selection criteria have been applied, but not saved. Tap <b>SAVE</b> to save the updated selection criteria if needed. Ctrl&nbsp+&nbspe",
        new StyleAttribute()
            .addStyle("color", "var(--paper-light-blue-600)")
            .addStyle("border-color", "var(--paper-light-blue-600)")
    );

    public final String message;
    public final StyleAttribute style;

    CriteriaIndication(final String message, final StyleAttribute style) {
        this.message = message;
        this.style = style;
    }
}
