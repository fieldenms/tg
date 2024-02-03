package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aEgiIconStyle;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1bCheckbox;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1cToolbar;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1dCentreScroll;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1dScroll;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1eDraggable;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1efRetrieveAll;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1fPageCapacity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1gMaxPageCapacity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1hHeaderWrap;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1iVisibleRowsCount;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1jFitBehaviour;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1kRowHeight;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2Properties;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder4aWidth;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderDynamicPropsAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderWidgetSelector;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup0;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;

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
            throw new EntityCentreConfigurationException("Property name should not be empty.");
        }

        if (!"this".equals(propName) && !EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new EntityCentreConfigurationException(format("Property expression [%s] is not valid for entity [%s].", propName, builder.getEntityType().getSimpleName()));
        }

        if (builder.selectionCriteria.contains(propName)) {
            throw new EntityCentreConfigurationException(format("Property [%s] has been already added to the selection critera for entity [%s].", propName, builder.getEntityType().getSimpleName()));
        }

        builder.currSelectionCrit = Optional.of(propName);
        builder.selectionCriteria.add(propName);
        return new SelectionCriteriaBuilder<>(builder, this);
    }

    @Override
    public IResultSetBuilder1cToolbar<T> hideCheckboxes() {
        return new ResultSetBuilder<>(builder).hideCheckboxes();
    }

    @Override
    public IResultSetBuilder1dScroll<T> setToolbar(final IToolbarConfig toolbar) {
        return new ResultSetBuilder<>(builder).setToolbar(toolbar);
    }

    @Override
    public IResultSetBuilder1dScroll<T> hideToolbar() {
        return new ResultSetBuilder<>(builder).hideToolbar();
    }

    @Override
    public IResultSetBuilder1dCentreScroll<T> notScrollable() {
        return new ResultSetBuilder<>(builder).notScrollable();
    }

    @Override
    public IResultSetBuilder1dCentreScroll<T> withScrollingConfig(final IScrollConfig scrollConfig) {
        return new ResultSetBuilder<>(builder).withScrollingConfig(scrollConfig);
    }

    @Override
    public IResultSetBuilder1eDraggable<T> lockScrollingForInsertionPoints() {
        return new ResultSetBuilder<>(builder).lockScrollingForInsertionPoints();
    }

    @Override
    public IResultSetBuilder1efRetrieveAll<T> draggable() {
        return new ResultSetBuilder<>(builder).draggable();
    }

    @Override
    public IResultSetBuilder1fPageCapacity<T> retrieveAll() {
        return new ResultSetBuilder<>(builder).retrieveAll();
    }

    @Override
    public IResultSetBuilder1gMaxPageCapacity<T> setPageCapacity(final int pageCapacity) {
        return new ResultSetBuilder<>(builder).setPageCapacity(pageCapacity);
    }

    @Override
    public IResultSetBuilder1hHeaderWrap<T> setMaxPageCapacity(final int maxPageCapacity) {
        return new ResultSetBuilder<>(builder).setMaxPageCapacity(maxPageCapacity);
    }

    @Override
    public IResultSetBuilder1jFitBehaviour<T> setVisibleRowsCount(final int visibleRowsCount) {
        return new ResultSetBuilder<>(builder).setVisibleRowsCount(visibleRowsCount);
    }

    @Override
    public IResultSetBuilder1jFitBehaviour<T> setHeight(final String height) {
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
    public IResultSetBuilder1kRowHeight<T> fitToHeight() {
        return new ResultSetBuilder<>(builder).fitToHeight();
    }

    @Override
    public IResultSetBuilder2Properties<T> rowHeight(final String rowHeight) {
        return new ResultSetBuilder<>(builder).rowHeight(rowHeight);
    }

    @Override
    public <M extends AbstractEntity<?>> IResultSetBuilderDynamicPropsAction<T> addProps(final String propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T,?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
        return new ResultSetBuilder<>(builder).addProps(propName, dynColBuilderType, entityPreProcessor, contextConfig);
    }

    @Override
    public IResultSetBuilderWidgetSelector<T> addEditableProp(final String propName) {
        return new ResultSetBuilder<>(builder).addEditableProp(propName);
    }

    @Override
    public IResultSetBuilder1iVisibleRowsCount<T> wrapHeader(final int headerLineNumber) {
        return new ResultSetBuilder<>(builder).wrapHeader(headerLineNumber);
    }

    @Override
    public IResultSetBuilder1bCheckbox<T> hideEgi() {
        return new ResultSetBuilder<>(builder).hideEgi();
    }

    @Override
    public IResultSetBuilder1aEgiIconStyle<T> withGridViewIcon(final String icon) {
        return new ResultSetBuilder<>(builder).withGridViewIcon(icon);
    }
}
