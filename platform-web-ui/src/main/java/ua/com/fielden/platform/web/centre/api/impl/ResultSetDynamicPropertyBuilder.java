package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointPreferred;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder9RenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderAlsoDynamicProps;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderDynamicProps;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilderDynamicPropsAction;
import ua.com.fielden.platform.web.centre.api.resultset.layout.IExpandedCardLayoutConfig;
import ua.com.fielden.platform.web.centre.api.resultset.summary.ISummaryCardLayout;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

public class ResultSetDynamicPropertyBuilder<T extends AbstractEntity<?>> implements IResultSetBuilderDynamicPropsAction<T>, IResultSetBuilderAlsoDynamicProps<T> {

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
    public IInsertionPointPreferred<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView) {
        return builder.addInsertionPoint(actionConfig, whereToInsertView);
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
            throw new IllegalArgumentException("Property action configuration should not be null.");
        }

        resultSetProp.setPropAction(() -> Optional.of(actionConfig));
        return this;
    }

    @Override
    public IResultSetBuilderAlsoDynamicProps<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier) {
        if (actionConfigSupplier == null) {
            throw new IllegalArgumentException("Property action configuration supplier should not be null.");
        }

        resultSetProp.setPropAction(actionConfigSupplier);
        return this;
    }

}
