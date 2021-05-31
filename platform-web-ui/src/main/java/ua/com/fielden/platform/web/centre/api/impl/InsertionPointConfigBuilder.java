package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.mkInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig.configInsertionPoint;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointPreferred;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointWithToolbar;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.exception.InsertionPointConfigException;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

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
        this.toolbarConfig = Optional.ofNullable(toolbar);
        return this;
    }

    @Override
    public IInsertionPointPreferred<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)));
        return new InsertionPointConfigBuilder<>(resultSetBuilder, actionConfig, whereToInsertView);
    }

    @Override
    public IInsertionPointWithToolbar<T> makePreferred() {
        if  (whereToInsertView != InsertionPoints.ALTERNATIVE_VIEW) {
            throw new InsertionPointConfigException(
                    format("Insertion point for %s action can not be preferred as it is not an alternative view.",
                            insertionPointAction.functionalEntity.map(type -> type.getSimpleName()).orElse("Default")));
        }
        this.preferred = true;
        return this;
    }
}
