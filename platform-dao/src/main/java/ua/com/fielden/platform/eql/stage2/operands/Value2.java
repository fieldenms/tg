package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.operands.Value3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.eql.retrieval.EntityResultTreeBuilder.hibTypeFromJavaType;
import static ua.com.fielden.platform.persistence.HibernateConstants.N;
import static ua.com.fielden.platform.persistence.HibernateConstants.Y;

public record Value2 (Object value, boolean ignoreNull) implements ISingleOperand2<Value3>, ToString.IFormattable {

    public Value2(final Object value) {
        this(value, false);
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

    /**
     * Maps over this instance on the underlying value.
     *
     * @param fn  the mapping function.
     *            Argument may be null.
     */
    public Value2 map(final Function</*Nullable*/ Object, Object> fn) {
        final var newValue = fn.apply(value);
        return Objects.equals(newValue, value) ? this : new Value2(newValue, ignoreNull);
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
    public String toString() {
        return toString(ToString.standard);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("value", value)
                .add("ignoreNull", ignoreNull)
                .$();
    }

}
