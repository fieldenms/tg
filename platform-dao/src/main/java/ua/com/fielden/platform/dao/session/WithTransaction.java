package ua.com.fielden.platform.dao.session;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;

/// A convenient transactional wrapper for executing instances of `Consumer<Connection>`.
/// It is important to get a new instrumented instance of `WithTransaction` each time for each execution, as each time a new db transaction starts and completes.
///
/// ```Java
/// supplier.get(WithTransaction.class).call(action);
/// ```
///
public abstract class WithTransaction implements ISessionEnabled  {

    public static final String
            ERR_SESSION_IS_MISSING = "Session is missing, most likely, due to missing @%s annotation.".formatted(SessionRequired.class.getSimpleName()),
            ERR_SESSION_IS_CLOSED = "Session is closed, most likely, due to missing @%s annotation.".formatted(SessionRequired.class.getSimpleName()),
            ERR_TRANSACTION_GUID_IS_MISSING = "Transaction GUID is missing.";

    private Session session;
    private String transactionGuid;

    @Override
    public Session getSession() {
        if (session == null) {
            throw new InvalidStateException(ERR_SESSION_IS_MISSING);
        } else if (!session.isOpen()) {
            throw new InvalidStateException(ERR_SESSION_IS_CLOSED);
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
            throw new InvalidStateException(ERR_TRANSACTION_GUID_IS_MISSING);
        }
        return transactionGuid;
    }
    
    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

}
