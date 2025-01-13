package ua.com.fielden.platform.companion;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.IAuditEntityCompanionGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import static ua.com.fielden.platform.audit.AuditUtils.*;

@Singleton
final class EntityCompanionGeneratorImpl implements IEntityCompanionGenerator {

    private final IAuditEntityCompanionGenerator auditCoGenerator;

    @Inject
    public EntityCompanionGeneratorImpl(final IAuditEntityCompanionGenerator auditCoGenerator) {
        this.auditCoGenerator = auditCoGenerator;
    }

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractEntity> type) {
        if (isAuditEntityType(type)) {
            return auditCoGenerator.generateCompanion((Class<? extends AbstractAuditEntity<?>>) type);
        }
        else if (isAuditPropEntityType(type)) {
            return auditCoGenerator.generateCompanionForAuditProp((Class<? extends AbstractAuditProp>) type);
        }
        else if (isSynAuditEntityType(type)) {
            return auditCoGenerator.generateCompanionForSynAuditEntity((Class<? extends AbstractSynAuditEntity>) type);
        }

        // Support for other kinds of generated companions should be added here

        throw new InvalidArgumentException("Companion object generation is unsupported for entity type [%s]".formatted(type.getTypeName()));
    }

}
