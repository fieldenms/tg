package ua.com.fielden.platform.test.transactional;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

/// A test case for transaction support that reuses [EntityWithMoney] test entity class and [LogicThatNeedsTransaction] with transactional methods.
///
public class TransactionalTest extends AbstractDaoTestCase {
    private LogicThatNeedsTransaction logic;
    private EntityWithMoneyDao dao;

    @Before
    public void setUp() {
        dao = co$(EntityWithMoney.class);
        logic = getInstance(LogicThatNeedsTransaction.class);
    }

    @Test
    public void single_transaction_is_committed_resulting_in_data_saving() {
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
    public void nested_transactions_are_supported_and_all_data_is_saved_upon_commit() {
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

    /// Every save in a TG application goes through a companion, which flushes explicitly — but a flush is not durability, only the commit is.
    /// When the commit fails, `SessionInterceptor.commitTransactionAndCloseSession` catches it,
    /// logs `Could not commit transaction.` and returns normally.
    /// As the result, a perfectly ordinary save is reported as successful while the database has rolled it back.
    /// This should not be happening with the correct error handling by the [SessionInterceptor].
    ///
    @Test
    public void commit_failure_after_a_successful_companion_save_is_reported_to_the_caller() {
        final String key = "lost";

        assertThrows("A save whose commit failed must not be reported to the caller as success.",
                     Exception.class,
                     () -> logic.saveThenLoseConnectionBeforeCommit(key));

        assertNull("The flushed INSERT was rolled back with the transaction.", dao.findByKey(key));
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

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        if (useSavedDataPopulationScript()) {
            return;
        }
    }

}