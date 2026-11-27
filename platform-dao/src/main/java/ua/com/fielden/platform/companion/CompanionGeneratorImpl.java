package ua.com.fielden.platform.companion;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.audit.*;
import ua.com.fielden.platform.dao.AbstractEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import static ua.com.fielden.platform.audit.AuditUtils.*;

@Singleton
final class CompanionGeneratorImpl implements ICompanionGenerator {

    private final IAuditCompanionGenerator auditCoGenerator;
    private final Injector injector;

    @Inject
    public CompanionGeneratorImpl(final IAuditCompanionGenerator auditCoGenerator, final Injector injector) {
        this.auditCoGenerator = auditCoGenerator;
        this.injector = injector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends AbstractEntity<?>> IEntityDao<E> generateCompanion(final Class<E> type) {
        if (isAuditEntityType(type)) {
            return (IEntityDao<E>) auditCoGenerator.generateCompanion((Class<? extends AbstractAuditEntity<?>>) type);
        }
        else if (isAuditPropEntityType(type)) {
            return (IEntityDao<E>) auditCoGenerator.generateCompanionForAuditProp((Class<? extends AbstractAuditProp<?>>) type);
        }
        else if (isSynAuditEntityType(type)) {
            return (IEntityDao<E>) auditCoGenerator.generateCompanionForSynAuditEntity((Class<? extends AbstractSynAuditEntity<?>>) type);
        }
        else if (isSynAuditPropEntityType(type)) {
            return (IEntityDao<E>) auditCoGenerator.generateCompanionForSynAuditProp((Class<? extends AbstractSynAuditProp<?>>) type);
        }
        else {
            return generateSimpleCompanion(type);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends AbstractEntity<?>> IEntityDao<E> generateSimpleCompanion(final Class<E> type) {
        final GenericEntityDao<E> dao = injector.getInstance(GenericEntityDao.class);
        dao.entityType = type;
        dao.keyType = AnnotationReflector.getKeyType(type);
        return dao;
    }

    static class GenericEntityDao<T extends AbstractEntity<?>> extends AbstractEntityDao<T> {

        Class<T> entityType;
        Class<? extends Comparable<?>> keyType;

        @Override
        public Class<T> getEntityType() {
            return entityType;
        }

        @Override
        public Class<? extends Comparable<?>> getKeyType() {
            return keyType;
        }

    }

}
