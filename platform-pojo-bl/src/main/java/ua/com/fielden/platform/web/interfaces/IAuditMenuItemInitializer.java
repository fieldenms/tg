package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AuditCompoundMenuItem;

/// A contract to initialize the audit menu item with embedded centre data is needed in order to load this centre on the client side.
///
public interface IAuditMenuItemInitializer {

    /// Initialise the given {@link AuditCompoundMenuItem} instance with entity centre data and returns it.
    ///
    /// @param auditedType the type of audited entity for which the corresponding auditing entity centre should be used to initiate the given `menuItem`
    AuditCompoundMenuItem init(final Class<? extends AbstractEntity<?>> auditedType, AuditCompoundMenuItem menuItem);
}
