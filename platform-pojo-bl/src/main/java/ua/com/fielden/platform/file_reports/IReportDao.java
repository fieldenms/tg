package ua.com.fielden.platform.file_reports;

import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

/**
 * Interface, implementors of which should provide reports.
 *
 * @author TG Team
 *
 */
public interface IReportDao {

    /**
     * This method should return compressed report of <code>reportType</code> filled with data obtained from specified <code>query</code>. <br>
     * Byte representation of report should be returned (usually PDF file).
     *
     * @param reportType
     * @param query
     * @param reportParams
     * @return
     * @throws Exception
     */
    byte[] getReport(String reportType, IQueryOrderedModel<EntityAggregates> query) throws Exception;

    /**
     * Username should be provided for every DAO instance in order to support data filtering and auditing.
     */
    void setUsername(final String username);
    String getUsername();

}
