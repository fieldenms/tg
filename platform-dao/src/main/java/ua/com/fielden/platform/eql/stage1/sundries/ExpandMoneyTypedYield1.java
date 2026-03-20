package ua.com.fielden.platform.eql.stage1.sundries;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.eql.antlr.EqlCompilationResult;
import ua.com.fielden.platform.eql.antlr.EqlCompiler;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.types.Money;

import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;

/// A transformation on individual yields [Yield1] whose destination is a [Money]-typed property.
///
/// The yielded expression is transformed into one or more yields for [Money] components, which are inferred from the yielded expression.
/// The set of yielded components is based on an intersection of [Money] represenations used for the yielded property
/// and the destination property.
/// ```
/// select(Vehicle.class)
/// .yield().prop("price").as("price")
/// .modelAsEntity(ReVehicle.class)
/// // If both Vehicle.price and ReVehicle.price have currency:
/// select(Vehicle.class)
/// .yield().prop("price.amount").as("price.amount")
/// .yield().prop("price.currency").as("price.currency")
/// .modelAsEntity(ReVehicle.class)
/// // If one of Vehicle.price, ReVehicle.price has only amount:
/// select(Vehicle.class)
/// .yield().prop("price.amount").as("price.amount")
/// .modelAsEntity(ReVehicle.class)
/// ```
///
public final class ExpandMoneyTypedYield1 {

    public static final ExpandMoneyTypedYield1 INSTANCE = new ExpandMoneyTypedYield1();

    private static final String
            ERR_UNSUPPORTED_COMPONENT = "Yield inference is unsupported for [%s.%s].",
            ERR_COULD_NOT_INFER = "[%s] must be yielded into explicitly. [%s] could not be inferred from yield [%s]: %s";

    private static final Logger LOGGER = getLogger();

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
                .map(mdResultProp -> context.domainMetadata.propertyMetadataUtils().subProperties(mdResultProp)
                                     .stream()
                                     .map(subProp -> expandComponent(yield, context, query, subProp.name()))
                                     .flatMap(Optional::stream));
    }

    private Optional<Yield1> expandComponent(
            final Yield1 yield,
            final TransformationContextFromStage1To2 context,
            final AbstractQuery1 query,
            final String componentName)
    {
        final var componentAlias = yield.alias() + "." + componentName;
        // Don't do anything if the query already yields this component.
        if (query.yields.yieldsMap().containsKey(componentAlias)) {
            return Optional.empty();
        }

        return switch (componentName) {
            case AMOUNT -> Optional.of(new Yield1(yield.operand(), yield.alias() + "." + AMOUNT, yield.hasNonnullableHint()));
            case CURRENCY -> {
                final var currencyModel = context.moneyComponentInference.infer(yield.operand(), CURRENCY, context.moneyComponentInference.predicateIsMoneyWithComponent(context, CURRENCY))
                        .orElseThrow(err -> new EqlStage1ProcessingException(format(ERR_COULD_NOT_INFER, componentAlias, componentAlias, yield.alias(), err)));
                LOGGER.debug(() -> format("Inferred yield for [%s] in a query with result type [%s].\nInferred expression: %s",
                                          componentAlias, query.resultType, currencyModel));
                final var expr1 = new EqlCompiler(context.stage1Transformer).compile(currencyModel.tokens(), EqlCompilationResult.StandaloneExpression.class).model();
                yield Optional.of(new Yield1(expr1, componentAlias, yield.hasNonnullableHint()));
            }
            default -> throw new EqlStage1ProcessingException(format(ERR_UNSUPPORTED_COMPONENT, Money.class.getSimpleName(), componentName));
        };
    }

}
