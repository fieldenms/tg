package auction.dao.hibernate;

import auction.dao.ItemDAO;
import auction.model.*;
import org.hibernate.*;

/**
 * Hibernate-specific implementation of the <tt>ItemDAO</tt> non-CRUD data access object.
 * 
 * @author Christian Bauer
 */
public class ItemDAOHibernate extends GenericHibernateDAO<Item, Long> implements ItemDAO {

    public Bid getMaxBid(Long itemId) {
	Query q = getSession().getNamedQuery("getItemMaxBid");
	q.setParameter("itemid", itemId);
	return (Bid) q.uniqueResult();
    }

    public Bid getMinBid(Long itemId) {
	Query q = getSession().getNamedQuery("getItemMinBid");
	q.setParameter("itemid", itemId);
	return (Bid) q.uniqueResult();
    }

}
