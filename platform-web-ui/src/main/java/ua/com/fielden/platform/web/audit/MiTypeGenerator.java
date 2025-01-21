package ua.com.fielden.platform.web.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * Generates Mi types (subclasses of {@link MiWithConfigurationSupport}) at runtime.
 */
@Singleton
final class MiTypeGenerator {

    @SuppressWarnings("unchecked")
    <E extends AbstractEntity<?>> Class<MiWithConfigurationSupport<E>> generate(final Class<E> type) {
        final var baseType = PropertyTypeDeterminator.baseEntityType(type);
        final var miTypeSimpleName = "Mi" + baseType.getSimpleName();
        // For manually created mi types, the conventional package is `fielden.main.menu.{module_name}`,
        // but for generated mi types we may choose an arbitrary package.
        final var miTypeFqn = Stream.of(baseType.getPackageName(), miTypeSimpleName)
                .filter(not(String::isEmpty))
                .collect(joining("."));

        final var miType = (Class<MiWithConfigurationSupport<E>>) new ByteBuddy()
                .subclass(MiWithConfigurationSupport.class)
                .name(miTypeFqn)
                .annotateType(AnnotationDescription.Builder.ofType(EntityType.class)
                                      .define("value", baseType)
                                      .build())
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();

        // Ensure that the generated mi type can be discovered by ClassesRetriever.
        // Mi types are located by their names.
        ClassesRetriever.registerClass(miType);

        return miType;
    }

}
