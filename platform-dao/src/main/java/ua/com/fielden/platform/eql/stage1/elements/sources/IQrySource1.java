package ua.com.fielden.platform.eql.stage1.elements.sources;

import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage1.elements.ITransformableToS2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;

public interface IQrySource1<S2 extends IQrySource2> extends ITransformableToS2<S2> {
    /**
     * Represents business alias of the query source
     * 
     * @return
     */
    String getAlias();

    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    EntityInfo sourceType();
}