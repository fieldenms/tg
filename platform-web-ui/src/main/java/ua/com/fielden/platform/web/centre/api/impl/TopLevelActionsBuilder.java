package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1Toolbar;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aScroll;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1bPageCapacity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1cVisibleRows;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1dFitBehaviour;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1eRowHeight;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2Properties;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2aDraggable;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder4aWidth;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup0;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class TopLevelActionsBuilder<T extends AbstractEntity<?>> implements ICentreTopLevelActions<T>, ICentreTopLevelActionsInGroup<T>, ICentreTopLevelActionsInGroup0<T>, IAlsoCentreTopLevelActions<T>{

    private final EntityCentreBuilder<T> builder;

    public TopLevelActionsBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IAlsoCentreTopLevelActions<T> addTopAction(final EntityActionConfig actionConfig) {
        if (actionConfig != null) {
            builder.topLevelActions.add(new Pair<>(actionConfig, builder.currGroup));
        }
        return this;
    }

    @Override
    public ICentreTopLevelActionsInGroup<T> beginTopActionsGroup(final String desc) {
        builder.currGroup = Optional.of(desc);
        return this;
    }

    @Override
    public ICentreTopLevelActionsInGroup0<T> addGroupAction(final EntityActionConfig actionConfig) {
        builder.topLevelActions.add(new Pair<>(actionConfig, builder.currGroup));
        return this;
    }

    @Override
    public IAlsoCentreTopLevelActions<T> endTopActionsGroup() {
        builder.currGroup = Optional.empty();
        return this;
    }

    @Override
    public ICentreTopLevelActions<T> also() {
        return this;
    }

    @Override
    public ISelectionCritKindSelector<T> addCrit(final String propName) {
        if (StringUtils.isEmpty(propName)) {
            throw new IllegalArgumentException("Property name should not be empty.");
        }

        if (!"this".equals(propName) && !EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new IllegalArgumentException(String.format("Provided value '%s' is not a valid property expression for entity '%s'", propName, builder.getEntityType().getSimpleName()));
        }

        if (builder.selectionCriteria.contains(propName)) {
            throw new IllegalArgumentException(String.format("Provided value '%s' has been already added as a selection criterion for entity '%s'", propName, builder.getEntityType().getSimpleName()));
        }

        builder.currSelectionCrit = Optional.of(propName);
        builder.selectionCriteria.add(propName);
        return new SelectionCriteriaBuilder<>(builder, this);
    }

    @Override
    public IResultSetBuilder1Toolbar<T> hideCheckboxes() {
        return new ResultSetBuilder<>(builder).hideCheckboxes();
    }

    @Override
    public IResultSetBuilder1aScroll<T> setToolbar(final IToolbarConfig toolbar) {
        return new ResultSetBuilder<>(builder).setToolbar(toolbar);
    }

    @Override
    public IResultSetBuilder1aScroll<T> hideToolbar() {
        return new ResultSetBuilder<>(builder).hideToolbar();
    }

    @Override
    public IResultSetBuilder2aDraggable<T> notScrollable() {
        return new ResultSetBuilder<>(builder).notScrollable();
    }

    @Override
    public IResultSetBuilder2aDraggable<T> withScrollingConfig(final IScrollConfig scrollConfig) {
        return new ResultSetBuilder<>(builder).withScrollingConfig(scrollConfig);
    }

    @Override
    public IResultSetBuilder1bPageCapacity<T> draggable() {
        return new ResultSetBuilder<>(builder).draggable();
    }

    @Override
    public IResultSetBuilder1cVisibleRows<T> setPageCapacity(final int pageCapacity) {
        return new ResultSetBuilder<>(builder).setPageCapacity(pageCapacity);
    }

    @Override
    public IResultSetBuilder1dFitBehaviour<T> setVisibleRowsCount(final int visibleRowsCount) {
        return new ResultSetBuilder<>(builder).setVisibleRowsCount(visibleRowsCount);
    }

    @Override
    public IResultSetBuilder1dFitBehaviour<T> setHeight(final String height) {
        return new ResultSetBuilder<>(builder).setHeight(height);
    }

    @Override
    public IResultSetBuilder3Ordering<T> addProp(final String propName) {
        return new ResultSetBuilder<>(builder).addProp(propName);
    }

    @Override
    public IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef) {
        return new ResultSetBuilder<>(builder).addProp(propDef);
    }

    @Override
    public IResultSetBuilder1eRowHeight<T> fitToHeight() {
        return new ResultSetBuilder<>(builder).fitToHeight();
    }

    @Override
    public IResultSetBuilder2Properties<T> rowHeight(final String rowHeight) {
        return new ResultSetBuilder<>(builder).rowHeight(rowHeight);
    }
}
