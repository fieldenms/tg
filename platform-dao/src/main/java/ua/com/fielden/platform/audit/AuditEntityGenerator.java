package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@ImplementedBy(AuditEntityGeneratorImpl.class)
public interface AuditEntityGenerator {

    /**
     * Properties of base entity types that are excluded from auditing.
     */
    Set<String> NON_AUDITED_PROPERTIES = Set.of(
            // id is captured by the audited entity reference property
            AbstractEntity.ID,
            AbstractEntity.VERSION,
            AbstractPersistentEntity.CREATED_BY,
            AbstractPersistentEntity.CREATED_DATE,
            AbstractPersistentEntity.CREATED_TRANSACTION_GUID,
            AbstractPersistentEntity.LAST_UPDATED_BY,
            AbstractPersistentEntity.LAST_UPDATED_DATE,
            AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID,
            ActivatableAbstractEntity.REF_COUNT);

    /**
     * Equivalent to {@link #generateSources(Iterable, VersionStrategy)}, but also writes generated sources to disk.
     *
     * @param sourceRoot  directory root where packages of generated sources will be placed
     * @return  paths to generated sources
     */
    Collection<Path> generate(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            Path sourceRoot,
            VersionStrategy versionStrategy);

    /**
     * For each specified audited entity type, generates source code for an audit-entity and its associated audit-prop entity.
     * It is an error if any of specified entity types are not audited.
     *
     * @return  a set of generated audit-entity and audit-prop entity types
     */
    Collection<SourceInfo> generateSources(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            VersionStrategy versionStrategy);

    /**
     * Represents the source code of a top-level class or interface.
     *
     * @param className  fully-qualified name of the class or interface
     * @param source  source code
     */
    record SourceInfo (String className, String source) {}

    /**
     * Strategies for choosing a version for generated audit types.
     */
    enum VersionStrategy {

        /**
         * A new audit-entity type is generated.
         * If there is an existing audit-entity type for the same audited type, its version is incremented to obtain the new version.
         * Otherwise, version 1 is used.
         */
        NEW,

        /**
         * An existing audit-entity type with the latest version is overwritten if it exists.
         * Otherwise, a new audit-entity type is generated with version 1.
         */
        OVERWRITE_LAST

    }

}
