package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;

public class QrySource2BasedOnPersistentType extends AbstractElement2 implements IQrySource2<QrySource3BasedOnTable> {
    private final Class<? extends AbstractEntity<?>> sourceType;
    private final EntityInfo entityInfo;
    private final String alias;

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo entityInfo, final String alias, final int contextId) {
        super(contextId);
        this.sourceType = sourceType;
        this.entityInfo = entityInfo;
        this.alias = alias;               
    }

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo entityInfo, final int contextId) {
        this(sourceType, entityInfo, null, contextId);               
    }

    @Override
    public TransformationResult<QrySource3BasedOnTable> transform(final TransformationContext context) {
        final QrySource3BasedOnTable transformedSource = new QrySource3BasedOnTable(context.getTable(sourceType().getName()),contextId);
        return new TransformationResult<QrySource3BasedOnTable>(transformedSource, context.cloneWithAdded(transformedSource, this));
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public EntityInfo<?> entityInfo() {
        return entityInfo;
    }

    @Override
    public String alias() {
        return alias;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
        
        return Objects.equals(sourceType, other.sourceType) && Objects.equals(alias, other.alias);
    }
}