package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Collections.emptySet;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnVoid;

public class QrySource2BasedOnVoid implements IQrySource2<QrySource3BasedOnVoid> {

    @Override
    public TransformationResult<QrySource3BasedOnVoid> transform(final TransformationContext context) {
        return new TransformationResult<QrySource3BasedOnVoid>(new QrySource3BasedOnVoid(), context);
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    } 
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        throw new EqlStage2ProcessingException("This method shouldn't be invoked.");
    }

    @Override
    public EntityInfo<?> entityInfo() {
        throw new EqlStage2ProcessingException("This method shouldn't be invoked.");
    }

    @Override
    public String alias() {
        throw new EqlStage2ProcessingException("This method shouldn't be invoked.");
    }

    @Override
    public String id() {
        return "";
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