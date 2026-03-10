package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.types.Money;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.types.Money.AMOUNT;

/// A transformation on individual yields [Yield1] whose destination is a [Money]-typed property.
///
/// * If the yielded expression is a property whose type is [Money], the yield is transformed into one or more yields for [Money] components.
///   The set of yielded components is based on an intersection of [Money] represenations used for the yielded property
///   and the destination property.
///   ```
///   select(Vehicle.class)
///   .yield().prop("price").as("price")
///   .modelAsEntity(ReVehicle.class)
///   // If both Vehicle.price and ReVehicle.price have currency:
///   select(Vehicle.class)
///   .yield().prop("price.amount").as("price.amount")
///   .yield().prop("price.currency").as("price.currency")
///   .modelAsEntity(ReVehicle.class)
///   // If one of Vehicle.price, ReVehicle.price has only amount:
///   select(Vehicle.class)
///   .yield().prop("price.amount").as("price.amount")
///   .modelAsEntity(ReVehicle.class)
///   ```
///
/// * If the yielded expression is NOT a property, the yield is transformed into an equivalent yield but with the
///   destination of [Money#AMOUNT].
///   ```
///   yield().X.as("price")
///   yield().X.as("price.amount")
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
        if (query.resultType == null || !yield.hasAlias()) {
            return Optional.empty();
        }

        return context.domainMetadata.forPropertyOpt(query.resultType, yield.alias())
                .filter(pm -> pm.type().javaType().equals(Money.class))
                .flatMap(mdResultProp -> {
                    if (yield.operand() instanceof Prop1 prop) {
                        final var resolution = Prop1.resolveProp(prop, context);
                        if (resolution.getPath().getLast() instanceof QuerySourceItemForComponentType<?> item && item.javaType().equals(Money.class)) {
                            final var yields = item.getSubitems().values()
                                    .stream()
                                    .filter(subItem -> context.domainMetadata.propertyMetadataUtils().hasSubProperty(mdResultProp, subItem.name))
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
                        return Optional.of(Stream.of(new Yield1(yield.operand(), yield.alias() + "." + AMOUNT, yield.hasNonnullableHint())));
                    }
                });
    }


}
