package ua.com.fielden.platform.swing.review.report.interfaces;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

/**
 * A Factory that allows one to create entity centre.
 * 
 * @author TG Team
 *
 */
public interface ICentreConfigurationFactory<T extends AbstractEntity<?>> {

    /**
     * Creates specific {@link CentreConfigurationView} instance.
     * 
     * @param menuItemType
     * @param name
     * @param centreFactory
     * @param gdtm
     * @param entityFactory
     * @param masterManager
     * @param criteriaGenerator
     * @param progressLayer
     * @return
     */
    CentreConfigurationView<T, ?> createCentreConfigurationView(final Class<? extends MiWithConfigurationSupport<T>> menuItemType,//
	    final String name,//
	    final IGlobalDomainTreeManager gdtm,//
	    final EntityFactory entityFactory,//
	    final IEntityMasterManager masterManager,//
	    final ICriteriaGenerator criteriaGenerator,//
	    final BlockingIndefiniteProgressLayer progressLayer);
}
