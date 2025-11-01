package ua.com.fielden.platform.dao.session;

import com.google.inject.Inject;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// A convenient transactional wrapper for executing instances of [Consumer] or [Supplier] of [Connection] .
/// It is important to get a new instrumented instance of [TransactionalExecution] each time for each execution, as each time a new db transaction starts and completes.
///
/// ```Java
/// supplier.get(TransactionalExecution.class).call(action);
/// ```
///
public class TransactionalExecution extends WithTransaction {
    
    private final IUserProvider up;
    private final Supplier<Session> maybeSessionSupplier;

    @Inject
    public TransactionalExecution(final IUserProvider up) {
        this.up = up;
        this.maybeSessionSupplier = super::getSession;
    }

    /// A constructor that should be used in situations where IoC is not possible and `sessionSupplier` can be provided.
    /// Instantiation without IoC cannot instrument an instance and therefore cannot provide supplier a session for methods [#exec(Supplier)] and [#exec(Consumer)].
    /// This is why `sessionSupplier` needs to be provided.
    ///
    public TransactionalExecution(final IUserProvider up, final Supplier<Session> sessionSupplier) {
        this.up = up;
        this.maybeSessionSupplier = sessionSupplier;
    }

    /// Executes the specified `action` transactionally, providing a database connection as its argument.
    ///
    @SessionRequired
    public void exec(final Consumer<Connection> action) {
        getSession().doWork(action::accept);
    }

    /// Executes the specified `action` transactionally, providing a database connection as its argument.
    ///
    /// This is a **strict** execution method — it throws an exception if invoked within the scope of an existing session.
    ///
    @SessionRequired(allowNestedScope = false)
    public void execStrict(final Consumer<Connection> action) {
        getSession().doWork(action::accept);
    }

    /// Executes the specified `action` transactionally, which does not require any arguments, and returns its result.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired
    public <R> R exec(final Supplier<R> action) {
        return action.get();
    }

    /// Executes the specified `action` transactionally, which does not require any arguments, and returns its result.
    ///
    /// This is a **strict** execution method — it throws an exception if invoked within the scope of an existing session.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired(allowNestedScope = false)
    public <R> R execStrict(final Supplier<R> action) {
        return action.get();
    }

    /// Executes the specified `action` transactionally, providing a database connection, and returns its result.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired
    public <R> R execFn(final Function<Connection, R> action) {
        return getSession().doReturningWork(action::apply);
    }

    /// Executes the specified `action` transactionally, providing a database connection, and returns its result.
    ///
    /// This is a **strict** execution method — it throws an exception if invoked within the scope of an existing session.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired(allowNestedScope = false)
    public <R> R execFnStrict(final Function<Connection, R> action) {
        return getSession().doReturningWork(action::apply);
    }

    /// Executes the specified `action` transactionally, providing this instance of [ISessionEnabled], and returns its result.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired
    public <R> R execWithSession(final Function<ISessionEnabled, R> action) {
        return action.apply(this);
    }

    /// Executes the specified `action` transactionally, providing this instance of [ISessionEnabled], and returns its result.
    ///
    /// This is a **strict** execution method — it throws an exception if invoked within the scope of an existing session.
    ///
    /// The return type `R` can be any type.
    /// Specify [Void] if the action does not return a result.
    ///
    @SessionRequired(allowNestedScope = false)
    public <R> R execWithSessionStrict(final Function<ISessionEnabled, R> action) {
        return action.apply(this);
    }

    @Override
    public User getUser() {
        return up.getUser();
    }

    @Override
    public Session getSession() {
        return maybeSessionSupplier.get();
    }

}
