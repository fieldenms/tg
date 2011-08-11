package auction.dao.hibernate;

import auction.model.*;
import auction.dao.BillingDetailsDAO;

import java.util.*;

/**
 * Hibernate-specific implementation of the <tt>BillingDetailsDAO</tt> non-CRUD data access object.
 * 
 * @author Christian Bauer
 */
public class BillingDetailsDAOHibernate extends GenericHibernateDAO<BillingDetails, Long> implements BillingDetailsDAO {

    @SuppressWarnings("unchecked")
    public List<BillingDetails> findConcrete(Class concreteClass) {
	return getSession().createCriteria(concreteClass).list();
    }
}
