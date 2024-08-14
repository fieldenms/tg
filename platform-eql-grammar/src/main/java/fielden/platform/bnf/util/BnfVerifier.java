package fielden.platform.bnf.util;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.BnfException;
import fielden.platform.bnf.Rule;
import fielden.platform.bnf.Variable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public final class BnfVerifier {

    public static void verifyBnf(final BNF bnf) {
        final var rhsVars = bnf.rules().stream().flatMap(BnfVerifier::ruleRhsVariables).map(Variable::normalize).collect(toSet());
        final var lhsVars = bnf.rules().stream().map(Rule::lhs).map(Variable::normalize).collect(toSet());
        final var diff = CollectionUtils.subtract(rhsVars, lhsVars);
        if (!diff.isEmpty()) {
            throw new BnfException("BNF uses non-terminals with no productions: { %s }".formatted(
                    diff.stream().map(Variable::name).collect(joining(","))));
        }
    }

    private static Stream<Variable> ruleRhsVariables(final Rule rule) {
        return rule.rhs().flatten()
                .mapMulti((term, sink) -> {if (term instanceof Variable v) sink.accept(v);});
    }

}
