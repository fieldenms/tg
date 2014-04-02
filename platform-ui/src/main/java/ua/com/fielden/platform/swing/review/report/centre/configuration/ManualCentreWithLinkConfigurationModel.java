package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.factory.DefaultGridForManualEntityCentreFactory;

/**
 * A base calass for manual centre-based configuration model. It is used for building details view for compound masters where the relationship between dependent entity and its
 * master should be represented as an entity centre.
 * 
 * @author TG TM
 * 
 * @param <T>
 *            -- dependent entity type
 * @param <M>
 *            -- master entity type
 */
public class ManualCentreWithLinkConfigurationModel<T extends AbstractEntity<?>, M extends AbstractEntity<?>> extends ManualCentreConfigurationModel<T> {

    private final String linkProperty;
    private M linkEntity;

    public ManualCentreWithLinkConfigurationModel(final Class<T> entityType, //
            final DefaultGridForManualEntityCentreFactory<T> analysisFactory,//
            final ICentreDomainTreeManagerAndEnhancer cdtme, //
            final IEntityMasterManager masterManager, //
            final ICriteriaGenerator criteriaGenerator,//
            final String linkProperty) {
        super(entityType, analysisFactory, cdtme, masterManager, criteriaGenerator);
        this.linkProperty = linkProperty;
    }

    /**
     * Set the binding entity for this manual entity centre.
     * 
     * @param masterEntity
     */
    public ManualCentreWithLinkConfigurationModel<T, M> setLinkPropertyValue(final M linkEntity) {
        this.linkEntity = linkEntity;
        return this;
    }

    /**
     * Returns the binding entity for this manual entity centre.
     * 
     * @return
     */
    public M getLinkPropertyValue() {
        return linkEntity;
    }

    /**
     * Returns the property name to which this manual entity centre was binded.
     * 
     * @return
     */
    public String getLinkProperty() {
        return linkProperty;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultGridForManualEntityCentreFactory<T> getAnalysisFactory() {
        return (DefaultGridForManualEntityCentreFactory<T>) super.getAnalysisFactory();
    }
}
