package ua.com.fielden.eql;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.*;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class EqlRandomGenerator {

    private final Random rng;

    public EqlRandomGenerator(final Random rng) {
        this.rng = rng;
    }

    public ICompoundCondition0<?> conditions(final ICompoundCondition0<?> chain, final int n) {
        var tmp = logicalOperator(chain);
        for (int i = 0; i < n - 1; i++) {
            tmp = logicalOperator(completePredicate(comparisonOperand(tmp)));
        }
        return completePredicate(comparisonOperand(tmp));
    }

    public <T> T logicalOperator(final ILogicalOperator<T> chain) {
        final List<Supplier<T>> choices = List.of(chain::and, chain::or);
        return randomChoice(choices).get();
    }

    public <T> T comparisonOperand(final EntityQueryProgressiveInterfaces.IComparisonOperand<T, ?> chain) {
        final List<Supplier<T>> choices = List.of(
                () -> chain.val(1),
                () -> chain.iVal(1),
                () -> chain.prop("propertyName"),
                () -> chain.param("paramName"),
                () -> chain.iParam("paramName"),
                () -> chain.extProp("propertyName"),
                () -> chain.anyOfProps("prop1", "prop2", "prop3"),
                () -> chain.allOfProps("prop1", "prop2", "prop3"),
                () -> chain.anyOfParams("param1", "param2", "param3"),
                () -> chain.allOfParams("param1", "param2", "param3"),
                () -> chain.anyOfIParams("param1", "param2", "param3"),
                () -> chain.allOfIParams("param1", "param2", "param3"),
                () -> chain.allOfValues(1, 2, 3),
                () -> chain.anyOfValues(1, 2, 3),
                () -> chain.allOfValues("a", "b", "c"),
                () -> chain.anyOfValues("a", "b", "c"),
                chain::now
                //                () -> chain.upperCase()
        );
        return randomChoice(choices).get();
    }

    public <T> T comparisonOperand(final IComparisonQuantifiedOperand<T, ?> chain) {
        final List<Supplier<T>> choices = List.of(
                () -> chain.val(1),
                () -> chain.iVal(1),
                () -> chain.prop("propertyName"),
                () -> chain.param("paramName"),
                () -> chain.iParam("paramName"),
                () -> chain.extProp("propertyName"),
                () -> chain.anyOfProps("prop1", "prop2", "prop3"),
                () -> chain.allOfProps("prop1", "prop2", "prop3"),
                () -> chain.anyOfParams("param1", "param2", "param3"),
                () -> chain.allOfParams("param1", "param2", "param3"),
                () -> chain.anyOfIParams("param1", "param2", "param3"),
                () -> chain.allOfIParams("param1", "param2", "param3"),
                () -> chain.allOfValues(1, 2, 3),
                () -> chain.anyOfValues(1, 2, 3),
                () -> chain.allOfValues("a", "b", "c"),
                () -> chain.anyOfValues("a", "b", "c"),
                chain::now
                //                () -> chain.all(null),
                //                () -> chain.any(null)
        );
        return randomChoice(choices).get();
    }

    public <T> T comparisonOperand(final IComparisonSetOperand<T> chain) {
        final List<Supplier<T>> choices = List.of(
                () -> chain.values(1, 2, 3),
                () -> chain.values("a", "b", "c"),
                () -> chain.props("a", "b", "c"),
                () -> chain.params("a", "b", "c"),
                () -> chain.iParams("a", "b", "c")
                //                () -> chain.model()
        );
        return randomChoice(choices).get();
    }

    public <T extends ILogicalOperator<?>> T completePredicate(final IComparisonOperator<T, ?> chain) {
        final List<Supplier<T>> choices = List.of(
                chain::isNull,
                chain::isNotNull,
                () -> comparisonOperand(chain.eq()),
                () -> comparisonOperand(chain.ne()),
                () -> comparisonOperand(chain.lt()),
                () -> comparisonOperand(chain.gt()),
                () -> comparisonOperand(chain.le()),
                () -> comparisonOperand(chain.ge()),
                () -> comparisonOperand(chain.like()),
                () -> comparisonOperand(chain.iLike()),
                () -> comparisonOperand(chain.notLike()),
                () -> comparisonOperand(chain.notILike()),
                () -> comparisonOperand(chain.likeWithCast()),
                () -> comparisonOperand(chain.notLikeWithCast()),
                () -> comparisonOperand(chain.iLikeWithCast()),
                () -> comparisonOperand(chain.notILikeWithCast()),
                () -> comparisonOperand(chain.in()),
                () -> comparisonOperand(chain.notIn())
        );
        return randomChoice(choices).get();
    }

    private <T> T randomChoice(final List<? extends T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Can't choose from an empty List.");
        }
        return items.get(rng.nextInt(0, items.size()));
    }

}
