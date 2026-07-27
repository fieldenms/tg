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

/// A generator responsible for generating companion objects at runtime for audit entities.
///
/// Generated companions are always based on the _original_ entity type.
/// If an _instrumented_ entity type is provided, its _original_ type is used.
///
@Singleton
final class AuditCompanionGenerator implements IAuditCompanionGenerator {

    private final AuditingMode auditingMode;

    @Inject
    AuditCompanionGenerator(final AuditingMode auditingMode) {
        this.auditingMode = auditingMode;
    }

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity<?>> type) {
        return generateCompanionCommon(type, CommonAuditEntityDao.class);
    }

    @Override
    public Class<?> generateCompanionForAuditProp(final Class<? extends AbstractAuditProp<?>> type) {
        return generateCompanionCommon(type, CommonAuditPropDao.class);
    }

    @Override
    public Class<?> generateCompanionForSynAuditEntity(final Class<? extends AbstractSynAuditEntity<?>> type) {
        return generateCompanionCommon(type, CommonSynAuditEntityDao.class);
    }

    @Override
    public Class<?> generateCompanionForSynAuditProp(final Class<? extends AbstractSynAuditProp<?>> type) {
        // CommonEntityDao is used correct here,
        // but it will need to be replaced with a dedicated CommonSynAuditProp, if such companion is ever going to be introduced.
        return generateCompanionCommon(type, CommonEntityDao.class);
    }

    /// A helper method for generating a new companion implementation extending `coSuperclass` for entity `type`.
    ///
    /// @param type an entity type for which the companion implementation needs to be generated
    /// @param coSuperclass a base type for the companion implementation.
    /// 
    private Class<?> generateCompanionCommon(final Class<? extends AbstractEntity<?>> type, final Class<?> coSuperclass) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditCompanionGenerator.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType(type);

        return new ByteBuddy()
                .subclass(coSuperclass)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();
    }

}
