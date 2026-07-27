package ua.com.fielden.platform.web.centre.api.context.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.*;
import ua.com.fielden.platform.web.centre.api.context.exceptions.CentreContextConfigException;

import java.util.function.BiFunction;

import static java.util.Optional.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

/// Default implementation for the entity centre context selector API.
///
class EntityCentreContextSelector0<T extends AbstractEntity<?>> implements IEntityCentreContextSelector0<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;
    private final boolean withChosenEntity;


    public EntityCentreContextSelector0(final boolean withCurrentEntity, final boolean withAllSelectedEntities, final boolean withChosenEntity) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withChosenEntity = withChosenEntity;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                false,
                false,
                withChosenEntity,
                null,
                empty(),
                empty()
               );
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, true, false, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelector2<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, true, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, false, withChosenEntity, computation, empty(), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithInsertionPointContext(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        if (insertionPointFunctionalType == null) {
            throw new CentreContextConfigException("The insertion point type of the context cannot null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, false, withChosenEntity, null, of(linkedMapOf(t2(insertionPointFunctionalType, contextForInsertionPoint))), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithParentCentreContext(final CentreContextConfig parentCentreContext) {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, false, withChosenEntity, null, empty(), ofNullable(parentCentreContext));
    }


}
