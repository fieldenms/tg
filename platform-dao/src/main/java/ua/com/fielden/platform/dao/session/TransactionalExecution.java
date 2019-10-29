package ua.com.fielden.platform.dao.session;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ua.com.fielden.platform.dao.annotations.SessionRequired;

/**
 * A convenient transactional wrapper for executing instances of {@code Consumer<Connection>} or {@code Supplier}.
 * It is important to get a new instrumented instance of {@code TransactionalExecution} each time for each execution, as each time a new db transaction starts and completes.
  * <pre>
  * supplier.get(WithTransaction.class).call(action);
 * </pre>

 * @author TG Team
 *
 */
public class TransactionalExecution extends WithTransaction {

    /**
     * A method for transactional execution of {@code action}, which requires a database connection as its argument.
     * @param action
     */
    @SessionRequired
    public void exec(final Consumer<Connection> action) {
        getSession().doWork(conn -> action.accept(conn));
    }

    /**
     * A method for transactional execution of {@code action} that does not require any arguments, but may return some value.
     * Type {@link Void} can be specified if action returns no result.
     * @param action
     * @return
     */
    @SessionRequired
    public <R> R exec(final Supplier<R> action) {
        return action.get();
    }

}
