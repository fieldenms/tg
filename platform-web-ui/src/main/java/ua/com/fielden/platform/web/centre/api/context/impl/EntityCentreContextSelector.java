package ua.com.fielden.platform.web.centre.api.context.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.*;
import ua.com.fielden.platform.web.centre.api.context.exceptions.CentreContextConfigException;

/// Default implementation for the entity centre context selector API.
///
public class EntityCentreContextSelector<T extends AbstractEntity<?>> implements IEntityCentreContextWithChosenEntitySelector<T>, IEntityCentreContextSelectorAfterChosenEntity<T> {

    private final boolean withChosenEntity;

    public static <T extends AbstractEntity<?>> IEntityCentreContextWithChosenEntitySelector<T> context() {
        return new EntityCentreContextSelector<>(false);
    }

    private EntityCentreContextSelector(final boolean withChosenEntity) {
        this.withChosenEntity = withChosenEntity;
    }

    @Override
    public IEntityCentreContextSelector3<T> withSelectionCrit() {
        return new EntityCentreContextSelector3<>(true, false, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector5<T> withMasterEntity() {
        return new EntityCentreContextSelector5<>(false, true, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector0<T> withCurrentEntity() {
        return new EntityCentreContextSelector0<>(true, false, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelector0<T> withSelectedEntities() {
        return new EntityCentreContextSelector0<>(false, true, withChosenEntity);
    }

    @Override
    public IEntityCentreContextSelectorAfterChosenEntity<T> withChosenEntity() {
        return new EntityCentreContextSelector<>(true);
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(false, false, false, false, withChosenEntity, null, empty(), empty());
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, false, false, withChosenEntity, computation, empty(), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithInsertionPointContext(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        if (insertionPointFunctionalType == null) {
            throw new CentreContextConfigException("The insertion point type of the context cannot null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, false, false, withChosenEntity, null, of(linkedMapOf(t2(insertionPointFunctionalType, contextForInsertionPoint))), empty());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWithParentCentreContext(final CentreContextConfig parentCentreContext) {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(false, false, false, false, withChosenEntity, null, empty(), ofNullable(parentCentreContext));
    }

}
