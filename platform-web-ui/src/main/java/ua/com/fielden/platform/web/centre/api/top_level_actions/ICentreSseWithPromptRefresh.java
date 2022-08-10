package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that tells center to show prompt on sse event.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreSseWithPromptRefresh<T extends AbstractEntity<?>> extends ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> {

    /**
     * Configures centre to show prompt on sse event before refresh. Allows one to specify the number of countdown seconds to refresh.
     * If countdown seconds is zero then prompt will have to options: refresh and skip without any countdown.
     * If countdown seconds is greater than zero then prompt will have only one option - skip. Centre will be refreshed after countdown.
     *
     * @param seconds - countdown seconds
     * @return
     */
    ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> withPromptForRefresh(final int seconds);
}
