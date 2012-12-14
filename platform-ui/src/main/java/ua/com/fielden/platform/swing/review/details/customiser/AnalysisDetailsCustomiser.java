package ua.com.fielden.platform.swing.review.details.customiser;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.details.DefaultGroupingAnalysisDetails;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.report.analysis.chart.AnalysisDetailsData;

/**
 * {@link IDetailsCustomiser} for analysis.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AnalysisDetailsCustomiser<T extends AbstractEntity<?>> implements IDetailsCustomiser {

    @SuppressWarnings("rawtypes")
    private final Map<Class, IDetails> detailsMap;

    public AnalysisDetailsCustomiser(//
	    final EntityFactory entityFactory, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager){
	detailsMap = new HashMap<>();
	detailsMap.put(AnalysisDetailsData.class, new DefaultGroupingAnalysisDetails<T>(entityFactory, criteriaGenerator, masterManager));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DT> IDetails<DT> getDetails(final Class<DT> detailsParamType) {
	return detailsMap.get(detailsParamType);
    }

}
