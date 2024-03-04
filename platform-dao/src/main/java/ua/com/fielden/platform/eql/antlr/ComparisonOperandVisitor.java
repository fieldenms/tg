package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.EQLParser.ComparisonOperand_MultiContext;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import java.util.List;
import java.util.function.Function;

import static ua.com.fielden.platform.eql.antlr.EQLParser.ComparisonOperand_SingleContext;

final class ComparisonOperandVisitor extends AbstractEqlVisitor<ComparisonOperandVisitor.Finisher> {

    // this can be made more general if needed, but now it is limited to results of type ICondition1
    public interface Finisher {

        /**
         * Builds a condition out of the comparison operand supplied to {@link ComparisonOperandVisitor}.
         * The result depends on the kind of that comparison operand:
         * <ul>
         *     <li> Single operand - simply returns the result of applying the given function.
         *     <li> Multi-operand - applies the given function to each operand constituting the whole and combines the
         *     conditions with OR / AND if the multi-operand specified ANY / ALL respectively.
         * </ul>
         *
         * @param fn  the function to apply to each operand
         */
        ICondition1<? extends ICondition2<?>> apply(
                Function<? super ISingleOperand1<? extends ISingleOperand2<?>>, ICondition1<? extends ICondition2<?>>> fn);

    }

    ComparisonOperandVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public Finisher visitComparisonOperand_Single(final ComparisonOperand_SingleContext ctx) {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = ctx.singleOperand().accept(new SingleOperandVisitor(transformer));
        return fn -> fn.apply(operand);
    }

    @Override
    public Finisher visitComparisonOperand_Multi(final ComparisonOperand_MultiContext ctx) {
        final boolean any;
        final List<? extends ISingleOperand1<? extends ISingleOperand2<?>>> operands;

        final Token token = ctx.multiOperand().token;
        switch (token) {
            case AnyOfPropsToken tok -> {
                any = true;
                operands = tok.props.stream().map(p -> new Prop1(p, false)).toList();
            }
            case AllOfPropsToken tok -> {
                any = false;
                operands = tok.props.stream().map(p -> new Prop1(p, false)).toList();
            }
            case AnyOfValuesToken tok -> {
                any = true;
                operands = tok.values.stream().map(v -> new Value1(preprocessValue(v))).toList();
            }
            case AllOfValuesToken tok -> {
                any = false;
                operands = tok.values.stream().map(v -> new Value1(preprocessValue(v))).toList();
            }
            case AnyOfParamsToken tok -> {
                any = true;
                operands = tok.params.stream().flatMap(p -> substParam(p, false)).toList();
            }
            case AnyOfIParamsToken tok -> {
                any = true;
                operands = tok.params.stream().flatMap(p -> substParam(p, true)).toList();
            }
            case AllOfParamsToken tok -> {
                any = false;
                operands = tok.params.stream().flatMap(p -> substParam(p, false)).toList();
            }
            case AllOfIParamsToken tok -> {
                any = false;
                operands = tok.params.stream().flatMap(p -> substParam(p, true)).toList();
            }
            case AnyOfModelsToken tok -> {
                any = true;
                operands = tok.models.stream().map(transformer::generateAsSubQuery).toList();
            }
            case AllOfModelsToken tok -> {
                any = false;
                operands = tok.models.stream().map(transformer::generateAsSubQuery).toList();
            }
            case AnyOfExpressionsToken tok -> {
                any = true;
                final EqlCompiler compiler  = new EqlCompiler(transformer);
                operands = tok.models.stream()
                        .map(m -> compiler.compile(m.getTokenSource(), EqlCompilationResult.StandaloneExpression.class).model())
                        .toList();
            }
            case AllOfExpressionsToken tok -> {
                any = false;
                final EqlCompiler compiler  = new EqlCompiler(transformer);
                operands = tok.models.stream()
                        .map(m -> compiler.compile(m.getTokenSource(), EqlCompilationResult.StandaloneExpression.class).model())
                        .toList();
            }
            default -> throw new EqlParseException("Unexpected token: %s".formatted(token.getText()));
        }

        final Function<List<? extends ICondition1<? extends ICondition2<?>>>, Conditions1> constructor = any ? Conditions1::or : Conditions1::and;

        return fn -> constructor.apply(operands.stream().map(fn).toList());
    }

}

