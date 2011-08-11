package ua.com.fielden.platform.file_reports;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

/**
 * Factory for creating reports of some specific type.
 * 
 * @author yura
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
    byte[] createReport(IQueryOrderedModel<EntityAggregates> query, IEntityAggregatesDao aggregatesDao) throws Exception;

}
