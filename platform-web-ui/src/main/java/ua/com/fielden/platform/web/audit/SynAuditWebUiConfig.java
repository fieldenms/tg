package ua.com.fielden.platform.web.audit;

import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;

public record SynAuditWebUiConfig<E extends AbstractEntity<?>>
        (EntityCentre<AbstractSynAuditEntity<E>> centre)
{}
