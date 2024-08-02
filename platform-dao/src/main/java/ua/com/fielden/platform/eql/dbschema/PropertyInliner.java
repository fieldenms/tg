package ua.com.fielden.platform.eql.dbschema;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Property inlining is a process by which a single entity property is replaced by two or more properties.
 * Inlining is used in mapping properties to one or more database columns.
 * <p>
 * The following inlining rules are in place:
 * <ol>
 *   <li> A property typed with a union entity is replaced by the union entity's properties.
 *   <li> A property with a composite type is replaced by the composite type's properties.
 * </ol>
 * All other properties are not subject to inlining, which can be thought of as "inlined by being replaced by themselves".
 */
@ImplementedBy(PropertyInlinerImpl.class)
public interface PropertyInliner {

    Optional<List<PropertyMetadata.Persistent>> inline(PropertyMetadata.Persistent property);

}
