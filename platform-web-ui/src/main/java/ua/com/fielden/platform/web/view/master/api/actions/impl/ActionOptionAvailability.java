package ua.com.fielden.platform.web.view.master.api.actions.impl;

import java.util.Optional;

/**
 * Defines what options are available for master actions
 *
 * @author TG Team
 *
 */
public enum ActionOptionAvailability {

    /**
     * All options are available ( &NEW, &CLOSE)
     */
    ALLON,
    /**
     * No available options
     */
    ALLOFF;

    private boolean matches(final String availableOptions) {
        if (availableOptions == null) {
            return false;
        }
        return name().equalsIgnoreCase(availableOptions);
    }

    /**
     * Returns optional instance of this {@link Enum} that matches specified parameter.
     *
     * @param availableOptions
     * @return
     */
    public static Optional<ActionOptionAvailability> optionalValueOf(final String availableOptions) {
        for (final ActionOptionAvailability enumVal : ActionOptionAvailability.values()) {
            if (enumVal.matches(availableOptions)) {
                return Optional.of(enumVal);
            }
        }
        return Optional.empty();
    }
}
