package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.JoinLeafNode2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;

import java.util.List;
import java.util.Optional;

import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.utils.CollectionUtil.append;

/**
 * A query transformer that adds property {@code id} as an ordering criteria.
 * <p>
 * Let Q be the query under transformation.
 * The transformation is applied if all of the following are true:
 * <ol>
 *   <li> Q is a union query.
 *   <li> Q contains a non-empty ordering.
 *   <li> Q yields a property named {@code id} and typed with {@link Long}.
 *        It is not checked whether such a property is truly an entity ID, because cases where it is not are unlikely.
 * </ol>
 */
final class UnionOrderById {

    public static final UnionOrderById INSTANCE = new UnionOrderById();

    private UnionOrderById() {}

    public AbstractQuery2 apply(final AbstractQuery2 query) {
        return maybeAddOrderById(query.orderings.orderBys(), query)
                .map(models -> query.setOrderings(query.orderings.setModels(models)))
                .orElse(query);
    }

    private static Optional<List<OrderBy2>> maybeAddOrderById(final List<OrderBy2> models, final AbstractQuery2 enclosingQuery) {
        return maybeOrderById(models, enclosingQuery)
                .map(orderBy3 -> append(models, orderBy3));
    }

    private static Optional<OrderBy2> maybeOrderById(final List<OrderBy2> models, final AbstractQuery2 enclosingQuery) {
        if (models.isEmpty()) {
            return Optional.empty();
        }
        else if (!isUnion(enclosingQuery)) {
            return Optional.empty();
        }
        else {
            final var containsId = models.stream().anyMatch(orderBy -> isPropId(orderByOperand(orderBy, enclosingQuery.yields)));
            if (containsId) {
                return Optional.empty();
            }
            else {
                // We could also check if the query source contains `id`, which would enable its use even if it is not yielded.
                // For now, let us rely only on yields, as this should cover most cases.
                final var maybeYieldId = enclosingQuery.yields.getYields()
                        .stream()
                        .filter(yield -> isPropId(yield.operand()))
                        .findAny();
                // The order must align with the index definition for `id`, which comes from its primary key constraint, where ascending is the default.
                return maybeYieldId.map(yield -> new OrderBy2(yield.alias(), false));
            }
        }
    }

    private static boolean isPropId(final ISingleOperand2<?> operand) {
        return operand instanceof Prop2 prop && prop.propPath.equals(AbstractEntity.ID) && prop.type().equals(LONG_PROP_TYPE);
    }

    private static ISingleOperand2<?> orderByOperand(final OrderBy2 orderBy, final Yields2 yields) {
        return orderBy.operand() != null
                ? orderBy.operand()
                : yields.yieldsMap().get(orderBy.yieldName()).operand();
    }

    private static boolean isUnion(final AbstractQuery2 query) {
        return query.maybeJoinRoot
                .filter(joinRoot -> joinRoot instanceof JoinLeafNode2 node && node.source() instanceof Source2BasedOnQueries)
                .isPresent();
    }

}
