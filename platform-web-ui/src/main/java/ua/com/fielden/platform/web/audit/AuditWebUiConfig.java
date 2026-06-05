package ua.com.fielden.platform.web.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;

/// Web UI configuration for a synthetic audit entity,
/// tying together the corresponding entity centre and audit-entity type.
///
/// @param centre    the entity centre associated with the synthetic audit-entity
/// @param auditType the concrete audit-entity type associated with the centre
///
public record AuditWebUiConfig(EntityCentre<?> centre, Class<? extends AbstractEntity<?>> auditType) {}
