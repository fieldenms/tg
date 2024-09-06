package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.operands.Value3;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.hibTypeFromJavaType;
import static ua.com.fielden.platform.persistence.HibernateConstants.N;
import static ua.com.fielden.platform.persistence.HibernateConstants.Y;

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
        return value == null ? NULL_TYPE : propType(value.getClass(), hibTypeFromJavaType(value.getClass())); // TODO provide proper hibType once value original (not converted) will be taken into account.
    }

    @Override
    public boolean isNonnullableEntity() {
        return false; // should be FALSE even if value is not null as there is no guarantee of presence of entity with such ID in DB
    }

    @Override
    public TransformationResultFromStage2To3<Value3> transform(final TransformationContextFromStage2To3 context) {
        if (needsParameter()) {
            final T2<String, TransformationContextFromStage2To3> paramTr = context.obtainParamNameAndUpdateContext(value);
            final Value3 transformed = new Value3(value, paramTr._1, type());
            return new TransformationResultFromStage2To3<>(transformed, paramTr._2);
        } else {
            return new TransformationResultFromStage2To3<>(new Value3(value, type()), context);
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
