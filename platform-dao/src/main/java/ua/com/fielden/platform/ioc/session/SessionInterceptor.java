package ua.com.fielden.platform.ioc.session;

import com.google.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.session.exceptions.SessionScopingException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionCommitException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.security.user.User;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.dao.annotations.SessionRequired.ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED;

/// Intercepts methods annotated with [SessionRequired] to inject Hibernate session before the actual method execution.
/// Nested invocation of methods annotated with [SessionRequired] is supported.
/// For example, method `findAll()` could invoke method `findByCriteria()` — both with [SessionRequired].
///
/// A very important functionality provided by this interceptor is transaction management:
///   - If the current session has no active transaction then it is activated and the current method invocation is marked as the one that should commit it.
///   - If the current session has an active transaction then the current method invocation is marked as the one that should NOT commit it.
///   - In case of an exception, which could occur during method invocation, the transaction is rolled back if it is active and the exception is propagated up.
///
/// The last item ensures that any exception at any level of method invocation would ensure transaction rollback.
/// If transaction is not active at the time of rollback then that means it has already been rolled back, or it has failed to begin.
/// An enclosing scope may catch the failure of a nested scope, which has rolled back the transaction, or closed or discarded the session.
/// The owning invocation then does not commit, but completes as after a rollback, which it determines from whether its session is open, as Hibernate may still report the transaction as active.
/// A discarded session is closed once discarding has completed.
/// If discarding takes longer than `DISCARD_SESSION_TIMEOUT`, the session is still open, and the owning invocation attempts to commit it while the discarding thread may still be aborting its connection;
/// the attempt then typically fails with [TransactionCommitException].
/// Please note that transaction can be started outside of this interceptor, which means it will not be committed within it, and the transaction originator is responsible for commit.
/// At the same time, if an exception occurs then transaction will be rolled back.
///
/// Beginning a transaction involves no exchange with the database, as pooled connections are handed out with auto-commit disabled (refer to `HibernateConfigurationFactory`).
/// The connection is acquired by the first statement of a unit of work, so a broken connection, for example, after the database server has reset it,
/// fails that statement, and is handled like any other failure within the unit of work.
/// The session holds the connection from then on until its transaction completes.
/// A session that holds no connection has no transaction on the server, so if a unit of work fails without acquiring a connection,
/// for example, because the connection pool is exhausted or the database is unreachable, its session is closed without rolling back.
/// Rolling back would acquire a connection only to roll back nothing, and if acquiring failed again, wait for a connection a second time.
/// For the same reason, the connection is acquired before committing, if the unit of work has not acquired it yet, so that a failure to acquire it is not followed by Hibernate rolling back.
///
/// A failure to *begin* a transaction closes the session before the failure propagates.
/// Current sessions are bound to threads, and a transaction that failed to begin is inactive, so the error handling would neither roll it back nor close the session,
/// which would otherwise remain bound to the thread, and be reused by the next unit of work on that thread.
///
/// A failure to *commit* is reported rather than swallowed.
/// Committing is the point at which a unit of work becomes durable, so a failure there means that the unit of work may not have been persisted, however successfully the method itself ran.
/// Whether it was depends on where committing failed: before the JDBC commit, the transaction is rolled back; in the JDBC commit itself, its outcome is unknown;
/// after it, for example, in a synchronization that Hibernate notifies, the unit of work has been persisted.
/// [TransactionCommitException] is thrown for an exception, and only after the session has been discarded or closed, so that cleanup is never skipped.
/// It is deliberately distinct from a business failure: the work was valid and its statements were accepted, but the
/// transaction could not be made durable — typically because of an infrastructure failure, such as a database
/// failover or a terminated connection.
/// A [VirtualMachineError], or an exception caused by one, propagates to the error handling of the invocation, which discards the session, as described below.
/// Another error propagates once the session has been closed, or discarded if rolling back fails;
/// if the error preceded the JDBC commit, the transaction is rolled back first (refer to `commitTransactionAndCloseSession`).
///
/// A failure to *roll back* discards the session, as described below for a [VirtualMachineError], instead of closing it.
/// After such a failure, the state of the connection is unknown — the transaction may remain open on the server, holding its locks —
/// and the connection pool would reuse the connection unless the failure indicates a broken connection.
/// The same applies to a failed commit that leaves the session holding its connection, as the transaction may not have completed (refer to `commitTransactionAndCloseSession`).
///
/// A [VirtualMachineError], such as [StackOverflowError] or [OutOfMemoryError], discards the session instead of rolling back its transaction.
/// Such an error can occur at any point, including in the middle of an exchange with the database, leaving the connection out of sync with the server,
/// so the connection is aborted rather than reused, and the database rolls back the transaction when the connection closes.
/// The same applies to an exception caused by such an error, as libraries may wrap it, and to one to which such an error is attached as a suppressed throwable,
/// as try-with-resources does with a failure to close a resource after its body has failed.
/// For example, the thread-bound Hibernate session is a JDK dynamic proxy, which wraps an error thrown through it in [java.lang.reflect.UndeclaredThrowableException].
/// Other errors, such as [LinkageError] or [AssertionError], are thrown at well-defined points, and are handled like exceptions,
/// so that an error recurring in a unit of work does not cost a connection each time.
/// The session is unbound from the thread before anything else, and the connection is aborted on a separate thread, because the stack of the current thread may be nearly exhausted.
/// If a new thread cannot be started, the connection is aborted on the current thread instead.
/// A failure to abort the connection does not prevent closing the session, unless it is a [VirtualMachineError], after which the session is left open, to be discarded again.
/// If discarding leaves the session open, or the error handling is itself interrupted, for example, by another `StackOverflowError`, the session remains recorded for the thread,
/// and the next invocation on the thread discards it before obtaining its own session; an invocation that cannot discard it fails.
/// If discarding merely takes longer than `DISCARD_SESSION_TIMEOUT`, it continues in the background, and the record is cleared.
/// Either way, an error affects only the unit of work in which it occurred, and not later units of work on the same thread, which matters for pooled threads.
///
/// The hand-over of a connection between Hibernate and the connection pool is beyond the reach of this interceptor.
/// A [VirtualMachineError] that strikes while completing a transaction releases its connection, once Hibernate has dropped its reference to it (`LogicalConnectionManagedImpl.releaseConnection`),
/// leaves the session without the connection, so discarding the session aborts none.
/// Hibernate returns the connection to the pool regardless, where it is reused: the error rarely interrupts an exchange with the server at that point,
/// whereas aborting the connection could break another unit of work, which may already be using it.
/// If such an error strikes within HikariCP, after it has taken the connection back, but before it has marked the connection as available,
/// or while it hands out a connection, before the session holds it, the pool entry remains in use for good.
///
/// Two further behaviours are not apparent from a call site:
///   - The session is put into [FlushMode#COMMIT], so Hibernate never auto-flushes.
///     DML reaches the database when something flushes it explicitly, and otherwise during the commit.
///   - If an intercepted method returns a [Stream], the transaction outlives that method call and is committed when the stream is closed.
///     Such a stream *must* be closed, ideally via try-with-resources, or its transaction and connection are never released.
///     If a failure propagates out of the traversal of the stream, closing it rolls back the transaction instead, or discards the session after a [VirtualMachineError],
///     as an owning invocation would, and throws [TransactionRollbackDueToThrowable] (refer to `completeTransactionOnClose`).
///     If a nested scope has completed the transaction, or closed or discarded the session, as described above, the stream is returned as it is, and closing it commits nothing.
///
/// Finally, a GUID is generated per transaction and assigned to the invocation owner, which is how a single unit of work is identified downstream — by auditing, for example.
///
public class SessionInterceptor implements MethodInterceptor {
    public static final String
            MSG_CLOSING_SESSION = "[%s] Closing session.",
            MSG_CLOSED_SESSION = "[%s] Closed session.",
            MSG_CLOSED_DISCARDED_SESSION_WITH_ERROR = "[%s] Closed a discarded session with error.",
            WARN_TRANSACTION_ROLLBACK = "[%s] Transaction completed (rolled back) with error.",
            WARN_DISCARDED_SESSION_PENDING_CLEANUP = "[%s] Discarded a session, whose cleanup after an error had not completed.",
            ERR_COULD_NOT_CLOSE_SESSION = "[%s] Could not close session.",
            ERR_COULD_NOT_ABORT_CONNECTION = "[%s] Could not abort connection.",
            ERR_COULD_NOT_DISCARD_SESSION = "[%s] Could not discard session.",
            ERR_COULD_NOT_DISCARD_SESSION_PENDING_CLEANUP = "[%s] Could not discard a session, whose cleanup after an error had not completed.",
            ERR_COULD_NOT_ROLLBACK = "[%s] Could not roll back transaction. The session was discarded.",
            ERR_COULD_NOT_COMMIT = "[%s] Could not commit transaction.",
            ERR_TRAVERSAL_OF_STREAM_FAILED = "[%s] Transaction rolled back, as the traversal of a stream failed with [%s].";

    private static final Logger LOGGER = getLogger(SessionInterceptor.class);

    /// How long discarding a session waits for the thread that aborts its connection and closes it.
    /// Both normally complete in milliseconds, as aborting a connection involves no exchange with the server.
    /// Waiting ensures that the session is closed before the error propagates to enclosing scopes, which check whether it is still open.
    /// An interrupt does not end the wait, and the interrupt status of the thread is restored once the wait ends.
    /// The timeout bounds the delay of a failing unit of work if aborting or closing hangs: a warning is logged, the error propagates, and the discarding thread continues in the background.
    /// The delay is normally incurred once per unit of work, whatever the nesting depth of its session scopes, as the session is discarded at most once
    /// (refer to [ThreadSessionState#discarderStarted], which also describes a rare exception).
    private static final Duration DISCARD_SESSION_TIMEOUT = Duration.ofSeconds(10);
    /// The maximum number of throwables examined, among the causes and suppressed throwables of a throwable, when determining whether it is caused by a [VirtualMachineError].
    /// Actual chains are short — a few levels of wrapping by proxies, JDBC drivers and Hibernate, and a few resources closed by try-with-resources.
    /// The bound guards against a chain that is unexpectedly long or cyclic: [Throwable#initCause] prevents only a throwable from being its own cause, and [Throwable#getCause] may be overridden.
    private static final int MAX_EXAMINED_THROWABLES = 32;
    /// Created eagerly, so that discarding a session does not create it when the stack may be nearly exhausted.
    /// A thread factory obtained from a builder is safe for use by concurrent threads.
    private static final ThreadFactory DISCARDER_THREAD_FACTORY = Thread.ofPlatform().name("discard-session").daemon(true).factory();

    // Discarding a session creates a SessionDiscarder on a thread whose stack may be nearly exhausted.
    // Its class is initialised here, so that its first use does not run the class loader and the verifier on that thread.
    // Method `ensureInitialized` also links and initialises the class, whereas a class literal only loads it.
    // The class literal shares its constant-pool entry with `new SessionDiscarder` in `discardSession`, so that reference is resolved here too.
    static {
        try {
            MethodHandles.lookup().ensureInitialized(SessionDiscarder.class);
        } catch (final IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final Provider<? extends SessionFactory> sessionFactory;
    /// Creates the threads that discard sessions, which is [#DISCARDER_THREAD_FACTORY] except in tests.
    private final ThreadFactory discarderThreadFactory;
    private final ThreadLocal<String> transactionGuid = new ThreadLocal<>();
    private final ThreadLocal<ThreadSessionState> sessionState = ThreadLocal.withInitial(ThreadSessionState::new);

    /// Per-thread state of the current session scope, and of the cleanup of a session after an error.
    /// It is a mutable holder, so that recording requires a field assignment only, and no method call that could overflow a nearly exhausted stack.
    ///
    /// The session pending cleanup is recorded only in error handling, and is cleared once cleanup has completed;
    /// otherwise, it outlives the unit of work that failed, until a later invocation on the thread has discarded it.
    /// The session of the current scope is recorded at the start of every unit of work, and is cleared once that unit of work completes;
    /// if its error handling is interrupted, or a stream it returned is not closed on this thread, it is replaced at the start of the next unit of work.
    /// During the error handling of an owning invocation, both may refer to the session of the same unit of work.
    ///
    private static final class ThreadSessionState {
        /// A session whose cleanup after an error has not completed.
        private Session sessionPendingCleanup;
        /// Whether a thread that discards the session of the current unit of work has been started, or the session has been discarded on the current thread, if a thread could not be started.
        /// Once it has, the session is not discarded again within the same unit of work, even if it is still open, which is the case if the thread has not completed within [#DISCARD_SESSION_TIMEOUT].
        /// The flag is reset if discarding has completed, but left the session open, as no thread is discarding the session any longer, and a later call may discard it again.
        /// This keeps enclosing scopes from starting further threads that discard the same session, and from waiting for each of them.
        /// A unit of work has a single session, which scopes may refer to either directly or through its thread-bound proxy, so the flag records that a discarding thread was started, not which object it was started for.
        ///
        /// It is cleared when an owning invocation begins, so that it never carries over to a later unit of work,
        /// including after a unit of work that completes without reaching its error handling, for example, when an enclosing scope catches the error and returns a stream.
        /// It is also cleared when an owning invocation completes with an error.
        ///
        /// An enclosing scope that catches the failure of a nested one may carry on with further units of work, which find the discarded session unbound, and own session scopes of their own,
        /// so the flag is cleared before the enclosing scope completes.
        /// If discarding the session has not completed within [#DISCARD_SESSION_TIMEOUT] by then, and the enclosing scope fails with a [VirtualMachineError] later,
        /// another discarding thread is started for the same session, and waited for, which delays that failure further, but does not affect its outcome.
        private boolean discarderStarted;
        /// The thread-bound session of the current unit of work, and the session underlying it, which the invocation that owns the session scope records (refer to `underlyingSession`).
        /// They are cleared once the unit of work completes, so that the thread does not retain the session, which is when the owning invocation completes,
        /// or, if it returns a stream, when the stream is closed.
        private Session scopeSession;
        private Session scopeUnderlyingSession;
    }

    public SessionInterceptor(final Provider<? extends SessionFactory> sessionFactory) {
        this(sessionFactory, DISCARDER_THREAD_FACTORY);
    }

    /// For tests, which can make starting the threads that discard sessions fail.
    ///
    SessionInterceptor(final Provider<? extends SessionFactory> sessionFactory, final ThreadFactory discarderThreadFactory) {
        this.sessionFactory = sessionFactory;
        this.discarderThreadFactory = discarderThreadFactory;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ISessionEnabled invocationOwner = (ISessionEnabled) invocation.getThis();
        final User user = invocationOwner.getUser();
        final ThreadSessionState sessionState = this.sessionState.get();
        // It might be the case that an earlier invocation on this thread owned a session scope that failed, and its error handling did not complete, for example, due to another StackOverflowError.
        // Its session may still be bound to this thread, with an active transaction, and a connection that may be out of sync with the server.
        // Obtaining the current session would then return that session, making this invocation a nested scope of a unit of work that has already failed.
        // To prevent this, the session is discarded first, typically with ample stack, as the failed invocation has unwound.
        // While a session is recorded, no session scope is active on this thread: it is recorded by the invocation that owns the scope,
        // once all nested invocations have completed, or, for a stream returned by such an invocation, when completing the transaction on closing the stream fails, or follows a failed traversal,
        // and is cleared once cleanup completes.
        if (sessionState.sessionPendingCleanup != null) {
            discardSessionPendingCleanup(sessionState, user);
        }

        final Session session = sessionFactory.get().getCurrentSession();
        final Transaction tr = session.getTransaction();
        // The invocation that begins the transaction owns the session scope and is responsible for completing it.
        final boolean ownsScope = !tr.isActive();
        // A new unit of work begins.
        // This must follow discarding a session pending cleanup, which may have been discarded already, as indicated by the flag.
        if (ownsScope) {
            sessionState.discarderStarted = false;
        }
        // The thread-bound session is a proxy that rejects most methods unless its transaction is active, which is not the case after a failed commit or rollback,
        // or once the transaction has been marked for rollback only.
        // The underlying session is obtained while the transaction is active, so that it can be discarded after an error, whatever the transaction status.
        Session underlyingSession = null;

        try {
            // This variable indicates whether a transaction commit should be handled in this method invocation.
            // Basically, if a transaction is activated in this method, then it should be committed only in this method.
            // Therefore, shouldCommit is assigned true only when the transaction is activated here.
            final boolean shouldCommit = initTransaction(invocationOwner, session, tr, user);
            underlyingSession = underlyingSession(sessionState, session, shouldCommit);

            // If we should not commit, which means the session was initiated earlier in the call stack,
            // and support for nested calls is not allowed, then an exception is thrown.
            if (!shouldCommit && !invocation.getStaticPart().getAnnotation(SessionRequired.class).allowNestedScope()) {
                throw new SessionScopingException(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED.formatted(invocation.getMethod().getDeclaringClass().getName(), invocation.getMethod().getName()));
            }
            
            // Now let's proceed with the actual method invocation, which may throw an exception... or even a throwable...
            final Object result = invocation.proceed();
            
            // If this is the invocation that activated the current transaction, then we should commit it,
            // but only if the result of invocation is not a stream -- in that case, closing of the session is the responsibility of that stream.
            // A transaction whose session has been closed is treated as rolled back, although Hibernate may still report it as active.
            // This is the case if a nested scope failed and closed or discarded the session, and an enclosing scope caught its failure: committing the closed session could only fail.
            // The session itself is checked, rather than state recorded for the thread, which a nested invocation that owns a session scope of its own would reset,
            // as an enclosing scope that catches a failure may carry on with further units of work, which find the session of the failed one unbound.
            if (shouldCommit && tr.isActive() && session.isOpen()) {
                // If the result is a stream, then the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed.
                if (result instanceof Stream<?> stream) {
                    return completeTransactionOnClose(stream, sessionState, session, underlyingSession, tr, user);
                }
                // Otherwise, commit the current transaction.
                else {
                    LOGGER.debug(() -> "[%s] Committing DB transaction".formatted(user));
                    commitTransactionAndCloseSession(sessionState, session, underlyingSession, tr, user);
                    clearSessionScope(sessionState, session);
                    LOGGER.debug(() -> "[%s] Committed DB transaction".formatted(user));
                    return result;
                }
            }
            // Otherwise, this is a nested scope, or an owning invocation whose transaction a nested scope has completed, or whose session it has closed or discarded.
            // The session is flushed only if it is still open.
            if (session.isOpen()) {
                session.flush();
            }
            // An owning invocation reaches this point if its transaction was completed by a nested scope, which an enclosing scope caught.
            if (shouldCommit) {
                clearSessionScope(sessionState, session);
            }
            return result;
        } catch (final Throwable ex) {
            final Session discardableSession = underlyingSession != null ? underlyingSession : session;
            // A field assignment requires no stack frame, so the session is recorded even if the stack is nearly exhausted.
            if (ownsScope) {
                sessionState.sessionPendingCleanup = discardableSession;
            }
            try {
                completeTransactionWithError(sessionState, ownsScope, session, discardableSession, tr, ex, user);
            } catch (final Throwable cleanupFailure) {
                // The failure of the cleanup propagates instead of `ex`, which is attached to it, so that it is not lost.
                // `ex` is older than the failure of the cleanup, and thus cannot refer to it, so the attachment creates no cycle.
                if (cleanupFailure != ex) {
                    cleanupFailure.addSuppressed(ex);
                }
                throw cleanupFailure;
            }
            // Exceptions propagate unchanged; other throwables are wrapped.
            throw ex instanceof Exception e ? e : new TransactionRollbackDueToThrowable(ex);
        }
    }

    /// Returns a stream that completes the transaction of the unit of work that `stream` was returned by, when it is closed.
    ///
    /// If the stream has been traversed without a failure, closing it commits the transaction.
    /// Otherwise, a failure that propagated out of its traversal is a failure of its unit of work, so closing the stream completes the transaction
    /// as the error handling of an owning invocation would: it rolls back the transaction, or, after a [VirtualMachineError] or an exception caused by one, discards the session,
    /// and then throws [TransactionRollbackDueToThrowable], whether or not the failure was caught after it propagated, which refers to the failure in its message, rather than as its cause.
    /// The failure includes exceptions thrown by the operations of the stream pipeline, such as the action of `forEach`, as they are invoked from within the traversal.
    /// A failure outside the traversal, for example, an exception thrown after the stream has been collected, but before it has been closed, is beyond the reach of this method.
    ///
    /// Failures are recorded by [TraversalFailureRecordingSpliterator], which wraps the spliterator of `stream`.
    /// The returned stream closes `stream` before completing the transaction, so the close handlers of `stream`, such as closing a scrollable result set, run first.
    ///
    /// Creating the returned stream obtains the characteristics of that spliterator.
    /// For a parallel stream with a stateful operation, such as `sorted`, this evaluates the pipeline up to that operation:
    /// the elements are retrieved and buffered before this method returns, within the error handling of the invocation, rather than by the terminal operation of the consumer.
    /// Sequential streams, and parallel streams without a stateful operation, are not traversed until the terminal operation.
    /// If creating the returned stream fails, `stream` is closed before the failure propagates to the error handling of the invocation, so that its close handlers run.
    /// A failure to close `stream` is added to that failure as a suppressed exception.
    ///
    /// @param sessionState  the session state of the thread that created the stream
    /// @param streamSession  the session underlying `session`
    ///
    private <T> Stream<T> completeTransactionOnClose(final Stream<T> stream, final ThreadSessionState sessionState, final Session session, final Session streamSession, final Transaction tr, final User user) {
        final TraversalFailure traversal = new TraversalFailure();
        final Stream<T> recordingStream;
        try {
            recordingStream = StreamSupport.stream(new TraversalFailureRecordingSpliterator<>(stream.spliterator(), traversal), stream.isParallel());
        } catch (final Throwable th) {
            try {
                stream.close();
            } catch (final Throwable closeFailure) {
                if (closeFailure != th) {
                    th.addSuppressed(closeFailure);
                }
            }
            throw th;
        }
        return recordingStream
                .onClose(stream::close)
                .onClose(() -> {
            // A field read, which requires no method call.
            final Throwable traversalFailure = traversal.failure;
            // The session state of the thread that closes the stream, obtained before completing the transaction, so that the error handling needs no method call to obtain it.
            // It is obtained within the try block, so that an error in obtaining it, such as StackOverflowError, is handled like an error in committing.
            ThreadSessionState closingSessionState = null;
            try {
                closingSessionState = this.sessionState.get();
                if (traversalFailure == null) {
                    LOGGER.debug(() -> "[%s] Committing DB transaction on stream close.".formatted(user));
                    commitTransactionAndCloseSession(closingSessionState, session, streamSession, tr, user);
                    clearSessionScope(closingSessionState, session);
                    LOGGER.debug(() -> "[%s] Committed DB transaction on stream close.".formatted(user));
                    return;
                }
            } catch (final RuntimeException | Error ex) {
                // Committing can fail in three ways that matter here:
                //   - With a RuntimeException not caused by a VirtualMachineError, typically TransactionCommitException, after the session has been discarded or closed.
                //     It is logged and rethrown.
                //   - With a VirtualMachineError, or a RuntimeException caused by one, such as UndeclaredThrowableException from the thread-bound session proxy.
                //     commitTransactionAndCloseSession propagates these without closing the session, so that it can be discarded instead.
                //   - With another error, such as AssertionError, after commitTransactionAndCloseSession has rolled back the transaction and closed the session, or discarded it if rolling back failed.
                //     It is logged and rethrown unchanged; if discarding left the session open, the session remains recorded below, so that the next invocation on this thread discards it.
                // No checked exception can occur, so RuntimeException and Error cover all failures.
                // This handler runs when the stream is closed, after this invocation has returned, and thus outside its error handling.
                // Without an enclosing invocation to discard the session, it is discarded here (refer to commitTransactionAndCloseSession).
                // Everything here acts on the thread that closes the stream: unbinding the session, as part of discarding it, removing the transaction GUID,
                // and, except in the case described below, recording the session pending cleanup, so that it does not affect a unit of work in progress on another thread.
                // If the stream were closed on a thread other than the one that created it, the session would remain bound to the latter;
                // streams are expected to be consumed and closed by the thread that created them, ideally with try-with-resources.
                //
                // If obtaining the session state of the closing thread failed, the session state of the thread that created the stream is used instead.
                // It is the session state of the closing thread if the stream is closed as expected, by the thread that created it.
                // Selecting it requires no method call.
                final ThreadSessionState recordingSessionState = closingSessionState != null ? closingSessionState : sessionState;
                // As in the error handling of an invocation, the session is recorded first, by a field assignment, which requires no stack frame.
                // If discarding it does not complete, the next invocation on this thread discards it, instead of becoming a nested scope of the failed unit of work.
                // A session that has already been closed, or is being discarded, remains recorded only until the record is cleared below.
                recordingSessionState.sessionPendingCleanup = streamSession;
                // The transaction GUID is removed whether or not the session is discarded.
                // commitTransactionAndCloseSession leaves it in place when propagating a VirtualMachineError, or an exception caused by one,
                // which includes the case where Hibernate has rolled back the transaction and closed the session.
                transactionGuid.remove();
                if (isCausedByVirtualMachineError(ex) && streamSession.isOpen()) {
                    discardSession(recordingSessionState, streamSession, user);
                }
                clearSessionPendingCleanup(recordingSessionState);
                clearSessionScope(recordingSessionState, session);
                LOGGER.fatal(() -> "[%s] Could not commit DB transaction on stream close.".formatted(user), ex);
                throw ex;
            }
            // The traversal of the stream failed, and the session state of the closing thread has been obtained.
            // As in the error handling of an owning invocation, the session is recorded first, by a field assignment, which requires no stack frame,
            // and the record is cleared once the transaction has been completed.
            // A new exception is thrown, rather than the failure, as the failure has propagated already: try-with-resources would add it to itself as a suppressed exception, which is not permitted.
            // The new exception refers to the failure in its message, rather than as its cause, as try-with-resources attaches the new exception to the failure as a suppressed exception,
            // and the failure would then refer to itself through it, a cycle that breaks serialising either exception, for example, to JSON.
            closingSessionState.sessionPendingCleanup = streamSession;
            completeTransactionWithError(closingSessionState, true, session, streamSession, tr, traversalFailure, user);
            throw new TransactionRollbackDueToThrowable(ERR_TRAVERSAL_OF_STREAM_FAILED.formatted(user, traversalFailure));
        });
    }

    /// The first failure, if any, that propagated out of the traversal of a stream.
    /// It is shared by the spliterators split from one another, which may be traversed by different threads, hence the volatile field.
    ///
    private static final class TraversalFailure {
        private volatile Throwable failure;
    }

    /// A spliterator that records the first failure that propagates out of traversing `delegate`, and then rethrows it unchanged.
    /// Recording is a field assignment, which requires no stack frame, so a failure is recorded even if the stack is nearly exhausted, as after a [StackOverflowError].
    ///
    private static final class TraversalFailureRecordingSpliterator<T> implements Spliterator<T> {
        private final Spliterator<T> delegate;
        private final TraversalFailure traversal;

        private TraversalFailureRecordingSpliterator(final Spliterator<T> delegate, final TraversalFailure traversal) {
            this.delegate = delegate;
            this.traversal = traversal;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super T> action) {
            try {
                return delegate.tryAdvance(action);
            } catch (final Throwable th) {
                if (traversal.failure == null) {
                    traversal.failure = th;
                }
                throw th;
            }
        }

        @Override
        public void forEachRemaining(final Consumer<? super T> action) {
            try {
                delegate.forEachRemaining(action);
            } catch (final Throwable th) {
                if (traversal.failure == null) {
                    traversal.failure = th;
                }
                throw th;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            final Spliterator<T> split;
            try {
                split = delegate.trySplit();
            } catch (final Throwable th) {
                if (traversal.failure == null) {
                    traversal.failure = th;
                }
                throw th;
            }
            return split == null ? null : new TraversalFailureRecordingSpliterator<>(split, traversal);
        }

        @Override
        public long estimateSize() {
            return delegate.estimateSize();
        }

        @Override
        public long getExactSizeIfKnown() {
            return delegate.getExactSizeIfKnown();
        }

        @Override
        public int characteristics() {
            return delegate.characteristics();
        }

        @Override
        public Comparator<? super T> getComparator() {
            return delegate.getComparator();
        }
    }

    private boolean initTransaction(final ISessionEnabled invocationOwner, final Session session, final Transaction tr, final User user) {
        invocationOwner.setSession(session);
        final boolean shouldCommit = !tr.isActive();
        if (!tr.isActive()) {
            LOGGER.debug(() -> "[%s] Starting new DB transaction".formatted(user));
            try {
                tr.begin();
            } catch (final Throwable ex) {
                // Beginning a transaction involves no exchange with the database (refer to HibernateConfigurationFactory), so the session holds no connection here.
                // The transaction remains inactive, so the error handling of the invocation would neither roll it back nor close the session.
                // An open session stays bound to the current thread.
                // Closing it here ensures that the next unit of work on this thread obtains a new session.
                closeSession(session, user);
                throw ex;
            }
            session.setHibernateFlushMode(FlushMode.COMMIT);
            LOGGER.debug(() -> "[%s] Started new DB transaction".formatted(user));
            
            // generate a GUID for the current transaction
            if (!StringUtils.isEmpty(transactionGuid.get())) {
                throw new SessionScopingException("There should have been no transaction GUID assigned yet for a new session scope."); 
            }
            final String guid = randomUUID().toString();
            transactionGuid.set(guid);
            invocationOwner.setTransactionGuid(guid);
        } else {
            // assigned a transaction GUID, which should already be generated
            final String guid = transactionGuid.get();
            if (StringUtils.isEmpty(guid)) {
                throw new SessionScopingException("A nested session scope is missing a transaction GUID."); 
            }
            
            invocationOwner.setTransactionGuid(guid);
        }
        
        return shouldCommit;
    }

    /// Returns the session underlying `session`, the thread-bound session of the current invocation.
    ///
    /// The thread-bound session is a proxy, which rejects unwrapping it unless its transaction is active.
    /// Within a unit of work, the transaction may cease to be active before a nested scope is invoked, for example, once a failed statement has marked it for rollback only.
    /// Therefore, the invocation that owns the session scope unwraps the session, while the transaction it has just begun is active, and records it in `sessionState`,
    /// and nested invocations take the underlying session from there, which spares them unwrapping it through the proxy.
    ///
    /// A nested invocation takes the recorded session only if its thread-bound session is the one recorded, which identifies the unit of work.
    /// Otherwise, for example, within a transaction begun outside this interceptor, it unwraps the session through the proxy.
    ///
    private static Session underlyingSession(final ThreadSessionState sessionState, final Session session, final boolean ownsScope) {
        if (!ownsScope && sessionState.scopeSession == session) {
            return sessionState.scopeUnderlyingSession;
        }
        final Session underlyingSession = session.unwrap(Session.class);
        if (ownsScope) {
            sessionState.scopeSession = session;
            sessionState.scopeUnderlyingSession = underlyingSession;
        }
        return underlyingSession;
    }

    /// Clears the record of the session scope of `session` in `sessionState`, once its unit of work has completed.
    /// A record of another session, such as one from a stream closed on another thread, is left in place, and is replaced by the next invocation that owns a session scope.
    ///
    private static void clearSessionScope(final ThreadSessionState sessionState, final Session session) {
        if (sessionState.scopeSession == session) {
            sessionState.scopeSession = null;
            sessionState.scopeUnderlyingSession = null;
        }
    }

    /// Completes the session scope after `th`, and logs it, but does not throw it, which is left to the caller.
    ///
    /// Cleanup precedes logging, so that it runs with as much stack as is available.
    /// If cleanup does not complete, the session remains recorded in `sessionState` by the invocation that owns the session scope, or by the stream that completes its transaction, and is discarded by the next invocation on this thread.
    /// If cleanup fails, its failure propagates unchanged, instead of `th`, which is not logged.
    /// A caller to which `th` has not propagated attaches `th` to the failure of the cleanup, so that it is not lost (refer to `invoke`);
    /// a caller to which it has propagated already, such as the consumer of a stream, does not, as that would create a cycle of suppressed throwables.
    ///
    /// @param ownsScope  whether the failed invocation owns the session scope, in which case the unit of work completes with this invocation
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to discard it after a [VirtualMachineError], or if rolling back fails
    ///
    private void completeTransactionWithError(final ThreadSessionState sessionState, final boolean ownsScope, final Session session, final Session discardableSession, final Transaction tr, final Throwable th, final User user) {
        try {
            if (isCausedByVirtualMachineError(th)) {
                // In nested scopes, the innermost scope discards the session, and the enclosing scopes find it closed,
                // or, if discarding has not completed in time, skip it in discardSession.
                if (discardableSession.isOpen()) {
                    discardSession(sessionState, discardableSession, user);
                }
            }
            // Otherwise, if the transaction is active, it should be rolled back.
            else if (session.isOpen() && tr.isActive()) {
                LOGGER.debug(() -> "[%s] Rolling back DB transaction".formatted(user));
                rollbackTransactionAndCloseSession(sessionState, session, discardableSession, tr, user);
                LOGGER.debug(() -> "[%s] Rolled back DB transaction".formatted(user));
            }
        } finally {
            transactionGuid.remove();
        }
        if (ownsScope) {
            clearSessionPendingCleanup(sessionState);
            sessionState.discarderStarted = false;
            clearSessionScope(sessionState, session);
        }

        switch (th) {
            // Most Result exceptions are validation errors, which are more relevant for debug messages.
            case Result _ -> LOGGER.debug(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case SessionScopingException _ -> LOGGER.error(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case TransactionCommitException _ -> {} // Already logged before.
            default -> LOGGER.warn(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
        }
    }

    /// Determines whether `th` is a [VirtualMachineError], or is caused by one, either as a cause or as a suppressed throwable, at any level.
    ///
    /// Suppressed throwables are examined, as try-with-resources attaches a failure to close a resource to the failure of its body, rather than propagating it.
    /// For example, a [StackOverflowError] while a JDBC driver closes a result set, which may involve an exchange with the server, is suppressed by an ordinary exception thrown before it.
    ///
    /// The chain of causes of `th` is examined first, as wrapping is the common case, so that many suppressed throwables cannot exhaust the bound before the chain has been examined.
    /// It is examined without allocating, unless a throwable in it has suppressed throwables, as memory may be exhausted.
    /// Only if a throwable in that chain has suppressed throwables are they examined, breadth-first, together with their own causes and suppressed throwables.
    ///
    /// The search is iterative, as it runs in error handling, where the stack may be nearly exhausted, and bounded by [#MAX_EXAMINED_THROWABLES], as a chain may, in principle, be cyclic.
    /// If the search itself fails, for example, with a `StackOverflowError`, the failure propagates, and the session remains recorded for the thread by the invocation that owns the session scope, or by the stream that completes its transaction.
    ///
    private static boolean isCausedByVirtualMachineError(final Throwable th) {
        boolean hasSuppressed = false;
        int chainLength = 0;
        for (Throwable cause = th; cause != null && chainLength < MAX_EXAMINED_THROWABLES; cause = cause.getCause(), chainLength++) {
            if (cause instanceof VirtualMachineError) {
                return true;
            }
            // getSuppressed() allocates only if there are suppressed throwables.
            hasSuppressed = hasSuppressed || cause.getSuppressed().length > 0;
        }
        if (!hasSuppressed) {
            return false;
        }

        // The throwables of the chain, which have been examined, followed by the suppressed throwables found, and their causes, which are yet to be examined.
        final Throwable[] found = new Throwable[MAX_EXAMINED_THROWABLES];
        int count = 0;
        for (Throwable cause = th; count < chainLength; cause = cause.getCause()) {
            found[count++] = cause;
        }
        for (int next = 0; next < count; next++) {
            final Throwable current = found[next];
            if (next >= chainLength) {
                if (current instanceof VirtualMachineError) {
                    return true;
                }
                final Throwable cause = current.getCause();
                if (cause != null && count < found.length) {
                    found[count++] = cause;
                }
            }
            for (final Throwable suppressed : current.getSuppressed()) {
                if (count == found.length) {
                    break;
                }
                found[count++] = suppressed;
            }
        }
        return false;
    }

    /// Discards a session, whose cleanup after an error did not complete, before the current invocation obtains its session.
    ///
    /// The session remains recorded until discarding it completes, and logging follows.
    /// If discarding fails with an error, the error propagates, and the next invocation on this thread discards the session again.
    /// If discarding completes, but leaves the session open, the current invocation fails with [SessionScopingException], rather than proceed:
    /// there is a single record per thread, which the current invocation would overwrite, if it owned a session scope that failed.
    ///
    private void discardSessionPendingCleanup(final ThreadSessionState sessionState, final User user) {
        transactionGuid.remove();
        discardSession(sessionState, sessionState.sessionPendingCleanup, user);
        if (!sessionState.discarderStarted) {
            throw new SessionScopingException(ERR_COULD_NOT_DISCARD_SESSION_PENDING_CLEANUP.formatted(user));
        }
        sessionState.sessionPendingCleanup = null;
        LOGGER.warn(() -> WARN_DISCARDED_SESSION_PENDING_CLEANUP.formatted(user));
    }

    /// Clears the record of the session pending cleanup, unless that session is still open, and no thread is discarding it, which is the case if discarding it left it open.
    /// Such a session remains recorded, so that the next invocation on this thread discards it again.
    ///
    private static void clearSessionPendingCleanup(final ThreadSessionState sessionState) {
        final Session session = sessionState.sessionPendingCleanup;
        if (session == null || sessionState.discarderStarted || !session.isOpen()) {
            sessionState.sessionPendingCleanup = null;
        }
    }

    /// Discards `session` after a [VirtualMachineError], or after a failure to roll back or to commit its transaction.
    ///
    /// Such an error, for example, [StackOverflowError], may interrupt an exchange with the database, which leaves the connection out of sync with the server.
    /// Rolling back over such a connection may wait indefinitely for a response, and returning it to the pool passes the problem on to another unit of work.
    /// Therefore, the connection is aborted, which closes it without any exchange with the server, and the session is closed without rolling back.
    /// The database rolls back the transaction of a closed connection, and the connection pool removes a closed connection instead of reusing it.
    ///
    /// The session is first unbound from the current thread, which leaves the thread ready for the next unit of work.
    /// Aborting and closing run on a new thread, as the stack of the current thread may be nearly exhausted.
    /// A new thread per discarded session, rather than a pool, ensures that a discard that does not complete — for example, closing a connection that hangs — cannot delay later discards.
    /// For the same reason as above, this path avoids lambdas, whose first use requires linkage, and uses only classes that are loaded beforehand:
    /// the thread factory and [SessionDiscarder] are initialised together with this class.
    /// It also obtains the session factory from the session, which is a plain getter.
    ///
    /// If the thread cannot be created or started, for example, due to [OutOfMemoryError] once the process has reached its limit of threads,
    /// the session is discarded on the current thread instead.
    /// The session has been unbound at that point, and is open, holding its connection and its transaction on the server.
    ///
    /// [SessionDiscarder] logs its errors, rather than propagating them, so whether discarding completed is determined from the session:
    /// if discarding has completed, but left the session open, it remains recorded for the thread by the invocation that owns the session scope, or by the stream that completes its transaction (refer to `clearSessionPendingCleanup`).
    /// An error on the current thread, for example, in starting the thread or in logging, propagates, and the session remains recorded likewise.
    ///
    /// The session is discarded at most once per unit of work.
    /// Once the discarding thread has been started, or the session has been discarded on the current thread, later calls within the same unit of work return immediately,
    /// unless discarding has completed, but left the session open (refer to [ThreadSessionState#discarderStarted]).
    ///
    private void discardSession(final ThreadSessionState sessionState, final Session session, final User user) {
        if (sessionState.discarderStarted) {
            return;
        }
        ThreadLocalSessionContext.unbind(session.getSessionFactory());
        final SessionDiscarder sessionDiscarder = new SessionDiscarder(session, user);
        final Thread discarder;
        try {
            discarder = discarderThreadFactory.newThread(sessionDiscarder);
            discarder.start();
        } catch (final Throwable ex) {
            sessionDiscarder.run();
            if (!session.isOpen()) {
                sessionState.discarderStarted = true;
            }
            LOGGER.warn("[{}] Could not start a thread to discard a session, which is discarded on the current thread instead.", user, ex);
            return;
        }
        sessionState.discarderStarted = true;
        if (!awaitTermination(discarder)) {
            LOGGER.warn("[{}] Discarding a session did not complete within {}.", user, DISCARD_SESSION_TIMEOUT);
        }
        else if (session.isOpen()) {
            sessionState.discarderStarted = false;
        }
    }

    /// Waits for `discarder` to terminate, for at most [#DISCARD_SESSION_TIMEOUT], and returns whether it has terminated.
    ///
    /// An interrupt does not end the wait, so that, as on a thread that is not interrupted, the session is closed before the error propagates to enclosing scopes.
    /// The interrupt status of the current thread is restored once the wait ends.
    ///
    private static boolean awaitTermination(final Thread discarder) {
        final long deadline = System.nanoTime() + DISCARD_SESSION_TIMEOUT.toNanos();
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    // Waits only for the time remaining until the deadline, so that interrupts do not extend the overall wait.
                    // Once the deadline has passed, the remaining time is not positive, and join returns immediately with whether the thread has terminated.
                    return discarder.join(Duration.ofNanos(deadline - System.nanoTime()));
                } catch (final InterruptedException _) {
                    // The interrupt status is cleared when InterruptedException is thrown, so the next join waits.
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// Aborts the connection of a session, and then closes the session.
    ///
    /// A closed session is skipped, as closing a session releases its connection, even if closing fails partway, and the session no longer holds it.
    ///
    /// A thread-bound session closes itself once its transaction completes, as `ThreadLocalSessionContext` enables auto-close, which releases its connection.
    /// For example, if committing fails before the JDBC commit, such as when flushing violates a constraint, Hibernate rolls back the transaction and the session closes,
    /// returning the connection to the pool in a known state, and the session is then not discarded (refer to `commitTransactionAndCloseSession`).
    /// The connection is aborted only if the session holds one, as obtaining the physical connection of a session that holds none would acquire one from the pool, only to abort it.
    /// A session remains open and holds its connection if its transaction did not complete, which is the case if:
    ///   - the JDBC commit failed, which leaves its outcome unknown;
    ///   - rolling back failed;
    ///   - an error was thrown before completion, for example, while flushing, as Hibernate rolls back only after a [RuntimeException].
    ///
    /// A failure reported by the JDBC commit itself, such as a deferred constraint violation or a serialisation failure, therefore costs a connection,
    /// even where the database has already rolled back the transaction.
    ///
    /// A failure to abort the connection, whether an exception or an error, such as [LinkageError], does not prevent closing the session,
    /// so that the session does not keep its connection and its transaction on the server indefinitely.
    /// Closing a session whose connection has not been aborted returns the connection to the pool, as after an ordinary failure.
    ///
    /// The exception is a [VirtualMachineError], such as [StackOverflowError] or [OutOfMemoryError], after which the session is not closed.
    /// Closing it on the same exhausted stack or heap could fail partway, after Hibernate has dropped its reference to the connection (`LogicalConnectionManagedImpl.releaseConnection`),
    /// but before the connection has been returned to the pool: the session would then report itself closed, and the pool entry would remain in use for good.
    /// A session left open remains recorded for the thread instead, and a later invocation discards it again, typically with ample stack.
    /// On a discarding thread, with its fresh stack, this is practically a matter of memory exhaustion only.
    ///
    /// Errors are logged rather than propagated: on a discarding thread, an uncaught error would only be printed to the standard error stream.
    /// The caller determines from the session whether discarding left it open (refer to `discardSession`).
    ///
    private record SessionDiscarder(Session session, User user) implements Runnable {
        @Override
        public void run() {
            try {
                if (!session.isOpen()) {
                    return;
                }
                try {
                    abortConnection();
                } catch (final VirtualMachineError err) {
                    throw err;
                } catch (final Throwable th) {
                    closeDiscardedSession();
                    throw th;
                }
                closeDiscardedSession();
            } catch (final Throwable th) {
                LOGGER.error(() -> ERR_COULD_NOT_DISCARD_SESSION.formatted(user), th);
            }
        }

        /// Aborts the connection regardless of the transaction status, which may be inaccurate after an error, for example, during commit.
        ///
        private void abortConnection() {
            try {
                final LogicalConnectionImplementor logicalConnection = logicalConnection(session);
                if (logicalConnection.isPhysicallyConnected()) {
                    logicalConnection.getPhysicalConnection().abort(Runnable::run);
                } else {
                    LOGGER.debug(() -> "[%s] A discarded session holds no connection.".formatted(user));
                }
            } catch (final Exception ex) {
                LOGGER.error(() -> ERR_COULD_NOT_ABORT_CONNECTION.formatted(user), ex);
            }
        }

        /// Closes the session, which reports errors if its connection has been aborted; these are expected.
        ///
        private void closeDiscardedSession() {
            try {
                session.close();
            } catch (final Exception ex) {
                LOGGER.debug(() -> MSG_CLOSED_DISCARDED_SESSION_WITH_ERROR.formatted(user), ex);
            }
        }
    }

    /// Commits the transaction and closes the session.
    ///
    /// If committing fails with a [VirtualMachineError], or an exception caused by one, the session is not closed,
    /// and the failure propagates to the error handling of the invocation, which discards the session.
    /// Closing it here would return a connection that may be out of sync with the server to the pool.
    ///
    /// If committing fails with an exception that is not caused by a [VirtualMachineError], the session is discarded, as after a failed rollback.
    /// The transaction may not have completed, in which case the state of the connection is unknown, and the connection pool would reuse it unless the failure indicates a broken connection.
    /// A session that holds no connection is closed instead, as there is no connection to abort.
    /// This is the case if acquiring a connection for committing failed, and after failures that Hibernate rolls back, which close the session, so they cost no connection.
    ///
    /// If committing fails with an error other than a [VirtualMachineError], such as [AssertionError], the error is thrown at a well-defined point, which leaves the connection in sync with the server,
    /// and the session is closed rather than discarded, so that an error recurring at commit does not cost a connection each time:
    ///   - If the error precedes the JDBC commit, typically while flushing, Hibernate neither commits nor rolls back the transaction, as it handles only exceptions.
    ///     As after such an error in the method itself, the transaction is rolled back before the session is closed, and if rolling back fails, the session is discarded (refer to `rollbackTransactionAndCloseSession`).
    ///   - If the error follows the JDBC commit, for example, in a process that Hibernate runs after completing the transaction, the transaction has been committed,
    ///     so the session is closed without rolling back.
    ///
    /// Committing requires a connection, which the session does not hold yet if the unit of work has executed no statement.
    /// Hibernate would acquire it on committing, and if that failed, roll back the transaction, which would acquire a connection again,
    /// waiting for one a second time if the connection pool is exhausted or the database is unreachable.
    /// The connection is therefore acquired before committing, which costs nothing extra, as committing would acquire it anyway.
    ///
    /// In all cases other than a [VirtualMachineError], or an exception caused by one, the session is closed or discarded before this method completes.
    ///
    /// @param discardableSession  the session underlying `session`, which is used to acquire its connection, and to discard it if committing fails
    ///
    private void commitTransactionAndCloseSession(final ThreadSessionState sessionState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
        try {
            if (tr.isActive()) {
                // Called for its side effect: it acquires a connection if the session does not hold one yet, and does nothing otherwise.
                // A failure to acquire a connection is thus raised here, and handled below, rather than within tr.commit(),
                // where Hibernate would follow it by rolling back, which would attempt to acquire a connection again (refer to the Javadoc above).
                logicalConnection(discardableSession).getPhysicalConnection();
                tr.commit();
            }
        } catch (final VirtualMachineError err) {
            throw err;
        } catch (final Exception ex) {
            if (isCausedByVirtualMachineError(ex)) {
                throw ex;
            }
            LOGGER.error(() -> ERR_COULD_NOT_COMMIT.formatted(user), ex);
            // A failed commit means that the unit of work may not have been persisted, and must not be reported as success.
            // The session is discarded or closed before this exception propagates, so it is never left as a dead current session.
            transactionGuid.remove();
            if (holdsConnection(discardableSession)) {
                discardSession(sessionState, discardableSession, user);
            } else {
                closeSession(session, user);
            }
            throw new TransactionCommitException(ERR_COULD_NOT_COMMIT.formatted(user), ex);
        } catch (final Error err) {
            transactionGuid.remove();
            rollbackTransactionAndCloseSession(sessionState, session, discardableSession, tr, user);
            throw err;
        }
        transactionGuid.remove();
        closeSession(session, user);
    }

    /// Rolls back the transaction and closes the session.
    ///
    /// If rolling back fails, for whatever reason, the session is discarded instead of being closed.
    /// After such a failure, the state of the connection is unknown: the transaction may remain open on the server, holding its locks, or the connection may be out of sync with the server.
    /// Closing the session would return the connection to the pool, which reuses it unless the failure indicates a broken connection (SQL state `08xxx`).
    ///
    /// A session that holds no connection has no transaction on the server, and is closed without rolling back, which would acquire a connection only to roll back nothing.
    /// This is the case if the unit of work failed without acquiring a connection, for example, because the connection pool is exhausted or the database is unreachable,
    /// in which case rolling back would wait for a connection a second time, fail, and discard the session.
    /// Whether the session holds a connection is known only if the underlying session has been obtained; otherwise, the transaction is rolled back.
    /// Hibernate still reports the transaction of a session closed this way as active, so the owning invocation checks whether the session is open, and treats the transaction of a closed session as rolled back.
    ///
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to determine whether it holds a connection, and to discard it if rolling back fails
    ///
    private void rollbackTransactionAndCloseSession(final ThreadSessionState sessionState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
        if (discardableSession != session && !holdsConnection(discardableSession)) {
            closeSession(session, user);
            return;
        }
        try {
            if (tr.isActive()) {
                tr.rollback();
            }
        } catch (final Throwable ex) {
            discardSession(sessionState, discardableSession, user);
            LOGGER.error(() -> ERR_COULD_NOT_ROLLBACK.formatted(user), ex);
            return;
        }

        closeSession(session, user);
    }

    /// Whether `session` is open and holds a connection, which is determined without acquiring one.
    ///
    /// @param session  an underlying session, as opposed to its thread-bound proxy, which rejects unwrapping without an active transaction
    ///
    private static boolean holdsConnection(final Session session) {
        return session.isOpen() && logicalConnection(session).isPhysicallyConnected();
    }

    /// The logical connection of an open underlying `session`, which may or may not hold a connection; obtaining it acquires none.
    ///
    private static LogicalConnectionImplementor logicalConnection(final Session session) {
        return session.unwrap(SharedSessionContractImplementor.class).getJdbcCoordinator().getLogicalConnection();
    }

    private static void closeSession(final Session session, final User user) {
        try {
            LOGGER.debug(() -> MSG_CLOSING_SESSION.formatted(user));
            if (session.isOpen()) {
                session.close();
            }
            LOGGER.debug(() -> MSG_CLOSED_SESSION.formatted(user));
        } catch (final Exception ex) {
            LOGGER.error(() -> ERR_COULD_NOT_CLOSE_SESSION.formatted(user), ex);
        }
    }

}
