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
        Condition,
        Operand,
        SingleOperand, MultiOperand,
        Prop,
        Operator, LogicalOp,
        And, Or, Eq, Gt, Lt,
        ComparisonOperation, UnaryOperator, BinaryOperator, Val, AnyProp, ExtProp, Param, Model
    }

    // Short names
    private static final Class<String> STR = String.class;
    private static final Class<Object> OBJ = Object.class;
    private static final Class<Enum> ENUM = Enum.class;
    private static final Class<IConvertableToPath> PROP_PATH = IConvertableToPath.class;

    /**
     * EQL grammar in Extended Backus-Naur form.
     */
    // @formatter:off
    public static final EBNF bnf = bnf(). //
        start(Query). // This is the start symbol

        // Query = Select | Expression;
        specialize(Query).into(Select, Expression).

        // Select = select(<EntityType>) [Where] Model
        derive(Select).to(select.with(Class.class), optional(Where), Model).

        // Where = where Condition { LogicalOp Condition };
        derive(Where).to(where, Condition, noneOrMore(LogicalOp, Condition)).

        // Condition = Operand ComparisonOperation
        derive(Condition).to(Operand, ComparisonOperation).

        // ComparisonOperation = UnaryOperator | BinaryOperator Operand
        derive(ComparisonOperation).to(UnaryOperator).or(BinaryOperator, Operand).
        derive(UnaryOperator).to(isNull).or(isNotNull).
        derive(BinaryOperator).to(eq).or(gt).or(lt).or(ge).or(le).or(like).or(iLike).or(notLike).or(notILike).

        specialize(Operand).into(SingleOperand, MultiOperand).

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

        derive(LogicalOp).to(And).or(Or).
        // excess rules to give resultant AST classes meaningful names instead of LogicalOp{n}
        derive(And).to(and).
        derive(Or).to(or).

        derive(Model).to(model.with(STR)).

        derive(Expression).to(expr, model).

        build();
    // @formatter:on

    private static final String API_CLASS_NAME = "Eql";

    // Program arguments: package [dir]
    // package -- package name for generated sources
    // dir -- output directory for generated sources that will contain the package;
    //        if omitted, then generated sources are printed to stdout
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: EqlGrammar.java package [dir]");
            System.exit(1);
        }

        String pkgName = args[0];
        Path outDirPath = args.length < 2 ? null : Path.of(args[1], pkgName.replace('.', '/'));

        JavaMediator jm = new JavaMediator(
                bnf,
                pkgName,
                API_CLASS_NAME,
                Σ.class // language terminals enum
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
        Formatter formatter = new Formatter();
        //        Formatter formatter = null;
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

    private static String formatSource(String s, @Nullable Formatter formatter) throws FormatterException {
        return formatter != null ? formatter.formatSource(s) : s;
    }

}
