package ua.com.fielden.platform.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * Generated companions should always be based on the original entity type. If an instrumented entity type is provided,
 * its original type should be used.
 */
@Singleton
final class AuditEntityCompanionGeneratorImpl implements IAuditEntityCompanionGenerator {

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity> type)
    {
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
