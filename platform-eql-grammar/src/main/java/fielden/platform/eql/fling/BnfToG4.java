package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.*;

import java.util.LinkedHashMap;

import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Converts a BNF to an ANTLR grammar in the g4 format.
 */
public class BnfToG4 {

    public BnfToG4() {}

    public String bnfToG4(EBNF bnf, String grammarName) {
        var sb = new StringBuilder();

        sb.append("grammar %s;\n\n".formatted(grammarName));
        sb.append("start : %s EOF;\n\n".formatted(convert(bnf.ε)));

        bnf.rules()
                .collect(groupingBy(rule -> rule.variable, LinkedHashMap::new, toList()))
                .entrySet().stream()
                .map(entry -> {
                    var variable = entry.getKey();
                    var rules = entry.getValue();
                    return new ERule(variable, rules.stream().flatMap(ERule::bodies).toList());
                })
                .map(this::convert)
                .forEach(rule -> {
                    sb.append(rule);
                    sb.append('\n');
                    sb.append('\n');
                });

        sb.append("""
                WHITESPACE : [ \\r\\t\\n]+ -> skip ;
                COMMENT : '//' .*? '\\n' -> skip ;
                BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
                """);

        return sb.toString();
    }

    protected String convert(ERule eRule) {
        return "%s :\n      %s\n;".formatted(
                convert(eRule.variable),
                eRule.bodies().map(this::convert).collect(joining("\n    | ")));
    }

    protected String convert(Body body) {
        return body.stream().map(this::convert).collect(joining(" "));
    }

    protected String convert(Component component) {
        return component.isToken() ? convert(component.asToken())
                : component.isQuantifier() ? convert(component.asQuantifier())
                : component.isVariable() ? convert(component.asVariable())
                : component.isTerminal() ? convert(component.asTerminal())
                : fail("Unrecognised rule component: %s", component);
    }

    protected String convert(Token token) {
        // TODO parameters
        return convert(token.terminal);
//        return !token.isParameterized() ? token.name()
//                : token.name() + token.parameters().map(this::convert).collect(joining(", ", "(", ")"));
    }

    protected String convert(Variable variable) {
        return uncapitalize(variable.name());
    }

    protected String convert(Terminal terminal) {
        return "'%s'".formatted(terminal.name());
    }

    protected String convert(Quantifier quantifier) {
        String q = switch (quantifier) {
            case NoneOrMore $ -> "*";
            case OneOrMore $ -> "+";
            case Opt $ -> "?";
            default -> fail("Unrecognised quantifier: %s", quantifier);
        };
        return quantifier.symbols().map(this::convert).collect(joining(" ", "(", ")" + q));
    }

    protected static <T> T fail(String formatString, Object... args) {
        throw new RuntimeException(String.format(formatString, args));
    }

}
