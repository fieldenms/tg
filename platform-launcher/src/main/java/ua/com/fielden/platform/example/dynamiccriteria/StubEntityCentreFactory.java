package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.MultipleAnalysisEntityCentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationFactory;

public class StubEntityCentreFactory<T extends AbstractEntity<?>> implements ICentreConfigurationFactory<T> {

    @SuppressWarnings("unchecked")
    @Override
    public CentreConfigurationView<T, ?> createCentreConfigurationView(final Class<? extends MiWithConfigurationSupport<T>> menuItemType, final String name, final IGlobalDomainTreeManager gdtm, final EntityFactory entityFactory, final IEntityMasterManager masterManager, final ICriteriaGenerator criteriaGenerator, final BlockingIndefiniteProgressLayer progressLayer) {
	final Class<T> entityType = (Class<T>)AnnotationReflector.getAnnotation(EntityType.class, menuItemType).value();
	final CentreConfigurationModel<T> configModel = new CentreConfigurationModel<T>(entityType, name, gdtm, entityFactory, masterManager, criteriaGenerator);
	return new MultipleAnalysisEntityCentreConfigurationView<T>(configModel, progressLayer);
    }

}
