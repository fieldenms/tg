package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/// The contract for generating audit entities.
/// It cannot be used when auditing is disabled, all methods will throw [AuditingModeException].
///
@ImplementedBy(AuditEntityGenerator.class)
public interface IAuditEntityGenerator {

    /// Properties of base entity types that are excluded from auditing.
    Set<String> NON_AUDITED_PROPERTIES = Set.of(
            AbstractEntity.ID, // ID is captured by the audited entity reference property.
            AbstractEntity.VERSION,
            AbstractPersistentEntity.CREATED_BY,
            AbstractPersistentEntity.CREATED_DATE,
            AbstractPersistentEntity.CREATED_TRANSACTION_GUID,
            AbstractPersistentEntity.LAST_UPDATED_BY,
            AbstractPersistentEntity.LAST_UPDATED_DATE,
            AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID,
            ActivatableAbstractEntity.REF_COUNT);

    /// Performs the same operation as [#generateSources(Iterable, VersionStrategy)],
    /// but additionally writes the generated source files to disk.
    ///
    /// @param sourceRoot the root directory where the generated source packages will be saved
    ///
    /// @return a collection of paths to the generated source files
    ///
    Collection<Path> generate(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            Path sourceRoot,
            VersionStrategy versionStrategy);

    /// Generates source code for an audit-entity and its associated audit-prop entity for each specified audited entity type.
    ///
    /// It is considered an error if any of the specified entity types are not marked as audited.
    ///
    /// @return a set of the generated audit-entity and audit-prop entity types
    ///
    Collection<SourceInfo> generateSources(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            VersionStrategy versionStrategy);

    /// Represents the source code of a top-level class or interface.
    ///
    /// @param className  fully-qualified name of the class or interface
    /// @param source  source code
    ///
    record SourceInfo (String className, String source) {}

    /// Strategies for choosing a version for generated audit types.
    ///
    enum VersionStrategy {

        /// A new audit-entity type is generated.
        /// If there is an existing audit-entity type for the same audited type, its version is incremented to obtain the new version.
        /// Otherwise, version 1 is used.
        NEW,

        /// An existing audit-entity type with the latest version is overwritten if it exists.
        /// Otherwise, a new audit-entity type is generated with version 1.
        OVERWRITE_LAST

    }

}
