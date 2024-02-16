package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.ERule;
import il.ac.technion.cs.fling.internal.grammar.rules.Variable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public final class BnfVerifier {

    public static void verifyBnf(EBNF bnf) {
        var rhsVars = bnf.rules().flatMap(ERule::variables).collect(Collectors.toSet());
        var lhsVars = bnf.rules().map(rule -> rule.variable).collect(Collectors.toSet());
        final Collection<Variable> diff = CollectionUtils.subtract(rhsVars, lhsVars);
        if (!diff.isEmpty()) {
            throw new RuntimeException("BNF uses non-terminals with no productions: { %s }".formatted(
                    diff.stream().map(Variable::name).collect(joining(","))));
        }
    }

}
