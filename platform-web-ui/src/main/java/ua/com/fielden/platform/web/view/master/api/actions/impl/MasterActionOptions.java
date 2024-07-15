package ua.com.fielden.platform.web.view.master.api.actions.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;

/**
 * Defines what options are available for master actions.
 * It is used mainly to turn off/on "SAVE & CLOSE" and similar options for master actions as the application level.
 *
 * @author TG Team
 *
 */
public enum MasterActionOptions {

    /**
     * All options are available ( &NEW, &CLOSE)
     */
    ALL_ON,
    /**
     * No available options
     */
    ALL_OFF;

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
    public static Optional<MasterActionOptions> optionalValueOf(final String availableOptions) {
        for (final MasterActionOptions enumVal : MasterActionOptions.values()) {
            if (enumVal.matches(availableOptions)) {
                return of(enumVal);
            }
        }
        return empty();
    }
}
