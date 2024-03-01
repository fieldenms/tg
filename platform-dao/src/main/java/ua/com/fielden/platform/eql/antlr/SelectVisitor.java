package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

final class SelectVisitor extends AbstractEqlVisitor<EqlCompilationResult.Select> {

    SelectVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.Select visitSelect(final SelectContext ctx) {
        return new EqlCompilationResult.Select(
                compileJoinRoot(ctx),
                compileWhere(ctx.where()),
                compileYields(ctx.selectEnd()),
                compileGroups(ctx.groupBy()),
                false);
    }

    private GroupBys1 compileGroups(final GroupByContext groupByContext) {
        throw new UnsupportedOperationException();
    }

    private Yields1 compileYields(final SelectEndContext selectEndContext) {
        throw new UnsupportedOperationException();
    }

    private IJoinNode1<? extends IJoinNode2<?>> compileJoinRoot(final SelectContext ctx) {
        throw new UnsupportedOperationException();
    }

    private Conditions1 compileWhere(final WhereContext where) {
        if (where == null) {
            return Conditions1.EMPTY_CONDITIONS;
        }

        final ICondition1<?> condition = where.condition().accept(new ConditionVisitor(transformer));
        return Conditions1.conditions(condition);
    }

}
