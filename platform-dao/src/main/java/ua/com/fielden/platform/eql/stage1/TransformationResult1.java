package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;

public class TransformationResult1<T extends IJoinNode2<?>> {
    public final T item;
    public final TransformationContext1 updatedContext;
    
    public TransformationResult1(final T item, final TransformationContext1 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}
