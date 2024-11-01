package ua.com.fielden.platform.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.entity.annotation.EntityType;

@Singleton
final class AuditEntityCompanionGeneratorImpl implements IAuditEntityCompanionGenerator {

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity> type)
    {
        return new ByteBuddy()
                .subclass(CommonAuditEntityDao.class)
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", type)
                                      .build())
                .make()
                .load(type.getClassLoader())
                .getLoaded();
    }

}
