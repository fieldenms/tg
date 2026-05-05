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
class EntityCentreContextSelector5<T extends AbstractEntity<?>> implements IEntityCentreContextSelector5<T> {

    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;
    private final boolean withChosenEntity;


    public EntityCentreContextSelector5(final boolean withSelectionCrit, final boolean withMasterEntity, final boolean withChosenEntity) {
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.withChosenEntity = withChosenEntity;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                false,
                false,
                withSelectionCrit,
                withMasterEntity,
                withChosenEntity,
                null,
                empty(),
                empty()
               );
    }

    @Override
    public IEntityCentreContextSelector1<T> withCurrentEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(true, false, withSelectionCrit, withMasterEntity, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, true, withSelectionCrit, withMasterEntity, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelector4<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, true, withMasterEntity, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, withSelectionCrit, withMasterEntity, withChosenEntity, computation, empty(), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithInsertionPointContext(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        if (insertionPointFunctionalType == null) {
            throw new CentreContextConfigException("The insertion point type of the context cannot null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, withSelectionCrit, withMasterEntity, withChosenEntity, null, of(linkedMapOf(t2(insertionPointFunctionalType, contextForInsertionPoint))), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithParentCentreContext(final CentreContextConfig parentCentreContext) {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, withSelectionCrit, withMasterEntity, withChosenEntity, null, empty(), ofNullable(parentCentreContext));
    }

}
