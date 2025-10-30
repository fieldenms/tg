package ua.com.fielden.platform.companion;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import ua.com.fielden.platform.audit.*;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;

import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

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
        else {
            return generateSimpleCompanion(type);
        }
    }

    private Class<?> generateSimpleCompanion(final Class<? extends AbstractEntity> type) {
        // Always use the base type.
        // * ByteBuddy will create a new class loader just for the generated companion type.
        // * The companion type does not need to reference `type`, only its base type.
        // * If we used `type`, which could be loaded by `DynamicEntityClassLoader`, we would be required to use `TypeMaker`
        //   to build the companion type.
        final var baseType = baseEntityType((Class<? extends AbstractEntity<?>>) type);

        final var fetchProvider = fetch(baseType);

        final var generatedType = new ByteBuddy()
                .subclass(CommonEntityDao.class)
                .name(baseType.getCanonicalName() + "Dao")
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .method(ElementMatchers.named("createFetchProvider"))
                .intercept(FixedValue.value(fetchProvider))
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();

        return generatedType;
    }

}
