package fielden.platform.eql.fling;

import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.internal.grammar.rules.*;
import il.ac.technion.cs.fling.internal.grammar.types.*;
import j2html.tags.DomContent;

import java.util.LinkedHashMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static j2html.TagCreator.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * Converts {@link EBNF} instances to HTML.
 */
public class BnfToHtml {

    protected static final String PARAMETER_CLASS = "parameter";
    protected static final String DELIMITER_ROW_CLASS = "delimiter-row";
    protected static final String NONTERMINAL_CLASS = "nonterminal";
    protected static final String NONTERMINAL_LHS_CLASS = "nonterminal-lhs";
    protected static final String TERMINAL_CLASS = "terminal";

    public BnfToHtml() {}

    public String bnfToHtml(EBNF ebnf) {
        // @formatter:off
        return document(
                html(
                 head(makeStyle()),
                 body(
                  table(
                   each(withDelimiter(
                           ebnf.rules()
                                   .collect(groupingBy(rule -> rule.variable, LinkedHashMap::new, Collectors.toList()))
                                   .entrySet().stream()
                                   .map(entry -> {
                                       var variable = entry.getKey();
                                       var rules = entry.getValue();
                                       return new ERule(variable, rules.stream().flatMap(ERule::bodies).toList());
                                   })
                                   .map(this::toHtml),
                           tr(td(), td()).withClass(DELIMITER_ROW_CLASS)))
                  )
                 )
                )
        );
        // @formatter:on
    }

    protected DomContent makeStyle() {
        var sb = new StringBuilder();

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

        return style(sb.toString());
    }

    protected DomContent toHtml(ERule eRule) {
        var first = tr(
                td(a(eRule.variable.name()).attr("name", eRule.variable.name()).withClass(NONTERMINAL_LHS_CLASS)),
                td(""));
       var rest = eRule.bodies().map(this::toHtml);
       return each(first, each(rest));
    }

    protected DomContent toHtml(final Body body) {
        return tr(
                td(),
                td(each(withDelimiter(body.stream().map(this::toHtml), text(" ")))));
    }

    protected DomContent toHtml(Component component) {
        return component.isToken() ? toHtml(component.asToken())
                : component.isQuantifier() ? toHtml(component.asQuantifier())
                : component.isVariable() ? toHtml(component.asVariable())
                : component.isTerminal() ? toHtml(component.asTerminal())
                : fail("Unrecognised rule component: %s", component);
    }

    protected DomContent toHtml(Variable variable) {
        return a(variable.name()).withHref("#" + variable.name()).withClass(NONTERMINAL_CLASS);
    }

    protected DomContent toHtml(Terminal terminal) {
        return span(terminal.name()).withClass(TERMINAL_CLASS);
    }

    protected DomContent toHtml(Token token) {
        return !token.isParameterized()
                ? toHtml(token.terminal)
                : each(
                toHtml(token.terminal),
                text("("),
                each(withDelimiter(token.parameters().map(this::toHtml), text(" "))),
                text(")"));
    }

    protected DomContent toHtml(Parameter parameter) {
        return parameter.isStringTypeParameter() ? toHtml(parameter.asStringTypeParameter())
                : parameter.isVariableTypeParameter() ? toHtml(parameter.asVariableTypeParameter())
                : parameter.isVarargsTypeParameter() ? toHtml(parameter.asVarargsVariableTypeParameter())
                : fail("Unrecognised token parameter: %s", parameter);
    }

    protected DomContent toHtml(StringTypeParameter parameter) {
        String name = switch (parameter) {
            case ClassParameter cp -> cp.parameterClass.getSimpleName();
            case VarargsClassParameter cp -> cp.parameterClass.getSimpleName();
            default -> fail("Unrecognised token parameter: %s", parameter);
        };
        var b = Stream.<DomContent>builder();
        b.add(wrapParameter("<%s>".formatted(name)));
        if (parameter instanceof VarargsClassParameter)
            b.add(text("*"));
        return each(b.build());
    }

    protected DomContent toHtml(VarargsVariableTypeParameter parameter) {
        return each(text("{ "),
                wrapParameter(parameter.variable.name()),
                text(" }*"));
    }

    protected DomContent toHtml(VariableTypeParameter parameter) {
        return wrapParameter(toHtml(parameter.variable));
    }

    private static DomContent wrapParameter(String text) {
        return span(text).withClass(PARAMETER_CLASS);
    }

    private static DomContent wrapParameter(DomContent content) {
        return span(content).withClass(PARAMETER_CLASS);
    }

    protected DomContent toHtml(Quantifier quantifier) {
        String q = switch (quantifier) {
            case NoneOrMore $ -> "*";
            case OneOrMore $ -> "+";
            case Opt $ -> "?";
            default -> fail("Unrecognised quantifier: %s", quantifier);
        };
        return each(
                text("{"),
                each(withDelimiter(quantifier.symbols().map(this::toHtml), text(" "))),
                text("}"),
                text(q)
        );
    }

    protected static <T> T fail(String formatString, Object... args) {
        throw new RuntimeException(String.format(formatString, args));
    }

    private static <T, D extends T> Stream<T> withDelimiter(Stream<T> stream, D delimiter) {
        return StreamSupport.stream(withDelimiter(stream.spliterator(), delimiter), false);
    }

    private static <T, D extends T> Spliterator<T> withDelimiter(final Spliterator<T> splitr, D delimiter) {
        return new Spliterators.AbstractSpliterator<>(splitr.estimateSize(), 0) {
            boolean afterFirst = false;

            @Override
            public boolean tryAdvance(final Consumer<? super T> consumer) {
                final boolean hadNext = splitr.tryAdvance(elt -> {
                    if (afterFirst)
                        consumer.accept(delimiter);
                    consumer.accept(elt);
                });
                if (hadNext)
                    afterFirst = true;
                return hadNext;
            }
        };
    }

}
