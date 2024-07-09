package fielden.platform.bnf.util;

import fielden.platform.bnf.*;

import static java.util.stream.Collectors.joining;

/**
 * Converts {@link BNF} instances to a human-readable text format.
 */
public class BnfToText {

    public BnfToText() {}

    public String bnfToText(BNF bnf) {
        return bnf.rules().stream().map(this::toString).collect(joining("\n\n"));
    }

    protected String toString(Rule rule) {
        final int prefixLen = rule.lhs().name().length();
        return rule.rhs().options().stream()
                .map(this::toString)
                .collect(joining("\n%s | ".formatted(" ".repeat(prefixLen)), "%s = ".formatted(rule.lhs().name()), ";"));
    }

    protected String toString(final Sequence body) {
        return body.stream().map(this::toString).collect(joining(" "));
    }

    protected String toString(Term term) {
        return switch (term) {
            case Symbol symbol -> toString(symbol);
            case Sequence sequence -> toString(sequence);
            case Notation notation -> toString(notation);
        };
    }

    protected String toString(Symbol symbol) {
        return switch (symbol) {
            case Terminal terminal -> toString(terminal);
            case Variable variable -> toString(variable);
        };
    }

    protected String toString(Variable variable) {
        return variable.name();
    }

    protected String toString(Terminal terminal) {
        return switch (terminal) {
            case Token token -> toString(token);
            default -> terminalToString(terminal);
        };
    }

    protected String toString(Token token) {
        if (!token.hasParameters()) {
            return terminalToString(token);
        }
        return terminalToString(token) + token.parameters().stream().map(this::toString).collect(joining(", ", "(", ")"));
    }

    private String terminalToString(final Terminal terminal) {
        return terminal.name();
    }

    protected String toString(Parameter parameter) {
        return switch (parameter) {
            case NormalParameter singleParameter -> toString(singleParameter);
            case VarArityParameter varArityParameter -> toString(varArityParameter);
        };
    }

    protected String toString(NormalParameter parameter) {
        return wrapParameter(parameter.type().getSimpleName());
    }

    protected String toString(VarArityParameter parameter) {
        return "%s*".formatted(wrapParameter(parameter.type().getSimpleName()));
    }

    private static String wrapParameter(String text) {
        return "<%s>".formatted(text);
    }
    
    protected String toString(Notation notation) {
        return switch (notation) {
            case Alternation alternation -> toString(alternation);
            case Quantifier quantifier -> toString(quantifier);
        };
    }

    protected String toString(Alternation alternation) {
        return alternation.options().stream().map(this::toString).collect(joining(" | "));
    }

    protected String toString(Quantifier quantifier) {
        String q = switch (quantifier) {
            case ZeroOrMore $ -> "*";
            case OneOrMore $ -> "+";
            case Optional $ -> "?";
        };
        return "{%s}%s".formatted(toString(quantifier.term()), q);
    }

}
