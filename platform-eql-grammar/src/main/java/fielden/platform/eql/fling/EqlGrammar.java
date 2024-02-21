package fielden.platform.eql.fling;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import il.ac.technion.cs.fling.EBNF;
import il.ac.technion.cs.fling.adapters.JavaMediator;
import il.ac.technion.cs.fling.internal.grammar.rules.Terminal;
import il.ac.technion.cs.fling.internal.grammar.rules.Variable;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;

import static fielden.platform.eql.fling.EqlGrammar.V.*;
import static fielden.platform.eql.fling.EqlGrammar.Σ.*;
import static il.ac.technion.cs.fling.grammars.api.BNFAPI.bnf;
import static il.ac.technion.cs.fling.internal.grammar.rules.Quantifiers.noneOrMore;
import static il.ac.technion.cs.fling.internal.grammar.rules.Quantifiers.optional;

public class EqlGrammar {

    /** Set of terminals, i.e., method names of generated fluent API. */
    public enum Σ implements Terminal {
        select, where,
        eq, gt, lt, ge, le, ne,
        like, iLike, notLike, likeWithCast, iLikeWithCast, notLikeWithCast, notILikeWithCast, notILike,
        in, notIn,
        isNull, isNotNull,
        and, or,
        expr,
        begin, notBegin, end,
        prop, extProp,
        val, iVal,
        param, iParam,
        now,
        count,
        upperCase, lowerCase,
        secondOf, minuteOf, hourOf, dayOf, monthOf, yearOf, dayOfWeekOf,
        ifNull,
        addTimeIntervalOf,
        caseWhen,
        round,
        concat,
        absOf,
        dateOf,
        anyOfProps, allOfProps,
        anyOfValues, allOfValues,
        anyOfParams, allOfParams, anyOfIParams, allOfIParams,
        anyOfModels, allOfModels,
        anyOfExpressions, allOfExpressions,
        exists, notExists, existsAnyOf, notExistsAnyOf, existsAllOf, notExistsAllOf,
        critCondition, condition, negatedCondition,
        all, any,
        values,
        props,
        params, iParams,
        maxOf, minOf, sumOf, countOf, avgOf, countAll, sumOfDistinct, countOfDistinct, avgOfDistinct,
        between,
        seconds, minutes, hours, days, months, years,
        to,
        when, then, otherwise,
        endAsInt, endAsBool, endAsStr, endAsDecimal,
        with,
        as, asRequired,
        model, modelAsEntity, modelAsPrimitive, modelAsAggregate,
        add, sub, mult, div, mod,
        beginExpr, endExpr,
        join, leftJoin, on,
        yield, yieldAll,
        groupBy, asc, desc, order,
    }

    /**
     * Set of non-terminals, i.e., abstract concepts of fluent API.
     * These names will be used for names of classes of the abstract syntax tree that Fling generates.
     */
    public enum V implements Variable {
        Query,
        Select,
        Expression,
        Where,
        Condition, Predicate, PredicateTail, AndCondition, OrCondition,
        Operand, SingleOperand, MultiOperand,
        Prop,
        And, Or, Eq, Gt, Lt,
        UnaryComparisonOperator, BinaryComparisonOperator, Val, AnyProp, ExtProp, Param,
        ArithmeticalOperator, SingleOperandOrExpr, ExprBody, ArithmeticalExpr, Condition__, Condition_, Model
    }

    // Short names
    private static final Class<String> STR = String.class;
    private static final Class<Object> OBJ = Object.class;
    private static final Class<Enum> ENUM = Enum.class;
    private static final Class<IConvertableToPath> PROP_PATH = IConvertableToPath.class;

    /**
     * EQL grammar in Extended Backus-Naur form for the purposes of fluent API generation.
     */
    // @formatter:off
    public static final EBNF bnf = bnf(). //
        start(Query). // This is the start symbol

        // Query = Select | Expression;
        specialize(Query).into(Select, Expression).

        // Select = select(<EntityType>) [Where] Model
        derive(Select).to(select.with(Class.class), optional(Where), Model).

        // Where = where Condition;
        derive(Where).to(where, Condition).

        // Condition = Predicate | Condition AND Condition | Condition OR Condition | begin Condition end;
        // AND takes precedence over OR
        derive(Condition).to(Predicate, Condition__).or(begin, Condition, end, Condition__).
        derive(Condition_).to(or, Condition).or(and, Condition).
        derive(Condition__).to(Condition_, Condition__).orNone().

        derive(Predicate).to(Operand, PredicateTail).
        derive(PredicateTail).to(UnaryComparisonOperator).or(BinaryComparisonOperator, Operand).

        derive(UnaryComparisonOperator).to(isNull).or(isNotNull).
        derive(BinaryComparisonOperator).to(eq).or(gt).or(lt).or(ge).or(le).or(like).or(iLike).or(notLike).or(notILike).

        specialize(Operand).into(SingleOperandOrExpr, MultiOperand).

        derive(SingleOperandOrExpr).to(SingleOperand).or(beginExpr, ExprBody, endExpr).
        derive(ExprBody).to(SingleOperandOrExpr, noneOrMore(ArithmeticalOperator, SingleOperandOrExpr)).
        derive(ArithmeticalOperator).to(add).or(sub).or(div).or(mult).or(mod).
        derive(SingleOperand).to(AnyProp).or(Val).or(Param).or(now).or(expr.with(ExpressionModel.class)).

        specialize(AnyProp).into(Prop, ExtProp).
        derive(Prop).
            to(prop.with(STR)).
            or(prop.with(PROP_PATH)).
            or(prop.with(ENUM)).
        derive(ExtProp).
            to(extProp.with(STR)).
            or(extProp.with(PROP_PATH)).
            or(extProp.with(ENUM)).

        derive(Val).to(val.with(OBJ)).or(iVal.with(OBJ)).

        derive(Param).
            to(param.with(STR)).
            or(param.with(ENUM)).
            or(iParam.with(STR)).
            or(iParam.with(ENUM)).

        derive(MultiOperand).
            to(anyOfProps.many(STR)).
            or(anyOfProps.many(PROP_PATH)).
            or(allOfProps.many(STR)).
            or(allOfProps.many(PROP_PATH)).

            or(anyOfValues.many(OBJ)).
            or(allOfValues.many(OBJ)).

            or(anyOfParams.many(STR)).
            or(anyOfIParams.many(STR)).
            or(allOfParams.many(STR)).
            or(allOfIParams.many(STR)).

            or(anyOfModels.many(PrimitiveResultQueryModel.class)).
            or(allOfModels.many(PrimitiveResultQueryModel.class)).
            or(anyOfExpressions.many(ExpressionModel.class)).
            or(allOfExpressions.many(ExpressionModel.class)).

        derive(Model).to(model.with(STR)).

        derive(Expression).to(expr, model).

        build();
    // @formatter:on

    private static final String API_CLASS_NAME = "Eql";

    // Program arguments: Command
    // Command = Generate | PrintBnf
    // Generate = 'generate' package dir?
    // PrintBnf = 'print-bnf'
    //
    // package -- package name for generated sources
    // dir -- output directory for generated sources that will contain the package;
    //        if omitted, then generated sources are printed to stdout
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: EqlGrammar.java command");
            System.exit(1);
        }

        if ("generate".equals(args[0]))
            generate(Arrays.copyOfRange(args, 1, args.length));
        else if ("print-bnf".equals(args[0]))
            printBNF();
    }

    private static void generate(final String[] args) throws Exception {
        String pkgName = args[0];
        Path outDirPath = args.length < 2 ? null : Path.of(args[1], pkgName.replace('.', '/'));

        JavaMediator jm = new JavaMediator(
                bnf,
                pkgName,
                API_CLASS_NAME,
                Σ.class // language terminals enum
                //                new WiseNamer(pkgName, API_CLASS_NAME)
        );

        Map<String, String> classes = Map.of(
                API_CLASS_NAME, jm.apiClass,
                "EqlAST", jm.astClass,
                "EqlCompiler", jm.astCompilerClass
        );

        if (outDirPath != null) {
            System.out.println(outDirPath.toAbsolutePath());
            if (!Files.exists(outDirPath)) {
                Files.createDirectories(outDirPath);
                System.out.println("directory " + outDirPath + " created successfully");
            }
        }

        // google-java-formatter will abort without useful messages if generated sources don't compile
        //        Formatter formatter = new Formatter();
        Formatter formatter = null;
        for (var entry : classes.entrySet()) {
            String name = entry.getKey();
            String code = formatSource(entry.getValue(), formatter);
            if (outDirPath != null) {
                Path filePath = Path.of(outDirPath.toString(), name + ".java");
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                Files.write(filePath, code.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                System.out.println("Generated file %s".formatted(filePath));
            } else {
                System.out.println("********** %s **********".formatted(name + ".java"));
                System.out.println(code);
                System.out.println();
            }
        }
    }

    public static void printBNF() {
        // switched to our own BNF representation so this won't work
//        System.out.println(new BnfToText().bnfToText(bnf));
    }

    private static String formatSource(String s, @Nullable Formatter formatter) throws FormatterException {
        return formatter != null ? formatter.formatSource(s) : s;
    }

}
