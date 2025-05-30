package ua.com.fielden.platform.web.centre.api.impl;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.resultset.*;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.api.top_level_actions.IAlsoCentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsInGroup0;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.lang.String.format;

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
    public ISelectionCritKindSelector<T> addCrit(final CharSequence propName) {
        if (StringUtils.isEmpty(propName)) {
            throw new EntityCentreConfigurationException("Property name should not be empty.");
        }

        if (!"this".contentEquals(propName) && !EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new EntityCentreConfigurationException(format("Property expression [%s] is not valid for entity [%s].", propName, builder.getEntityType().getSimpleName()));
        }

        if (builder.selectionCriteria.contains(propName.toString())) {
            throw new EntityCentreConfigurationException(format("Property [%s] has been already added to the selection critera for entity [%s].", propName, builder.getEntityType().getSimpleName()));
        }

        builder.currSelectionCrit = Optional.of(propName.toString());
        builder.selectionCriteria.add(propName.toString());
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

    @Deprecated
    @Override
    public IResultSetBuilder3Ordering<T> addProp(final CharSequence propName) {
        return new ResultSetBuilder<>(builder).addProp(propName, true);
    }

    @Override
    public IResultSetBuilder3Ordering<T> addProp(final CharSequence prop, final boolean presentByDefault) {
        return new ResultSetBuilder<>(builder).addProp(prop, presentByDefault);
    }

    @Override
    public IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef, final boolean presentByDefault) {
        return new ResultSetBuilder<>(builder).addProp(propDef, presentByDefault);
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
    public IResultSetBuilderDynamicPropsAction<T> addProps(final CharSequence propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<T, Optional<CentreContext<T,?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
        return new ResultSetBuilder<>(builder).addProps(propName, dynColBuilderType, entityPreProcessor, contextConfig);
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> addProps(final CharSequence propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<T, Optional<CentreContext<T, ?>>> entityPreProcessor, final BiFunction<T, Optional<CentreContext<T, ?>>, Map> renderingHintsProvider, final CentreContextConfig contextConfig) {
        return new ResultSetBuilder<>(builder).addProps(propName, dynColBuilderType, entityPreProcessor, renderingHintsProvider, contextConfig);
    }

    @Override
    public IResultSetBuilderWidgetSelector<T> addEditableProp(final CharSequence propName) {
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
