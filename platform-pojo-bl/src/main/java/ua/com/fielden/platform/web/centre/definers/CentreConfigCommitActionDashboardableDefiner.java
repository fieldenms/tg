package ua.com.fielden.platform.web.centre.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.web.centre.AbstractCentreConfigCommitAction;

/**
 * Definer that assigns {@link AbstractCentreConfigCommitAction#getDashboardRefreshFrequency()} meta-data for configuration being made dashboardable
 * or non-dashboardable.
 * 
 * @author TG Team
 *
 */
public class CentreConfigCommitActionDashboardableDefiner implements IAfterChangeEventHandler<Boolean> {
    
    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean dashboardable) {
        property.getEntity().getPropertyIfNotProxy("dashboardRefreshFrequency").ifPresent(prop -> {
            prop.setRequired(false);
            prop.setValue(null);
            prop.setRequired(dashboardable);
            // TODO uncomment when dashboardable functionality will be available: prop.setEditable(dashboardable);
        });
    }
    
}