package ua.com.fielden.platform.audit;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.eql.dbschema.PropertyInliner;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.reflection.AnnotationReflector;

/// A generator responsible for generating companion objects at runtime for audit entities.
///
@Singleton
final class AuditCompanionGenerator implements IAuditCompanionGenerator {

    private final AuditingMode auditingMode;
    private final Injector injector;
    private final IAuditTypeFinder a3tFinder;
    private final IDomainMetadata domainMetadata;
    private final PropertyInliner propInliner;

    @Inject
    AuditCompanionGenerator(
            final AuditingMode auditingMode,
            final Injector injector,
            final IAuditTypeFinder a3tFinder,
            final IDomainMetadata domainMetadata,
            final PropertyInliner propInliner)
    {
        this.auditingMode = auditingMode;
        this.injector = injector;
        this.a3tFinder = a3tFinder;
        this.domainMetadata = domainMetadata;
        this.propInliner = propInliner;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends AbstractAuditEntity<?>> IEntityDao<E> generateCompanion(final Class<E> type) {
        assertAuditingEnabled();
        final CommonAuditEntityDao dao = injector.getInstance(CommonAuditEntityDao.class);
        dao.init(type, AnnotationReflector.getKeyType(type), auditingMode, a3tFinder, domainMetadata, propInliner);
        return dao;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends AbstractAuditProp<?>> IEntityDao<E> generateCompanionForAuditProp(final Class<E> type) {
        assertAuditingEnabled();
        final CommonAuditPropDao dao = injector.getInstance(CommonAuditPropDao.class);
        dao.init(type, AnnotationReflector.getKeyType(type), auditingMode, domainMetadata, a3tFinder);
        return dao;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends AbstractSynAuditEntity<?>> IEntityDao<E> generateCompanionForSynAuditEntity(final Class<E> type) {
        assertAuditingEnabled();
        final CommonSynAuditEntityDao dao = injector.getInstance(CommonSynAuditEntityDao.class);
        dao.init(type, AnnotationReflector.getKeyType(type), auditingMode, a3tFinder, domainMetadata);
        return dao;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends AbstractSynAuditProp<?>> IEntityDao<E> generateCompanionForSynAuditProp(final Class<E> type) {
        assertAuditingEnabled();
        final CommonSynAuditPropDao dao = injector.getInstance(CommonSynAuditPropDao.class);
        dao.init(type, AnnotationReflector.getKeyType(type));
        return dao;
    }

    private void assertAuditingEnabled() {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }
    }

}
