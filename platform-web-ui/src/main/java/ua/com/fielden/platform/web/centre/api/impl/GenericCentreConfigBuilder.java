package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.front_actions.IAlsoFrontActions;
import ua.com.fielden.platform.web.centre.api.front_actions.IFrontWithTopActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreSseWithPromptRefresh;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithRunConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithSse;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.sse.IEventSource;

public class GenericCentreConfigBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ICentreSseWithPromptRefresh<T>,  ICentreTopLevelActionsWithRunConfig<T>{

    private static final String ERR_EVENT_SOURCE_CLASS_NULL = "Event Source Class can not be null.";
    private static final String ERR_COUNTDOWN_SECONDS_LESS_THAN_ZERO = "The countdown seconds [%s] should be greater than zero.";

    public GenericCentreConfigBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
    }

    @Override
    public ICentreSseWithPromptRefresh<T> hasEventSource(final Class<? extends IEventSource> eventSourceClass) {
        if (eventSourceClass == null) {
            throw new EntityCentreConfigurationException(ERR_EVENT_SOURCE_CLASS_NULL);
        }
        builder.eventSourceClass = eventSourceClass;
        return this;
    }

    @Override
    public ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> withCountdownRefreshPrompt(final int seconds) {
        if (seconds <= 0) {
            throw new EntityCentreConfigurationException(format(ERR_COUNTDOWN_SECONDS_LESS_THAN_ZERO, seconds));
        }
        builder.refreshCountdown = Integer.valueOf(seconds);
        return this;
    }

    @Override
    public ICentreTopLevelActionsWithEnforcePostSaveRefreshConfig<T> withRefreshPrompt() {
        builder.refreshCountdown = 0;
        return this;
    }

    @Override
    public IFrontWithTopActions<T> enforcePostSaveRefresh() {
        builder.enforcePostSaveRefresh = true;
        return this;
    }

    @Override
    public IAlsoFrontActions<T> addFrontAction(final EntityActionConfig actionConfig) {
        return new FrontActionBuilder<>(builder).addFrontAction(actionConfig);
    }

    @Override
    public IAlsoCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig) {
        return new TopLevelActionsBuilder<>(builder).addTopAction(actionConfig);
    }

    @Override
    public ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc) {
        return new TopLevelActionsBuilder<>(builder).beginTopActionsGroup(desc);
    }

    @Override
    public ISelectionCritKindSelector<T> addCrit(final CharSequence propName) {
        return new TopLevelActionsBuilder<T>(builder).addCrit(propName);
    }

    @Override
    public ICentreTopLevelActionsWithSse<T> runAutomatically() {
        builder.runAutomatically = true;
        return this;
    }
}
