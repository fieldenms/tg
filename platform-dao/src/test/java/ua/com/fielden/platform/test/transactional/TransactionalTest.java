package ua.com.fielden.platform.test.transactional;

import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for transaction support that reuses {@link EntityWithMoney} test entity class and {@link LogicThatNeedsTransaction} with transactional methods.
 *
 * @author TG Team
 *
 */
public class TransactionalTest extends DbDrivenTestCase2 {
    private LogicThatNeedsTransaction logic;
    private EntityWithMoneyDao dao;

    @Override
    public void setUp() throws Exception {
	super.setUp();
	dao = injector.getInstance(EntityWithMoneyDao.class);
	logic = injector.getInstance(LogicThatNeedsTransaction.class);
	// commit transaction opened in the parent setUp in order not to mess the transactional testing...
	hibernateUtil.getSessionFactory().getCurrentSession().close();
    }

    @Test
    public void testSingleTransactionInvocaion() {
	logic.singleTransactionInvocaion("20.00", "30.00");
	assertFalse("Transaction should have been inactive at this stage (committed).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNotNull("It is expected that transaction was committed.", dao.findByKey("one"));
	assertNotNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Test
    public void testNestedTransactionInvocaion() {
	logic.nestedTransactionInvocaion("20.00", "30.00");
	assertFalse("Transaction should have been inactive at this stage (committed).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNotNull("It is expected that transaction was committed.", dao.findByKey("one"));
	assertNotNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Test
    public void testTransactionalInvocaionWithException() {
	try {
	    logic.transactionalInvocaionWithException("20.00", "30.00");
	    fail("should have thrown an exception");
	} catch (final RuntimeException e) {
	}
	assertFalse("Transaction should have been inactive at this stage (rollbacked).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("three"));
    }

    @Test
    public void testNestedTransactionalInvocaionWithException() {
	try {
	    logic.nestedTransactionalInvocaionWithException("20.00", "30.00");
	    fail("should have thrown an exception");
	} catch (final RuntimeException e) {
	}
	assertFalse("Transaction should have been inactive at this stage (rollbacked).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
	assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("three"));
    }

    @Test
    public void test_single_transaction_invocaion_with_exception() {
	try {
	    logic.singleTransactionInvocaionWithExceptionInDao("20.00");
	    fail("should have thrown an exception");
	} catch (final RuntimeException e) {
	}
	assertFalse("Transaction should have been inactive at this stage (committed).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
    }

    @Test
    public void test_single_transaction_invocaion_with_exception_for_two_entities_in_save() {
	try {
	    logic.singleTransactionInvocaionWithExceptionInDao2();
	    fail("should have thrown an exception");
	} catch (final RuntimeException e) {
	}
	assertFalse("Transaction should have been inactive at this stage (committed).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
	assertNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Test
    public void test_session_required_atomic_behaviour() {
	hibernateUtil.getSessionFactory().getCurrentSession().close();
	try {
	    final EntityWithMoney one = new EntityWithMoney("one", "first", new Money("0.00"));
	    final EntityWithMoney two = new EntityWithMoney("one", "first", new Money("0.00"));

	    final EntityWithMoneyDao dao = injector.getInstance(EntityWithMoneyDao.class);
	    dao.saveTwoWithException(one, two);
	    fail("should have thrown an exception");
	} catch (final RuntimeException e) {
	}
	assertFalse("Transaction should have been inactive at this stage (committed).", hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive());
	assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
	assertNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/transactional-test-case.flat.xml" };
    }

}
