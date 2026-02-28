// Generated from EQL.g4 by ANTLR 4.13.2
package ua.com.fielden.platform.eql.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link EQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface EQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link EQLParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(EQLParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Select}
	 * labeled alternative in {@link EQLParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(EQLParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StandaloneExpression}
	 * labeled alternative in {@link EQLParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandaloneExpression(EQLParser.StandaloneExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StandaloneCondExpr}
	 * labeled alternative in {@link EQLParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandaloneCondExpr(EQLParser.StandaloneCondExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StandaloneOrderBy}
	 * labeled alternative in {@link EQLParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandaloneOrderBy(EQLParser.StandaloneOrderByContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SelectEnd_Model}
	 * labeled alternative in {@link EQLParser#selectEnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectEnd_Model(EQLParser.SelectEnd_ModelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SelectEnd_AnyYield}
	 * labeled alternative in {@link EQLParser#selectEnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectEnd_AnyYield(EQLParser.SelectEnd_AnyYieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#where}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere(EQLParser.WhereContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PredicateCondition}
	 * labeled alternative in {@link EQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateCondition(EQLParser.PredicateConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CompoundCondition}
	 * labeled alternative in {@link EQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundCondition(EQLParser.CompoundConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrCondition}
	 * labeled alternative in {@link EQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrCondition(EQLParser.OrConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegatedCompoundCondition}
	 * labeled alternative in {@link EQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegatedCompoundCondition(EQLParser.NegatedCompoundConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndCondition}
	 * labeled alternative in {@link EQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndCondition(EQLParser.AndConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryPredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryPredicate(EQLParser.UnaryPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ComparisonPredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonPredicate(EQLParser.ComparisonPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code QuantifiedComparisonPredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuantifiedComparisonPredicate(EQLParser.QuantifiedComparisonPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LikePredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikePredicate(EQLParser.LikePredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MembershipPredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMembershipPredicate(EQLParser.MembershipPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SingleConditionPredicate}
	 * labeled alternative in {@link EQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleConditionPredicate(EQLParser.SingleConditionPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#unaryComparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryComparisonOperator(EQLParser.UnaryComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#likeOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikeOperator(EQLParser.LikeOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ComparisonOperand_Single}
	 * labeled alternative in {@link EQLParser#comparisonOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperand_Single(EQLParser.ComparisonOperand_SingleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ComparisonOperand_Multi}
	 * labeled alternative in {@link EQLParser#comparisonOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperand_Multi(EQLParser.ComparisonOperand_MultiContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(EQLParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#quantifiedOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuantifiedOperand(EQLParser.QuantifiedOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#exprBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBody(EQLParser.ExprBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#arithmeticalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticalOperator(EQLParser.ArithmeticalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Prop}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProp(EQLParser.PropContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExtProp}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtProp(EQLParser.ExtPropContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Val}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVal(EQLParser.ValContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Param}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(EQLParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SingleOperand_Expr}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleOperand_Expr(EQLParser.SingleOperand_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SingleOperand_Model}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleOperand_Model(EQLParser.SingleOperand_ModelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryFunction}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryFunction(EQLParser.UnaryFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfNull}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfNull(EQLParser.IfNullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SingleOperand_Now}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleOperand_Now(EQLParser.SingleOperand_NowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DateDiffInterval}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateDiffInterval(EQLParser.DateDiffIntervalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DateAddInterval}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateAddInterval(EQLParser.DateAddIntervalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Round}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRound(EQLParser.RoundContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Concat}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConcat(EQLParser.ConcatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CaseWhen}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen(EQLParser.CaseWhenContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Expr}
	 * labeled alternative in {@link EQLParser#singleOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(EQLParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#unaryFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryFunctionName(EQLParser.UnaryFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#dateIntervalUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateIntervalUnit(EQLParser.DateIntervalUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#caseWhenEnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhenEnd(EQLParser.CaseWhenEndContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#multiOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiOperand(EQLParser.MultiOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#membershipOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMembershipOperator(EQLParser.MembershipOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#membershipOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMembershipOperand(EQLParser.MembershipOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#join}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin(EQLParser.JoinContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#joinOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinOperator(EQLParser.JoinOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#joinCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinCondition(EQLParser.JoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#groupBy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupBy(EQLParser.GroupByContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldAll}
	 * labeled alternative in {@link EQLParser#anyYield}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldAll(EQLParser.YieldAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldSome}
	 * labeled alternative in {@link EQLParser#anyYield}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldSome(EQLParser.YieldSomeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Yield1Tail}
	 * labeled alternative in {@link EQLParser#yieldTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYield1Tail(EQLParser.Yield1TailContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldManyTail}
	 * labeled alternative in {@link EQLParser#yieldTail}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldManyTail(EQLParser.YieldManyTailContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#aliasedYield}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasedYield(EQLParser.AliasedYieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldOperand_SingleOperand}
	 * labeled alternative in {@link EQLParser#yieldOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperand_SingleOperand(EQLParser.YieldOperand_SingleOperandContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldOperandExpr}
	 * labeled alternative in {@link EQLParser#yieldOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperandExpr(EQLParser.YieldOperandExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldOperand_CountAll}
	 * labeled alternative in {@link EQLParser#yieldOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperand_CountAll(EQLParser.YieldOperand_CountAllContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldOperandFunction}
	 * labeled alternative in {@link EQLParser#yieldOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperandFunction(EQLParser.YieldOperandFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code YieldOperandConcatOf}
	 * labeled alternative in {@link EQLParser#yieldOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperandConcatOf(EQLParser.YieldOperandConcatOfContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#yieldOperandFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperandFunctionName(EQLParser.YieldOperandFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#yieldOperandConcatOfSeparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldOperandConcatOfSeparator(EQLParser.YieldOperandConcatOfSeparatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#yieldAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldAlias(EQLParser.YieldAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#yield1Model}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYield1Model(EQLParser.Yield1ModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#yieldManyModel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYieldManyModel(EQLParser.YieldManyModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#model}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModel(EQLParser.ModelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndStandaloneCondition}
	 * labeled alternative in {@link EQLParser#standaloneCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndStandaloneCondition(EQLParser.AndStandaloneConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StandaloneCondition_Predicate}
	 * labeled alternative in {@link EQLParser#standaloneCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandaloneCondition_Predicate(EQLParser.StandaloneCondition_PredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrStandaloneCondition}
	 * labeled alternative in {@link EQLParser#standaloneCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrStandaloneCondition(EQLParser.OrStandaloneConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#orderBy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderBy(EQLParser.OrderByContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrderByOperand_Single}
	 * labeled alternative in {@link EQLParser#orderByOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByOperand_Single(EQLParser.OrderByOperand_SingleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrderByOperand_Yield}
	 * labeled alternative in {@link EQLParser#orderByOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByOperand_Yield(EQLParser.OrderByOperand_YieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrderByOperand_OrderingModel}
	 * labeled alternative in {@link EQLParser#orderByOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByOperand_OrderingModel(EQLParser.OrderByOperand_OrderingModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link EQLParser#order}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder(EQLParser.OrderContext ctx);
}