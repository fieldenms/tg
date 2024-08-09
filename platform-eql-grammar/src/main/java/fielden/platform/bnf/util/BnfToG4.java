package fielden.platform.bnf.util;

import com.squareup.javapoet.*;
import fielden.platform.bnf.Optional;
import fielden.platform.bnf.*;
import org.antlr.v4.runtime.CommonToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static fielden.platform.bnf.Rule.isSingleAltRule;
import static fielden.platform.bnf.Metadata.LABEL;
import static fielden.platform.bnf.Metadata.LIST_LABEL;
import static fielden.platform.bnf.util.BnfUtils.removeUnused;
import static java.util.stream.Collectors.*;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

/**
 * Converts a {@link BNF} to an ANTLR4 grammar, producing the following artifacts:
 * <ul>
 *   <li> ANTLR4 grammar file
 *   <li> Java source files with definitions of custom token types for ANTLR. These token types correspond to parameterised
 *        {@link Token}s in the given {@link BNF}.
 * </ul>
 *
 * Grammar generation details:
 * <ul>
 *   <li> Terminal rules, i.e., rules with the right hand side in the form of a single terminal or an alternation between
 *        single terminals are generated with
 *        <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#rule-element-labels>rule element labels</a>
 *        to make the resulting parse trees easier to work with.
 *   <li> ANTLR parser rules that have one or more alternatives are generated with
 *        <a href=https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#alternative-labels>alternative labels</a>
 *        unless the rule is a terminal rule.
 * </ul>
 */
public class BnfToG4 {

    protected final BNF bnf;
    protected final BNF originalBnf;
    protected final String grammarName;

    public BnfToG4(final BNF bnf, final String grammarName) {
        this.originalBnf = bnf;
        this.bnf = removeUnused.compose(RuleInliner.inlineRules.compose(stripParameters)).apply(bnf);
        this.grammarName = grammarName;
    }

    public static void writeResult(final Result result, final Path directory) throws IOException {
        Files.createDirectories(directory);

        Path filePath = directory.resolve(result.grammarName + ".g4");
        Files.writeString(filePath, result.grammarSource());

        for (final JavaFile file : result.files) {
            file.writeTo(directory);
        }
    }

    protected String lexerRule(final Terminal terminal) {
        return terminal.name().toUpperCase();
    }

    public record Result(String grammarName, String grammarSource, Collection<? extends JavaFile> files) {}

    public Result bnfToG4() {
        return new Result(grammarName, generateGrammar(), generateFiles());
    }

    protected String generateGrammar() {
        final var sb = new StringBuilder();

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
                .sorted()
                .forEach(sb::append);

        // Using "channel(HIDDEN)" instead of "skip" makes these tokens appear in error messages, making them more readable.
        sb.append("""

                WHITESPACE : [ \\r\\t\\n]+ -> channel(HIDDEN) ;
                COMMENT : '//' .*? '\\n' -> channel(HIDDEN) ;
                BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;

                """);

        return sb.toString();
    }

    protected String convert(final Rule rule) {
        final Function<String, String> labeler =  switch (rule) {
            case Derivation $ -> isSingleTerminalRule(rule) ? s -> "token=" + s : Function.identity();
            case Specialization $ -> s -> makeAltLabelName(rule, s);
        };
        return "%s :\n      %s\n;".formatted(
                convert(rule.lhs()),
                rule.rhs().options().stream().map(this::convert).map(labeler).collect(joining("\n    | ")));
    }

    protected String makeAltLabelName(final Rule rule, final String alt) {
        return "%s # %s_%s".formatted(alt, capitalize(rule.lhs().name()), capitalize(alt));
    }

    protected String convert(final Sequence seq) {
        return seq.stream().map(this::convert).collect(joining(" "));
    }

    protected String convert(final Term term) {
        String s = switch (term) {
            case Symbol symbol -> convert(symbol);
            case Sequence sequence -> convert(sequence);
            case Notation notation -> convert(notation);
        };
        return term.metadata().get(LIST_LABEL).map(lbl -> convertListLabeled(lbl, s))
                .or(() -> term.metadata().get(LABEL).map(lbl -> convertLabeled(lbl, s)))
                .orElse(s);
    }

    protected String convertListLabeled(String label, String term) {
        return "%s+=%s".formatted(label, term);
    }

    protected String convertLabeled(final String label, final String term) {
        return "%s=%s".formatted(label, term);
    }

    protected String convert(final Symbol symbol) {
        return switch (symbol) {
            case Terminal terminal -> convert(terminal);
            case Variable variable -> convert(variable);
        };
    }

    protected String convert(final Variable variable) {
        return uncapitalize(variable.name());
    }

    protected String convert(final Terminal terminal) {
        return switch (terminal) {
            case Token token -> convert(token);
            default -> convertTerminal(terminal);
        } ;
    }

    protected String convertTerminal(final Terminal terminal) {
        return lexerRule(terminal);
    }

    protected String convert(final Token token) {
        return convertTerminal(token);
    }

    protected String convert(final Notation notation) {
        return switch (notation) {
            case Quantifier quantifier -> convert(quantifier);
            case Alternation alternation -> convert(alternation);
        };
    }

    protected String convert(final Alternation alternation) {
        final List<? extends Term> options = alternation.options();
        if (options.size() == 1) {
            return convert(options.getFirst());
        }
        return options.stream().map(this::convert).collect(joining(" | ", "(", ")"));
    }

    protected String convert(final Quantifier quantifier) {
        final String q = switch (quantifier) {
            case ZeroOrMore x -> "*";
            case OneOrMore x ->  "+";
            case Optional x ->   "?";
        };
        final Function<String, String> wrapper = switch (quantifier.term()) {
            case Sequence seq when (seq.size() > 1) -> "(%s)"::formatted;
            default -> Function.identity();
        };

        return wrapper.apply(convert(quantifier.term())) + q;
    }

    static boolean isSingleTerminalRule(final Rule rule) {
        return switch (rule) {
            case Specialization $ -> false;
            case Derivation derivation -> derivation.rhs().options().stream().allMatch(term -> term instanceof Terminal);
        };
    }

    protected Collection<JavaFile> generateFiles() {
        return originalBnf.rules().stream()
                .flatMap(rule -> rule.rhs().flatten())
                .mapMulti(typeFilter(Token.class))
                .filter(Token::hasParameters)
                .map(Token::normalize)
                .distinct()
                .map(token -> new TokenSourceGenerator(token).generate())
                // TODO control the destination package
                .map(typeSpec -> JavaFile.builder("tokens", typeSpec)
                        .addFileComment("This file was generated. Timestamp: %s".formatted(ZonedDateTime.now().toString()))
                        .build())
                .toList();
    }

    /**
     * Generates a custom token type for ANTLR corresponding to the token in the BNF grammar.
     */
    private class TokenSourceGenerator {
        final Token token;

        TokenSourceGenerator(final Token token) {
            this.token = token;
        }

        public TypeSpec generate() {
            final List<FieldSpec> fields = enumerate(token.parameters(), this::makeFieldForParam);

            return TypeSpec.classBuilder(nameTokenType(token))
                    .addModifiers(PUBLIC, FINAL)
                    .superclass(CommonToken.class)
                    .addFields(fields)
                    .addMethod(makeConstructor(fields))
                    .build();
        }

        protected MethodSpec makeConstructor(final List<FieldSpec> fields) {
            final List<ParameterSpec> parameterSpecs = fields.stream()
                    .map(f -> ParameterSpec.builder(f.type, f.name, FINAL).build())
                    .toList();

            final CodeBlock assignments = makeStatements(
                    zip(fields.stream(), parameterSpecs.stream(),
                            (field, param) -> CodeBlock.of("this.$N = $N", field.name, param.name))
                            .toList());

            return constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameters(parameterSpecs)
                    // TODO extract the lexer name
                    .addStatement("super(EQLLexer.$N, $S)", lexerRule(token), token.name())
                    .addCode(assignments)
                    .build();
        }

        protected FieldSpec makeFieldForParam(final Parameter parameter, final int counter) {
            final TypeName typeName = switch (parameter) {
                case NormalParameter p -> ClassName.get(p.type());
                case VarArityParameter p -> ParameterizedTypeName.get(List.class, p.type());
            };
            return FieldSpec.builder(typeName, "x%s".formatted(counter), PUBLIC, FINAL).build();
        }

        protected String nameTokenType(final Token token) {
            return capitalize("%sToken".formatted(token.name()));
        }
    }

    private static CodeBlock makeStatements(final Collection<? extends CodeBlock> codeBlocks) {
        final var builder = CodeBlock.builder();
        codeBlocks.forEach(builder::addStatement);
        return builder.build();
    }

    // -------------------- Utilities

    private static final GrammarTransformer stripParameters = bnf -> {
        final Set<Rule> newRules = bnf.rules().stream()
                .map(rule -> switch (rule) {
                    case Derivation derivation -> derivation.recMap(term -> switch (term) {
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
    };

    /**
     * @return a new rule without duplicate alternations on the right-hand side
     */
    private static Rule deduplicate(final Rule rule) {
        return switch (rule) {
            case Derivation d -> new Derivation(d.lhs(), new Alternation(d.rhs().options().stream().distinct().toList(), d.rhs().metadata()));
            case Specialization s -> new Specialization(s.lhs(), s.specializers().stream().distinct().toList());
        };
    }

    /**
     * A rule associated with a variable that occurs in another rule's body is inlineable if that variable doesn't occur
     * anywhere else.
     * <p>
     * Specialisation can be inlined only if each specialiser can, resulting in a derivation.
     * Inlinining of a derivation doesn't require all of its alternatives to be inlineable, only the inlineable ones will
     * be inlined.
     * <p>
     * A rule can be inlined if any of the following holds:
     * <ul>
     *   <li> Its RHS consists of a single alternative. For example, {@code Add = Number + Number}.
     *   <li> Its RHS consists of an alternation of sole terminals. For example, {@code Symbol = a | b | c}.
     * </ul>
     *
     * To inline a rule is to inline a variable associated with that rule. For example,
     * <pre>
     * Condition = Predicate Argument | AndCondition
     * AndCondition = Condition and Condition
     *
     * // AndCondition can be inlined
     *
     * Condition = Predicate Argument | Condition and Condition
     * </pre>
     */
    private static final class RuleInliner {
        public static final GrammarTransformer inlineRules = bnf -> new RuleInliner(bnf).inlineRules();

        private final BNF bnf;

        private RuleInliner(final BNF bnf) {
            this.bnf = bnf;
        }

        public BNF inlineRules() {
            final Set<Rule> newRules = bnf.rules().stream()
                    .map(this::inline)
                    .collect(toCollection(LinkedHashSet::new));

            return new BNF(bnf.terminals(), bnf.variables(), bnf.start(), newRules);
        }

        private Rule inline(final Rule rule) {
            return switch (rule) {
                case Derivation derivation -> inline(derivation);
                case Specialization specialization -> inline(specialization);
            };
        }

        private Rule inline(final Derivation derivation) {
            return derivation.mapRhs(term -> term instanceof Variable var ? inlineIn(var, derivation).orElse(term) : term);
        }

        private Rule inline(final Specialization rule) {
            return new Derivation(rule.lhs(),
                                  new Alternation(rule.specializers().stream()
                                                          .map(var -> inlineIn(var, rule).orElse(var))
                                                          .toList()));
        }

        /**
         * Returns the result of inlining a variable inside a rule. If the variable can't be inlined, an empty optional
         * is returned.
         */
        private java.util.Optional<Term> inlineIn(final Variable variable, final Rule rule) {
            if (occursOnlyInRhsOf(variable, rule)) {
                final var varRule = bnf.getRuleFor(variable);
                if (isSingleAltRule(varRule)) {
                    return java.util.Optional.of(varRule.rhs().options().getFirst());
                }
                else if (varRule instanceof Derivation derivation) {
                    return matchRuleWithSingleTerminals(derivation).map(Alternation::new);
                }
            }

            return java.util.Optional.empty();
        }

        /**
         * Does a symbol occur only in the RHS of the specified rule?
         */
        private boolean occursOnlyInRhsOf(final Symbol symbol, final Rule rule) {
            final var occurences = BnfUtils.findRhsOccurences(symbol, bnf).toList();
            return occurences.size() == 1 && rule.equals(occurences.getFirst());
        }

        /**
         * Is this a rule whose RHS is an alternation of terminals?
         */
        private java.util.Optional<List<Terminal>> matchRuleWithSingleTerminals(final Rule rule) {
            final List<Terminal> terminals = new ArrayList<>();
            for (final Term option : rule.rhs().options()) {
                if (option instanceof Terminal terminal) {
                    terminals.add(terminal);
                } else {
                    return java.util.Optional.empty();
                }
            }
            return java.util.Optional.of(terminals);
        }
    }

    private static <T, R> List<R> enumerate(final Collection<? extends T> items, final BiFunction<? super T, Integer, ? extends R> mapper) {
        final var result = new ArrayList<R>(items.size());
        int index = 0;
        for (final T item : items) {
            result.add(mapper.apply(item, index));
            index++;
        }
        return result;
    }

}
