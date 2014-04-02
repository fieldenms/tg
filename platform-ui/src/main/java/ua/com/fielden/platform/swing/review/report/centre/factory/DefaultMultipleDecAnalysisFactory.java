package ua.com.fielden.platform.swing.review.report.centre.factory;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.details.DefaultGroupingAnalysisDetails;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.details.customiser.MapBasedDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.configuration.MultipleDecConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;

import com.google.inject.Inject;

public abstract class DefaultMultipleDecAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, MultipleDecConfigurationView<T>> {

    /**
     * Details customiser for chart analysis.
     */
    protected IDetailsCustomiser detailsCustomiser;

    @Inject
    public DefaultMultipleDecAnalysisFactory(//
    final EntityFactory entityFactory,//
            final ICriteriaGenerator criteriaGenerator,//
            final IEntityMasterManager masterManager) {
        detailsCustomiser = new MapBasedDetailsCustomiser()//
        .addDetails(AnalysisDetailsData.class, new DefaultGroupingAnalysisDetails<T>(entityFactory, criteriaGenerator, masterManager));
    }

    @Override
    public IAnalysisFactory<T, MultipleDecConfigurationView<T>> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser) {
        throw new UnsupportedOperationException("The analysis tool bar customiser can not be set for multiple dec analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, MultipleDecConfigurationView<T>> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser) {
        throw new UnsupportedOperationException("The analysis query customiser can not be set for multiple dec analysis factory.");
    }

    @Override
    public IAnalysisFactory<T, MultipleDecConfigurationView<T>> setDetailsCustomiser(final IDetailsCustomiser detailsCustomiser) {
        this.detailsCustomiser = detailsCustomiser;
        return this;
    }

    @Override
    public IAnalysisFactory<T, MultipleDecConfigurationView<T>> setAnalysisViewCustomiser(final IAnalysisViewCustomiser<?> analysisViewCustomiser) {
        throw new UnsupportedOperationException("The analysis view customiser can not be set for multiple dec analysis factory.");
    }
}
