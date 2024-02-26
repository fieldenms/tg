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
import static fielden.platform.bnf.TermMetadata.LABEL;
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

    public BnfToG4(BNF bnf, String grammarName) {
        this.originalBnf = bnf;
        this.bnf = stripParameters(bnf);
        this.grammarName = grammarName;
    }

    public static void writeResult(Result result, Path directory) throws IOException {
        Files.createDirectories(directory);

        Path filePath = directory.resolve(result.grammarName + ".g4");
        Files.writeString(filePath, result.grammarSource());

        for (final JavaFile file : result.files) {
            file.writeTo(directory);
        }
    }

    protected String lexerRule(Terminal terminal) {
        return terminal.name().toUpperCase();
    }

    public record Result(String grammarName, String grammarSource, Collection<? extends JavaFile> files) {}

    public Result bnfToG4() {
        return new Result(grammarName, generateGrammar(), generateFiles());
    }

    protected String generateGrammar() {
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
                .sorted()
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

    protected Collection<JavaFile> generateFiles() {
        return originalBnf.rules().stream()
                .flatMap(Rule::rhsTerms)
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
    class TokenSourceGenerator {
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
        var builder = CodeBlock.builder();
        for (final CodeBlock codeBlock : codeBlocks) {
            builder = builder.addStatement(codeBlock);
        }
        return builder.build();
    }

    // -------------------- Utilities

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

    private static <T, R> List<R> enumerate(Collection<? extends T> items, BiFunction<? super T, Integer, ? extends R> mapper) {
        var result = new ArrayList<R>(items.size());

        int i = 0;
        for (final T item : items) {
            result.add(mapper.apply(item, i));
            i++;
        }

        return result;
    }

}
