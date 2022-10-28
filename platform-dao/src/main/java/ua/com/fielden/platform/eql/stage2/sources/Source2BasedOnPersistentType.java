package ua.com.fielden.platform.eql.stage2.sources;

import static java.lang.String.format;
import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

public class Source2BasedOnPersistentType extends AbstractSource2 implements ISource2<Source3BasedOnTable> {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public Source2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo<?> entityInfo, final String alias, final Integer id) {
        super(id, alias, entityInfo);
        this.sourceType = sourceType;
    }

    public Source2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo<?> entityInfo, final Integer id) {
        this(sourceType, entityInfo, null, id);               
    }

    @Override
    public TransformationResult2<Source3BasedOnTable> transform(final TransformationContext2 context) {
        final TransformationContext2 newContext = context.cloneWithNextSqlId();
        final Source3BasedOnTable transformedSource = new Source3BasedOnTable(newContext.getTable(sourceType().getName()), id, newContext.sqlId);
        return new TransformationResult2<>(transformedSource, newContext);
    }

    @Override
    public Set<Prop2> collectProps() {
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
        
        if (!(obj instanceof Source2BasedOnPersistentType)) {
            return false;
        }
        
        final Source2BasedOnPersistentType other = (Source2BasedOnPersistentType) obj;
        
        return Objects.equals(sourceType, other.sourceType);
    }
}