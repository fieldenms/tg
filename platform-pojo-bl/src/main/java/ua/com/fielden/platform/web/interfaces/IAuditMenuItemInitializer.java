package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AuditCompoundMenuItem;

/// A contract for initialising the audit menu item with embedded centre data, required to load the centre on the client side.
///
public interface IAuditMenuItemInitializer {

    /// Initialises the given {@link AuditCompoundMenuItem} instance with entity centre data and returns it.
    ///
    /// @param auditedType the type of the audited entity used to determine which auditing entity centre should initialize the given `menuItem`
    AuditCompoundMenuItem init(final Class<? extends AbstractEntity<?>> auditedType, AuditCompoundMenuItem menuItem);
}
