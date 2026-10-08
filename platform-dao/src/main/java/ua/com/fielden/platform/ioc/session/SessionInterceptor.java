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
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

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
/// A failure to *begin* a transaction closes the session before the failure propagates.
/// Such a failure typically indicates a broken connection, for example, after the database server has reset it.
/// Current sessions are bound to threads, and a transaction that failed to begin is inactive, so the session would otherwise remain bound to the thread,
/// holding on to the broken connection and failing every subsequent unit of work on that thread.
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
/// If even that does not complete, the session remains recorded for the thread, and the next invocation on the thread discards it before obtaining its own session.
/// Either way, an error affects only the unit of work in which it occurred, and not later units of work on the same thread, which matters for pooled threads.
///
/// Two further behaviours are not apparent from a call site:
///   - The session is put into [FlushMode#COMMIT], so Hibernate never auto-flushes.
///     DML reaches the database when something flushes it explicitly, and otherwise during the commit.
///   - If an intercepted method returns a [Stream], the transaction outlives that method call and is committed when the stream is closed.
///     Such a stream *must* be closed, ideally via try-with-resources, or its transaction and connection are never released.
///
/// Finally, a GUID is generated per transaction and assigned to the invocation owner, which is how a single unit of work is identified downstream — by auditing, for example.
///
public class SessionInterceptor implements MethodInterceptor {
    public static final String
            MSG_CLOSING_SESSION = "[%s] Closing session.",
            MSG_CLOSED_SESSION = "[%s] Closed session.",
            MSG_CLOSED_DISCARDED_SESSION_WITH_ERROR = "[%s] Closed a discarded session with error.",
            WARN_TRANSACTION_ROLLBACK = "[%s] Transaction completed (rolled back) with error.",
            WARN_DISCARDING_SESSION_PENDING_CLEANUP = "[%s] Discarding a session, whose cleanup after an error did not complete.",
            WARN_DISCARD_SESSION_TIMEOUT = "[%s] Discarding a session did not complete within %s.",
            ERR_COULD_NOT_CLOSE_SESSION = "[%s] Could not close session.",
            ERR_COULD_NOT_ABORT_CONNECTION = "[%s] Could not abort connection.",
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
    private final ThreadLocal<String> transactionGuid = new ThreadLocal<>();
    private final ThreadLocal<CleanupState> cleanupState = ThreadLocal.withInitial(CleanupState::new);

    /// Per-thread record of the cleanup of a session after an error.
    /// It is a mutable holder, so that recording requires a field assignment only, and no method call that could overflow a nearly exhausted stack.
    ///
    private static final class CleanupState {
        /// A session whose cleanup after an error has not completed.
        private Session sessionPendingCleanup;
        /// Whether a thread that discards the session of the current unit of work has been started.
        /// Once it has, the session is not discarded again within the same unit of work, even if it is still open, which is the case if the thread has not completed within [#DISCARD_SESSION_TIMEOUT].
        /// This keeps enclosing scopes from starting further threads that discard the same session, and from waiting for each of them.
        /// A unit of work has a single session, which scopes may refer to either directly or through its thread-bound proxy, so the flag records that a discarding thread was started, not which object it was started for.
        ///
        /// It is cleared when an owning invocation begins, so that it never carries over to a later unit of work,
        /// including after a unit of work that completes without reaching its error handling, for example, when an enclosing scope catches the error and returns a stream.
        /// It is also cleared when an owning invocation completes with an error.
        private boolean discarderStarted;
    }

    public SessionInterceptor(final Provider<? extends SessionFactory> sessionFactory) {
        this.sessionFactory = sessionFactory;
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
            if (shouldCommit && tr.isActive()) {
                // If the result is a stream, then the current transaction becomes associated with that stream
                // and needs to be committed once the stream has been processed.
                if (result instanceof Stream<?> stream) {
                    final Session streamSession = underlyingSession;
                    return stream.onClose(() -> {
                        // The cleanup state of the thread that closes the stream, obtained before committing, so that the error handling needs no method call to obtain it.
                        // It is obtained within the try block, so that an error in obtaining it, such as StackOverflowError, is handled like an error in committing.
                        CleanupState closingCleanupState = null;
                        try {
                            closingCleanupState = this.cleanupState.get();
                            LOGGER.debug(() -> "[%s] Committing DB transaction on stream close.".formatted(user));
                            commitTransactionAndCloseSession(closingCleanupState, session, streamSession, tr, user);
                            LOGGER.debug(() -> "[%s] Committed DB transaction on stream close.".formatted(user));
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
                            // A session that has already been closed remains recorded only until the record is cleared below.
                            recordingCleanupState.sessionPendingCleanup = streamSession;
                            if (isCausedByVirtualMachineError(ex) && streamSession.isOpen()) {
                                transactionGuid.remove();
                                discardSession(recordingCleanupState, streamSession, user);
                            }
                            recordingCleanupState.sessionPendingCleanup = null;
                            LOGGER.fatal(() -> "[%s] Could not commit DB transaction on stream close.".formatted(user), ex);
                            throw ex;
                        }
                    });
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
            throw completeTransactionWithError(cleanupState, ownsScope, session, discardableSession, tr, e, user);
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
                // The transaction remains inactive, so the error handling of the invocation would neither roll it back nor close the session.
                // An open session stays bound to the current thread, and holds on to the connection that failed to begin the transaction.
                // Closing it here ensures that the next unit of work on this thread obtains a new session and a new connection.
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

    /// Completes the session scope after `th`, and returns the exception to be thrown.
    ///
    /// Cleanup precedes logging, so that it runs with as much stack as is available.
    /// If cleanup does not complete, the session remains recorded in `cleanupState`, and is discarded by the next invocation on this thread.
    ///
    /// @param ownsScope  whether the failed invocation owns the session scope, in which case the unit of work completes with this invocation
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to discard it after a [VirtualMachineError], or if rolling back fails
    ///
    private Exception completeTransactionWithError(final CleanupState cleanupState, final boolean ownsScope, final Session session, final Session discardableSession, final Transaction tr, final Throwable th, final User user) {
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
            cleanupState.sessionPendingCleanup = null;
            cleanupState.discarderStarted = false;
        }

        switch (th) {
            // Most Result exceptions are validation errors, which are more relevant for debug messages.
            case Result _ -> LOGGER.debug(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case SessionScopingException _ -> LOGGER.error(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
            case TransactionCommitException _ -> {} // Already logged before.
            default -> LOGGER.warn(() -> WARN_TRANSACTION_ROLLBACK.formatted(user), th);
        }
        return th instanceof Exception ex ? ex : new TransactionRollbackDueToThrowable(th);
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
    private void discardSessionPendingCleanup(final CleanupState cleanupState, final User user) {
        final Session session = cleanupState.sessionPendingCleanup;
        cleanupState.sessionPendingCleanup = null;
        LOGGER.warn(() -> WARN_DISCARDING_SESSION_PENDING_CLEANUP.formatted(user));
        transactionGuid.remove();
        discardSession(cleanupState, session, user);
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
    /// The session is discarded at most once per unit of work.
    /// Once the discarding thread has been started, later calls within the same unit of work return immediately (refer to [CleanupState#discarderStarted]).
    ///
    private void discardSession(final CleanupState cleanupState, final Session session, final User user) {
        if (cleanupState.discarderStarted) {
            return;
        }
        ThreadLocalSessionContext.unbind(session.getSessionFactory());
        final Thread discarder = DISCARDER_THREAD_FACTORY.newThread(new SessionDiscarder(session, user));
        discarder.start();
        cleanupState.discarderStarted = true;
        if (!awaitTermination(discarder)) {
            LOGGER.warn(() -> WARN_DISCARD_SESSION_TIMEOUT.formatted(user, DISCARD_SESSION_TIMEOUT));
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
    private record SessionDiscarder(Session session, User user) implements Runnable {
        @Override
        public void run() {
            // The connection is aborted regardless of the transaction status, which may be inaccurate after an error, for example, during commit.
            try {
                if (!session.isOpen()) {
                    return;
                }
                final LogicalConnectionImplementor logicalConnection = session.unwrap(SharedSessionContractImplementor.class).getJdbcCoordinator().getLogicalConnection();
                if (logicalConnection.isPhysicallyConnected()) {
                    logicalConnection.getPhysicalConnection().abort(Runnable::run);
                } else {
                    LOGGER.debug(() -> "[%s] A discarded session holds no connection.".formatted(user));
                }
            } catch (final Exception ex) {
                LOGGER.error(() -> ERR_COULD_NOT_ABORT_CONNECTION.formatted(user), ex);
            }
            // Closing a session with an aborted connection reports errors, which are expected.
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
    /// Failures that Hibernate rolls back close the session, which discarding skips, so they cost no connection (refer to [SessionDiscarder]).
    ///
    /// In all cases other than a [VirtualMachineError], the session is closed or discarded before this method completes.
    ///
    /// @param discardableSession  the session underlying `session`, which is used to discard it if committing fails
    ///
    private void commitTransactionAndCloseSession(final CleanupState cleanupState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
        try {
            if (tr.isActive()) {
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
            // The session is discarded before this exception propagates, so it is never left as a dead current session.
            transactionGuid.remove();
            discardSession(cleanupState, discardableSession, user);
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
    /// @param discardableSession  the session underlying `session`, if obtained, which is used to discard it if rolling back fails
    ///
    private void rollbackTransactionAndCloseSession(final CleanupState cleanupState, final Session session, final Session discardableSession, final Transaction tr, final User user) {
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
