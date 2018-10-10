package ua.com.fielden.platform.test.transactional;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for transaction support that reuses {@link EntityWithMoney} test entity class and {@link LogicThatNeedsTransaction} with transactional methods.
 * 
 * @author TG Team
 * 
 */
public class TransactionalTest extends AbstractDaoTestCase {
    private LogicThatNeedsTransaction logic;
    private EntityWithMoneyDao dao;
    
    @Before
    public void setUp()  {
        dao = co$(EntityWithMoney.class);
        logic = getInstance(LogicThatNeedsTransaction.class);
    }

    @Test
    public void single_transacation_is_committed_resulting_in_data_saving() {
        logic.singleTransactionInvocaion("20.00", "30.00");
        assertFalse("Current session is expected to be closed.", logic.getSession().isOpen());

        final EntityWithMoney one = dao.findByKey("one");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", one);
        assertEquals(new Money("20.00"), one.getMoney());
        
        final EntityWithMoney two = dao.findByKey("two");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", two);
        assertEquals(new Money("30.00"), two.getMoney());
    }

    @Test
    public void netsted_transactions_are_supported_and_all_data_is_saved_upon_commit() {
        logic.nestedTransactionInvocaion("20.00", "30.00");
        assertFalse("Current session is expected to be closed.", logic.getSession().isOpen());
        
        final EntityWithMoney one = dao.findByKey("one");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", one);
        assertEquals(new Money("20.00"), one.getMoney());
        
        final EntityWithMoney two = dao.findByKey("two");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", two);
        assertEquals(new Money("30.00"), two.getMoney());
    }

    @Test
    public void nested_transactions_with_exception_rollback_all_changes() {
        assertNull(dao.findByKey("one"));
        assertNull(dao.findByKey("two"));
        assertNull(dao.findByKey("three"));
        
        try {
            logic.transactionalInvocaionWithException("20.00", "30.00");
            fail("should have thrown an exception");
        } catch (final Exception e) {
        }
        
        assertFalse("Current session is expected to be closed.", logic.getSession().isOpen());
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("three"));
    }

    @Test
    public void nested_transactions_with_error_rollbacks_all_changes() {
        assertNull(dao.findByKey("one"));
        assertNull(dao.findByKey("two"));
        assertNull(dao.findByKey("three"));
        
        try {
            logic.transactionalInvocaionWithError("20.00", "30.00");
            fail("should have thrown an exception");
        } catch (final TransactionRollbackDueToThrowable e) {
        }
        
        assertFalse("Current session is expected to be closed.", logic.getSession().isOpen());
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("three"));
    }

    @Test
    public void deep_nested_transactional_invocaion_with_exception_rollbacks_all_changes() {
        try {
            logic.nestedTransactionalInvocaionWithException("20.00", "30.00");
            fail("should have thrown an exception");
        } catch (final Exception e) {
        }
        assertFalse("Transaction should have been inactive at this stage (rollbacked).", logic.getSession().isOpen());
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("three"));
    }

    @Test
    public void single_transaction_invocaion_with_exception_rollbacks_all_changes() {
        try {
            logic.singleTransactionInvocaionWithExceptionInDao("20.00");
            fail("should have thrown an exception");
        } catch (final Exception e) {
        }
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
    }

    @Test
    public void single_transaction_invocaion_with_exception_for_two_entities_to_be_saved_rollbacks_all_changes() {
        try {
            logic.singleTransactionInvocaionWithExceptionInDao2();
            fail("should have thrown an exception");
        } catch (final Exception e) {
        }
        assertFalse("Transaction should have been inactive at this stage (committed).", logic.getSession().isOpen());
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("one"));
        assertNull("It is expected that transaction was rollbacked, and thus no data was committed.", dao.findByKey("two"));
    }

    @Test
    public void methods_with_disallowed_nested_scope_transactions_can_be_invoked_in_their_own_scope() {
        logic.cannotBeInvokeWithinExistingTransaction();

        final EntityWithMoney one = dao.findByKey("one");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", one);
        assertEquals(new Money("20.00"), one.getMoney());
        
        final EntityWithMoney two = dao.findByKey("two");
        assertNotNull("It is expected that transaction was committed, saving a new entity.", two);
        assertEquals(new Money("30.00"), two.getMoney());
    }

    @Test
    @SessionRequired
    public void methods_with_disallowed_nested_scope_transactions_throw_exception_for_nested_calls() {
        try {
            logic.cannotBeInvokeWithinExistingTransaction();
        } catch (final SessionScopingException ex) {
            assertEquals(format(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED, LogicThatNeedsTransaction.class.getName(), "cannotBeInvokeWithinExistingTransaction"), ex.getMessage());
        }
    }

}
