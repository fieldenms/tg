package ua.com.fielden.platform.audit;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * Generated companions should always be based on the original entity type.
 * If an instrumented entity type is provided, its original type should be used.
 */
@Singleton
final class AuditCompanionGenerator implements IAuditCompanionGenerator {

    private final AuditingMode auditingMode;

    @Inject
    AuditCompanionGenerator(final AuditingMode auditingMode) {
        this.auditingMode = auditingMode;
    }

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity> type) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType((Class<? extends AbstractEntity<?>>) type);

        return new ByteBuddy()
                .subclass(CommonAuditEntityDao.class)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();
    }

    @Override
    public Class<?> generateCompanionForAuditProp(final Class<? extends AbstractAuditProp> type) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType((Class<? extends AbstractEntity<?>>) type);

        return new ByteBuddy()
                .subclass(CommonAuditPropDao.class)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();
    }

    @Override
    public Class<?> generateCompanionForSynAuditEntity(final Class<? extends AbstractSynAuditEntity> type) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType((Class<? extends AbstractEntity<?>>) type);

        return new ByteBuddy()
                .subclass(CommonSynAuditEntityDao.class)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();
    }

    @Override
    public Class<?> generateCompanionForSynAuditProp(final Class<? extends AbstractSynAuditProp> type) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType((Class<? extends AbstractEntity<?>>) type);

        return new ByteBuddy()
                .subclass(CommonEntityDao.class)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();
    }

}
