package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.mkInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig.configInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.IWithRightSplitterPosition;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointPreferred;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointWithToolbar;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.exception.InsertionPointConfigException;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 * Implementation for insertion point APIs.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class InsertionPointConfigBuilder<T extends AbstractEntity<?>> implements IInsertionPoints<T>, IInsertionPointPreferred<T> {
    private final ResultSetBuilder<T> resultSetBuilder;
    private final EntityActionConfig insertionPointAction;
    private final InsertionPoints whereToInsertView;
    private boolean preferred = false;
    private Optional<IToolbarConfig> toolbarConfig = Optional.empty();

    public InsertionPointConfigBuilder(final ResultSetBuilder<T> resultSetBuilder, final EntityActionConfig insertionPointAction, final InsertionPoints whereToInsertView) {
        this.resultSetBuilder = resultSetBuilder;
        this.insertionPointAction = insertionPointAction;
        this.whereToInsertView = whereToInsertView;
    }

    @Override
    public EntityCentreConfig<T> build() {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)).setPreferred(preferred).setToolbar(toolbarConfig));
        return resultSetBuilder.build();
    }

    @Override
    public IInsertionPoints<T> setToolbar(final IToolbarConfig toolbar) {
        this.toolbarConfig = ofNullable(toolbar);
        return this;
    }

    @Override
    public IInsertionPointPreferred<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)).setPreferred(preferred).setToolbar(toolbarConfig));
        return new InsertionPointConfigBuilder<>(resultSetBuilder, actionConfig, whereToInsertView);
    }

    @Override
    public IInsertionPointWithToolbar<T> makePreferred() {
        if  (whereToInsertView != ALTERNATIVE_VIEW) {
            throw new InsertionPointConfigException(
                    format("Insertion point for %s action can not be preferred as it is not an alternative view.",
                            insertionPointAction.functionalEntity.map(type -> type.getSimpleName()).orElse("Default")));
        }
        this.preferred = true;
        return this;
    }

    @Override
    public IWithRightSplitterPosition<T> withLeftSplitterPosition(final int percentage) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)).setPreferred(preferred).setToolbar(toolbarConfig));
        return resultSetBuilder.withLeftSplitterPosition(percentage);
    }

    @Override
    public IEcbCompletion<T> withRightSplitterPosition(final int percentage) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)).setPreferred(preferred).setToolbar(toolbarConfig));
        return resultSetBuilder.withRightSplitterPosition(percentage);
    }
}
