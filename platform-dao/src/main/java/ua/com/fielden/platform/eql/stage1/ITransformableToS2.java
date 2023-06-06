package ua.com.fielden.platform.eql.stage1;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ITransformableToS2<S2> {
    S2 transform(final TransformationContext1 context);
    
    /**
     * Traverses given element structure and returns a set of all entity types (persistent and synthetic) that are referenced there via select(..) or join(..) operators.
     * 
     * @return
     */
    Set<Class<? extends AbstractEntity<?>>> collectEntityTypes();
}