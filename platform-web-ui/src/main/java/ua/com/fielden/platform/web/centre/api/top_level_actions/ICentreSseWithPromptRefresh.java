package ua.com.fielden.platform.web.centre.api.top_level_actions;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for Entity Centre Web UI DSL to support a user prompt before refreshing Entity Centres when receiving an SSE event.
 * This API is relevant strictly for Entity Centres that are associated with an Event Source.
 * <p>
 * A none of the APIs defined in this contract are used when configuring an Entity Cetre, assocaited with an Event Source, then the default behaviour takes place (i.e., an auto-refresh happens without any prompts).
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICentreSseWithPromptRefresh<T extends AbstractEntity<?>> extends ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> {

    /**
     * Provides a way to configure Entity Centres, associated with an Event Source, to show a prompt before a auto-refresh due to an SSE event.
     * <p>
     * If {@code countdown} is greater than zero then the prompt will have button Skip and an active countdown decrementing by 1 second; auto-refresh happens upon reaching 0, unless button Skip was actioned.
     * If {@code countdown} is less than or equal to zero, then a misconfiguration exception is thrown.
     *
     * @param seconds - countdown seconds
     * @return
     */
    ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> withCountdownRefreshPrompt(final int seconds);

    /**
     * Provides a way to configure Entity Centres, associated with an Event Source, to show a prompt with buttons "Refresh" and "Skip" upon receiving an SSE event.
     * Using this API effectively blocks the auto-refresh, awaiting an explicit user decision to perform a refresh or to skip it.
     *
     * @return
     */
    ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> withRefreshPrompt();

}