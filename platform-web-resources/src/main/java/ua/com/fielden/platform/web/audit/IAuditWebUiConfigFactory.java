package ua.com.fielden.platform.web.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * A factory for creating Web UI configuration for audit-entities.
 */
@ImplementedBy(AuditWebUiConfigFactory.class)
public interface IAuditWebUiConfigFactory {

    /**
     * Creates a Web UI configuration object for a synthetic audit-entity type.
     * <p>
     * <b>Side-effect</b>: the created configuration is registered with the specified builder.
     *
     * @param auditedType   type of an audited entity whose audit types are used
     */
    AuditWebUiConfig create(Class<? extends AbstractEntity<?>> auditedType, IWebUiBuilder builder);

    /**
     * Creates an embedded entity centre for a synthetic audit-entity type.
     *
     * @param auditedType   type of an audited entity whose audit types are used
     */
    EntityCentre<?> createEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);

}
