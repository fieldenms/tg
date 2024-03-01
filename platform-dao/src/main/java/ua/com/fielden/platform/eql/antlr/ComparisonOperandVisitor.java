package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import static ua.com.fielden.platform.eql.antlr.EQLParser.ComparisonOperand_MultiContext;
import static ua.com.fielden.platform.eql.antlr.EQLParser.ComparisonOperand_SingleContext;

final class ComparisonOperandVisitor extends AbstractEqlVisitor<ISingleOperand1<? extends ISingleOperand2<?>>> {

    ComparisonOperandVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitComparisonOperand_Single(final ComparisonOperand_SingleContext ctx) {
        return ctx.singleOperand().accept(new SingleOperandVisitor(transformer));
    }

    @Override
    public ISingleOperand1<? extends ISingleOperand2<?>> visitComparisonOperand_Multi(final ComparisonOperand_MultiContext ctx) {
        throw new UnsupportedOperationException();
    }

}

