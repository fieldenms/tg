package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.TransformationResult1;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;

public interface IJoinNode1<S2 extends IJoinNode2<?>> extends ITransformableToS2<TransformationResult1<S2>> {
    
    /**
     * Gets the leftmost query source. Needed for UDF (user data filtering).
     * 
     * @return
     */
    ISource1<? extends ISource2<?>> mainSource();
}
