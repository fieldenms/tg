package ua.com.fielden.platform.dao.session;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.ISessionEnabled;

/**
 * A convenient transactional wrapper for executing instances of {@code Consumer<Connection>}.
 * It is important to get a new instrumented instance of {@code WithTransaction} each time for each execution, as each time a new db transaction starts and completes.
  * <pre>
  * supplier.get(WithTransaction.class).call(action);
 * </pre>

 * @author TG Team
 *
 */
public abstract class WithTransaction implements ISessionEnabled  {

    private Session session;
    private String transactionGuid;

    @Override
    public Session getSession() {
        if (session == null) {
            throw new IllegalStateException("Someone forgot to annotate some method with SessionRequired!");
        }
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }
    
    @Override
    public String getTransactionGuid() {
        if (isEmpty(transactionGuid)) {
            throw new IllegalStateException("Transaction GUID is missing.");
        }
        return transactionGuid;
    }
    
    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

}
