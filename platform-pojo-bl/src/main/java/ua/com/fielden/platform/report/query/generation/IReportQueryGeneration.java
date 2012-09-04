package ua.com.fielden.platform.report.query.generation;

import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public interface IReportQueryGeneration<T extends AbstractEntity<?>> {

    List<QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModel();
}
