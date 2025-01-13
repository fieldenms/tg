package ua.com.fielden.platform.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@Singleton
final class AuditEntityCompanionGeneratorImpl implements IAuditEntityCompanionGenerator {

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity> type)
    {
        return new ByteBuddy()
                .subclass(CommonAuditEntityDao.class)
                .name(type.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", type)
                                      .build())
                .make()
                .load(type.getClassLoader())
                .getLoaded();
    }

    @Override
    public Class<?> generateCompanionForAuditProp(final Class<? extends AbstractAuditProp> type) {
        return new ByteBuddy()
                .subclass(CommonAuditPropDao.class)
                .name(type.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", type)
                                      .build())
                .make()
                .load(type.getClassLoader())
                .getLoaded();
    }

    @Override
    public Class<?> generateCompanionForSynAuditEntity(final Class<? extends AbstractSynAuditEntity> type) {
        return new ByteBuddy()
                .subclass(CommonEntityDao.class)
                .name(type.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", type)
                                      .build())
                .make()
                .load(type.getClassLoader())
                .getLoaded();
    }

}
