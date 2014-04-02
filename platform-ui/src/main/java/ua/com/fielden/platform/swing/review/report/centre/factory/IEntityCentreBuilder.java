package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

/**
 * A contract for anything that interested in creating entity centre.
 * 
 * @author TG Team
 * 
 */
public interface IEntityCentreBuilder<T extends AbstractEntity<?>> {

    /**
     * Creates new entity centre.
     * 
     * @return
     */
    CentreConfigurationView<T, ?> createEntityCentre(final Class<? extends MiWithConfigurationSupport<T>> menuItemType,//
            final String name,//
            final BlockingIndefiniteProgressLayer progressLayer);
}
