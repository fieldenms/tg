package ua.com.fielden.platform.web.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;

/// A factory for creating Web UI configuration for audit-entities.
///
@ImplementedBy(AuditWebUiConfigFactory.class)
public interface IAuditWebUiConfigFactory {

    /// Creates a Web UI configuration object for a synthetic audit-entity type.
    ///
    /// **Side-effect**: the created configuration is registered with the specified builder.
    ///
    /// @param auditedType  the type of audited entity whose audit types are used
    /// @param builder      the builder where the created configuration is registered
    ///
    AuditWebUiConfig create(Class<? extends AbstractEntity<?>> auditedType, IWebUiBuilder builder);

    /// Creates an embedded entity centre for a synthetic audit-entity type.
    ///
    /// @param auditedType   the type of audited entity whose audit types are used
    ///
    EntityCentre<?> createEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);

    /// Returns a Mi-type for the audit centre embedded into a master for the specified audited type.
    ///
    /// For example, given audited type `Vehicle`, returns Mi-type `MiVehicleMaster_ReVehicle_a3t`.
    ///
    Class<MiWithConfigurationSupport<?>> miTypeForEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);

}
