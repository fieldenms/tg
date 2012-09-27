package ua.com.fielden.platform.report.query.generation;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IReportQueryGeneration<T extends AbstractEntity<?>> {

    AnalysisResultClassBundle<T> generateQueryModel();
}
