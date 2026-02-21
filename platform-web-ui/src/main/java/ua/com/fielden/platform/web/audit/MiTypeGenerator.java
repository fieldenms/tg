package ua.com.fielden.platform.web.audit;

import jakarta.inject.Singleton;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;

/// Generates Mi types (subclasses of [MiWithConfigurationSupport]).
///
@Singleton
final class MiTypeGenerator {

    private static final String PACKAGE = "main.menu.audit";

    <E extends AbstractEntity<?>> Class<MiWithConfigurationSupport<E>> generate(final Class<E> type) {
        final var simpleName = "Mi" + baseEntityType(type).getSimpleName();
        return generate(simpleName, type);
    }

    @SuppressWarnings("unchecked")
    <E extends AbstractEntity<?>> Class<MiWithConfigurationSupport<E>>
    generate(final String simpleName,
             final Class<E> type)
    {
        final var baseType = baseEntityType(type);

        // Use a fixed package that doesn't depend on the entity type so that the resulting Mi type is not affected by
        // renaming / moving of the entity type.
        final var miTypeFqn = Stream.of(PACKAGE, simpleName)
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
