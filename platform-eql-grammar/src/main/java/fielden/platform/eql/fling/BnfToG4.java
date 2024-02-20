package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Converts a BNF to an ANTLR grammar in the g4 format.
 * <p>
 * Rules with the right hand side in the form of a single terminal or an alternation between single terminals are
 * generated with <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels>rule element labels</a>
 * to make the resulting parse trees easier to work with.
 */
public class BnfToG4 {

    protected final EBNF bnf;
    protected final String grammarName;
    protected final Map<Terminal, /*rule name*/ String> lexerRules;

    public BnfToG4(EBNF bnf, String grammarName) {
        this.bnf = bnf;
        this.grammarName = grammarName;
        this.lexerRules = bnf.Σ.stream().map(tok -> tok.terminal).distinct()
                .collect(toMap(Function.identity(), t -> t.name().toUpperCase()));
    }

    protected String lexerRule(Terminal terminal) {
        String rule = lexerRules.get(terminal);
        if (rule == null)
            throw new IllegalArgumentException("Terminal doesn't belong to this grammar: %s".formatted(terminal));
        return rule;
    }

    public String bnfToG4() {
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

        lexerRules.forEach((token, ruleName) -> {
            sb.append("%s : '%s' ;\n".formatted(ruleName, token.name()));
        });

        sb.append("""
                
                WHITESPACE : [ \\r\\t\\n]+ -> skip ;
                COMMENT : '//' .*? '\\n' -> skip ;
                BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
                
                """);

        return sb.toString();
    }

    protected String convert(ERule eRule) {
        Function<String, String> labeler = isSingleTerminalRule(eRule) ? s -> "token=" + s : Function.identity();
        return "%s :\n      %s\n;".formatted(
                convert(eRule.variable),
                eRule.bodies().map(this::convert).map(labeler).collect(joining("\n    | ")));
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
        return lexerRule(terminal);
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

    static boolean isSingleTerminalRule(final ERule rule) {
        return rule.bodies().allMatch(body -> body.size() == 1 && (body.getFirst().isTerminal() || body.getFirst().isToken()));
    }

    protected static <T> T fail(String formatString, Object... args) {
        throw new RuntimeException(String.format(formatString, args));
    }

}
