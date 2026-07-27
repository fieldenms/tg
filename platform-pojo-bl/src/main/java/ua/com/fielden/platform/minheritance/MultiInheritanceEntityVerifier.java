package ua.com.fielden.platform.minheritance;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Extends;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.isGeneratedMultiInheritanceEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.specTypeFor;

/// Verifies the definitions of generated multi-inheritance entity types.
///
@Singleton
public class MultiInheritanceEntityVerifier {

    @Inject
    MultiInheritanceEntityVerifier() {}

    public void verify(final Iterable<? extends Class<?>> types) {
        types.forEach(this::verify);
    }

    public void verify(final Class<?> type) {
        if (isGeneratedMultiInheritanceEntityType(type)) {
            final var specType = specTypeFor((Class<? extends AbstractEntity<?>>) type);
            final var atExtends = AnnotationReflector.requireAnnotation(specType, Extends.class);
            final var expectedQualName = "%s.%s".formatted(specType.getPackageName(), atExtends.name());
            if (!expectedQualName.equals(type.getCanonicalName())) {
                throw new EntityDefinitionException(format(
                        """
                        [%s] is an orphaned multi-inheritance type. Its specification type [%s] specifies a different name in @%s.%s. \
                        Either [%s] should be deleted, or [%s] should be reassociated with [%s].""",
                        type.getCanonicalName(), specType.getCanonicalName(), Extends.class.getSimpleName(), "name",
                        type.getCanonicalName(), specType.getCanonicalName(), type.getCanonicalName()));
            }
        }
    }

}
