package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EqlTable;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

public class Source2BasedOnPersistentType extends AbstractSource2 implements ISource2<Source3BasedOnTable> {

    public Source2BasedOnPersistentType(final QuerySourceInfo<?> querySourceInfo, final String alias, final Integer id, final boolean isExplicit, final boolean isPartOfCalcProp) {
        super(id, alias, querySourceInfo, isExplicit, isPartOfCalcProp);
    }

    public Source2BasedOnPersistentType(final QuerySourceInfo<?> querySourceInfo, final Integer id, final boolean isExplicit, final boolean isPartOfCalcProp) {
        this(querySourceInfo, null, id, isExplicit, isPartOfCalcProp);
    }

    @Override
    public TransformationResultFromStage2To3<Source3BasedOnTable> transform(final TransformationContextFromStage2To3 context) {
        final TransformationContextFromStage2To3 updatedContext = context.cloneWithNextSqlId();
        final Source3BasedOnTable transformedSource = new Source3BasedOnTable(updatedContext.getTable(sourceType()), id, updatedContext.sqlId);
        return new TransformationResultFromStage2To3<>(transformedSource, updatedContext);
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
        return format("Source(%s, alias=%s, id=%s)", sourceType().getTypeName(), (alias != null ? alias : ""), id);
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
