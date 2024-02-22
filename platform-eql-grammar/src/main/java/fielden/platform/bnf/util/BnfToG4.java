package fielden.platform.bnf.util;

import fielden.platform.bnf.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import static fielden.platform.bnf.TermMetadata.LABEL;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

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

    public BnfToG4(BNF bnf, String grammarName) {
        this.bnf = stripParameters(bnf);
        this.grammarName = grammarName;
    }

    protected String lexerRule(Terminal terminal) {
        return terminal.name().toUpperCase();
    }

    public String bnfToG4() {
        var sb = new StringBuilder();

        sb.append("// This grammar was generated. Timestamp: %s\n\n".formatted(ZonedDateTime.now().toString()));
        sb.append("grammar %s;\n\n".formatted(grammarName));
        sb.append("start : %s EOF;\n\n".formatted(convert(bnf.start())));

        bnf.rules().stream()
                .map(this::convert)
                .forEach(rule -> {
                    sb.append(rule);
                    sb.append('\n');
                    sb.append('\n');
                });

        bnf.terminals().stream()
                .map(terminal -> "%s : '%s' ;\n".formatted(lexerRule(terminal), terminal.name()))
                .distinct()
                .forEach(sb::append);

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

    private static BNF stripParameters(final BNF bnf) {
        final Set<Rule> newRules = bnf.rules().stream()
                .map(rule -> switch (rule) {
                    case Derivation derivation -> derivation.map(term -> switch (term) {
                        case Token token -> token.stripParameters();
                        default -> term;
                    });
                    case Specialization $ -> $;
                })
                .map(BnfToG4::deduplicate)
                .collect(toCollection(LinkedHashSet::new));

        final Set<Terminal> newTerminals = bnf.terminals().stream()
                .map(terminal -> switch (terminal) {
                    case Token token -> token.stripParameters();
                    default -> terminal;
                })
                .collect(toSet());

        return new BNF(newTerminals, bnf.variables(), bnf.start(), newRules);
    }

    /**
     * @return a new rule without duplicate alternations on the right-hand side
     */
    private static Rule deduplicate(Rule rule) {
        return switch (rule) {
            case Derivation d -> new Derivation(d.lhs(), d.rhs().distinct().toList());
            case Specialization s -> new Specialization(s.lhs(), s.specializers().stream().distinct().toList());
        };
    }

}
