package ua.com.fielden.platform.eql.retrieval;

import java.util.Collection;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;

public class EntityHibernateRetrievalQueryProducer {
    //private static final Logger LOGGER = getLogger(EntityHibernateRetrievalQueryProducer.class);

    public static Query<?> produceQueryWithPagination(final Session session, final String sql, final Collection<YieldDetails> retrievedColumns, final Map<String, Object> queryParams, final Integer pageNumber, final Integer pageCapacity) {
        // LOGGER.debug("\nSQL:\n   " + sql + "\n");
        final NativeQuery<?> sqlQuery = session.createNativeQuery(sql);
        specifyResultingFieldsToHibernateQuery(sqlQuery, retrievedColumns);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams);
        specifyPaginationToHibernateQuery(sqlQuery, pageNumber, pageCapacity);

        return sqlQuery.setReadOnly(true).setCacheable(false).setCacheMode(CacheMode.IGNORE);
    }

    public static Query<?> produceQueryWithoutPagination(final Session session, final String sql, final Collection<YieldDetails> retrievedColumns, final Map<String, Object> queryParams) {
        return produceQueryWithPagination(session, sql, retrievedColumns, queryParams, null, null);
    }

    private static void specifyResultingFieldsToHibernateQuery(final NativeQuery<?> query, final Collection<YieldDetails> retrievedColumns) {
        for (final YieldDetails aliasEntry : retrievedColumns) {
            final Type hibType = aliasEntry.getHibTypeAsType();
            if (hibType != null) {
                // LOGGER.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "] type = [" + aliasEntry.getHibType() + "]");
                query.addScalar(aliasEntry.column, hibType);
            } else {
                // LOGGER.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "]");
                query.addScalar(aliasEntry.column);
            }
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