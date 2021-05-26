package ua.com.fielden.platform.web.centre.api.impl;

import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.mkInsertionPoint;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig.configInsertionPoint;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointWithToolbar;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointsFlexible;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

public class InsertionPointConfigBuilder<T extends AbstractEntity<?>> implements IInsertionPoints<T>, IInsertionPointWithToolbar<T> {

    private final ResultSetBuilder<T> resultSetBuilder;

    private final EntityActionConfig insertionPointAction;
    private final InsertionPoints whereToInsertView;
    private boolean flex = false;
    private Optional<IToolbarConfig> toolbarConfig = Optional.empty();

    public InsertionPointConfigBuilder(final ResultSetBuilder<T> resultSetBuilder, final EntityActionConfig insertionPointAction, final InsertionPoints whereToInsertView) {
        this.resultSetBuilder = resultSetBuilder;
        this.insertionPointAction = insertionPointAction;
        this.whereToInsertView = whereToInsertView;
    }

    @Override
    public EntityCentreConfig<T> build() {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)).setFlex(flex).setToolbar(toolbarConfig));
        return resultSetBuilder.build();
    }

    @Override
    public IInsertionPoints<T> flex() {
        this.flex = true;
        return this;
    }

    @Override
    public IInsertionPointsFlexible<T> setToolbar(final IToolbarConfig toolbar) {
        this.toolbarConfig = Optional.ofNullable(toolbar);
        return this;
    }

    @Override
    public IInsertionPointWithToolbar<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        resultSetBuilder.addInsertionPoint(configInsertionPoint(mkInsertionPoint(this.insertionPointAction, this.whereToInsertView)));
        return new InsertionPointConfigBuilder<>(resultSetBuilder, actionConfig, whereToInsertView);
    }
}
