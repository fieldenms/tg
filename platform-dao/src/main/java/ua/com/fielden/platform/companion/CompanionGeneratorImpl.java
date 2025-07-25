package ua.com.fielden.platform.companion;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.audit.*;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import static ua.com.fielden.platform.audit.AuditUtils.*;

@Singleton
final class CompanionGeneratorImpl implements ICompanionGenerator {

    private final IAuditCompanionGenerator auditCoGenerator;

    @Inject
    public CompanionGeneratorImpl(final IAuditCompanionGenerator auditCoGenerator) {
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
        else if (isSynAuditPropEntityType(type)) {
            return auditCoGenerator.generateCompanionForSynAuditProp((Class<? extends AbstractSynAuditProp>) type);
        }

        // Support for other kinds of generated companions should be added here

        throw new InvalidArgumentException("Companion object generation is unsupported for entity type [%s]".formatted(type.getTypeName()));
    }

}
