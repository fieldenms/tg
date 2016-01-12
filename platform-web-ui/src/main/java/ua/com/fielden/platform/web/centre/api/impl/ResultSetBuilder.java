package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPoints;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoProp;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Checkbox;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1Toolbar;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aScroll;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1bPageCapacity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1cVisibleRows;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2Properties;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder4OrderingDirection;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder7SecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder9RenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.layout.IExpandedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class ResultSetBuilder<T extends AbstractEntity<?>> implements IResultSetBuilder0Checkbox<T>, IResultSetBuilder3Ordering<T>, IResultSetBuilder4OrderingDirection<T>, IResultSetBuilder7SecondaryAction<T>, IExpandedCardLayoutConfig<T>, ISummaryCardLayout<T> {

    private final EntityCentreBuilder<T> builder;
    private final ResultSetSecondaryActionsBuilder secondaryActionBuilder = new ResultSetSecondaryActionsBuilder();

    protected Optional<String> propName = Optional.empty();
    protected Optional<String> tooltipProp = Optional.empty();
    protected Optional<PropDef<?>> propDef = Optional.empty();
    private EntityActionConfig entityActionConfig;
    private Integer orderSeq;

    public ResultSetBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IResultSetBuilder3Ordering<T> addProp(final String propName) {
        if (StringUtils.isEmpty(propName)) {
            throw new IllegalArgumentException("Property name should not be null.");
        }

        if (!"this".equals(propName) && !EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new IllegalArgumentException(String.format("Provided value '%s' is not a valid property expression for entity '%s'", propName, builder.getEntityType().getSimpleName()));
        }

        this.propName = Optional.of(propName);
        this.tooltipProp = Optional.empty();
        this.propDef = Optional.empty();
        this.orderSeq = null;
        this.entityActionConfig = null;
        return this;
    }

    @Override
    public IResultSetBuilder4OrderingDirection<T> order(final int orderSeq) {
        if (builder.resultSetOrdering.containsKey(orderSeq)) {
            final Pair<String, EntityCentreConfig.OrderDirection> pair = builder.resultSetOrdering.get(orderSeq);
            throw new IllegalArgumentException(format("Ordering by property '%s' with sequence %s conflicts with ordering by property '%s'@'%s' (%s), which has the same sequence.",
                    propName, orderSeq, pair.getKey(), builder.getEntityType().getSimpleName(), pair.getValue()));
        }
        this.orderSeq = orderSeq;
        return this;
    }

    @Override
    public IWithTooltip<T> desc() {
        this.builder.resultSetOrdering.put(orderSeq, new Pair<>(propName.get(), OrderDirection.DESC));
        return this;
    }

    @Override
    public IWithTooltip<T> asc() {
        this.builder.resultSetOrdering.put(orderSeq, new Pair<>(propName.get(), OrderDirection.ASC));
        return this;
    }

    @Override
    public IWithTooltip<T> addProp(final PropDef<?> propDef) {
        if (propDef == null) {
            throw new IllegalArgumentException("Custom property should not be null.");
        }

        this.propName = Optional.empty();
        this.tooltipProp = Optional.empty();
        this.propDef = Optional.of(propDef);
        this.orderSeq = null;
        this.entityActionConfig = null;
        return this;
    }

    @Override
    public IWithSummary<T> withTooltip(final String propertyName) {
        this.tooltipProp = Optional.ofNullable(propertyName);
        return this;
    }

    @Override
    public IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc) {
        if (!propName.isPresent()) {
            throw new IllegalStateException("There is no property to associated the summary expression with. This indicated an out of secuquence call, which is most likely due to a programming mistake.");
        }
        final String[] td = titleAndDesc.split(":");
        final String title = td[0];
        final String desc = td.length > 1 ? td[1] : null;
        this.builder.summaryExpressions.put(propName.get(), new SummaryPropDef(alias, expression, title, desc));
        return this;
    }

    @Override
    public IAlsoProp<T> withAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new IllegalArgumentException("Property action configuration should not be null.");
        }

        this.entityActionConfig = actionConfig;
        completePropIfNeeded();
        return this;
    }

    @Override
    public IResultSetBuilder2Properties<T> also() {
        completePropIfNeeded();
        return this;
    }

    private Optional<Device> lastResultsetLayoutDevice = Optional.empty();
    private Optional<Orientation> lastResultsetLayoutOrientation = Optional.empty();

    @Override
    public IExpandedCardLayoutConfig<T> setCollapsedCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalStateException("Resultset card layout requries device and orientation (optional) to be specified.");
        }

        this.lastResultsetLayoutDevice = Optional.of(device);
        this.lastResultsetLayoutOrientation = orientation;
        this.builder.resultsetCollapsedCardLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    @Override
    public ICollapsedCardLayoutConfig<T> withExpansionLayout(final String flexString) {
        if (!lastResultsetLayoutDevice.isPresent()) {
            throw new IllegalStateException("Resultset card layout cannot be set if no device is specified.");
        }
        this.builder.resultsetExpansionCardLayout.whenMedia(lastResultsetLayoutDevice.get(), lastResultsetLayoutOrientation.isPresent() ? lastResultsetLayoutOrientation.get()
                : null).set(flexString);
        this.lastResultsetLayoutDevice = Optional.empty();
        this.lastResultsetLayoutOrientation = Optional.empty();
        return this;
    }

    @Override
    public IAlsoSecondaryAction<T> addPrimaryAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new IllegalArgumentException("Primary action configuration should not be null.");
        }

        completePropIfNeeded();
        this.builder.resultSetPrimaryEntityAction = actionConfig;
        return secondaryActionBuilder;
    }

    @Override
    public IAlsoSecondaryAction<T> addSecondaryAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new IllegalArgumentException("Secondary action configuration should not be null.");
        }

        this.builder.resultSetSecondaryEntityActions.add(actionConfig);

        return secondaryActionBuilder;
    }

    @Override
    public IResultSetBuilder9RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Assignment handler for custom properties should not be null.");
        }

        // complete property registration if there is an oustanding one
        completePropIfNeeded();

        // check if there are any custom properties
        // and indicate to the developer that assignment of an assigner handler
        // is not appropriate in case of no custom properties
        if (builder.resultSetProperties.stream().filter(v -> v.propDef.isPresent()).count() == 0) {
            throw new IllegalArgumentException("Assignment handler for custom properties is meaningless as there are the result set configuration contains no definitions for custom properties.");
        }
        // then if there are custom properties, but all of them have default values already specified, there is no reason to have custom assignment logic
        if (builder.resultSetProperties.stream().filter(v -> v.propDef.isPresent() && !v.propDef.get().value.isPresent()).count() == 0) {
            throw new IllegalArgumentException("Assignment handler for custom properties is meaningless as all custom properties have been provided with default values.");
        }

        this.builder.resultSetCustomPropAssignmentHandlerType = handler;
        return this;
    }

    @Override
    public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<? extends AbstractEntity<?>, ?>> type) {
        if (type == null) {
            throw new IllegalArgumentException("Rendering customised type should not be null.");
        }

        completePropIfNeeded();
        this.builder.resultSetRenderingCustomiserType = type;
        return this;
    }

    @Override
    public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig) {
        if (type == null) {
            throw new IllegalArgumentException("Query enhancer should not be null.");
        }

        completePropIfNeeded();
        this.builder.queryEnhancerConfig = new Pair<>(type, Optional.ofNullable(contextConfig));
        return this;
    }

    @Override
    public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type) {
        return setQueryEnhancer(type, null);
    }

    @Override
    public ISummaryCardLayout<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
        if (fetchProvider == null) {
            throw new IllegalArgumentException("Fetch provider should not be null.");
        }

        completePropIfNeeded();
        this.builder.fetchProvider = fetchProvider;
        return this;
    }

    @Override
    public EntityCentreConfig<T> build() {
        completePropIfNeeded();
        return this.builder.build();
    }

    /**
     * Constructs an instance of {@link EntityCentreConfig.ResultSetProp} if possible and adds it the result set list.
     */
    private void completePropIfNeeded() {
        // construct and add property to the builder
        if (propName.isPresent()) {
            final ResultSetProp prop = ResultSetProp.propByName(propName.get(), (tooltipProp.isPresent() ? tooltipProp.get() : null), entityActionConfig);
            this.builder.resultSetProperties.add(prop);
        } else if (propDef.isPresent()) {
            final ResultSetProp prop = ResultSetProp.propByDef(propDef.get(), (tooltipProp.isPresent() ? tooltipProp.get() : null), entityActionConfig);
            this.builder.resultSetProperties.add(prop);
        }

        // clear things up for the next property to be added if any
        this.propName = Optional.empty();
        this.tooltipProp = Optional.empty();
        this.propDef = Optional.empty();
        this.orderSeq = null;
        this.entityActionConfig = null;
    }

    @Override
    public ISummaryCardLayout<T> setSummaryCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalStateException("Summary card layout requries device and orientation (optional) to be specified.");
        }

        this.builder.resultsetSummaryCardLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    /**
     * A helper class to assist in name collision resolution.
     */
    private class ResultSetSecondaryActionsBuilder implements IAlsoSecondaryAction<T> {

        @Override
        public IResultSetBuilder9RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>> handler) {
            return ResultSetBuilder.this.setCustomPropsValueAssignmentHandler(handler);
        }

        @Override
        public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<? extends AbstractEntity<?>, ?>> type) {
            return ResultSetBuilder.this.setRenderingCustomiser(type);
        }

        @Override
        public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig) {
            return ResultSetBuilder.this.setQueryEnhancer(type, contextConfig);
        }

        @Override
        public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type) {
            return ResultSetBuilder.this.setQueryEnhancer(type);
        }

        @Override
        public ISummaryCardLayout<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
            return ResultSetBuilder.this.setFetchProvider(fetchProvider);
        }

        @Override
        public EntityCentreConfig<T> build() {
            return ResultSetBuilder.this.build();
        }

        @Override
        public IResultSetBuilder7SecondaryAction<T> also() {
            return ResultSetBuilder.this;
        }

        @Override
        public ISummaryCardLayout<T> setSummaryCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
            return ResultSetBuilder.this.setSummaryCardLayoutFor(device, orientation, flexString);
        }

        @Override
        public IInsertionPoints<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
            return ResultSetBuilder.this.addInsertionPoint(actionConfig, whereToInsertView);
        }

    }

    @Override
    public IInsertionPoints<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        this.builder.insertionPointActions.add(EntityActionConfig.mkInsertionPoint(actionConfig, whereToInsertView));
        return this;
    }

    @Override
    public IResultSetBuilder1Toolbar<T> hideCheckboxes() {
        this.builder.hideCheckboxes = true;
        return this;
    }

    @Override
    public IResultSetBuilder1aScroll<T> hideToolbar() {
        this.builder.hideToolbar = true;
        return this;
    }

    @Override
    public IResultSetBuilder1bPageCapacity<T> notScrollable() {
        this.builder.scrollConfig = ScrollConfig.configScroll()
                .withFixedCheckboxesAndPrimaryActions()
                .withFixedSecondaryActions()
                .withFixedHeader()
                .withFixedSummary()
                .done();
        return this;
    }

    @Override
    public IResultSetBuilder1cVisibleRows<T> setPageCapacity(final int pageCapacity) {
        this.builder.pageCapacity = pageCapacity;
        return this;
    }

    @Override
    public IResultSetBuilder2Properties<T> setVisibleRowsCount(final int visibleRowsCount) {
        this.builder.visibleRowsCount = visibleRowsCount;
        return this;
    }

    @Override
    public IResultSetBuilder1bPageCapacity<T> withScrollingConfig(final IScrollConfig scrollConfig) {
        this.builder.scrollConfig = scrollConfig;
        return this;
    }
}
