package fielden.platform.eql.fling;

import fielden.platform.eql.fling.BNF.Rule;
import il.ac.technion.cs.fling.internal.grammar.rules.Variable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public final class BnfVerifier {

    public static void verifyBnf(BNF bnf) {
        var rhsVars = bnf.rules().stream().flatMap(Rule::variables).collect(toSet());
        var lhsVars = bnf.rules().stream().map(Rule::lhs).collect(toSet());
        final Collection<Variable> diff = CollectionUtils.subtract(rhsVars, lhsVars);
        if (!diff.isEmpty()) {
            throw new RuntimeException("BNF uses non-terminals with no productions: { %s }".formatted(
                    diff.stream().map(Variable::name).collect(joining(","))));
        }
    }

}
