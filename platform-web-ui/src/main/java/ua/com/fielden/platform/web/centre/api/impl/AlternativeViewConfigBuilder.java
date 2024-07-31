package ua.com.fielden.platform.web.centre.api.impl;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.mkInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig.configInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewAlso;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewPreferred;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewWithActions;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewWithActionsExtended;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 * Implements alternative view API.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AlternativeViewConfigBuilder<T extends AbstractEntity<?>> implements IAlternativeView<T>, IAlternativeViewPreferred<T>, IAlternativeViewAlso<T> {

    private final ResultSetBuilder<T> resultSetBuilder;
    private final EntityActionConfig alternativeViewAction;
    private boolean preferred = false;
    private Optional<IToolbarConfig> toolbarConfig = Optional.empty();
    private final List<EntityActionConfig> actions = new ArrayList<>();

    public AlternativeViewConfigBuilder(final ResultSetBuilder<T> resultSetBuilder, final EntityActionConfig alternativeViewAction) {
        this.resultSetBuilder = resultSetBuilder;
        this.alternativeViewAction = alternativeViewAction;
    }

    @Override
    public EntityCentreConfig<T> build() {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.alternativeViewAction, ALTERNATIVE_VIEW))
                .setPreferred(preferred)
                .setToolbar(toolbarConfig)
                .setActions(actions));
        return resultSetBuilder.build();
    }

    @Override
    public IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.alternativeViewAction, ALTERNATIVE_VIEW))
                .setPreferred(preferred)
                .setToolbar(toolbarConfig)
                .setActions(actions));
        return new AlternativeViewConfigBuilder<>(resultSetBuilder, actionConfig);
    }

    @Override
    public IAlternativeView<T> setToolbar(final IToolbarConfig toolbar) {
        this.toolbarConfig = ofNullable(toolbar);
        return this;
    }

    @Override
    public IAlternativeViewAlso<T> addTopAction(final EntityActionConfig actionConfig) {
        this.actions.add(actionConfig);
        return this;
    }

    @Override
    public IAlternativeViewWithActionsExtended<T> makePreferred() {
        this.preferred = true;
        return this;
    }

    @Override
    public IAlternativeViewWithActions<T> also() {
        return this;
    }
}
