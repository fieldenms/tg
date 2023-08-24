package ua.com.fielden.platform.eql.stage2.operands;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.N;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.Y;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.hibTypeFromJavaType;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.operands.Value3;
import ua.com.fielden.platform.types.tuples.T2;

public class Value2 implements ISingleOperand2<Value3> {
    private final Object value;
    private final boolean ignoreNull;

    public Value2(final Object value) {
        this(value, false);
    }

    public Value2(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
    }
    
    private boolean needsParameter() {
        return !(value == null || value instanceof Integer || Y.equals(value) || N.equals(value));
    }
    
    @Override
    public boolean ignore() {
        return ignoreNull && value == null;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public PropType type() {
        return value == null ? null : new PropType(value.getClass(), hibTypeFromJavaType(value.getClass())); // TODO provide proper hibType once value original (not converted) will be taken into account.
    }
    
    @Override
    public boolean isNonnullableEntity() {
        return false; // should be FALSE even if value is not null as there is no guarantee of presence of entity with such ID in DB 
    }
    
    @Override
    public TransformationResult2<Value3> transform(final TransformationContext2 context) {
        if (needsParameter()) {
            final T2<String, TransformationContext2> paramTr = context.obtainParamNameAndUpdateContext(value);
            final Value3 transformed = new Value3(value, paramTr._1, type());
            return new TransformationResult2<>(transformed, paramTr._2);
        } else {
            return new TransformationResult2<>(new Value3(value, null, type()), context);
        }
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ignoreNull ? 1231 : 1237);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Value2)) {
            return false;
        }

        final Value2 other = (Value2) obj;
        
        return Objects.equals(value, other.value) && ignoreNull == other.ignoreNull;
    }
}