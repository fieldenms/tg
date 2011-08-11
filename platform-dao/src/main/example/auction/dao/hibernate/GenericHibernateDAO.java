package auction.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

import auction.dao.GenericDAO;
import auction.persistence.HibernateUtil;

/**
 * Implements the generic CRUD data access operations using Hibernate APIs.
 * <p>
 * To write a DAO, subclass and parameterize this class with your persistent class. Of course, assuming that you have a traditional 1:1 appraoch for Entity:DAO design.
 * <p>
 * You have to inject a current Hibernate <tt>Session</tt> to use a DAO. Otherwise, this generic implementation will use <tt>HibernateUtil.getSessionFactory()</tt> to obtain
 * the curren <tt>Session</tt>.
 *
 * @see HibernateDAOFactory
 *
 * @author Christian Bauer
 */
public abstract class GenericHibernateDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

    private Class<T> persistentClass;
    private Session session;

    public GenericHibernateDAO() {
	this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void setSession(final Session s) {
	this.session = s;
    }

    protected Session getSession() {
	if (session == null) {
	    session = HibernateUtil.getSessionFactory().getCurrentSession();
	}
	return session;
    }

    public Class<T> getPersistentClass() {
	return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(final ID id, final boolean lock) {
	T entity;
	if (lock) {
	    entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
	} else {
	    entity = (T) getSession().load(getPersistentClass(), id);
	}

	return entity;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
	return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(final T exampleInstance, final String... excludeProperty) {
	final Criteria crit = getSession().createCriteria(getPersistentClass());
	final Example example = Example.create(exampleInstance);
	for (final String exclude : excludeProperty) {
	    example.excludeProperty(exclude);
	}
	crit.add(example);
	return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T makePersistent(final T entity) {
	getSession().saveOrUpdate(entity);
	return entity;
    }

    public void makeTransient(final T entity) {
	getSession().delete(entity);
    }

    public void flush() {
	getSession().flush();
    }

    public void clear() {
	getSession().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(final Criterion... criterion) {
	final Criteria crit = getSession().createCriteria(getPersistentClass());
	for (final Criterion c : criterion) {
	    crit.add(c);
	}
	return crit.list();
    }

}
