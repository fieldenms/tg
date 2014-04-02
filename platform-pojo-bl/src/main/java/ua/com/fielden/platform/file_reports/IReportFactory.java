package ua.com.fielden.platform.file_reports;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;

/**
 * Factory for creating reports of some specific type.
 * 
 * @author TG Team
 * 
 */
public interface IReportFactory {

    /**
     * Should return report filled with data obtained from the specified <code>query</code> and specified <code>reportParams</code> set. <br>
     * Byte representation of report should be returned (usually PDF file)
     * 
     * @param query
     * @param reportParams
     * @param aggregatesDao
     * @return
     * @throws Exception
     */
    byte[] createReport(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, IEntityAggregatesDao aggregatesDao, final Map<String, Object> allParams)
            throws Exception;
}
