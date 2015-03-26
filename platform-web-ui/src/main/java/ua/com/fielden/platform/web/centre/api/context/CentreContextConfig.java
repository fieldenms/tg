package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * An entity centre context configuration to be used for determining the parts of an entity centre, which should be serialised
 * to construct an execution context {@link CentreContext} for execution of an associated action.
 *
 * @author TG Team
 *
 */
public class CentreContextConfig {
    public final boolean withCurrentEtity;
    public final boolean withAllSelectedEntities;
    public final boolean withSelectionCrit;
    public final boolean withMasterEntity;

    public CentreContextConfig(
            final boolean withCurrentEtity,
            final boolean withAllSelectedEntities,
            final boolean withSelectionCrit,
            final boolean withMasterEntity) {
        this.withCurrentEtity = withCurrentEtity;
        this.withAllSelectedEntities = withAllSelectedEntities;
        this.withSelectionCrit = withSelectionCrit;
        this.withMasterEntity = withMasterEntity;
    }

}
