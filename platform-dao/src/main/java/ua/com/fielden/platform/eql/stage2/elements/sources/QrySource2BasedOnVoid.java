package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Collections.emptySet;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnVoid;

public class QrySource2BasedOnVoid implements IQrySource2<QrySource3BasedOnVoid> {

    @Override
    public TransformationResult<QrySource3BasedOnVoid> transform(final TransformationContext context) {
        return new TransformationResult<QrySource3BasedOnVoid>(new QrySource3BasedOnVoid(), context);
    }

    @Override
    public Set<EntProp2> collectProps() {
        return emptySet();
    } 
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        throw new EqlException("This method shouldn't be invoked.");
    }

    @Override
    public EntityInfo<?> entityInfo() {
        throw new EqlException("This method shouldn't be invoked.");
    }

    @Override
    public String alias() {
        throw new EqlException("This method shouldn't be invoked.");
    }

    @Override
    public String contextId() {
        throw new EqlException("This method shouldn't be invoked.");
    }
    
    @Override
    public String toString() {
        return "QrySource2BasedOnVoid";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + QrySource2BasedOnVoid.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof QrySource2BasedOnVoid;
    } 
}