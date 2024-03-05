package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

final class StandaloneConditionVisitor extends AbstractEqlVisitor<EqlCompilationResult.StandaloneCondition> {

    StandaloneConditionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.StandaloneCondition visitStandaloneCondExpr(final StandaloneCondExprContext ctx) {
        return ctx.standaloneCondition().accept(this);
    }

    @Override
    public EqlCompilationResult.StandaloneCondition visitAndStandaloneCondition(final AndStandaloneConditionContext ctx) {
        return new EqlCompilationResult.StandaloneCondition(Conditions1.and(
                ctx.left.accept(this).model(),
                ctx.right.accept(this).model()));
    }

    @Override
    public EqlCompilationResult.StandaloneCondition visitOrStandaloneCondition(final OrStandaloneConditionContext ctx) {
        return new EqlCompilationResult.StandaloneCondition(Conditions1.or(
                ctx.left.accept(this).model(),
                ctx.right.accept(this).model()));
    }

    @Override
    public EqlCompilationResult.StandaloneCondition visitStandaloneCondition_Predicate(final StandaloneCondition_PredicateContext ctx) {
        final ICondition1<?> condition = ctx.predicate().accept(new ConditionVisitor(transformer));
        return new EqlCompilationResult.StandaloneCondition(Conditions1.conditions(condition));
    }

}
