package ua.com.fielden.platform.web.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * A factory for creating Web UI configuration for synthetic audit-entities.
 */
@ImplementedBy(SynAuditWebUiConfigFactoryImpl.class)
public interface SynAuditWebUiConfigFactory {

    <E extends AbstractEntity<?>> SynAuditWebUiConfig<E> create(Class<E> auditedType, IWebUiBuilder builder);

    <E extends AbstractEntity<?>> EntityCentre<E> createEmbeddedCentre(Class<E> auditedType);

}
