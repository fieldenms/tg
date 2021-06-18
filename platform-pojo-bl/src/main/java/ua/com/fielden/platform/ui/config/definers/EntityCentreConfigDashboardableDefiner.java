package ua.com.fielden.platform.ui.config.definers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.utils.IDates;

/**
 * Definer that assigns {@link EntityCentreConfig#getDashboardableDate()} for configuration being made dashboardable
 * and removes that date for configuration being made non-dashboardable.
 * 
 * @author TG Team
 *
 */
public class EntityCentreConfigDashboardableDefiner implements IAfterChangeEventHandler<Boolean> {
    private final IDates dates;
    
    @Inject
    protected EntityCentreConfigDashboardableDefiner(final IDates dates) {
        this.dates = dates;
    }
    
    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean dashboardable) {
        final EntityCentreConfig entity = property.getEntity();
        if (!entity.isInitialising()) {
            entity.setDashboardableDate(dashboardable ? dates.now().toDate() : null);
        }
    }
    
}