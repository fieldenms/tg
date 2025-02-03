package ua.com.fielden.platform.web.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * A factory for creating Web UI configuration for audit-entities.
 */
@ImplementedBy(AuditWebUiConfigFactoryImpl.class)
public interface AuditWebUiConfigFactory {

    AuditWebUiConfig create(Class<? extends AbstractEntity<?>> auditedType, IWebUiBuilder builder);

    EntityCentre<?> createEmbeddedCentre(Class<? extends AbstractEntity<?>> auditedType);

}
