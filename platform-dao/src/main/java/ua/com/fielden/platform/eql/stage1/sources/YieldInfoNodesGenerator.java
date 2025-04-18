package ua.com.fielden.platform.eql.stage1.sources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage1.sources.exceptions.InvalidYieldMatrixException;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.NullPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.AbstractSingleOperand2;
import ua.com.fielden.platform.eql.stage2.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;
import static ua.com.fielden.platform.utils.EntityUtils.laxSplitPropPath;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;

/**
 * Transforms yields from multiple source queries into <i>yield trees</i>.
 * <p>
 * Example:
 * <pre>
 * yield(...).as("model.key")
 * yield(...).as("model.id")
 * yield(...).as("person")
 * yield(...).as("person.id")
 * yield(...).as("cost")
 *
 *            key
 *          /
 * 1. model
 *          \
 *            id
 *
 * 2. person
 *           \
 *             id
 *
 * 3. cost
 * </pre>
 */
public class YieldInfoNodesGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ERR_INVALID_YIELDS_MATRIX =
    """
    Invalid yields: [%s].
    All source queries in a union must have the same number of yields and use the same set of aliases.
    This union contains %s source queries. To correct the error, each reported yield must occur in all queries.\
    """;

    private YieldInfoNodesGenerator() {}

    /**
     * Processes yields of given source queries.
     * The number of returned yield nodes is equal to the number of yields in each model (all of which must be equal).
     *
     * @param models  sources of yields; must form a rectangular shape: all models must have the same number of yields
     *                sharing the same set of aliases used, otherwise an exception is thrown
     */
    public static Collection<YieldInfoNode> generate(final List<SourceQuery2> models) {
        final List<YieldInfoTail> yieldsInfo = generateYieldInfos(models).stream()
                .map(yield -> new YieldInfoTail(laxSplitPropPath(yield.name()), yield.propType(), yield.nonnullable()))
                .toList();

        return group(yieldsInfo).values();
    }

    private static List<YieldInfo> generateYieldInfos(final List<SourceQuery2> models) {
        if (models.size() == 1) {
            return models.getFirst().yields.getYields().stream()
                    .map(yield -> new YieldInfo(yield.alias(), yield.operand().type(), determineNonnullability(new YieldAndConditions(yield, models.getFirst().whereConditions))))
                    .toList();
        } else {
            final Map<String, List<YieldAndConditions>> yieldMatrix = generateYieldsMatrix(models);
            validateYieldsMatrix(yieldMatrix, models);
            return yieldMatrix.entrySet().stream()
                    .map(yieldEntry -> new YieldInfo(yieldEntry.getKey(), determinePropType(yieldEntry.getValue()), determineNonnullability(yieldEntry.getValue())))
                    .toList();
        }
    }

    private static boolean determineNonnullability(final YieldAndConditions yieldAndConditions) {
        return yieldAndConditions.yield().hasNonnullableHint() || yieldAndConditions.yield().operand().isNonnullableEntity() || yieldAndConditions.conditions().conditionIsSatisfied(new NullPredicate2(yieldAndConditions.yield().operand(), true));
    }

    private static boolean determineNonnullability(final List<YieldAndConditions> yieldVariants) {
        return yieldVariants.stream().allMatch(YieldInfoNodesGenerator::determineNonnullability);
    }

    private static PropType determinePropType(final List<YieldAndConditions> yieldVariants) {
        final Set<PropType> propTypes = yieldVariants.stream()
                .map(yv -> yv.yield.operand().type())
                .filter(PropType::isNotNull)
                .collect(toSet());

        return propTypes.isEmpty() ? NULL_TYPE : AbstractSingleOperand2.getTypeHighestPrecedence(propTypes);
    }

    private static Map<String, List<YieldAndConditions>> generateYieldsMatrix(final List<SourceQuery2> models) {
        return models.stream()
                .flatMap(m -> m.yields.getYields().stream().map(y -> new YieldAndConditions(y, m.whereConditions)))
                .collect(groupingBy(yac -> yac.yield.alias()));
    }

    private static void validateYieldsMatrix(final Map<String, List<YieldAndConditions>> yieldMatrix, final List<SourceQuery2> models) {
        // Each tuple is of the form ("yield alias", "number of models where it occurs").
        final Collection<T2<String, Integer>> invalidYields = yieldMatrix.entrySet().stream()
                .filter(entry -> entry.getValue().size() != models.size())
                .map(entry -> T2.t2(entry.getKey(), entry.getValue().size()))
                .collect(toImmutableList());

        if (!invalidYields.isEmpty()) {
            final var exception = new InvalidYieldMatrixException(
                    ERR_INVALID_YIELDS_MATRIX.formatted(invalidYields.stream().map(t2 -> t2.map("\"%s\" (occurs %s times)"::formatted)).collect(joining(", ")),
                                                        models.size()));
            LOGGER.error(() -> "%s%nSource queries:%n%s".formatted(exception.getMessage(), enumerate(models.stream(), (m, i) -> "%s. %s".formatted(i, m))), exception);
            throw exception;
        }
    }

    private static Map<String, YieldInfoNode> group(final List<YieldInfoTail> yields) {
        return CollectionUtil.mapValues(yields.stream().collect(groupingBy(YieldInfoTail::firstName, mapping(YieldInfoTail::rest, toList()))),
                // each tail's name starts after firstName; or is empty if it's a simple yield
                (firstName, tails) -> {
                    final Optional<YieldInfo> optSimpleYield = tails.stream().filter(YieldInfoTail::isEmpty).findFirst()
                            .map(tail -> new YieldInfo(firstName, tail.propType, tail.nonnullable));
                    final List<YieldInfoTail> nonEmptyTails = tails.stream().filter(tail -> !tail.isEmpty()).toList();

                    return new YieldInfoNode(firstName,
                            optSimpleYield.map(y -> y.propType).orElse(null),
                            optSimpleYield.filter(y -> y.nonnullable).isPresent(),
                            group(nonEmptyTails));
                }
        );
    }

    private record YieldAndConditions(Yield2 yield, Conditions2 conditions) {}

    private record YieldInfo(String name, PropType propType, boolean nonnullable) {}

    private record YieldInfoTail(List<String> name, PropType propType, boolean nonnullable) {

        String firstName() {
            return name.getFirst();
        }

        boolean isEmpty() {
            return name.isEmpty();
        }

        YieldInfoTail rest() {
            return new YieldInfoTail(name.subList(1, name.size()), propType, nonnullable);
        }

    }

}
