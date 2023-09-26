package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.ITransformableToS3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public interface ISource2<S3 extends ISource3> extends ITransformableToS3<S3> {
    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    Class<? extends AbstractEntity<?>> sourceType();
    
    QuerySourceInfo<?> querySourceInfo();
    
    String alias();
    
    Integer id();
    
    boolean isExplicit(); // i.e. explicitly declared as part of user query or calculated property expression.
}