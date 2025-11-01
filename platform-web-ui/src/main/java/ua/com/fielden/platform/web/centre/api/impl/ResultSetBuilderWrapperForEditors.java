package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.IWithRightSplitterPosition;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewPreferred;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointConfig0;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointWithConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointsWithCustomLayout;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.*;
import ua.com.fielden.platform.web.centre.api.resultset.layout.IExpandedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

import java.util.Optional;
import java.util.function.Supplier;

public class ResultSetBuilderWrapperForEditors<T extends AbstractEntity<?>> implements IResultSetBuilder3Ordering<T>, IInsertionPointWithConfig<T> {

    public final ResultSetBuilder<T> builder;

    public ResultSetBuilderWrapperForEditors(final ResultSetBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IResultSetBuilder4OrderingDirection<T> order(final int orderSeq) {
        return builder.order(orderSeq);
    }

    @Override
    public IResultSetBuilder4bWordWrap<T> width(final int width) {
        return builder.width(width);
    }

    @Override
    public IResultSetBuilder4bWordWrap<T> minWidth(final int minWidth) {
        return builder.minWidth(minWidth);
    }

    @Override
    public IWithTooltip<T> withWordWrap() {
        return builder.withWordWrap();
    }

    @Override
    public IWithSummary<T> withTooltip(final CharSequence propertyName) {
        return builder.withTooltip(propertyName);
    }

    @Override
    public IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc) {
        return builder.withSummary(alias, expression, titleAndDesc);
    }

    @Override
    public IWithSummary<T> withSummary(final String alias, final String expression, final String titleAndDesc, final int precision, final int scale) {
        builder.withSummary(alias, expression, titleAndDesc, precision, scale);
        return builder;
    }

    @Override
    public IAlsoProp<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier) {
        return builder.withActionSupplier(actionConfigSupplier);
    }

    @Override
    public IResultSetBuilder2Properties<T> also() {
        return builder.also();
    }

    @Override
    public IExpandedCardLayoutConfig<T> setCollapsedCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        return builder.setCollapsedCardLayoutFor(device, orientation, flexString);
    }

    @Override
    public IAlsoSecondaryAction<T> addPrimaryAction(final EntityActionConfig actionConfig) {
        return builder.addPrimaryAction(actionConfig);
    }

    @Override
    public IAlsoSecondaryAction<T> addPrimaryAction(final EntityMultiActionConfig actionConfig) {
        return builder.addPrimaryAction(actionConfig);
    }

    @Override
    public IResultSetBuilder9RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler> handler) {
        return builder.setCustomPropsValueAssignmentHandler(handler);
    }

    @Override
    public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<?>> type) {
        return builder.setRenderingCustomiser(type);
    }

    @Override
    public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig) {
        return builder.setQueryEnhancer(type, contextConfig);
    }

    @Override
    public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type) {
        return builder.setQueryEnhancer(type);
    }

    @Override
    public ISummaryCardLayout<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
        return builder.setFetchProvider(fetchProvider);
    }

    @Override
    public ISummaryCardLayout<T> setSummaryCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        return builder.setSummaryCardLayoutFor(device, orientation, flexString);
    }

    @Override
    public IInsertionPointConfig0<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        return builder.addInsertionPoint(actionConfig, whereToInsertView);
    }

    @Override
    public IAlternativeViewPreferred<T> addAlternativeView(final EntityActionConfig actionConfig) {
        return builder.addAlternativeView(actionConfig);
    }

    @Override
    public EntityCentreConfig<T> build() {
        return builder.build();
    }

    @Override
    public IAlsoProp<T> withAction(final EntityActionConfig actionConfig) {
        return builder.withAction(actionConfig);
    }

    @Override
    public IAlsoProp<T> withMultiAction(final EntityMultiActionConfig multiActionConfig) {
        return builder.withMultiAction(multiActionConfig);
    }

    @Override
    public IWithRightSplitterPosition<T> withLeftSplitterPosition(final int percentage) {
        return builder.withLeftSplitterPosition(percentage);
    }

    @Override
    public IInsertionPointsWithCustomLayout<T> withRightSplitterPosition(final int percentage) {
        return builder.withRightSplitterPosition(percentage);
    }

    @Override
    public IAlternativeView<T> withCustomisableLayout() {
        return builder.withCustomisableLayout();
    }
}
