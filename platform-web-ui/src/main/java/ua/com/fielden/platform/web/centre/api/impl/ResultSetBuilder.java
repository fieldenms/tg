package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.extra_fetch.IExtraFetchProviderSetter;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.query_enhancer.IQueryEnhancerSetter;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoProp;
import ua.com.fielden.platform.web.centre.api.resultset.IAlsoSecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1OrderingDirection;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2WithPropAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder4SecondaryAction;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder6RenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;

/**
* A package private helper class to decompose the task of implementing the Entity Centre DSL.
* It has direct access to protected fields in {@link EntityCentreBuilder}.
*
* @author TG Team
*
* @param <T>
*/
class ResultSetBuilder<T extends AbstractEntity<?>> implements IResultSetBuilder<T>, IResultSetBuilder0Ordering<T>, IResultSetBuilder1OrderingDirection<T>, IResultSetBuilder4SecondaryAction<T> {

    private final EntityCentreBuilder<T> builder;
    private final ResultSetSecondaryActionsBuilder secondaryActionBuilder = new ResultSetSecondaryActionsBuilder();

    protected Optional<String> propName = Optional.empty();
    protected Optional<PropDef<?>> propDef = Optional.empty();
    private EntityActionConfig entityActionConfig;
    private Integer orderSeq;

    public ResultSetBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IResultSetBuilder0Ordering<T> addProp(final String propName) {
        if (!EntityUtils.isProperty(this.builder.getEntityType(), propName)) {
            throw new IllegalArgumentException(String.format("Provided value '%s' is not a valid property expression for entity '%s'", propName, builder.getEntityType().getSimpleName()));
        }

        this.propName = Optional.of(propName);
        this.propDef = Optional.empty();
        this.orderSeq = null;
        this.entityActionConfig = null;
        return this;
    }

    @Override
    public IResultSetBuilder1OrderingDirection<T> order(final int orderSeq) {
        this.orderSeq = orderSeq;
        return this;
    }

    @Override
    public IResultSetBuilder2WithPropAction<T> desc() {
        this.builder.resultSetOrdering.put(orderSeq, new Pair<>(propName.get(), OrderDirection.DESC));
        return this;
    }

    @Override
    public IResultSetBuilder2WithPropAction<T> asc() {
        this.builder.resultSetOrdering.put(orderSeq, new Pair<>(propName.get(), OrderDirection.ASC));
        return this;
    }

    @Override
    public IResultSetBuilder2WithPropAction<T> addProp(final PropDef<?> propDef) {
        this.propName = Optional.empty();
        this.propDef = Optional.of(propDef);
        this.orderSeq = null;
        this.entityActionConfig = null;
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
    public IResultSetBuilder<T> also() {
        completePropIfNeeded();
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
    public IResultSetBuilder6RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<T>> handler) {
        completePropIfNeeded();
        this.builder.resultSetCustomPropAssignmentHandlerType = handler;
        return this;
    }

    @Override
    public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<T, ?>> type) {
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
    public IEcbCompletion<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
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
            final ResultSetProp prop = ResultSetProp.propByName(propName.get(), entityActionConfig);
            this.builder.resultSetProperties.add(prop);
        } else if (propDef.isPresent()) {
            final ResultSetProp prop = ResultSetProp.propByDef(propDef.get(), entityActionConfig);
            this.builder.resultSetProperties.add(prop);
        }

        // clear things up for the next property to be added if any
        this.propName = Optional.empty();
        this.propDef = Optional.empty();
        this.orderSeq = null;
        this.entityActionConfig = null;
    }

    /**
     * A helper class to assist in name collision resolution.
     */
    private class ResultSetSecondaryActionsBuilder implements IAlsoSecondaryAction<T> {

        @Override
        public IResultSetBuilder6RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<T>> handler) {
            return ResultSetBuilder.this.setCustomPropsValueAssignmentHandler(handler);
        }

        @Override
        public IQueryEnhancerSetter<T> setRenderingCustomiser(final Class<? extends IRenderingCustomiser<T, ?>> type) {
            return ResultSetBuilder.this.setRenderingCustomiser(type);
        }

        @Override
        public IExtraFetchProviderSetter<T> setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig) {
            return ResultSetBuilder.this.setQueryEnhancer(type, contextConfig);
        }

        @Override
        public IEcbCompletion<T> setFetchProvider(final IFetchProvider<T> fetchProvider) {
            return ResultSetBuilder.this.setFetchProvider(fetchProvider);
        }

        @Override
        public EntityCentreConfig<T> build() {
            return ResultSetBuilder.this.build();
        }

        @Override
        public IResultSetBuilder4SecondaryAction<T> also() {
            return ResultSetBuilder.this;
        }

    }
}
