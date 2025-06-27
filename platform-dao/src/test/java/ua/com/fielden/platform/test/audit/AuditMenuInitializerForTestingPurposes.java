package ua.com.fielden.platform.test.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AuditCompoundMenuItem;
import ua.com.fielden.platform.web.interfaces.IAuditMenuItemInitializer;

/// Implementation of {@link IAuditMenuItemInitializer} for testing purposes.
///
public class AuditMenuInitializerForTestingPurposes implements IAuditMenuItemInitializer {

    @Override
    public AuditCompoundMenuItem init(final Class<? extends AbstractEntity<?>> auditedType, final AuditCompoundMenuItem menuItem) {
        return null;
    }
}
