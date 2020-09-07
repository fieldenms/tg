package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;

public class EntityHibernateRetrievalQueryProducer {
    private static final Logger LOGGER = Logger.getLogger(EntityHibernateRetrievalQueryProducer.class);

    public final String sql;
    private final SortedSet<HibernateScalar> retrievedColumns;
    private final Map<String, Object> queryParams;
    private final Integer pageNumber;
    private final Integer pageCapacity;

    private EntityHibernateRetrievalQueryProducer(final String sql, final SortedSet<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams, final Integer pageNumber, final Integer pageCapacity) {
        this.sql = sql;
        this.retrievedColumns = retrievedColumns;
        this.queryParams = new HashMap<>(queryParams);
        this.pageNumber = pageNumber;
        this.pageCapacity = pageCapacity;
    }

    public static EntityHibernateRetrievalQueryProducer mkQueryProducerWithPagination(final String sql, final SortedSet<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams, final Integer pageNumber, final Integer pageCapacity) {
        return new EntityHibernateRetrievalQueryProducer(sql, retrievedColumns, queryParams, pageNumber, pageCapacity);
    }

    public static EntityHibernateRetrievalQueryProducer mkQueryProducerWithoutPagination(final String sql, final SortedSet<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams) {
        return new EntityHibernateRetrievalQueryProducer(sql, retrievedColumns, queryParams, null, null);
    }

    public Query<?> produceHibernateQuery(final Session session) {
        // LOGGER.debug("\nSQL:\n   " + sql + "\n");
        final NativeQuery<?> sqlQuery = session.createNativeQuery(sql);
        specifyResultingFieldsToHibernateQuery(sqlQuery, retrievedColumns);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams);
        specifyPaginationToHibernateQuery(sqlQuery, pageNumber, pageCapacity);

        return sqlQuery.setReadOnly(true).setCacheable(false).setCacheMode(CacheMode.IGNORE);
    }

    private void specifyResultingFieldsToHibernateQuery(final NativeQuery<?> query, final SortedSet<HibernateScalar> retrievedColumns) {
        for (final HibernateScalar aliasEntry : retrievedColumns) {
            if (aliasEntry.hasHibType()) {
                // LOGGER.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "] type = [" + aliasEntry.getHibType() + "]");
                query.addScalar(aliasEntry.getColumnName(), aliasEntry.getHibType());
            } else {
                // LOGGER.debug("adding scalar: alias = [" + aliasEntry.getColumnName() + "]");
                query.addScalar(aliasEntry.getColumnName());
            }
        }
    }

    private void specifyParamValuesToHibernateQuery(final NativeQuery<?> query, final Map<String, Object> queryParams) {
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

    private void specifyPaginationToHibernateQuery(final NativeQuery<?> query, final Integer pageNumber, final Integer pageCapacity) {
        if (pageNumber != null && pageCapacity != null) {
            query.setFirstResult(pageNumber * pageCapacity)
                 .setFetchSize(pageCapacity)
                 .setMaxResults(pageCapacity);
        }
    }
}