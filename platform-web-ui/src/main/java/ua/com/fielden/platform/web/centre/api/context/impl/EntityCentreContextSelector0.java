package ua.com.fielden.platform.web.centre.api.context.impl;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector0;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector2;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;
import ua.com.fielden.platform.web.centre.api.context.IExtendedEntityCentreContextWithFunctionSelector;
import ua.com.fielden.platform.web.centre.api.context.exceptions.CentreContextConfigException;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector0<T extends AbstractEntity<?>> implements IEntityCentreContextSelector0<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;


    public EntityCentreContextSelector0(final boolean withCurrentEntity, final boolean withAllSelectedEntities) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                false,
                false,
                null,
                new LinkedHashMap<>()
               );
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, true, false, null, new LinkedHashMap<>());
    }

    @Override
    public IEntityCentreContextSelector2<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, true, null, new LinkedHashMap<>());
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, false, computation, new LinkedHashMap<>());
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWith(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        if (insertionPointFunctionalType == null) {
            throw new CentreContextConfigException("The insertion point type of the context cannot null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, false, false, null, linkedMapOf(t2(insertionPointFunctionalType, contextForInsertionPoint)));
    }


}
