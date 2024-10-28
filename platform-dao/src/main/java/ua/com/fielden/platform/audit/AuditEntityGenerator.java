package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.nio.file.Path;
import java.util.Set;

@ImplementedBy(AuditEntityGeneratorImpl.class)
public interface AuditEntityGenerator {

    default Set<GeneratedResult> generate(Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
        return generate(entityTypes, Path.of("src/main/java"));
    }

    Set<GeneratedResult> generate(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            Path sourceRoot);

    /**
     * Result of audit-entity generation.
     *
     * @param auditEntityPath  path to a source file with an {@linkplain AbstractAuditEntity audit-entity} type
     * @param auditEntityModPropPath  path to a source file with a {@linkplain AbstractAuditModProp one-to-many entity type that represents changed properties}
     */
    record GeneratedResult (Path auditEntityPath, Path auditEntityModPropPath) { }

    // TODO: Display deltas

}
