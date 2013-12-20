package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.MultipleAnalysisEntityCentreConfigurationView;

public class DefaultEntityCentreFactory<T extends AbstractEntity<?>> implements IEntityCentreFactory<T> {

    @Override
    public CentreConfigurationView<T, ?> createEntityCentre(//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemType, //
	    final String name, //
	    final IAnalysisBuilder<T> analysisBuilder, //
	    final IGlobalDomainTreeManager gdtm, //
	    final EntityFactory entityFactory, //
	    final IEntityMasterManager masterManager, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	final CentreConfigurationModel<T> configModel = new CentreConfigurationModel<T>(menuItemType, name, analysisBuilder, gdtm, entityFactory, masterManager, criteriaGenerator);
	return new MultipleAnalysisEntityCentreConfigurationView<T>(configModel, progressLayer);
    }

}
