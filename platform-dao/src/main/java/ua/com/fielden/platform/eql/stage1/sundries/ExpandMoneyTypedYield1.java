package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.types.Money;

import java.util.Optional;
import java.util.stream.Stream;

/// A transformation on individual yields [Yield1] whose destination is a [Money]-typed property.
///
/// * If the yielded expression is a property whose type is [Money], the yield is transformed into a set of yields,
///   one for each component of [Money] based on the representation used for that particular property.
///   ```
///   yield().prop("price").as("price")
///   =>
///   yield().prop("price.amount").as("price.amount")
///   yield().prop("price.currency").as("price.currency")
///   ```
///
public final class ExpandMoneyTypedYield1 {

    public static final ExpandMoneyTypedYield1 INSTANCE = new ExpandMoneyTypedYield1();

    private ExpandMoneyTypedYield1() {}

    /// If the transformation is applicable, returns its result.
    /// Otherwise, returns an empty optional.
    ///
    public Optional<Stream<Yield1>> apply(
            final Yield1 yield,
            final TransformationContextFromStage1To2 context,
            final AbstractQuery1 query)
    {
        if (yield.operand() instanceof Prop1 prop
            && query.resultType != null
            && yield.hasAlias()
            && context.domainMetadata.forPropertyOpt(query.resultType, yield.alias())
                    .filter(pm -> pm.type().javaType().equals(Money.class))
                    .isPresent())
        {
            final var resolution = Prop1.resolveProp(prop, context);
            if (resolution.getPath().getLast() instanceof QuerySourceItemForComponentType<?> item && item.javaType().equals(Money.class)) {
                final var yields = item.getSubitems().values()
                        .stream()
                        .map(subItem -> new Yield1(new Prop1("%s.%s".formatted(prop.propPath(), subItem.name), prop.external()),
                                                   "%s.%s".formatted(yield.alias(), subItem.name),
                                                   false));
                return Optional.of(yields);
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }


}
