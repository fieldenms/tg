package ua.com.fielden.platform.audit.exceptions;

import ua.com.fielden.platform.audit.AuditingMode;

public class AuditingModeException extends AuditingRuntimeException {

    public AuditingModeException(final String message) {
        super(message);
    }

    public static AuditingModeException cannotBeUsed(final Class<?> type, final AuditingMode auditingMode) {
        return new AuditingModeException("[%s] cannot be used when the auditing mode is [%s].".formatted(type.getCanonicalName(), auditingMode));
    }

}
