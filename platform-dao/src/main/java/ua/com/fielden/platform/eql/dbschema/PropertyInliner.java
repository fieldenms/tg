package ua.com.fielden.platform.eql.dbschema;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.either.Either;

import java.util.List;
import java.util.Optional;

/// Property inlining is a process by which a single entity property is replaced by two or more properties.
/// Inlining is used in mapping properties to one or more database columns.
///
/// The following inlining rules are in place:
/// - A property typed with a union entity is replaced by the union entity's properties.
/// - A property with a composite type is replaced by the composite type's properties.
///
/// All other properties are not subject to inlining, which can be thought of as "inlined by being replaced by themselves".
///
@ImplementedBy(PropertyInlinerImpl.class)
public interface PropertyInliner {

    /// If the property is inlinable, returns an optional containing the result of inlining.
    /// Otherwise, returns an empty optional.
    ///
    Optional<List<PropertyMetadata.Persistent>> inline(PropertyMetadata.Persistent property);

    /// If the property is inlinable, returns a right value containing the result of inlining.
    /// Otherwise, returns a left value containing the specified property.
    ///
    default Either<PropertyMetadata.Persistent, List<PropertyMetadata.Persistent>> inlineOrGet(PropertyMetadata.Persistent property) {
        return inline(property)
                .<Either<PropertyMetadata.Persistent, List<PropertyMetadata.Persistent>>> map(Either::right)
                .orElseGet(() -> Either.left(property));
    }

}
