package ua.com.fielden.platform.web.centre.api.context.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector2;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector4;
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
class EntityCentreContextSelector1_2_4_extender_function_done<T extends AbstractEntity<?>> implements IEntityCentreContextSelector1<T>, IEntityCentreContextSelector2<T>, IEntityCentreContextSelector4<T>, IEntityCentreContextSelectorDone<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;
    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;
    private final Map<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>, CentreContextConfig> extensions = new LinkedHashMap<>();
    private final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation;

    public EntityCentreContextSelector1_2_4_extender_function_done(
            final boolean withCurrentEntity, final boolean withAllSelectedEntities,
            final boolean withSelectionCrit, final boolean withMasterEntity,
            final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation,
            final Map<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>, CentreContextConfig> extensions) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.computation = computation;
        this.extensions.putAll(extensions);
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                withSelectionCrit,
                withMasterEntity,
                computation,
                extensions
               );
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, withSelectionCrit, true, computation, extensions);
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, true, withMasterEntity, computation, extensions);
    }

    @Override
    public IEntityCentreContextSelector1<T> withCurrentEntity() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(true, withAllSelectedEntities, withSelectionCrit, withMasterEntity, computation, extensions);
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, true, withSelectionCrit, withMasterEntity, computation, extensions);
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        if (computation == null) {
            throw new CentreContextConfigException("The computational component of the context cannot be set as value null.");
        }
        return new EntityCentreContextSelector1_2_4_extender_function_done<T>(withCurrentEntity, withAllSelectedEntities, withSelectionCrit, withMasterEntity, computation, extensions);
    }

    @Override
    public IExtendedEntityCentreContextWithFunctionSelector<T> extendWith(final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> insertionPointFunctionalType, final CentreContextConfig contextForInsertionPoint) {
        extensions.put(insertionPointFunctionalType, contextForInsertionPoint);
        return this;
    }


}
