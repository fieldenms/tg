package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder4SecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder6RenderingCustomiser;

class ResultSetSecondaryActionsBuilder<T extends AbstractEntity<?>> implements IAlsoSecondaryAction<T> {

    private final ResultSetBuilder<T> resultSetBuilder;

    public ResultSetSecondaryActionsBuilder(final ResultSetBuilder<T> resultSetBuilder) {
        this.resultSetBuilder = resultSetBuilder;
    }

    @Override
    public IResultSetBuilder6RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<T>> handler) {
        return resultSetBuilder.setCustomPropsValueAssignmentHandler(handler);
    }

    @Override
    public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<T, ?>> type) {
        return resultSetBuilder.setRenderingCustomiser(type);
    }

    @Override
    public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig) {
        return resultSetBuilder.setQueryEnhancer(type, contextConfig);
    }

    @Override
    public IEcbCompletion<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
        return resultSetBuilder.setFetchProvider(fetchProvider);
    }

    @Override
    public EntityCentreConfig<T> build() {
        return resultSetBuilder.build();
    }

    @Override
    public IResultSetBuilder4SecondaryAction<T> also() {
        return resultSetBuilder;
    }

}
