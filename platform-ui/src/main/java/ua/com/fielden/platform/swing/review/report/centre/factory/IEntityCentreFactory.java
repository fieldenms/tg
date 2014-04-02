package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

/**
 * Factory for entity centre.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <C>
 */
public interface IEntityCentreFactory<T extends AbstractEntity<?>> {

    /**
     * Creates new entity centre.
     * 
     * @param menuItemType
     * @param name
     * @param analysisBuilder
     * @param gdtm
     * @param entityFactory
     * @param masterManager
     * @param criteriaGenerator
     * @param progressLayer
     * @return
     */
    CentreConfigurationView<T, ?> createEntityCentre(final Class<? extends MiWithConfigurationSupport<T>> menuItemType,//
            final String name,//
            final IAnalysisBuilder<T> analysisBuilder,//
            final IGlobalDomainTreeManager gdtm,//
            final EntityFactory entityFactory,//
            final IEntityMasterManager masterManager,//
            final ICriteriaGenerator criteriaGenerator,//
            final BlockingIndefiniteProgressLayer progressLayer);
}
