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

    private static final String PACKAGE = "main.menu.audit";

    @SuppressWarnings("unchecked")
    <E extends AbstractEntity<?>> Class<MiWithConfigurationSupport<E>> generate(final Class<E> type) {
        final var baseType = PropertyTypeDeterminator.baseEntityType(type);
        final var miTypeSimpleName = "Mi" + baseType.getSimpleName();

        // Use a fixed package that doesn't depend on the entity type so that the resulting Mi type is not affected by
        // renaming / moving of the entity type.
        final var miTypeFqn = Stream.of(PACKAGE, miTypeSimpleName)
                .filter(not(String::isEmpty))
                .collect(joining("."));

        // Generated Mi types can be cached on their names.
        // There should never be 2 entity types with the same FQN but loaded by different class loaders.

        return (Class<MiWithConfigurationSupport<E>>)
                ClassesRetriever.maybeFindClass(miTypeFqn)
                        .orElseGet(() -> {
                            final var miType = new ByteBuddy()
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
                        });
    }

}
