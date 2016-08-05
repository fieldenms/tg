package ua.com.fielden.platform.web.centre.api.context.impl;

import java.util.function.Function;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector1;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector2;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelector4;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorFunction;

/**
 * Default implementation for the entity centre context selector API.
 *
 * @author TG Team
 *
 * @param <T>
 */
class EntityCentreContextSelector1_2_4_function_done<T extends AbstractEntity<?>> implements IEntityCentreContextSelector1<T>, IEntityCentreContextSelector2<T>, IEntityCentreContextSelector4<T>, IEntityCentreContextSelectorDone<T> {

    private final boolean withCurrentEntity;
    private final boolean withAllSelectedEntities;
    private final boolean withSelectionCrit;
    private final boolean withMasterEntity;
    private final Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computation;

    public EntityCentreContextSelector1_2_4_function_done(
            final boolean withCurrentEntity, final boolean withAllSelectedEntities,
            final boolean withSelectionCrit, final boolean withMasterEntity,
            final Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
        this.withCurrentEntity = withCurrentEntity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.computation = computation;
    }

    @Override
    public CentreContextConfig build() {
        return new CentreContextConfig(
                withCurrentEntity,
                withAllSelectedEntities,
                withSelectionCrit,
                withMasterEntity,
                computation
               );
    }

    @Override
    public IEntityCentreContextSelectorFunction<T> withMasterEntity() {
        return new EntityCentreContextSelector1_2_4_function_done<T>(withCurrentEntity, withAllSelectedEntities, withSelectionCrit, true, computation);
    }

    @Override
    public IEntityCentreContextSelectorFunction<T> withSelectionCrit() {
        return new EntityCentreContextSelector1_2_4_function_done<T>(withCurrentEntity, withAllSelectedEntities, true, withMasterEntity, computation);
    }

    @Override
    public IEntityCentreContextSelector1<T> withCurrentEntity() {
        return new EntityCentreContextSelector1_2_4_function_done<T>(true, withAllSelectedEntities, withSelectionCrit, withMasterEntity, computation);
    }

    @Override
    public IEntityCentreContextSelector1<T> withSelectedEntities() {
        return new EntityCentreContextSelector1_2_4_function_done<T>(withCurrentEntity, true, withSelectionCrit, withMasterEntity, computation);
    }

    @Override
    public IEntityCentreContextSelectorDone<T> withComputation(final Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
        return new EntityCentreContextSelector1_2_4_function_done<T>(withCurrentEntity, withAllSelectedEntities, withSelectionCrit, withMasterEntity, computation);
    }


}
