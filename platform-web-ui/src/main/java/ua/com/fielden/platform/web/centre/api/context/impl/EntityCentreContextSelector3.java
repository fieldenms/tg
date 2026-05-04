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

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector3<T extends AbstractEntity<?>> implements IEntityCentreContextSelector3<T> {

    private final boolean withChosenEntity;
    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;


    public EntityCentreContextSelector3(final boolean withChosenEntity, final boolean withSelectionCrit, final boolean withMasterEntity) {
        this.withChosenEntity = withChosenEntity;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
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
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, true, false, withSelectionCrit, withMasterEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, false, true, withSelectionCrit, withMasterEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelector4<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, false, false, withSelectionCrit, true, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, false, false, withSelectionCrit, withMasterEntity, computation, empty(), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithInsertionPointContext(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        if (insertionPointFunctionalType == null) {
            throw new CentreContextConfigException("The insertion point type of the context cannot null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, false, false, withSelectionCrit, withMasterEntity, null, of(linkedMapOf(t2(insertionPointFunctionalType, contextForInsertionPoint))), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithParentCentreContext(final CentreContextConfig parentCentreContext) {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withChosenEntity, false, false, withSelectionCrit, withMasterEntity, null, empty(), ofNullable(parentCentreContext));
    }
}
