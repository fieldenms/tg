package ua.com.fielden.platform.eql.stage1;

import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class TransformationResultFromStage1To2<T extends IJoinNode2<? extends IJoinNode3>> {
    public final T item;
    public final TransformationContextFromStage1To2 updatedContext;
    
    public TransformationResultFromStage1To2(final T item, final TransformationContextFromStage1To2 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}
