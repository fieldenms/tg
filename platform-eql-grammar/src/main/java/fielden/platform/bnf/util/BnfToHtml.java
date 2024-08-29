package fielden.platform.bnf.util;

import fielden.platform.bnf.*;
import j2html.TagCreator;
import j2html.tags.DomContent;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static j2html.TagCreator.*;

/**
 * Converts {@link BNF} instances to HTML.
 */
public class BnfToHtml {

    protected static final String PARAMETER_CLASS = "parameter";
    protected static final String DELIMITER_ROW_CLASS = "delimiter-row";
    protected static final String NONTERMINAL_CLASS = "nonterminal";
    protected static final String NONTERMINAL_LHS_CLASS = "nonterminal-lhs";
    protected static final String TERMINAL_CLASS = "terminal";

    public BnfToHtml() {}

    public String bnfToHtml(final BNF bnf) {
        // @formatter:off
        return document(
                html(
                 head(makeStyle()),
                 body(
                  table(
                   each(withDelimiter(
                           bnf.rules().stream().map(this::toHtml),
                           tr(td(), td()).withClass(DELIMITER_ROW_CLASS)))
                  )
                 )
                )
        );
        // @formatter:on
    }

    protected DomContent makeStyle() {
        final var sb = new StringBuilder();

        sb.append("""
                .%s {
                  font-family: mono;
                }
                """.formatted(TERMINAL_CLASS));

        sb.append("""
                .%s {
                  font-style: italic;
                }
                """.formatted(NONTERMINAL_LHS_CLASS));

        sb.append("""
                .%s {
                  font-style: italic;
                }
                a.%1$s { text-decoration: none; }
                """.formatted(NONTERMINAL_CLASS));

        sb.append("""
                a.%1$s:link    { color: green; }
                a.%1$s:visited { color: green; }
                a.%1$s:hover   { color: blue;  }
                a.%1$s:active  { color: blue;  }
                """.formatted(NONTERMINAL_CLASS));

        sb.append("""
                .%s {
                  background-color: lightgray;
                }
                """.formatted(DELIMITER_ROW_CLASS));

        sb.append("""
                .%s {
                  color: gray;
                }
                """.formatted(PARAMETER_CLASS));

        return style(sb.toString());
    }

    protected DomContent toHtml(final Rule rule) {
        var first = tr(
                td(a(rule.lhs().name()).attr("name", rule.lhs().name()).withClass(NONTERMINAL_LHS_CLASS)),
                td(""));
       var rest = rule.rhs().options().stream().map(this::ruleBodyToHtml);
       return each(first, TagCreator.each(rest));
    }

    protected DomContent ruleBodyToHtml(final Term term) {
        return tr(td(), td(toHtml(term)));
    }

    protected DomContent toHtml(final Term term) {
        return switch (term) {
            case Symbol symbol -> toHtml(symbol);
            case Sequence sequence -> toHtml(sequence);
            case Notation notation -> toHtml(notation);
        };
    }

    protected DomContent toHtml(final Symbol symbol) {
        return switch (symbol) {
            case Terminal terminal -> toHtml(terminal);
            case Variable variable -> toHtml(variable);
        };
    }

    protected DomContent toHtml(final Variable variable) {
        return a(variable.name()).withHref("#" + variable.name()).withClass(NONTERMINAL_CLASS);
    }

    protected DomContent toHtml(final Terminal terminal) {
        return switch (terminal) {
            case Token token -> toHtml(token);
            default -> terminalToHtml(terminal);
        };
    }

    protected DomContent terminalToHtml(final Terminal terminal) {
        return span(terminal.name()).withClass(TERMINAL_CLASS);
    }

    protected DomContent toHtml(final Token token) {
        if (!token.hasParameters()) {
            return terminalToHtml(token);
        }
        return each(
                terminalToHtml(token),
                text("("),
                each(withDelimiter(token.parameters().stream().map(this::toHtml), text(" "))),
                text(")"));
    }

    protected DomContent toHtml(final Parameter parameter) {
        return switch (parameter) {
            case NormalParameter singleParameter -> toHtml(singleParameter);
            case VarArityParameter varArityParameter -> toHtml(varArityParameter);
        };
    }

    protected DomContent toHtml(final NormalParameter parameter) {
        String name = parameter.type().getSimpleName();
        return wrapParameter(name);
    }

    protected DomContent toHtml(final VarArityParameter parameter) {
        return each(wrapParameter(parameter.type().getSimpleName()), text("*"));
    }

    private static DomContent wrapParameter(final String text) {
        return span("<%s>".formatted(text)).withClass(PARAMETER_CLASS);
    }

    protected DomContent toHtml(final Notation notation) {
        return switch (notation) {
            case Alternation alternation -> toHtml(alternation);
            case Quantifier quantifier -> toHtml(quantifier);
        };
    }

    protected DomContent toHtml(final Alternation alternation) {
        final List<? extends Term> options = alternation.options();
        if (options.size() == 1) {
            return toHtml(options.getFirst());
        }
        return each(
                text("{"),
                each(withDelimiter(options.stream().map(this::toHtml), text(" | "))),
                text("}"));
    }

    protected DomContent toHtml(final Quantifier quantifier) {
        final String q = switch (quantifier) {
            case ZeroOrMore x -> "*";
            case OneOrMore x ->  "+";
            case Optional x ->   "?";
        };
        return each(
                text("{"),
                toHtml(quantifier.term()),
                text("}"),
                text(q)
        );
    }

    protected DomContent toHtml(final Sequence seq) {
        return each(withDelimiter(seq.stream().map(this::toHtml), text(" ")));
    }

    private static <T, D extends T> Stream<T> withDelimiter(final Stream<T> stream, final D delimiter) {
        return StreamSupport.stream(withDelimiter(stream.spliterator(), delimiter), false);
    }

    private static <T, D extends T> Spliterator<T> withDelimiter(final Spliterator<T> splitr, final D delimiter) {
        return new Spliterators.AbstractSpliterator<>(splitr.estimateSize(), 0) {
            private boolean afterFirst = false;

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                final boolean hadNext = splitr.tryAdvance(elt -> {
                    if (afterFirst) {
                        consumer.accept(delimiter);
                    }
                    consumer.accept(elt);
                });
                if (hadNext) {
                    afterFirst = true;
                }
                return hadNext;
            }
        };
    }

}
