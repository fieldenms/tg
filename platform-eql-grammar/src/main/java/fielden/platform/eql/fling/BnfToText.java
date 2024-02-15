package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.*;
import il.ac.technion.cs.fling.internal.grammar.types.ClassParameter;
import il.ac.technion.cs.fling.internal.grammar.types.Parameter;
import il.ac.technion.cs.fling.internal.grammar.types.StringTypeParameter;
import il.ac.technion.cs.fling.internal.grammar.types.VarargsClassParameter;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * Converts {@link EBNF} instances to a human-readable text format.
 */
public class BnfToText {

    public BnfToText() {}

    public String bnfToText(EBNF ebnf) {
        return ebnf.rules()
                .collect(groupingBy(rule -> rule.variable, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    var variable = entry.getKey();
                    var rules = entry.getValue();
                    return new ERule(variable, rules.stream().flatMap(ERule::bodies).toList());
                })
                .map(this::toString).collect(joining("\n\n"));
    }

    protected String toString(ERule eRule) {
        final int prefixLen = eRule.variable.name().length();
        return eRule.bodies()
                .map(this::toString)
                .collect(joining("\n%s | ".formatted(" ".repeat(prefixLen)), "%s = ".formatted(eRule.variable), ";"));
    }

    protected String toString(final Body body) {
        return body.stream().map(this::toString).collect(joining(" "));
    }

    protected String toString(Component component) {
        return component.isToken() ? toString(component.asToken())
                : component.isQuantifier() ? toString(component.asQuantifier())
                : (component.isVariable() || component.isTerminal()) ? component.name()
                : fail("Unrecognised rule component: %s", component);
    }

    protected String toString(Token token) {
        return !token.isParameterized() ? token.name()
                : token.name() + token.parameters().map(this::toString).collect(joining(", ", "(", ")"));
    }

    protected String toString(Parameter parameter) {
        return parameter.isStringTypeParameter() ? toString(parameter.asStringTypeParameter())
                : parameter.isVariableTypeParameter() ? parameter.asVariableTypeParameter().variable.name()
                : parameter.isVarargsTypeParameter() ? "{ %s }*".formatted(parameter.asVarargsVariableTypeParameter().variable.name())
                : fail("Unrecognised token parameter: %s", parameter);
    }

    protected String toString(StringTypeParameter parameter) {
        String s = "<%s>".formatted(parameter instanceof ClassParameter cp ? cp.parameterClass.getSimpleName()
                : parameter instanceof VarargsClassParameter vcp ? vcp.parameterClass.getSimpleName()
                : fail("Unrecognised token parameter: %s", parameter));
        return parameter instanceof VarargsClassParameter ? s + "*" : s;
    }

    protected String toString(Quantifier quantifier) {
        var symbols = quantifier.symbols().map(this::toString);
        return quantifier instanceof NoneOrMore ? symbols.collect(joining(" ", "{ ", " }*"))
                : quantifier instanceof OneOrMore ? symbols.collect(joining(" ", "{ ", " }+"))
                : quantifier instanceof Opt ? symbols.collect(joining(" ", "{ ", " }?"))
                : fail("Unrecognised quantifier: %s", quantifier);
    }

    protected static <T> T fail(String formatString, Object... args) {
        throw new RuntimeException(String.format(formatString, args));
    }

}
