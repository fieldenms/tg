package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IQrySource2 {
    
    void addProp(EntProp2 prop);

    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    Class<? extends AbstractEntity<?>> sourceType();
}