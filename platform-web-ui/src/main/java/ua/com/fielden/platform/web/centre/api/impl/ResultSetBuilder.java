package ua.com.fielden.platform.web.centre.api.impl;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.IWithRightSplitterPosition;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewPreferred;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.insertion_points.*;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.*;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.layout.IExpandedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.EntityAutocompletionWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.checkbox.impl.CheckboxWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl.CollectionalRepresentorWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.colour.impl.ColourWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.datetimepicker.impl.DateTimePickerWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.decimal.impl.DecimalWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.hyperlink.impl.HyperlinkWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.money.impl.MoneyWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;
import ua.com.fielden.platform.web.view.master.api.widgets.spinner.impl.SpinnerWidget;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp.dynamicProps;

/// A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
///
class ResultSetBuilder<T extends AbstractEntity<?>> implements IResultSetBuilderDynamicProps<T>, IResultSetBuilderWidgetSelector<T>, IResultSetBuilder3Ordering<T>, IResultSetBuilder1aEgiAppearance<T>, IResultSetBuilder1aEgiIconStyle<T>, IResultSetBuilder4OrderingDirection<T>, IResultSetBuilder7SecondaryAction<T>, IExpandedCardLayoutConfig<T>, ISummaryCardLayout<T>, IInsertionPointWithConfig<T> {

    private static final String ERR_SPLITTER_OVERLAPPING = "The left and right splitters are overlapping (i.e., left splitter position + right splitter position > 100).";
    private static final String ERR_SPLITTER_POSITION_OUT_OF_BOUNDS = "The splitter position should be greater than 0 and less than 100.";
    private static final String ERR_EDITABLE_SUB_PROP_DISALLOWED = "Dot-notated property [%s] cannot be added as editable. Only first-level properties are supported.";

    protected final EntityCentreBuilder<T> builder;
    private final ResultSetSecondaryActionsBuilder secondaryActionBuilder = new ResultSetSecondaryActionsBuilder();

    private final Map<String, Class<? extends IValueMatcherWithContext<T, ?>>> valueMatcherForProps = new HashMap<>();

    protected Optional<String> propName = empty();
    protected boolean presentByDefault = true;
    protected Optional<String> tooltipProp = empty();
    protected Optional<PropDef<?>> propDef = empty();
    protected Optional<AbstractWidget> widget = empty();
    private Optional<EntityMultiActionConfig> entityActionConfig = empty();
    private Integer orderSeq;
    private int width = 80;
    private boolean wordWrap = false;
    private boolean isFlexible = true;

    public ResultSetBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IResultSetBuilder3Ordering<T> addProp(final CharSequence propName) {
        return addProp(propName, true);
    }

    /// Implementation used by both [IResultSetBuilder2Properties#addProp(CharSequence, boolean)] and deprecated [IResultSetBuilder2Properties#addProp(CharSequence)].
    ///
    public IResultSetBuilder3Ordering<T> addProp(final CharSequence propName, final boolean presentByDefault) {
        if (StringUtils.isEmpty(propName)) {
            throw new EntityCentreConfigurationException("Property name should not be null.");
        }

        if (!"this".contentEquals(propName) && !EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new EntityCentreConfigurationException(format("Provided value [%s] is not a valid property expression for entity [%s]", propName, builder.getEntityType().getSimpleName()));
        }

        this.propName = of(propName.toString());
        this.presentByDefault = presentByDefault;
        this.tooltipProp = empty();
        this.propDef = empty();
        this.orderSeq = null;
        this.entityActionConfig = empty();
        return this;
    }

    @Override
    public IResultSetBuilderWidgetSelector<T> addEditableProp(final CharSequence propName) {
        if (propName.toString().contains(".")) {
            throw  new EntityCentreConfigurationException(format(ERR_EDITABLE_SUB_PROP_DISALLOWED, propName));
        }
        this.addProp(propName);
        this.widget = createWidget(propName.toString());
        return this;
    }

    private Optional<AbstractWidget> createWidget(final String propName) {
        final Class<? extends AbstractEntity<?>> root = this.builder.getEntityType();
        final String resultPropName = treeName(propName);
        final boolean isEntityItself = "".equals(resultPropName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, resultPropName);
        final String widgetPropName = "".equals(resultPropName) ? AbstractEntity.KEY : resultPropName;
        if (isEntityType(propertyType)) {
            return of(new EntityAutocompletionWidget(pair("", TitlesDescsGetter.getTitleAndDesc(widgetPropName, root).getValue()), widgetPropName, (Class<AbstractEntity<?>>)propertyType, false));
        } else if (isString(propertyType)) {
            return of(new SinglelineTextWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (isInteger(propertyType)) {
            return of(new SpinnerWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return of(new MoneyWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return of(new DecimalWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (Hyperlink.class.isAssignableFrom(propertyType)){
            return of(new HyperlinkWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (Colour.class.isAssignableFrom(propertyType)) {
            return of(new ColourWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (isBoolean(propertyType)) {
            return of(new CheckboxWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName));
        } else if (isDate(propertyType)) {
            return of(new DateTimePickerWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()), propName, false,
                    DefaultValueContract.getTimeZone(root, propName),
                    DefaultValueContract.getTimePortionToDisplay(root, propName)));
        } else if (isCollectional(propertyType)) {
            return of(new CollectionalRepresentorWidget(pair("", TitlesDescsGetter.getTitleAndDesc(propName, root).getValue()),propName));
        }
        return empty();
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> addProps(final CharSequence propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<T, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
        final ResultSetProp<T> prop = dynamicProps(propName, dynColBuilderType, entityPreProcessor, contextConfig);
        this.builder.addToResultSet(prop);
        return new ResultSetDynamicPropertyBuilder<>(this, prop);
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> addProps(final CharSequence propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<T, Optional<CentreContext<T, ?>>> entityPreProcessor, final BiFunction<T, Optional<CentreContext<T, ?>>, Map> renderingHintsProvider, final CentreContextConfig contextConfig) {
        final ResultSetProp<T> prop = dynamicProps(propName, dynColBuilderType, entityPreProcessor, renderingHintsProvider, contextConfig);
        this.builder.addToResultSet(prop);
        return new ResultSetDynamicPropertyBuilder<>(this, prop);
    }

    @Override
    public IResultSetBuilder4OrderingDirection<T> order(final int orderSeq) {
        if (builder.resultSetOrdering.containsKey(orderSeq)) {
            final Pair<String, EntityCentreConfig.OrderDirection> pair = builder.resultSetOrdering.get(orderSeq);
            throw new IllegalArgumentException(format("Ordering by property [%s] with sequence [%s] conflicts with ordering by property [%s@%s] (%s), which has the same sequence.",
                    propName, orderSeq, pair.getKey(), builder.getEntityType().getSimpleName(), pair.getValue()));
        }
        this.orderSeq = orderSeq;
        return this;
    }

    @Override
    public IResultSetBuilder4aWidth<T> desc() {
        this.builder.resultSetOrdering.put(orderSeq, pair(propName.get(), OrderDirection.DESC));
        return this;
    }

    @Override
    public IResultSetBuilder4aWidth<T> asc() {
        this.builder.resultSetOrdering.put(orderSeq, pair(propName.get(), OrderDirection.ASC));
        return this;
    }

    @Override
    public IResultSetBuilder4bWordWrap<T> width(final int width) {
        this.width = width;
        this.isFlexible = false;
        return this;
    }

    @Override
    public IResultSetBuilder4bWordWrap<T> minWidth(final int minWidth) {
        this.width = minWidth;
        this.isFlexible = true;
        return this;
    }

    @Override
    public IWithTooltip<T> withWordWrap() {
        this.wordWrap = true;
        return this;
    }

    @Override
    public IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef, final boolean presentByDefault) {
        if (propDef == null) {
            throw new EntityCentreConfigurationException("Custom property should not be null.");
        }

        this.propName = empty();
        this.presentByDefault = presentByDefault;
        this.tooltipProp = empty();
        this.propDef = of(propDef);
        this.orderSeq = null;
        this.entityActionConfig = empty();
        return this;
    }

    @Override
    public IWithSummary<T> withTooltip(final CharSequence propertyName) {
        this.tooltipProp = Optional.ofNullable(propertyName).map(CharSequence::toString);
        return this;
    }

    @Override
    public IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc) {
        this.builder.summaryExpressions.put(propName.get(), mkSummaryPropDef(alias, expression, titleAndDesc, IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE));
        return this;
    }

    @Override
    public IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc, final int precision, final int scale) {
        this.builder.summaryExpressions.put(propName.get(), mkSummaryPropDef(alias, expression, titleAndDesc, precision, scale));
        return this;
    }

    private SummaryPropDef mkSummaryPropDef(final String alias, final String expression, final String titleAndDesc, final int precision, final int scale) {
        if (!propName.isPresent()) {
            throw new IllegalStateException("There is no property to associated the summary expression with. This indicated an out of secuquence call, which is most likely due to a programming mistake.");
        }
        final String[] td = titleAndDesc.split(":");
        final String title = td[0];
        final String desc = td.length > 1 ? td[1] : null;
        return new SummaryPropDef(alias, expression, title, desc, precision, scale);
    }

    @Override
    public IAlsoProp<T> withAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new EntityCentreConfigurationException("Property action configuration should not be null.");
        }

        this.entityActionConfig = of(new EntityMultiActionConfig(SingleActionSelector.class, asList(() -> of(actionConfig))));
        completePropIfNeeded();
        return this;
    }

    @Override
    public IAlsoProp<T> withMultiAction(final EntityMultiActionConfig multiActionConfig) {
        if (multiActionConfig == null) {
            throw new IllegalArgumentException("Property action configuration should not be null.");
        }

        this.entityActionConfig = of(multiActionConfig);
        completePropIfNeeded();
        return this;
    }

    @Override
    public IAlsoProp<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier) {
        if (actionConfigSupplier == null) {
            throw new IllegalArgumentException("Property action configuration supplier should not be null.");
        }

        this.entityActionConfig = of(new EntityMultiActionConfig(SingleActionSelector.class, asList(actionConfigSupplier)));
        completePropIfNeeded();
        return this;
    }

    private Optional<Device> lastResultsetLayoutDevice = empty();
    private Optional<Orientation> lastResultsetLayoutOrientation = empty();

    @Override
    public IExpandedCardLayoutConfig<T> setCollapsedCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalStateException("Resultset card layout requries device and orientation (optional) to be specified.");
        }

        this.lastResultsetLayoutDevice = of(device);
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
        this.lastResultsetLayoutDevice = empty();
        this.lastResultsetLayoutOrientation = empty();
        return this;
    }

    @Override
    public IAlsoSecondaryAction<T> addPrimaryAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new IllegalArgumentException("Primary action configuration should not be null.");
        }

        return addPrimaryAction(new EntityMultiActionConfig(SingleActionSelector.class, listOf(() -> of(actionConfig))));
    }

    @Override
    public IAlsoSecondaryAction<T> addPrimaryAction(final EntityMultiActionConfig actionConfig) {
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

        return addSecondaryAction(new EntityMultiActionConfig(SingleActionSelector.class, Arrays.asList(() -> of(actionConfig))));
    }

    @Override
    public IAlsoSecondaryAction<T> addSecondaryAction(final EntityMultiActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new IllegalArgumentException("Secondary action configuration should not be null.");
        }

        this.builder.resultSetSecondaryEntityActions.add(actionConfig);

        return secondaryActionBuilder;
    }

    @Override
    public IResultSetBuilder9RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler> handler) {
        if (handler == null) {
            throw new EntityCentreConfigurationException("Assignment handler for custom properties should not be null.");
        }

        // complete property registration if there is an oustanding one
        completePropIfNeeded();

        // check if there are any custom properties
        // and indicate to the developer that assignment of an assigner handler
        // is not appropriate in case of no custom properties
        if (builder.resultSetProperties().noneMatch(v -> v.propDef.isPresent())) {
            throw new EntityCentreConfigurationException("Assignment handler for custom properties is meaningless as there are the result set configuration contains no definitions for custom properties.");
        }
        // then if there are custom properties, but all of them have default values already specified, there is no reason to have custom assignment logic
        if (builder.resultSetProperties().noneMatch(v -> v.propDef.isPresent() && !v.propDef.get().value.isPresent())) {
            throw new EntityCentreConfigurationException("Assignment handler for custom properties is meaningless as all custom properties have been provided with default values.");
        }

        this.builder.resultSetCustomPropAssignmentHandlerType = handler;
        return this;
    }

    @Override
    public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<?>> type) {
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
        this.builder.queryEnhancerConfig = pair(type, Optional.ofNullable(contextConfig));
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

    /// Constructs an instance of [EntityCentreConfig.ResultSetProp] if possible and adds it the result set list.
    ///
    private void completePropIfNeeded() {
        // construct and add property to the builder
        if (propName.isPresent()) {
            final ResultSetProp<T> prop = ResultSetProp.propByName(propName.get(), presentByDefault, width, wordWrap, isFlexible, widget, (tooltipProp.isPresent() ? tooltipProp.get() : null), entityActionConfig);
            this.builder.addToResultSet(prop);
        } else if (propDef.isPresent()) {
            final ResultSetProp<T> prop = ResultSetProp.propByDef(propDef.get(), presentByDefault, width, wordWrap, isFlexible, (tooltipProp.isPresent() ? tooltipProp.get() : null), entityActionConfig);
            this.builder.addToResultSet(prop);
        }

        // clear things up for the next property to be added if any
        this.propName = empty();
        this.presentByDefault = true;
        this.tooltipProp = empty();
        this.propDef = empty();
        this.orderSeq = null;
        this.entityActionConfig = empty();
        this.widget = empty();
        this.wordWrap = false;
    }

    @Override
    public ISummaryCardLayout<T> setSummaryCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalStateException("Summary card layout requries device and orientation (optional) to be specified.");
        }

        this.builder.resultsetSummaryCardLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    @Override
    public IAlternativeView<T> withCustomisableLayout() {
        this.builder.insertionPointCustomLayoutEnabled = true;
        return this;
    }

    /// A helper class to assist in name collision resolution.
    ///
    private class ResultSetSecondaryActionsBuilder implements IAlsoSecondaryAction<T>, IInsertionPointWithConfig<T> {

        @Override
        public IResultSetBuilder9RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler> handler) {
            return ResultSetBuilder.this.setCustomPropsValueAssignmentHandler(handler);
        }

        @Override
        public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<?>> type) {
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
        public IInsertionPointConfig0<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
            return ResultSetBuilder.this.addInsertionPoint(actionConfig, whereToInsertView);
        }

        @Override
        public IWithRightSplitterPosition<T> withLeftSplitterPosition(final int percantage) {
            return ResultSetBuilder.this.withLeftSplitterPosition(percantage);
        }

        @Override
        public IInsertionPointsWithCustomLayout<T> withRightSplitterPosition(final int percentage) {
            return ResultSetBuilder.this.withRightSplitterPosition(percentage);
        }

        @Override
        public IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig) {
            return ResultSetBuilder.this.addAlternativeView(actionConfig);
        }

        @Override
        public IAlternativeView<T> withCustomisableLayout() {
            return ResultSetBuilder.this.withCustomisableLayout();
        }
    }

    @Override
    public IInsertionPointConfig0<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        return new InsertionPointConfigBuilder<>(this, actionConfig, whereToInsertView);
    }

    @Override
    public IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig) {
        return new AlternativeViewConfigBuilder<>(this, actionConfig);
    }

    @Override
    public IResultSetBuilder1bCheckbox<T> hideEgi() {
        this.builder.egiHidden = true;
        return this;
    }

    @Override
    public IResultSetBuilder1aEgiIconStyle<T> withGridViewIcon(final String icon) {
        this.builder.gridViewIcon = icon;
        return this;
    }

    @Override
    public IResultSetBuilder1bCheckbox<T> style(final String iconStyle) {
        this.builder.gridViewIconStyle = iconStyle;
        return this;
    }

    @Override
    public IResultSetBuilder1cToolbar<T> hideCheckboxes() {
        this.builder.hideCheckboxes = true;
        return this;
    }

    @Override
    public IResultSetBuilder1dScroll<T> hideToolbar() {
        this.builder.hideToolbar = true;
        return this;
    }

    @Override
    public IResultSetBuilder1dCentreScroll<T> notScrollable() {
        this.builder.scrollConfig = ScrollConfig.configScroll()
                .withFixedCheckboxesAndPrimaryActions()
                .withFixedSecondaryActions()
                .withFixedHeader()
                .withFixedSummary()
                .done();
        return this;
    }

    @Override
    public IResultSetBuilder1gMaxPageCapacity<T> setPageCapacity(final int pageCapacity) {
        this.builder.pageCapacity = pageCapacity;
        return this;
    }

    @Override
    public IResultSetBuilder1fPageCapacity<T> retrieveAll() {
        this.builder.retrieveAll = true;
        return this;
    }

    @Override
    public IResultSetBuilder1hHeaderWrap<T> setMaxPageCapacity(final int maxPageCapacity) {
        this.builder.maxPageCapacity = maxPageCapacity;
        return this;
    }

    @Override
    public IResultSetBuilder1jFitBehaviour<T> setVisibleRowsCount(final int visibleRowsCount) {
        this.builder.visibleRowsCount = visibleRowsCount;
        return this;
    }

    @Override
    public IResultSetBuilder1dCentreScroll<T> withScrollingConfig(final IScrollConfig scrollConfig) {
        this.builder.scrollConfig = scrollConfig;
        return this;
    }

    @Override
    public IResultSetBuilder1eDraggable<T> lockScrollingForInsertionPoints() {
        this.builder.lockScrollingForInsertionPoints = true;
        return this;
    }

    @Override
    public IResultSetBuilder1dScroll<T> setToolbar(final IToolbarConfig toolbar) {
        this.builder.toolbarConfig = toolbar;
        return this;
    }

    @Override
    public IResultSetBuilder1efRetrieveAll<T> draggable() {
        builder.draggable = true;
        return this;
    }

    @Override
    public IResultSetBuilder1jFitBehaviour<T> setHeight(final String height) {
        this.builder.egiHeight = height;
        return this;
    }

    @Override
    public IResultSetBuilder1kRowHeight<T> fitToHeight() {
        this.builder.fitToHeight = true;
        return this;
    }

    @Override
    public IResultSetBuilder2Properties<T> rowHeight(final String rowHeight) {
        this.builder.rowHeight = rowHeight;
        return this;
    }

    @Override
    public IResultSetBuilder2Properties<T> also() {
        completePropIfNeeded();
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IResultSetAutocompleterConfig<T> asAutocompleter() {
        return createAutocompleter(Optional.empty());
    }

    @Override
    public IResultSetAutocompleterConfig<T> asAutocompleter(final Class<? extends AbstractEntity<?>> entityType) {
        return createAutocompleter(Optional.ofNullable(entityType));
    }

    private IResultSetAutocompleterConfig<T> createAutocompleter(Optional<Class<? extends AbstractEntity<?>>> optPropertyType) {
        final Class<? extends AbstractEntity<?>> root = this.builder.getEntityType();
        final String resultPropName = treeName(this.propName.get());
        final String widgetPropName = "".equals(resultPropName) ? AbstractEntity.KEY : resultPropName;
        final EntityAutocompletionWidget editor = WidgetSelector.createAutocompleter(root, widgetPropName, optPropertyType);
        this.widget = of(editor);
        return new ResultSetAutocompleterConfig<>(this, editor);
    }

    public void assignMatcher(final String propName, final Class<? extends IValueMatcherWithContext<T, ?>> matcher) {
        valueMatcherForProps.put(propName, matcher);
    }

    @Override
    public IResultSetBuilder3Ordering<T> skipValidation() {
        this.widget.ifPresent(widget -> widget.skipValidation());
        return this;
    }

    @Override
    public IResultSetBuilder1iVisibleRowsCount<T> wrapHeader(final int headerLineNumber) {
        this.builder.setHeaderLineNumber(headerLineNumber);
        return this;
    }

    public ResultSetBuilder<T> addInsertionPoint(final InsertionPointConfig insertionPoint) {
        this.builder.insertionPointConfigs.add(insertionPoint);
        return this;
    }

    @Override
    public IWithRightSplitterPosition<T> withLeftSplitterPosition(final int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new EntityCentreConfigurationException(ERR_SPLITTER_POSITION_OUT_OF_BOUNDS);
        }
        builder.leftSplitterPosition = Integer.valueOf(percentage);
        return this;
    }

    @Override
    public IInsertionPointsWithCustomLayout<T> withRightSplitterPosition(final int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new EntityCentreConfigurationException(ERR_SPLITTER_POSITION_OUT_OF_BOUNDS);
        }
        if (builder.leftSplitterPosition != null && builder.leftSplitterPosition.intValue() + percentage > 100) {
            throw new EntityCentreConfigurationException(ERR_SPLITTER_OVERLAPPING);
        }
        builder.rightSplitterPosition = Integer.valueOf(percentage);
        return this;
    }
}
