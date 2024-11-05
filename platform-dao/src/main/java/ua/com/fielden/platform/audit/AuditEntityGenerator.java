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
     * @param auditPropEntityPath  path to a source file with a {@linkplain AbstractAuditProp one-to-many entity type that represents changed properties}
     */
    record GeneratedResult (Path auditEntityPath, Path auditPropEntityPath) { }

    /**
     * For each specified audited entity type, generates source code for an audit-entity and its associated audit-prop entity.
     *
     * @return  a set of generated audit-entity and audit-prop entity types
     */
    Set<SourceInfo> generateSources(Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes);

    /**
     * Represents the source code of a top-level class or interface.
     *
     * @param className  fully-qualified name of the class or interface
     * @param source  source code
     */
    record SourceInfo (String className, String source) {}

    // TODO: Display deltas

}
