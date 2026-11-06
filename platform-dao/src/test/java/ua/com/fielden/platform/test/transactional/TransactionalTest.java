package ua.com.fielden.platform.test.transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionCommitException;

import java.sql.Connection;
import java.util.stream.Stream;
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

    /// A unit of work handles the failure of a nested scope, which failed before executing any statement, as a nested save that fails validation would, and carries on.
    /// The nested scope closes the session without rolling back, as the session holds no connection, and the unit of work treats its transaction as rolled back, rather than attempt to commit the closed session.
    /// The work it carries on with finds no session bound to the thread, so it is a unit of work of its own, which commits.
    ///
    @Test
    public void unit_of_work_that_handles_a_nested_failure_before_any_statement_and_carries_on_completes_without_an_error() {
        logic.handleNestedFailureBeforeAnyStatementAndSave("carried on");

        assertFalse("Current session is expected to be closed.", logic.getSession().isOpen());
        assertNotNull("The work carried on with was committed.", dao.findByKey("carried on"));
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

    /// The same commit runs from a `Stream.onClose` handler when a `SessionRequired` method returns a stream,
    /// so the failure has to survive `Stream#close` to reach the caller.
    ///
    @Test
    public void commit_failure_on_stream_close_is_reported_to_the_caller() {
        final String key = "streamed";
        assertThrows("A commit that failed on stream close must not be silent.",
                     TransactionCommitException.class,
                     () -> {
                         try (final Stream<EntityWithMoney> stream = logic.saveAndStream(key)) {
                             stream.forEach(entity -> {
                             });                 // consume while the connection is still alive
                             logic.getSession().doWork(Connection::close);  // then release it, as a pool eviction would
                         }
                     });

        assertNull("The flushed INSERT was rolled back with the transaction.", dao.findByKey(key));
    }

    /// A stream returned by a `SessionRequired` method, which is traversed without a failure, commits the transaction when it is closed.
    ///
    @Test
    public void stream_traversed_without_failure_commits_the_transaction_on_stream_close() {
        final String key = "streamed";
        try (final Stream<EntityWithMoney> stream = logic.saveAndStream(key)) {
            stream.forEach(_ -> {});
        }

        assertFalse(logic.getSession().isOpen());
        assertNotNull("The INSERT was committed with the transaction.", dao.findByKey(key));
    }

    /// A failure that propagates out of the traversal of a stream returned by a `SessionRequired` method is a failure of its unit of work, as it would be without streaming.
    /// Closing the stream rolls back the transaction, instead of committing it, and reports the rollback, which try-with-resources adds to the failure as a suppressed exception.
    ///
    /// The report refers to the failure in its message, rather than as its cause, so that the failure does not refer to itself through it,
    /// which would break serialising the failure, for example, to JSON in a `Result`.
    ///
    @Test
    public void failure_during_traversal_of_a_stream_rolls_back_the_transaction_on_stream_close() throws Exception {
        final String key = "streamed";
        final var exception = new IllegalStateException("Purposeful exception.");
        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            try (final Stream<EntityWithMoney> stream = logic.saveAndStream(key)) {
                stream.forEach(_ -> { throw exception; });
            }
        });

        assertSame(exception, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertTrue(thrown.getSuppressed()[0] instanceof TransactionRollbackDueToThrowable);
        assertNull(thrown.getSuppressed()[0].getCause());
        assertTrue(thrown.getSuppressed()[0].getMessage().contains(exception.toString()));
        new ObjectMapper().writeValueAsString(thrown);
        assertFalse(logic.getSession().isOpen());
        assertNull("The flushed INSERT was rolled back with the transaction.", dao.findByKey(key));
    }

    /// A failure that propagates out of the traversal of a stream rolls back the transaction, even if the consumer catches it, as a failure of a nested scope does.
    /// Closing the stream then reports the rollback.
    /// The traversal is short-circuiting, which advances the stream element by element, as opposed to `forEach`, which traverses the remaining elements in bulk.
    ///
    @Test
    public void failure_during_traversal_of_a_stream_caught_by_the_consumer_rolls_back_the_transaction_on_stream_close() {
        final String key = "streamed";
        final var exception = new IllegalStateException("Purposeful exception.");
        final TransactionRollbackDueToThrowable thrown = assertThrows(TransactionRollbackDueToThrowable.class, () -> {
            try (final Stream<EntityWithMoney> stream = logic.saveAndStream(key)) {
                try {
                    stream.anyMatch(_ -> { throw exception; });
                } catch (final IllegalStateException _) {
                    // The failure is deliberately ignored, as a consumer might do.
                }
            }
        });

        assertNull(thrown.getCause());
        assertTrue(thrown.getMessage().contains(exception.toString()));
        assertFalse(logic.getSession().isOpen());
        assertNull("The flushed INSERT was rolled back with the transaction.", dao.findByKey(key));
    }

    /// The traversal of a parallel stream splits it, and the split-off parts are traversed by worker threads.
    /// A failure in a split-off part is recorded as a failure of the traversal, and closing the stream rolls back the transaction.
    ///
    /// The stream is retrieved through the companion, which cannot be split, but it is parallel and sorted, so its elements are buffered into an array, which can.
    /// An array is split by splitting off its first half and keeping the second, so the first element in sort order is always traversed through a split-off part.
    /// A failure thrown by a worker thread may be rethrown to the consumer as a new exception of the same type, whose cause is the original one.
    ///
    @Test
    public void failure_during_parallel_traversal_of_a_stream_rolls_back_the_transaction_on_stream_close() {
        final String[] keys = {"streamed1", "streamed2", "streamed3", "streamed4", "streamed5", "streamed6", "streamed7", "streamed8"};
        final var exception = new IllegalStateException("Purposeful exception.");
        final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            try (final Stream<EntityWithMoney> stream = logic.saveAndStreamInParallelSortedByKey(keys)) {
                assertTrue(stream.isParallel());
                stream.forEach(entity -> {
                    if (keys[0].equals(entity.getKey())) {
                        throw exception;
                    }
                });
            }
        });

        assertTrue(thrown == exception || thrown.getCause() == exception);
        assertEquals(1, thrown.getSuppressed().length);
        assertTrue(thrown.getSuppressed()[0] instanceof TransactionRollbackDueToThrowable);
        assertTrue(thrown.getSuppressed()[0].getMessage().contains(exception.toString()));
        assertFalse(logic.getSession().isOpen());
        for (final String key : keys) {
            assertNull("The flushed INSERT was rolled back with the transaction.", dao.findByKey(key));
        }
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