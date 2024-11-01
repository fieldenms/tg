package ua.com.fielden.platform.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import ua.com.fielden.platform.entity.annotation.EntityType;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

@Singleton
final class AuditEntityCompanionGeneratorImpl implements IAuditEntityCompanionGenerator {

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractAuditEntity> type)
    {
        // TODO Construct a fetch provider by including all properties
        final var fetchProvider = fetch(type);

        final var generatedType = new ByteBuddy()
                .subclass(CommonAuditEntityDao.class)
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", type)
                                      .build())
                .method(ElementMatchers.named("createFetchProvider"))
                .intercept(FixedValue.value(fetchProvider))
                .make()
                .load(type.getClassLoader())
                .getLoaded();

        return generatedType;
    }

}
