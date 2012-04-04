package ua.com.fielden.platform.persistence.types;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * Test Hibernate interaction with {@link Money} type and instrumented entities. Also test correctness of instrumented entities being handled by XStream.
 *
 * @author Yura
 * @author 01es
 */
public class MoneyAndSimpleMoneyTestCase extends DbDrivenTestCase {
    private boolean changeIndicator = false;

    @Override
    public void setUp() throws Exception {
	super.setUp();
	changeIndicator = false;
    }

    @SuppressWarnings("unchecked")
    public void testThatCanSaveAndRetrieveEntityWithMoney() {
	final EntityWithMoney instance = entityFactory.newEntity(EntityWithMoney.class, "name", "desc");
	instance.setMoney(new Money(new BigDecimal(100000d), Currency.getInstance("USD")));
	// saving instance of MoneyClass
	final IEntityDao dao = injector.getInstance(DaoFactory.class).newDao(EntityWithMoney.class);
	dao.save(instance);
	// retrieve saved instance
	final EntityWithMoney instance2 = (EntityWithMoney) dao.findByKey("name");
	assertEquals(instance, instance2);
    }

    public void testThatRetrievedEntityWithMoneyIsObservable() {
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityWithMoney instance = (EntityWithMoney) session.createQuery("from " + EntityWithMoney.class.getName() + " where id = 1").uniqueResult();
	instance.addPropertyChangeListener("money", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		changeIndicator = true;
	    }
	});
	instance.setMoney(new Money(new BigDecimal(100d), Currency.getInstance("USD")));

	assertEquals("Change was not observed.", true, changeIndicator);
    }

    @SuppressWarnings("unchecked")
    public void testThatCanSaveAndRetrieveEntityWithSimpleMoney() {
	final EntityWithSimpleMoney instance = entityFactory.newEntity(EntityWithSimpleMoney.class, "name", "desc");
	instance.setMoney(new Money(new BigDecimal(100000d), Currency.getInstance(Locale.getDefault())));
	// saving instance of MoneyClass
	final IEntityDao dao = injector.getInstance(DaoFactory.class).newDao(EntityWithSimpleMoney.class);
	dao.save(instance);
	// retrieve saved instance
	final EntityWithSimpleMoney instance2 = (EntityWithSimpleMoney) dao.findByKey("name");

	assertEquals(instance, instance2);
	assertEquals("Incorrect number of entities with simple money.", 2, dao.getPage(0, 25).data().size());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/money-user-type-test-case.flat.xml" };
    }
}