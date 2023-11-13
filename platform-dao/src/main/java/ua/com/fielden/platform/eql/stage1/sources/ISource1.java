package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public interface ISource1<T extends ISource2<?>> extends ITransformableFromStage1To2<T> {

    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    Class<? extends AbstractEntity<?>> sourceType();
}