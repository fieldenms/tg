package ua.com.fielden.platform.eql.stage2;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

import java.util.Set;

public interface ITransformableFromStage2To3<T> {
    TransformationResultFromStage2To3<T> transform(TransformationContextFromStage2To3 context);

    Set<Prop2> collectProps();

    /**
     * Traverses given element structure and returns a stream of all entity types (persistent and synthetic) that are
     * referenced there via {@code select} or {@code join} operators.
     */
     // TODO Potentially such traversal should also include calculated properties expressions from properties resolution paths.
    Set<Class<? extends AbstractEntity<?>>> collectEntityTypes();
}
