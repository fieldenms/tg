package ua.com.fielden.platform.test.transactional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;

/**
 * A test case for transaction support that reuses {@link EntityWithMoney} test entity class and {@link LogicThatNeedsTransaction} with transactional methods.
 * 
 * @author TG Team
 * 
 */
public class TransactionalTest extends AbstractDaoTestCase {
    private LogicThatNeedsTransaction logic;
    private EntityWithMoneyDao dao;
    private EntityFactory factory;
    
    @Before
    public void setUp()  {
        dao = co(EntityWithMoney.class);
        logic = getInstance(LogicThatNeedsTransaction.class);
        factory = getInstance(EntityFactory.class);
    }

    @Test
    public void testSingleTransactionInvocaion() {
        logic.singleTransactionInvocaion("20.00", "30.00");
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
        assertNotNull("It is expected that transaction was committed.", dao.findByKey("one"));
        assertNotNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Test
    public void testNestedTransactionInvocaion() {
        logic.nestedTransactionInvocaion("20.00", "30.00");
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
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
        assertFalse("Transaction should have been inactive at this stage (rollbacked).", logic.getSession().isOpen());
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
        assertFalse("Transaction should have been inactive at this stage (rollbacked).", logic.getSession().isOpen());
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
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
        assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
    }

    @Test
    public void test_single_transaction_invocaion_with_exception_for_two_entities_in_save() {
        try {
            logic.singleTransactionInvocaionWithExceptionInDao2();
            fail("should have thrown an exception");
        } catch (final RuntimeException e) {
        }
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
        assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

    @Test
    public void test_session_required_atomic_behaviour() {
        final EntityWithMoneyDao dao = co(EntityWithMoney.class);
        try {
            final EntityWithMoney one = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money("0.00")); 
                    //new EntityWithMoney("one", "first", new Money("0.00"));
            final EntityWithMoney two = factory.newEntity(EntityWithMoney.class, "one", "first").setMoney(new Money("0.00")); 
                    //new EntityWithMoney("one", "first", new Money("0.00"));

            dao.saveTwoWithException(one, two);
            fail("should have thrown an exception");
        } catch (final RuntimeException e) {
        }
        assertFalse("Transaction should have been inactive at this stage (committed).", dao.getSession().isOpen());
        assertNull("It is expected that transaction was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was committed.", dao.findByKey("two"));
    }

}
