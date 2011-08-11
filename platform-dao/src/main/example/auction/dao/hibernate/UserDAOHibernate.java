package auction.dao.hibernate;

import org.hibernate.*;
import auction.dao.UserDAO;
import auction.model.*;
import static org.hibernate.criterion.Expression.*;

/**
 * Hibernate-specific implementation of the <tt>UserDAO</tt> non-CRUD data access object.
 * 
 * @author Christian Bauer
 */
public class UserDAOHibernate extends GenericHibernateDAO<User, Long> implements UserDAO {

    public User validateLogin(User user) {
	Criteria crit = getSession().createCriteria(getPersistentClass());
	crit.add(eq("username", user.getUsername()));
	crit.add(eq("password", user.getPassword()));
	return (User) crit.uniqueResult();
    }

    public void persistAddress(AddressEntity address) {
	getSession().save(address);
    }

}
