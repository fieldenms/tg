package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent audit-entity test type for [Entity_Simple].
/// Used to verify that `auditDate` is indexed in descending order.
///
@MapEntityTo
@KeyType(ua.com.fielden.platform.entity.DynamicEntityKey.class)
public class Entity_Audit extends AbstractAuditEntity<Entity_Simple> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Entity_Simple auditedEntity;

    @Override
    public Entity_Simple getAuditedEntity() {
        return auditedEntity;
    }

    @Override
    @Observable
    public Entity_Audit setAuditedEntity(final Entity_Simple auditedEntity) {
        this.auditedEntity = auditedEntity;
        return this;
    }

}
