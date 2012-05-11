package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationFactory;

/**
 * Default implementation of the {@link ICentreConfigurationFactory}. It creates multiple analysis entity centre configuration view.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class DefaultCentreConfigurationFactory<T extends AbstractEntity<?>> implements ICentreConfigurationFactory<T> {

    @Override
    public CentreConfigurationView<T, ?> createCentreConfigurationView(final Class<? extends MiWithConfigurationSupport<T>> menuItemType, final String name, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final IEntityMasterManager masterManager, final ICriteriaGenerator criteriaGenerator, final BlockingIndefiniteProgressLayer progressLayer) {
	final CentreConfigurationModel<T> configModel = new CentreConfigurationModel<T>(menuItemType, name, gdtm, entityFactory, masterManager, criteriaGenerator);
	return new MultipleAnalysisEntityCentreConfigurationView<T>(configModel, progressLayer);
    }

}


