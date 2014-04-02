package ua.com.fielden.platform.basic.autocompleter;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * This is a Hibernate based implementation of {@link IValueMatcher}. It can be used in to ways -- by passing HQL query or by passing just an entity type.
 * 
 * If HQL query was provided then its parameter specified in the paramName property is assigned the value in method findMatches, which returns the query result. Otherwise, a HQL
 * query is composed dynamically during class instantiation based on the klass information.
 * 
 * @author 01es
 * 
 * @param <T>
 */
public class HibernateValueMatcher<T extends AbstractEntity<?>> implements IValueMatcher<T> {
    /**
     * Used for Hibernate Session instantiation.
     */
    private final SessionFactory sessionFactory;
    /**
     * HQL query used for entity matching
     */
    private final String hqlQuery;
    /**
     * This is a field representing query parameter name used for data filtering.
     */
    private final String paramName;
    /**
     * Controls how many records are turned as the result of query execution.
     */
    private int maxResults = 10;

    /**
     * This constructor should be used when a custom Hibernate query needs to be provided.
     * 
     * @param hqlQuery
     *            -- HQL query; all its parameters except paramName should have assigned values;
     * @param paramName
     *            -- The name of the parameter in the provided query used for value assignment in method {@link #findMatches(String)};
     * @param sessionFactory
     *            -- Hibernate session factory, which should be used for session instantiation;
     */
    public HibernateValueMatcher(final String hqlQuery, final String paramName, final SessionFactory sessionFactory) {
        this.hqlQuery = hqlQuery;
        this.paramName = paramName;
        this.sessionFactory = sessionFactory;
    }

    /**
     * This constructor should be used when a default query constructed dynamically based on the klass is sufficient.
     * 
     * @param klass
     *            -- Type of the entity to be return by the query.
     * @param property
     *            -- The property of the entity used for matching; it is also used for ordering of the returned result set.
     * @param sessionFactory
     *            -- Hibernate session factory, which should be used for session instantiation;
     */
    public HibernateValueMatcher(final Class<T> klass, final String property, final SessionFactory sessionFactory) {
        this("from " + klass.getName() + " where " + property + " like :in_value order by " + property, "in_value", sessionFactory);
    }

    /**
     * Retrieves matching data based in the provided or composed query.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> findMatches(final String value) {
        final Session session = sessionFactory.getCurrentSession();
        final Transaction tx = session.beginTransaction();
        try {
            final Query query = session.createQuery(hqlQuery).setParameter(paramName, value).setMaxResults(getPageSize());
            return query.list();
        } finally {
            tx.rollback();
        }
    }

    @Override
    public Integer getPageSize() {
        return maxResults;
    }

    public void setMaxResults(final int limit) {
        this.maxResults = limit;
    }

    @Override
    public List<T> findMatchesWithModel(final String value) {
        return findMatches(value);
    }

    @Override
    public <FT extends AbstractEntity<?>> fetch<FT> getFetchModel() {
        throw new UnsupportedOperationException("Entity query model is not supported by Hibernate value matcher.");
    }

    @Override
    public <FT extends AbstractEntity<?>> void setFetchModel(final fetch<FT> fetchModel) {
        throw new UnsupportedOperationException("Entity query model is not supported by Hibernate value matcher.");
    }
}