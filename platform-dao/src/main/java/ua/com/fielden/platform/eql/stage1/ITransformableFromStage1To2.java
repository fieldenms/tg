package ua.com.fielden.platform.eql.stage1;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to be implemented by stage 1 structures that are meant to be transformed to stage 2.
 *
 * @param <T>  type of the result of the transformation
 */
public interface ITransformableFromStage1To2<T> {

    /**
     * Transforms this element from stage 1 to stage 2.
     */
    T transform(final TransformationContextFromStage1To2 context);

    /**
     * Traverses this structure and returns a set of all entity types (persistent and synthetic) that are referenced via
     * {@code select} or {@code join}.
     */
    Set<Class<? extends AbstractEntity<?>>> collectEntityTypes();
}
