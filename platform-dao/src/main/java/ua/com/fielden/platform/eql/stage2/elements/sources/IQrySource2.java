package ua.com.fielden.platform.eql.stage2.elements.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.ITransformableToS3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public interface IQrySource2<S3 extends IQrySource3> extends ITransformableToS3<S3> {
    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    Class<? extends AbstractEntity<?>> sourceType();
    
    EntityInfo<?> entityInfo();
    
    String alias();
}