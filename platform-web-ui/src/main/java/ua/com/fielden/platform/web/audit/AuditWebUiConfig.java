package ua.com.fielden.platform.web.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * @param centre  entity centre for a synthetic audit-entity
 * @param auditType  type of a synthetic audit-entity associated with the centre
 */
public record AuditWebUiConfig (EntityCentre<?> centre, Class<? extends AbstractEntity<?>> auditType) {}
