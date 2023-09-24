package ua.com.fielden.platform.eql.stage2.sources;

import static java.lang.String.format;
import static java.util.Collections.emptySet;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

public class Source2BasedOnPersistentType extends AbstractSource2 implements ISource2<Source3BasedOnTable> {

    public Source2BasedOnPersistentType(final QuerySourceInfo<?> querySourceInfo, final String alias, final Integer id) {
        super(id, alias, querySourceInfo);
    }

    public Source2BasedOnPersistentType(final QuerySourceInfo<?> querySourceInfo, final Integer id) {
        this(querySourceInfo, null, id);               
    }

    @Override
    public TransformationResult2<Source3BasedOnTable> transform(final TransformationContext2 context) {
        final TransformationContext2 newContext = context.cloneWithNextSqlId();
        final Source3BasedOnTable transformedSource = new Source3BasedOnTable(newContext.getTable(sourceType()), id, newContext.sqlId);
        return new TransformationResult2<>(transformedSource, newContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return Set.of(sourceType());
    }
    
    @Override
    public String toString() {
        return format("type = [%s], ID = [%s], alias = [%s]", sourceType().getSimpleName(), id, (alias != null ? alias : ""));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Source2BasedOnPersistentType.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof Source2BasedOnPersistentType;
    }
}