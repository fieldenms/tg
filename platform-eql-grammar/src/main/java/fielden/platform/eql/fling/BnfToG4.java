package fielden.platform.eql.fling;

import fielden.platform.eql.fling.BNF.Rule;
import fielden.platform.eql.fling.LabeledTempSymbol.LabeledTerminal;
import il.ac.technion.cs.fling.internal.grammar.rules.*;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Converts a BNF to an ANTLR grammar in the g4 format.
 * <p>
 * Rules with the right hand side in the form of a single terminal or an alternation between single terminals are
 * generated with <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels>rule element labels</a>
 * to make the resulting parse trees easier to work with.
 */
public class BnfToG4 {

    protected final BNF bnf;
    protected final String grammarName;
    protected final Map<Terminal, /*rule name*/ String> lexerRules;

    public BnfToG4(BNF bnf, String grammarName) {
        this.bnf = bnf;
        this.grammarName = grammarName;
        this.lexerRules = bnf.tokens().stream().map(tok -> tok.terminal).distinct()
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
        sb.append("start : %s EOF;\n\n".formatted(convert(bnf.start())));

        bnf.rules().stream()
                .map(this::convert)
                .forEach(rule -> {
                    sb.append(rule);
                    sb.append('\n');
                    sb.append('\n');
                });

        StreamUtils.distinct(lexerRules.entrySet().stream(), Map.Entry::getValue)
                .forEach(entry -> {
                    var terminal = entry.getKey();
                    var ruleName = entry.getValue();
                    sb.append("%s : '%s' ;\n".formatted(ruleName, terminal.name()));
                });

        sb.append("""
                
                WHITESPACE : [ \\r\\t\\n]+ -> skip ;
                COMMENT : '//' .*? '\\n' -> skip ;
                BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
                
                """);

        return sb.toString();
    }

    protected String convert(Rule rule) {
        Function<String, String> labeler = isSingleTerminalRule(rule) ? s -> "token=" + s : Function.identity();
        return "%s :\n      %s\n;".formatted(
                convert(rule.lhs()),
                rule.rhs().map(this::convert).map(labeler).collect(joining("\n    | ")));
    }

    protected String convert(Body body) {
        return body.stream().map(this::convert).collect(joining(" "));
    }

    protected String convert(Component component) {
        // it is important to match labeled symbol first since it is more specific
        return component instanceof LabeledTempSymbol s ? convert(s)
                : component.isToken() ? convert(component.asToken())
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
        String s = lexerRule(terminal);
        return switch (terminal) {
            case LabeledTerminal lt -> "%s=%s".formatted(lt.label(), s);
            default -> s;
        } ;
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

    protected String convert(LabeledTempSymbol symbol) {
        return "%s=%s".formatted(symbol.label(), convert(symbol.symbol().normalize()));
    }

    static boolean isSingleTerminalRule(final Rule rule) {
        return switch (rule) {
            case BNF.Specialization $ -> false;
            case BNF.Derivation derivation -> derivation.rhs()
                    .allMatch(body -> body.size() == 1 && (body.getFirst().isTerminal() || body.getFirst().isToken()));
        };
    }

    protected static <T> T fail(String formatString, Object... args) {
        throw new RuntimeException(String.format(formatString, args));
    }

}
