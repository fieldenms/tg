package ua.com.fielden.platform.report.query.generation;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for generating query model.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IReportQueryGenerator<T extends AbstractEntity<?>> {

    /**
     * Generates query model and returns the {@link AnalysisResultClassBundle} structure.
     *
     * @return
     */
    AnalysisResultClassBundle<T> generateQueryModel();
}
