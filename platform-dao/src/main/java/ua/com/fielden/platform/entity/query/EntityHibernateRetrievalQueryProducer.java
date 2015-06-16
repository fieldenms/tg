package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class EntityHibernateRetrievalQueryProducer {
    private final String sql;
    private final SortedSet<HibernateScalar> retrievedColumns;
    private final Map<String, Object> queryParams;
    private final Integer pageNumber;
    private final Integer pageCapacity;
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityHibernateRetrievalQueryProducer(String sql, SortedSet<HibernateScalar> retrievedColumns, Map<String, Object> queryParams, Integer pageNumber, Integer pageCapacity) {
        super();
        this.sql = sql;
        this.retrievedColumns = retrievedColumns;
        this.queryParams = queryParams;
        this.pageNumber = pageNumber;
        this.pageCapacity = pageCapacity;
    }

    public Query produceHibernateQuery(Session session) {
        logger.debug("\nSQL:\n   " + sql + "\n");
        final SQLQuery sqlQuery = session.createSQLQuery(sql);
        specifyResultingFieldsToHibernateQuery(sqlQuery, retrievedColumns);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams);
        specifyPaginationToHibernateQuery(sqlQuery, pageNumber, pageCapacity);

        return sqlQuery;
    }

    private void specifyResultingFieldsToHibernateQuery(SQLQuery query, final SortedSet<HibernateScalar> retrievedColumns) {
        for (final HibernateScalar aliasEntry : retrievedColumns) {
            if (aliasEntry.hasHibType()) {
                logger.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "] type = [" + aliasEntry.getHibType() + "]");
                query.addScalar(aliasEntry.getColumnName(), aliasEntry.getHibType());
            } else {
                logger.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "]");
                query.addScalar(aliasEntry.getColumnName());
            }
        }
    }

    private void specifyParamValuesToHibernateQuery(SQLQuery query, final Map<String, Object> queryParams) {
        logger.debug("\nPARAMS:\n   " + queryParams + "\n");
        for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
            if (paramEntry.getValue() instanceof Collection) {
                throw new IllegalStateException("Should not have collectional param at this level: [" + paramEntry + "]");
            } else {
                logger.debug("setting param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
                query.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }

    private void specifyPaginationToHibernateQuery(SQLQuery query, final Integer pageNumber, final Integer pageCapacity) {
        if (pageNumber != null && pageCapacity != null) {
            query.
                    setFirstResult(pageNumber * pageCapacity).
                    setFetchSize(pageCapacity).
                    setMaxResults(pageCapacity);
        }
    }
}