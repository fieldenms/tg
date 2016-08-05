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
    public final Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computation;

    public CentreContextConfig(
            final boolean withCurrentEtity,
            final boolean withAllSelectedEntities,
            final boolean withSelectionCrit,
            final boolean withMasterEntity,
            final Function<? extends AbstractFunctionalEntityWithCentreContext<?>, Object> computedFunction
            ) {
        this.withCurrentEtity = withCurrentEtity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.computation = computedFunction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((computation == null) ? 0 : computation.hashCode());
        result = prime * result + (withAllSelectedEntities ? 1231 : 1237);
        result = prime * result + (withCurrentEtity ? 1231 : 1237);
        result = prime * result + (withMasterEntity ? 1231 : 1237);
        result = prime * result + (withSelectionCrit ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CentreContextConfig other = (CentreContextConfig) obj;
        if (computation == null) {
            if (other.computation != null) {
                return false;
            }
        } else if (!computation.equals(other.computation)) { // TODO Function type does not redeclare equals and hashCode. Please, investigate implications.
            return false;
        }
        if (withAllSelectedEntities != other.withAllSelectedEntities) {
            return false;
        }
        if (withCurrentEtity != other.withCurrentEtity) {
            return false;
        }
        if (withMasterEntity != other.withMasterEntity) {
            return false;
        }
        if (withSelectionCrit != other.withSelectionCrit) {
            return false;
        }
        return true;
    }

    public final boolean withComputation() {
        return this.computation != null;
    }

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + (withAllSelectedEntities ? 1231 : 1237);
//        result = prime * result + (withCurrentEtity ? 1231 : 1237);
//        result = prime * result + (withMasterEntity ? 1231 : 1237);
//        result = prime * result + (withSelectionCrit ? 1231 : 1237);
//        result = prime * result + (withComputedFunction ? 1231 : 1237);
//        return result;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (!(obj instanceof CentreContextConfig)) {
//            return false;
//        }
//
//        final CentreContextConfig that = (CentreContextConfig) obj;
//
//        return  (this.withAllSelectedEntities == that.withAllSelectedEntities) &&
//                (this.withCurrentEtity == that.withCurrentEtity) &&
//                (this.withMasterEntity == that.withMasterEntity) &&
//                (this.withSelectionCrit == that.withSelectionCrit) &&
//                (this.withComputedFunction == that.withComputedFunction);
//    }

}
