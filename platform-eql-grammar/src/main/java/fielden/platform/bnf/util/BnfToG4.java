package fielden.platform.bnf.util;

import fielden.platform.bnf.*;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.Map;
import java.util.function.Function;

import static fielden.platform.bnf.TermMetadata.LABEL;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Converts a BNF to an ANTLR grammar in the g4 format.
 * <p>
 * Rules with the right hand side in the form of a single terminal or an alternation between single terminals are
 * generated with <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels>rule element labels</a>
 * to make the resulting parse trees easier to work with.
 * <p>
 * ANTLR parser rules corresponding to BNF <i>specialization</i> rules are generated with
 * <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#alternative-labels>alternative labels</a>.
 */
public class BnfToG4 {

    protected final BNF bnf;
    protected final String grammarName;
    protected final Map<Terminal, /*rule name*/ String> lexerRules;

    public BnfToG4(BNF bnf, String grammarName) {
        this.bnf = bnf;
        this.grammarName = grammarName;
        this.lexerRules = bnf.terminals().stream().distinct()
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
        Function<String, String> labeler =  switch (rule) {
            case Derivation $ -> isSingleTerminalRule(rule) ? s -> "token=" + s : Function.identity();
            case Specialization $ -> s -> makeAltLabelName(rule, s);
        };
        return "%s :\n      %s\n;".formatted(
                convert(rule.lhs()),
                rule.rhs().map(this::convert).map(labeler).collect(joining("\n    | ")));
    }

    protected String makeAltLabelName(Rule rule, String alt) {
        return "%s # %s_%s".formatted(alt, capitalize(rule.lhs().name()), capitalize(alt));
    }

    protected String convert(Sequence seq) {
        return seq.stream().map(this::convert).collect(joining(" "));
    }

    protected String convert(Term term) {
        String s = switch (term) {
            case Symbol symbol -> convert(symbol);
            case Sequence sequence -> convert(sequence);
            case Notation notation -> convert(notation);
        };
        return term.metadata().maybeGet(LABEL).map(lbl -> convertLabeled(lbl, s)).orElse(s);
    }

    protected String convertLabeled(String label, String term) {
        return "%s=%s".formatted(label, term);
    }

    protected String convert(Symbol symbol) {
        return switch (symbol) {
            case Terminal terminal -> convert(terminal);
            case Variable variable -> convert(variable);
        };
    }

    protected String convert(Variable variable) {
        return uncapitalize(variable.name());
    }

    protected String convert(Terminal terminal) {
        return switch (terminal) {
            case Token token -> convert(token);
            default -> convertTerminal(terminal);
        } ;
    }

    protected String convertTerminal(Terminal terminal) {
        return lexerRule(terminal);
    }

    protected String convert(Token token) {
        return convertTerminal(token);
    }

    protected String convert(final Notation notation) {
        final String q = switch (notation) {
            case ZeroOrMore $ -> "*";
            case OneOrMore $ -> "+";
            case Optional $ -> "?";
        };

        final String s = convert(notation.term());

        final Function<String, String> wrapper = switch (notation.term()) {
            case Sequence seq -> (seq.size() > 1) ? str -> "(%s)".formatted(str) : Function.identity();
            default -> Function.identity();
        };

        return wrapper.apply(s) + q;
    }

    static boolean isSingleTerminalRule(final Rule rule) {
        return switch (rule) {
            case Specialization $ -> false;
            case Derivation derivation -> derivation.rhs()
                    .allMatch(body -> body.size() == 1 && body.getFirst() instanceof Terminal);
        };
    }

}
