package ua.com.fielden.platform.eql.retrieval;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.eql.retrieval.records.HibernateScalar;

public class EntityHibernateRetrievalQueryProducer {
    //private static final Logger LOGGER = getLogger(EntityHibernateRetrievalQueryProducer.class);

    private EntityHibernateRetrievalQueryProducer() {}

    public static Query<?> produceQueryWithPagination(final Session session, final String sql, final List<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams, final Integer pageNumber, final Integer pageCapacity) {
        // LOGGER.debug("\nSQL:\n   " + sql + "\n");
        final NativeQuery<?> sqlQuery = session.createNativeQuery(sql);
        specifyResultingFieldsToHibernateQuery(sqlQuery, retrievedColumns);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams);
        specifyPaginationToHibernateQuery(sqlQuery, pageNumber, pageCapacity);

        return sqlQuery.setReadOnly(true).setCacheable(false).setCacheMode(CacheMode.IGNORE);
    }

    public static Query<?> produceQueryWithoutPagination(final Session session, final String sql, final List<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams) {
        return produceQueryWithPagination(session, sql, retrievedColumns, queryParams, null, null);
    }

    private static void specifyResultingFieldsToHibernateQuery(final NativeQuery<?> query, final List<HibernateScalar> retrievedColumns) {
        for (final HibernateScalar hibScalar : retrievedColumns) {
            query.addScalar(hibScalar.column(), hibScalar.hibType());
            // LOGGER.debug("adding scalar: alias = [" + hibScalar.column() + "] type = [" + hibScalar.hibType() + "]");
        }
    }

    private static void specifyParamValuesToHibernateQuery(final NativeQuery<?> query, final Map<String, Object> queryParams) {
        // LOGGER.debug("\nPARAMS:\n   " + queryParams + "\n");
        for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
            if (paramEntry.getValue() instanceof Collection) {
                throw new IllegalStateException("Should not have collectional param at this level: [" + paramEntry + "]");
            } else if (!(paramEntry.getValue() instanceof DynamicQueryBuilder.QueryProperty)){
                // LOGGER.debug("setting param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
                query.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }

    private static void specifyPaginationToHibernateQuery(final NativeQuery<?> query, final Integer pageNumber, final Integer pageCapacity) {
        if (pageNumber != null && pageCapacity != null) {
            query.setFirstResult(pageNumber * pageCapacity)
                 .setFetchSize(pageCapacity)
                 .setMaxResults(pageCapacity);
        }
    }
}