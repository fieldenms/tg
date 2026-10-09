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
/// Committing is the point at which a unit of work becomes durable, so a failure there means nothing was persisted, however successfully the method itself ran.
/// [TransactionCommitException] is thrown for this, and only after the session has been discarded, so that cleanup is never skipped.
/// It is deliberately distinct from a business failure: the work was valid and its statements were accepted, but the
/// transaction could not be made durable — typically because of an infrastructure failure, such as a database
/// failover or a terminated connection.
///
/// A failure to *roll back* discards the session, as described below for a [VirtualMachineError], instead of closing it.
/// After such a failure, the state of the connection is unknown — the transaction may remain open on the server, holding its locks —
/// and the connection pool would reuse the connection unless the failure indicates a broken connection.
/// The same applies to a failed commit, which may leave the transaction incomplete (refer to `commitTransactionAndCloseSession`).
///
/// A [VirtualMachineError], such as [StackOverflowError] or [OutOfMemoryError], discards the session instead of rolling back its transaction.
/// Such an error can occur at any point, including in the middle of an exchange with the database, leaving the connection out of sync with the server,
/// so the connection is aborted rather than reused, and the database rolls back the transaction when the connection closes.
/// The same applies to an exception caused by such an error, as libraries may wrap it.
/// For example, the thread-bound Hibernate session is a JDK dynamic proxy, which wraps an error thrown through it in [java.lang.reflect.UndeclaredThrowableException].
/// Other errors, such as [LinkageError] or [AssertionError], are thrown at well-defined points, and are handled like exceptions,
/// so that an error recurring in a unit of work does not cost a connection each time.
/// The session is unbound from the thread before anything else, and the connection is aborted on a separate thread, because the stack of the current thread may be nearly exhausted.
/// If a new thread cannot be started, the connection is aborted on the current thread instead.
/// A failure to abort the connection does not prevent closing the session, unless it is a [VirtualMachineError], after which the session is left open, to be discarded again.
/// If discarding leaves the session open, or does not complete at all, the session remains recorded for the thread,
/// and the next invocation on the thread discards it before obtaining its own session; an invocation that cannot discard it fails.
/// Either way, an error affects only the unit of work in which it occurred, and not later units of work on the same thread, which matters for pooled threads.
///
/// Two further behaviours are not apparent from a call site:
///   - The session is put into [FlushMode#COMMIT], so Hibernate never auto-flushes.
///     DML reaches the database when something flushes it explicitly, and otherwise during the commit.
///   - If an intercepted method returns a [Stream], the transaction outlives that method call and is committed when the stream is closed.
///     Such a stream *must* be closed, ideally via try-with-resources, or its transaction and connection are never released.
///     If a failure propagates out of the traversal of the stream, closing it rolls back the transaction instead, or discards the session after a [VirtualMachineError],
///     as an owning invocation would, and throws [TransactionRollbackDueToThrowable] (refer to `completeTransactionOnClose`).
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
            WARN_DISCARD_SESSION_TIMEOUT = "[%s] Discarding a session did not complete within %s.",
            ERR_COULD_NOT_CLOSE_SESSION = "[%s] Could not close session.",
            ERR_COULD_NOT_ABORT_CONNECTION = "[%s] Could not abort connection.",
            ERR_COULD_NOT_DISCARD_SESSION = "[%s] Could not discard session.",
            ERR_COULD_NOT_DISCARD_SESSION_PENDING_CLEANUP = "[%s] Could not discard a session, whose cleanup after an error had not completed.",
            ERR_COULD_NOT_ROLLBACK = "[%s] Could not roll back transaction. The session was discarded.",
            ERR_COULD_NOT_COMMIT = "[%s] Could not commit transaction.";

    private static final Logger LOGGER = getLogger(SessionInterceptor.class);

    /// How long discarding a session waits for the thread that aborts its connection and closes it.
    /// Both normally complete in milliseconds, as aborting a connection involves no exchange with the server.
    /// Waiting ensures that the session is closed before the error propagates to enclosing scopes, which check whether it is still open.
    /// An interrupt does not end the wait, and the interrupt status of the thread is restored once the wait ends.
    /// The timeout bounds the delay of a failing unit of work if aborting or closing hangs: a warning is logged, the error propagates, and the discarding thread continues in the background.
    /// The delay is incurred once per unit of work, whatever the nesting depth of its session scopes, as the session is discarded at most once (refer to [CleanupState#discarderStarted]).
    private static final Duration DISCARD_SESSION_TIMEOUT = Duration.ofSeconds(10);
    /// The maximum number of throwables examined in a chain of causes, when determining whether a throwable is caused by a [VirtualMachineError].
    /// Actual chains are short — a few levels of wrapping by proxies, JDBC drivers and Hibernate.
    /// The bound guards against a chain that is unexpectedly long or cyclic: [Throwable#initCause] prevents only a throwable from being its own cause, and [Throwable#getCause] may be overridden.
    private static final int MAX_CAUSE_DEPTH = 32;
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
    private final ThreadLocal<CleanupState> cleanupState = ThreadLocal.withInitial(CleanupState::new);

    /// Per-thread record of the cleanup of a session after an error.
    /// It is a mutable holder, so that recording requires a field assignment only, and no method call that could overflow a nearly exhausted stack.
    ///
    private static final class CleanupState {
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
        private boolean discarderStarted;
        /// Whether the session of the current unit of work has been closed without rolling back its transaction, as it held no connection (refer to `rollbackTransactionAndCloseSession`).
        /// Hibernate still reports such a transaction as active, so this flag stands in for its status:
        /// if a nested scope closed the session this way, and an enclosing scope caught its failure, the owning invocation does not commit, as if the transaction had been rolled back.
        ///
        /// It is cleared at the same points as [#discarderStarted].
        private boolean closedWithoutRollback;
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
        final CleanupState cleanupState = this.cleanupState.get();
        // It might be the case that an earlier invocation on this thread owned a session scope that failed, and its error handling did not complete, for example, due to another StackOverflowError.
        // Its session may still be bound to this thread, with an active transaction, and a connection that may be out of sync with the server.
        // Obtaining the current session would then return that session, making this invocation a nested scope of a unit of work that has already failed.
        // To prevent this, the session is discarded first, typically with ample stack, as the failed invocation has unwound.
        // While a session is recorded, no session scope is active on this thread: it is recorded by the invocation that owns the scope,
        // once all nested invocations have completed, or, for a stream returned by such an invocation, when committing fails on closing the stream,
        // and is cleared once cleanup completes.
        if (cleanupState.sessionPendingCleanup != null) {
            discardSessionPendingCleanup(cleanupState, user);
        }

        final Session session = sessionFactory.get().getCurrentSession();
        final Transaction tr = session.getTransaction();
        // The invocation that begins the transaction owns the session scope and is responsible for completing it.
        final boolean ownsScope = !tr.isActive();
        // A new unit of work begins.
        // This must follow discarding a session pending cleanup, which may have been discarded already, as indicated by the flag.
        if (ownsScope) {
            cleanupState.discarderStarted = false;
            cleanupState.closedWithoutRollback = false;
        }
        // The thread-bound session is a proxy that rejects most methods without an active transaction, which is the case after a failed commit or rollback.
        // The underlying session is obtained while the transaction is active, so that it can be discarded after an error, whatever the transaction status.
        Session underlyingSession = null;

        try {
            // This variable indicates whether a transaction commit should be handled in this method invocation.
            // Basically, if a transaction is activated in this method, then it should be committed only in this method.
            // Therefore, shouldCommit is assigned true only when the transaction is activated here.
            final boolean shouldCommit = initTransaction(invocationOwner, session, tr, user);
            underlyingSession = session.unwrap(Session.class);

            // If we should not commit, which means the session was initiated earlier in the call stack,
            // and support for nested calls is not allowed, then an exception is thrown.
            if (!shouldCommit && !invocation.getStaticPart().getAnnotation(SessionRequired.class).allowNestedScope()) {
                throw new SessionScopingException(ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED.formatted(invocation.getMethod().getDeclaringClass().getName(), invocation.getMethod().getName()));
            }
            
            // Now let's proceed with the actual method invocation, which may throw an exception... or even a throwable...
            final Object result = invocation.proceed();
            
            // If this is the invocation that activated the current transaction, then we should commit it,
            // but only if the result of invocation is not a stream -- in that case, closing of the session is the responsibility of that stream.
            // A transaction whose session was closed without rolling back is treated as rolled back, although Hibernate reports it as active.
            if (shouldCommit && tr.isActive() && !cleanupState.closedWithoutRollback) {
                // If the result is a stream, then the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed.
                if (result instanceof Stream<?> stream) {
                    return completeTransactionOnClose(stream, cleanupState, session, underlyingSession, tr, user);
                }
                // Otherwise, commit the current transaction.
                else {
                    LOGGER.debug(() -> "[%s] Committing DB transaction".formatted(user));
                    commitTransactionAndCloseSession(cleanupState, session, underlyingSession, tr, user);
                    LOGGER.debug(() -> "[%s] Committed DB transaction".formatted(user));
                    return result;
                }
            }
            // Otherwise, this is the case of a nested transaction.
            // We should flush only if the current session is still open.
            // This check was not needed before migrating off Hibernate 3.2.6 GA.
            if (session.isOpen()) {
                session.flush();
            }
            return result;
        } catch (final Throwable e) {
            final Session discardableSession = underlyingSession != null ? underlyingSession : session;
            // A field assignment requires no stack frame, so the session is recorded even if the stack is nearly exhausted.
            if (ownsScope) {
                cleanupState.sessionPendingCleanup = discardableSession;
            }
            completeTransactionWithError(cleanupState, ownsScope, session, discardableSession, tr, e, user);
            // Exceptions propagate unchanged; other throwables are wrapped.
            throw e instanceof Exception ex ? ex : new TransactionRollbackDueToThrowable(e);
        }
    }

    /// Returns a stream that completes the transaction of the unit of work that `stream` was returned by, when it is closed.
    ///
    /// If the stream has been traversed without a failure, closing it commits the transaction.
    /// Otherwise, a failure that propagated out of its traversal is a failure of its unit of work, so closing the stream completes the transaction
    /// as the error handling of an owning invocation would: it rolls back the transaction, or, after a [VirtualMachineError] or an exception caused by one, discards the session,
    /// and then throws [TransactionRollbackDueToThrowable], whether or not the failure was caught after it propagated.
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
    /// @param cleanupState  the cleanup state of the thread that created the stream
    /// @param streamSession  the session underlying `session`
    ///
    private <T> Stream<T> completeTransactionOnClose(final Stream<T> stream, final CleanupState cleanupState, final Session session, final Session streamSession, final Transaction tr, final User user) {
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
            // The cleanup state of the thread that closes the stream, obtained before completing the transaction, so that the error handling needs no method call to obtain it.
            // It is obtained within the try block, so that an error in obtaining it, such as StackOverflowError, is handled like an error in committing.
            CleanupState closingCleanupState = null;
            try {
                closingCleanupState = this.cleanupState.get();
                if (traversalFailure == null) {
                    LOGGER.debug(() -> "[%s] Committing DB transaction on stream close.".formatted(user));
                    commitTransactionAndCloseSession(closingCleanupState, session, streamSession, tr, user);
                    LOGGER.debug(() -> "[%s] Committed DB transaction on stream close.".formatted(user));
                    return;
                }
            } catch (final RuntimeException | VirtualMachineError ex) {
                // Committing can fail in three ways that matter here:
                //   - With a RuntimeException not caused by a VirtualMachineError, typically TransactionCommitException, after the session has been discarded.
                //     It is logged and rethrown, as before.
                //   - With a VirtualMachineError, or a RuntimeException caused by one, such as UndeclaredThrowableException from the thread-bound session proxy.
                //     commitTransactionAndCloseSession propagates these without closing the session, so that it can be discarded instead.
                //   - Other errors, such as AssertionError, are not caught: commitTransactionAndCloseSession has already discarded the session, and they propagate unchanged.
                // No checked exception can occur, so RuntimeException covers all exceptions.
                // This handler runs when the stream is closed, after this invocation has returned, and thus outside its error handling.
                // Without an enclosing invocation to discard the session, it is discarded here (refer to commitTransactionAndCloseSession).
                // Everything here acts on the thread that closes the stream: unbinding the session, as part of discarding it, removing the transaction GUID,
                // and, except in the case described below, recording the session pending cleanup, so that it does not affect a unit of work in progress on another thread.
                // If the stream were closed on a thread other than the one that created it, the session would remain bound to the latter;
                // streams are expected to be consumed and closed by the thread that created them, ideally with try-with-resources.
                //
                // If obtaining the cleanup state of the closing thread failed, the cleanup state of the thread that created the stream is used instead.
                // It is the cleanup state of the closing thread if the stream is closed as expected, by the thread that created it.
                // Selecting it requires no method call.
                final CleanupState recordingCleanupState = closingCleanupState != null ? closingCleanupState : cleanupState;
                // As in the error handling of an invocation, the session is recorded first, by a field assignment, which requires no stack frame.
                // If discarding it does not complete, the next invocation on this thread discards it, instead of becoming a nested scope of the failed unit of work.
                // A session that has already been closed, or is being discarded, remains recorded only until the record is cleared below.
                recordingCleanupState.sessionPendingCleanup = streamSession;
                // The transaction GUID is removed whether or not the session is discarded.
                // commitTransactionAndCloseSession leaves it in place when propagating a VirtualMachineError, or an exception caused by one,
                // which includes the case where Hibernate has rolled back the transaction and closed the session.
                transactionGuid.remove();
                if (isCausedByVirtualMachineError(ex) && streamSession.isOpen()) {
                    discardSession(recordingCleanupState, streamSession, user);
                }
                clearSessionPendingCleanup(recordingCleanupState);
                LOGGER.fatal(() -> "[%s] Could not commit DB transaction on stream close.".formatted(user), ex);
                throw ex;
            }
            // The traversal of the stream failed, and the cleanup state of the closing thread has been obtained.
            // As in the error handling of an owning invocation, the session is recorded first, by a field assignment, which requires no stack frame,
            // and the record is cleared once the transaction has been completed.
            // The failure is wrapped, rather than rethrown, as it has propagated already: try-with-resources would add it to itself as a suppressed exception, which is not permitted.
            closingCleanupState.sessionPendingCleanup = streamSession;
            completeTransactionWithError(closingCleanupState, true, session, streamSession, tr, traversalFailure, user);
            throw new TransactionRollbackDueToThrowable(traversalFailure);
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

    /// Completes the session scope after `th`, and logs it, but does not throw it, which is left to the caller.
    ///
    /// Cleanup precedes logging, so that it runs with as much stack as is available.
    /// If cleanup does not complete, the session remains recorded in `cleanupState`, and is discarded by the next invocation on this thread.
    ///
    /// @param ownsScope  whether the failed invocation owns the session scope, in which case the unit of work completes with this invocation
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to discard it after a [VirtualMachineError], or if rolling back fails
    ///
    private void completeTransactionWithError(final CleanupState cleanupState, final boolean ownsScope, final Session session, final Session discardableSession, final Transaction tr, final Throwable th, final User user) {
        try {
            if (isCausedByVirtualMachineError(th)) {
                // In nested scopes, the innermost scope discards the session, and the enclosing scopes find it closed,
                // or, if discarding has not completed in time, skip it in discardSession.
                if (discardableSession.isOpen()) {
                    discardSession(cleanupState, discardableSession, user);
                }
            }
            // Otherwise, if the transaction is active, it should be rolled back.
            else if (session.isOpen() && tr.isActive()) {
                LOGGER.debug(() -> "[%s] Rolling back DB transaction".formatted(user));
                rollbackTransactionAndCloseSession(cleanupState, session, discardableSession, tr, user);
                LOGGER.debug(() -> "[%s] Rolled back DB transaction".formatted(user));
            }
        } finally {
            transactionGuid.remove();
        }
        if (ownsScope) {
            clearSessionPendingCleanup(cleanupState);
            cleanupState.discarderStarted = false;
            cleanupState.closedWithoutRollback = false;
        }

        switch (th) {
            // Most Result exceptions are validation errors, which are more relevant for debug messages.
            case Result _ -> LOGGER.debug(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case SessionScopingException _ -> LOGGER.error(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case TransactionCommitException _ -> {} // Already logged before.
            default -> LOGGER.warn(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
        }
    }

    /// Determines whether `th` is a [VirtualMachineError], or is caused by one.
    /// The search is bounded, as a chain of causes may, in principle, be cyclic.
    ///
    private static boolean isCausedByVirtualMachineError(final Throwable th) {
        Throwable cause = th;
        for (int depth = 0; cause != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (cause instanceof VirtualMachineError) {
                return true;
            }
            cause = cause.getCause();
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
    private void discardSessionPendingCleanup(final CleanupState cleanupState, final User user) {
        transactionGuid.remove();
        discardSession(cleanupState, cleanupState.sessionPendingCleanup, user);
        if (!cleanupState.discarderStarted) {
            throw new SessionScopingException(ERR_COULD_NOT_DISCARD_SESSION_PENDING_CLEANUP.formatted(user));
        }
        cleanupState.sessionPendingCleanup = null;
        LOGGER.warn(() -> WARN_DISCARDED_SESSION_PENDING_CLEANUP.formatted(user));
    }

    /// Clears the record of the session pending cleanup, unless that session is still open, and no thread is discarding it, which is the case if discarding it left it open.
    /// Such a session remains recorded, so that the next invocation on this thread discards it again.
    ///
    private static void clearSessionPendingCleanup(final CleanupState cleanupState) {
        final Session session = cleanupState.sessionPendingCleanup;
        if (session == null || cleanupState.discarderStarted || !session.isOpen()) {
            cleanupState.sessionPendingCleanup = null;
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
    /// if discarding has completed, but left the session open, it remains recorded for the thread by the invocation that owns the session scope (refer to `clearSessionPendingCleanup`).
    /// An error on the current thread, for example, in starting the thread or in logging, propagates, and the session remains recorded likewise.
    ///
    /// The session is discarded at most once per unit of work.
    /// Once the discarding thread has been started, or the session has been discarded on the current thread, later calls within the same unit of work return immediately,
    /// unless discarding has completed, but left the session open (refer to [CleanupState#discarderStarted]).
    ///
    private void discardSession(final CleanupState cleanupState, final Session session, final User user) {
        if (cleanupState.discarderStarted) {
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
                cleanupState.discarderStarted = true;
            }
            LOGGER.warn("[{}] Could not start a thread to discard a session, which is discarded on the current thread instead.", user, ex);
            return;
        }
        cleanupState.discarderStarted = true;
        if (!awaitTermination(discarder)) {
            LOGGER.warn(() -> WARN_DISCARD_SESSION_TIMEOUT.formatted(user, DISCARD_SESSION_TIMEOUT));
        }
        else if (session.isOpen()) {
            cleanupState.discarderStarted = false;
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
    /// returning the connection to the pool in a known state, so discarding the session costs no connection.
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
    /// If committing fails otherwise, the session is discarded, as after a failed rollback.
    /// The transaction may not have completed, in which case the state of the connection is unknown, and the connection pool would reuse it unless the failure indicates a broken connection.
    /// A session that holds no connection is closed instead, as there is no connection to abort.
    /// This is the case if acquiring a connection for committing failed, and after failures that Hibernate rolls back, which close the session, so they cost no connection.
    ///
    /// Committing requires a connection, which the session does not hold yet if the unit of work has executed no statement.
    /// Hibernate would acquire it on committing, and if that failed, roll back the transaction, which would acquire a connection again,
    /// waiting for one a second time if the connection pool is exhausted or the database is unreachable.
    /// The connection is therefore acquired before committing, which costs nothing extra, as committing would acquire it anyway.
    ///
    /// In all cases other than a [VirtualMachineError], the session is closed or discarded before this method completes.
    ///
    /// @param discardableSession  the session underlying `session`, which is used to acquire its connection, and to discard it if committing fails
    ///
    private void commitTransactionAndCloseSession(final CleanupState cleanupState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
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
            // A failed commit means the unit of work was not persisted, and must not be reported as success.
            // The session is discarded or closed before this exception propagates, so it is never left as a dead current session.
            transactionGuid.remove();
            if (holdsConnection(discardableSession)) {
                discardSession(cleanupState, discardableSession, user);
            } else {
                closeSession(session, user);
            }
            throw new TransactionCommitException(ERR_COULD_NOT_COMMIT.formatted(user), ex);
        } catch (final Error err) {
            transactionGuid.remove();
            discardSession(cleanupState, discardableSession, user);
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
    /// Hibernate still reports the transaction of a session closed this way as active, which is recorded in `cleanupState`, so that the owning invocation treats it as rolled back (refer to [CleanupState#closedWithoutRollback]).
    ///
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to determine whether it holds a connection, and to discard it if rolling back fails
    ///
    private void rollbackTransactionAndCloseSession(final CleanupState cleanupState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
        if (discardableSession != session && !holdsConnection(discardableSession)) {
            cleanupState.closedWithoutRollback = true;
            closeSession(session, user);
            return;
        }
        try {
            if (tr.isActive()) {
                tr.rollback();
            }
        } catch (final Throwable ex) {
            discardSession(cleanupState, discardableSession, user);
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
