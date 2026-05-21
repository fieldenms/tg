package ua.com.fielden.platform.web.centre.api.impl;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.IWithRightSplitterPosition;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.SingleActionSelector;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeViewPreferred;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.exceptions.CentreConfigException;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointConfig0;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointWithConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointsWithCustomLayout;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder5WithPropAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder9RenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderAlsoDynamicProps;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderDynamicProps;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderDynamicPropsAction;
import ua.com.fielden.platform.web.centre.api.resultset.layout.IExpandedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/// Implements the DSL for configuring dynamic-column properties and their actions on an entity centre.
/// Intersects with most of the functionality in [ResultSetBuilder], but is a separate class because [IResultSetBuilderDynamicPropsAction] and [IResultSetBuilderAlsoDynamicProps] cannot be implemented by [ResultSetBuilder] — the methods `withAction(EntityActionConfig)` / `withActionSupplier(Supplier)` declared by [IResultSetBuilderDynamicPropsAction] share signatures with [IResultSetBuilder5WithPropAction] which [ResultSetBuilder] already implements.
///
/// All methods that overlap with [ResultSetBuilder] are implemented here by delegation to an instance of [ResultSetBuilder] to keep behaviour aligned.
/// Per-column property actions accumulate into [ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp#propActions] in DSL declaration order; repeated calls to `withAction`, `withMultiAction`, and `withActionSupplier` append new action groups to that list.
///
public class ResultSetDynamicPropertyBuilder<T extends AbstractEntity<?>> implements IResultSetBuilderDynamicPropsAction<T>, IResultSetBuilderAlsoDynamicProps<T>, IInsertionPointWithConfig<T> {

    private final ResultSetBuilder<T> builder;
    private final ResultSetProp<T> resultSetProp;

    public ResultSetDynamicPropertyBuilder(final ResultSetBuilder<T> builder, final ResultSetProp<T> resultSetProp) {
        this.builder = builder;
        this.resultSetProp = resultSetProp;
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
    public IResultSetBuilderDynamicProps<T> also() {
        return builder;
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> withAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new CentreConfigException("Property action configuration should not be null.");
        }

        resultSetProp.getPropActions().add(new EntityMultiActionConfig(SingleActionSelector.class, asList(() -> of(actionConfig))));
        return this;
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> withMultiAction(final EntityMultiActionConfig multiActionConfig) {
        if (multiActionConfig == null) {
            throw new CentreConfigException("Property action configuration should not be null.");
        }

        resultSetProp.getPropActions().add(multiActionConfig);
        return this;
    }

    @Override
    public IResultSetBuilderDynamicPropsAction<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier) {
        if (actionConfigSupplier == null) {
            throw new CentreConfigException("Property action configuration supplier should not be null.");
        }

        resultSetProp.getPropActions().add(new EntityMultiActionConfig(SingleActionSelector.class, asList(actionConfigSupplier)));
        return this;
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
