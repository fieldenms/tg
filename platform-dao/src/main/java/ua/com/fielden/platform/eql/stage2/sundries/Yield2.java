package ua.com.fielden.platform.eql.stage2.sundries;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.queries.AbstractQuery2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Component;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.CompositeKey;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Entity;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.utils.ToString;

import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

public record Yield2(ISingleOperand2<? extends ISingleOperand3> operand, String alias, boolean hasNonnullableHint)
        implements ToString.IFormattable
{

    @Inject
    private static IDomainMetadata domainMetadata;

    public TransformationResultFromStage2To3<Yield3> transform(
            final TransformationContextFromStage2To3 context,
            final AbstractQuery2 query)
    {
        final var operandTransformationResult = operand.transform(context);
        final var updatedContext = operandTransformationResult.updatedContext.cloneWithNextSqlId();

        // If the operand has the null type, try to use the type of the property which is the target of this yield.
        // This makes type information more precise, enabling us to generate explicit type casts for PostgreSQL.
        final PropType type;
        if (operand.type().isNull() && query.resultType != null && query.resultType != EntityAggregates.class && isEntityType(query.resultType)) {
            type = domainMetadata.forPropertyOpt(query.resultType, alias())
                    .filter(pm -> pm.hibType() != null)
                    .map(pm -> switch (pm.type()) {
                        case Component it -> propType(it.javaType(), pm.hibType());
                        case CompositeKey it -> propType(it.javaType(), pm.hibType());
                        case Entity it -> propType(it.javaType(), pm.hibType());
                        case Primitive it -> propType(it.javaType(), pm.hibType());
                        default -> null;
                    })
                    .orElseGet(operand::type);
        }
        else {
            type = operand.type();
        }

        return new TransformationResultFromStage2To3<>(
                new Yield3(operandTransformationResult.item, alias, updatedContext.sqlId, type),
                updatedContext);
    }

        @Override
        public String toString() {
            return toString(ToString.separateLines());
        }

        @Override
        public String toString(final ToString.IFormat format) {
            return format.toString(this)
                    .add("alias", alias)
                    .add("hasNonnullableHint", hasNonnullableHint)
                    .add("operand", operand)
                    .$();
        }

}
