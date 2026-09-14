package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.AppendIdToUnionTypedProp1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Entity;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

/// This transformation must be applied before [AppendIdToUnionTypedProp1].
/// The order is ensured by this implementation.
///
public class ExpandUnionTypedYield1 {

    private final IDomainMetadata domainMetadata;

    public ExpandUnionTypedYield1(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /// * If a union-typed property is yielded by `yield`, the result is a stream of yields that yield all corresponding union members.
    /// * If null is yielded into a union-typed property, the result is a stream of yields that all yield null into the corresponding union members.
    /// * Otherwise, the result is an empty optional.
    ///
    public Optional<Stream<Yield2>> apply(
            final Yield1 yield,
            final TransformationContextFromStage1To2 context,
            final AbstractQuery1 query)
    {
        if (yield.operand() instanceof Prop1 prop1) {
            final var prop2 = prop1.transformBase(context);
            if (prop2.getPath().getLast() instanceof QuerySourceItemForUnionType<?> item) {
                final var yields = item.getProps().values().stream()
                        .filter(subItem -> isEntityType(subItem.javaType()) && !subItem.hasExpression())
                        // Although we could create Yield2 directly, let's follow a simpler approach -- create Yield1 and transform it.
                        .map(subItem -> new Yield1(new Prop1("%s.%s".formatted(prop1.propPath(), subItem.name), false),
                                                   "%s.%s".formatted(yield.alias(), subItem.name),
                                                   false))
                        .map(y -> y.transform(context));
                return Optional.of(yields);
            }
            else {
                return Optional.empty();
            }
        }
        else if (yield.operand() instanceof Value1 value1
                 && value1.value() == null
                 && query.resultType != null
                 && query.resultType != EntityAggregates.class)
        {
            return domainMetadata.forPropertyOpt(query.resultType, yield.alias())
                    .flatMap(pm -> pm.type().asEntity().map(Entity::javaType).map(domainMetadata::forEntity).flatMap(EntityMetadata::asUnion))
                    .map(domainMetadata.entityMetadataUtils()::unionMembers)
                    .map(members -> members.stream()
                            .map(member -> new Yield1(value1, "%s.%s".formatted(yield.alias(), member.name()), false))
                            .map(y -> y.transform(context)));
        }
        else {
            return Optional.empty();
        }
    }


}
