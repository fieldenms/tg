package ua.com.fielden.platform.entity;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/// A mechanism for pattern matching on references between two entities.
///
@Singleton
public class EntityReferenceAlgebra {

    @Inject
    EntityReferenceAlgebra() {}

    /// Applies an operation in `ops` depending on the kind of reference from `entity` to `value` via `property`.
    ///
    public <T> T reference(final AbstractEntity<?> entity, final CharSequence property, final @Nullable AbstractEntity<?> value, final Ops<T> ops) {
        if (value instanceof AbstractUnionEntity union) {
            return ops.applyUnion(entity, property, union, union.activeEntity());
        }
        else {
            return ops.apply(entity, property, value);
        }
    }

    public interface Ops<T> {

        T apply(AbstractEntity<?> entity, final CharSequence property, @Nullable AbstractEntity<?> value);

        T applyUnion(AbstractEntity<?> entity, final CharSequence property, AbstractUnionEntity union, AbstractEntity<?> unionMember);

    }

}
