package ua.com.fielden.platform.web.centre.api.context;

import java.util.function.Function;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * An entity centre context configuration to be used for determining the parts of an entity centre, which should be serialised to construct an execution context
 * {@link CentreContext} for execution of an associated action.
 *
 * @author TG Team
 *
 */
public final class CentreContextConfig {
    public final boolean withCurrentEtity;
    public final boolean withAllSelectedEntities;
    public final boolean withSelectionCrit;
    public final boolean withMasterEntity;
    public final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation;

    public CentreContextConfig(
            final boolean withCurrentEtity,
            final boolean withAllSelectedEntities,
            final boolean withSelectionCrit,
            final boolean withMasterEntity,
            final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation
            ) {
        this.withCurrentEtity = withCurrentEtity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.computation = computation;
    }

    public final boolean withComputation() {
        return this.computation != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (withAllSelectedEntities ? 1231 : 1237);
        result = prime * result + (withCurrentEtity ? 1231 : 1237);
        result = prime * result + (withMasterEntity ? 1231 : 1237);
        result = prime * result + (withSelectionCrit ? 1231 : 1237);
        // WARN: please note that CentreContextConfig with non-identical (in sense of object references) 'computation' functions will not be equal and will not produce the same hashCode.
        result = prime * result + ((computation == null) ? 0 : computation.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CentreContextConfig)) {
            return false;
        }

        final CentreContextConfig that = (CentreContextConfig) obj;

        // WARN: please note that CentreContextConfig with non-identical (in sense of object references) 'computation' functions will not be equal and will not produce the same hashCode.
        if (computation == null) {
            if (that.computation != null) {
                return false;
            }
        } else if (!computation.equals(that.computation)) {
            return false;
        }
        return (this.withAllSelectedEntities == that.withAllSelectedEntities) &&
                (this.withCurrentEtity == that.withCurrentEtity) &&
                (this.withMasterEntity == that.withMasterEntity)  &&
                (this.withSelectionCrit == that.withSelectionCrit);
    }

}
