package ua.com.fielden.platform.eql.stage2.sources;

import static java.lang.String.format;
import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnTable;

public class QrySource2BasedOnPersistentType extends AbstractQrySource2 implements IQrySource2<QrySource3BasedOnTable> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo<?> entityInfo, final String alias, final String id) {
        super(id, alias, entityInfo);
        this.sourceType = sourceType;
    }

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo<?> entityInfo, final String id) {
        this(sourceType, entityInfo, null, id);               
    }

    @Override
    public TransformationResult<QrySource3BasedOnTable> transform(final TransformationContext context) {
        final TransformationContext newContext = context.cloneWithNextSqlId();
        final QrySource3BasedOnTable transformedSource = new QrySource3BasedOnTable(newContext.getTable(sourceType().getName()), id, newContext.sqlId);
        return new TransformationResult<QrySource3BasedOnTable>(transformedSource, newContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        return emptySet();
    } 
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public String toString() {
        return format("type = [%s], ID = [%s], alias = [%s]", sourceType.getSimpleName(), id, (alias != null ? alias : ""));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + sourceType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof QrySource2BasedOnPersistentType)) {
            return false;
        }
        
        final QrySource2BasedOnPersistentType other = (QrySource2BasedOnPersistentType) obj;
        
        return Objects.equals(sourceType, other.sourceType);
    }
}