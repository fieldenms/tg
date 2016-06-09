package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class EntityHibernateDeletionQueryProducer {
    private final String sql;
    private final Map<String, Object> queryParams;
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityHibernateDeletionQueryProducer(String sql, Map<String, Object> queryParams) {
        super();
        this.sql = sql;
        this.queryParams = queryParams;
    }

    public Query produceHibernateQuery(Session session) {
        logger.debug("\nSQL:\n   " + sql + "\n");
        final SQLQuery sqlQuery = session.createSQLQuery(sql);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams);

        return sqlQuery;
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
}