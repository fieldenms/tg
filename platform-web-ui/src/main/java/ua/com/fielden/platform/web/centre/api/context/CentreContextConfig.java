package ua.com.fielden.platform.web.centre.api.context;

import java.util.Optional;
import java.util.function.BiFunction;
import ua.com.fielden.platform.entity.AbstractEntity;
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
    public final Boolean withCurrentEtity;
    public final Boolean withAllSelectedEntities;
    public final Boolean withSelectionCrit;
    public final Boolean withMasterEntity;
    public final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation;

    public CentreContextConfig(
            final boolean withCurrentEtity,
            final boolean withAllSelectedEntities,
            final boolean withSelectionCrit,
            final boolean withMasterEntity,
            final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation
            ) {
        this.withCurrentEtity = withCurrentEtity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
        this.computation = Optional.ofNullable(computation);
    }

    public final boolean withComputation() {
        return this.computation.isPresent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + withAllSelectedEntities.hashCode();
        result = prime * result + withCurrentEtity.hashCode();
        result = prime * result + withMasterEntity.hashCode();
        result = prime * result + withSelectionCrit.hashCode();
        // WARN: CentreContextConfig instances with referentially different 'computation' values yield different hash codes.
        result = prime * result + computation.hashCode();
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

        // WARN: CentreContextConfig instances with referentially different 'computation' values are considered different
        return (this.withAllSelectedEntities == that.withAllSelectedEntities) &&
                (this.withCurrentEtity == that.withCurrentEtity) &&
                (this.withMasterEntity == that.withMasterEntity)  &&
                (this.withSelectionCrit == that.withSelectionCrit) &&
                computation.equals(that.computation);
    }

}
