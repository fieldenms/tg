package ua.com.fielden.platform.file_reports;

import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;


/**
 * Interface, implementors of which should provide reports.
 *
 * @author TG Team
 *
 */
public interface IReport {

    /**
     * This method should return compressed report of <code>reportType</code> filled with data obtained from specified <code>query</code>. <br>
     * Byte representation of report should be returned (usually PDF file).
     *
     * @param reportType
     * @param query
     * @return
     * @throws Exception
     */
    byte[] getReport(String reportType, QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final Map<String, Object> allParams) throws Exception;

    /**
     * Username should be provided for every DAO instance in order to support data filtering and auditing.
     */
    String getUsername();

}
