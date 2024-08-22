package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.IWithRightSplitterPosition;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewPreferred;
import ua.com.fielden.platform.web.centre.api.insertion_points.*;
import ua.com.fielden.platform.web.centre.api.insertion_points.exception.InsertionPointConfigException;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.mkInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig.configInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;

/**
 * Implementation for insertion point APIs.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class InsertionPointConfigBuilder<T extends AbstractEntity<?>> implements IInsertionPoints<T>, IInsertionPointConfig0<T>, IAlternativeView<T> {

    private static final String ERR_ALTERNATIVE_VIEW_CANNOT_BE_PREFERRED = "Insertion point for action %s cannot be preferred as it is not an alternative view.";
    private static final String ERR_ALTERNATIVE_VIEW_CANNOT_BE_RESIZABLE = "Insertion point for action %s, which is an alternative view, is not resizable by default. This option should not be specified explicitly.";

    private final ResultSetBuilder<T> resultSetBuilder;
    private final EntityActionConfig insertionPointAction;
    private final InsertionPoints whereToInsertView;
    private boolean preferred = false;
    private boolean noResizing = false;
    private Optional<IToolbarConfig> toolbarConfig = Optional.empty();

    public InsertionPointConfigBuilder(final ResultSetBuilder<T> resultSetBuilder, final EntityActionConfig insertionPointAction, final InsertionPoints whereToInsertView) {
        this.resultSetBuilder = resultSetBuilder;
        this.insertionPointAction = insertionPointAction;
        this.whereToInsertView = whereToInsertView;
    }

    @Override
    public EntityCentreConfig<T> build() {
        addInsertionPointToBuilder();
        return resultSetBuilder.build();
    }

    @Override
    public IInsertionPointWithConfig<T> setToolbar(final IToolbarConfig toolbar) {
        this.toolbarConfig = ofNullable(toolbar);
        return this;
    }

    @Override
    public IInsertionPointConfig0<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        addInsertionPointToBuilder();
        return new InsertionPointConfigBuilder<>(resultSetBuilder, actionConfig, whereToInsertView);
    }

    @Override
    public IInsertionPointWithToolbar<T> makePreferred() {
        if  (whereToInsertView != ALTERNATIVE_VIEW) {
            throw new InsertionPointConfigException(
                    format(ERR_ALTERNATIVE_VIEW_CANNOT_BE_PREFERRED,
                            insertionPointAction.functionalEntity.map(type -> type.getSimpleName()).orElse("Default")));
        }
        this.preferred = true;
        return this;
    }

    @Override
    public IWithRightSplitterPosition<T> withLeftSplitterPosition(final int percentage) {
        addInsertionPointToBuilder();
        return resultSetBuilder.withLeftSplitterPosition(percentage);
    }

    @Override
    public IInsertionPointsWithCustomLayout<T> withRightSplitterPosition(final int percentage) {
        addInsertionPointToBuilder();
        return resultSetBuilder.withRightSplitterPosition(percentage);
    }

    @Override
    public IInsertionPointWithToolbar<T> noResizing() {
        if  (whereToInsertView == ALTERNATIVE_VIEW) {
            throw new InsertionPointConfigException(
                    format(ERR_ALTERNATIVE_VIEW_CANNOT_BE_RESIZABLE,
                            insertionPointAction.functionalEntity.map(type -> type.getSimpleName()).orElse("Default")));
        }
        this.noResizing = true;
        return this;
    }

    @Override
    public IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig) {
        addInsertionPointToBuilder();
        return new AlternativeViewConfigBuilder<>(resultSetBuilder, actionConfig);
    }

    @Override
    public IAlternativeView<T> withCustomisableLayout() {
        addInsertionPointToBuilder();
        return resultSetBuilder.withCustomisableLayout();
    }

    /**
     * Adds last insertion point configuration into {@link ResultSetBuilder}.<br>
     * This concludes insertion point configuration and either next insertion point will be added or splitter/customisable layout/alternative view will be configured next.
     */
    private void addInsertionPointToBuilder() {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView))
                .setPreferred(preferred)
                .setNoResizing(noResizing)
                .setToolbar(toolbarConfig));
    }

}
