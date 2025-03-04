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

/**
 * This class implements the DSL for configuring dynamic properties and their actions. It intersects with most of the functionality in {@link ResultSetBuilder}, but it had to be created, because {@link IResultSetBuilderDynamicPropsAction} and {@link IResultSetBuilderAlsoDynamicProps}
 * can not be implemented by {@link ResultSetBuilder} due to the fact that methods {@link IResultSetBuilderDynamicPropsAction#withAction(EntityActionConfig)} and {@link IResultSetBuilderDynamicPropsAction#withActionSupplier(Supplier)} have the same signature as methods declared in {@link IResultSetBuilder5WithPropAction}, implemented by {@link ResultSetBuilder}.
 * <p>
 * However, to improve the reuse, all common with {@link ResultSetBuilder} methods are implemented by means of delegation to an instance of {@link ResultSetBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
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
    public IResultSetBuilderAlsoDynamicProps<T> withAction(final EntityActionConfig actionConfig) {
        if (actionConfig == null) {
            throw new CentreConfigException("Property action configuration should not be null.");
        }

        resultSetProp.setPropAction(of(new EntityMultiActionConfig(SingleActionSelector.class, asList(() -> of(actionConfig)))));
        return this;
    }

    @Override
    public IResultSetBuilderAlsoDynamicProps<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier) {
        if (actionConfigSupplier == null) {
            throw new CentreConfigException("Property action configuration supplier should not be null.");
        }

        resultSetProp.setPropAction(of(new EntityMultiActionConfig(SingleActionSelector.class, asList(actionConfigSupplier))));
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
