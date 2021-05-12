package ua.com.fielden.platform.ui.config.definers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.web.centre.AbstractCentreConfigCommitAction;

public class CentreConfigCommitActionDashboardableDefiner implements IAfterChangeEventHandler<Boolean> {
    
    @Inject
    protected CentreConfigCommitActionDashboardableDefiner() {
    }
    
    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean dashboardable) {
        final AbstractCentreConfigCommitAction entity = property.getEntity();
        if (!entity.isInitialising()) {
            entity.getPropertyIfNotProxy("dashboardRefreshFrequency").map(prop -> prop.setRequired(dashboardable));
        }
    }
    
}