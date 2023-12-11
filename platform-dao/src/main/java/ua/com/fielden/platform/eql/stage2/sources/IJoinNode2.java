package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public interface IJoinNode2<T extends IJoinNode3> extends ITransformableFromStage2To3<T> {
    
    /**
     * Gets the leftmost query source. Needed for auto-yielding and UDF (user data filtering).
     * 
     * @return
     */
    ISource2<? extends ISource3> mainSource();
}