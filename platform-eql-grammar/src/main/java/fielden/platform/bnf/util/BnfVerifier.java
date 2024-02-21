package fielden.platform.bnf.util;

import fielden.platform.bnf.BNF;
import fielden.platform.bnf.Rule;
import fielden.platform.bnf.Variable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public final class BnfVerifier {

    public static void verifyBnf(BNF bnf) {
        var rhsVars = bnf.rules().stream().flatMap(BnfVerifier::ruleRhsVariables).collect(toSet());
        var lhsVars = bnf.rules().stream().map(Rule::lhs).collect(toSet());
        final Collection<Variable> diff = CollectionUtils.subtract(rhsVars, lhsVars);
        if (!diff.isEmpty()) {
            throw new RuntimeException("BNF uses non-terminals with no productions: { %s }".formatted(
                    diff.stream().map(Variable::name).collect(joining(","))));
        }
    }

    private static Stream<Variable> ruleRhsVariables(Rule rule) {
        return rule.rhsTerms().mapMulti((term, sink) -> {if (term instanceof Variable v) sink.accept(v);});
    }

}
