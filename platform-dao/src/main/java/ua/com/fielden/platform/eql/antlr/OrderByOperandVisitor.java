package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.antlr.tokens.OrderToken;
import ua.com.fielden.platform.eql.antlr.tokens.YieldToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBy1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import java.util.stream.Stream;

import static ua.com.fielden.platform.eql.antlr.EQLParser.ASC;
import static ua.com.fielden.platform.eql.antlr.EQLParser.DESC;

final class OrderByOperandVisitor extends AbstractEqlVisitor<Stream<OrderBy1>> {

    OrderByOperandVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public Stream<OrderBy1> visitOrderByOperand_Single(final EQLParser.OrderByOperand_SingleContext ctx) {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = ctx.singleOperand()
                .accept(new SingleOperandVisitor(transformer));
        return Stream.of(new OrderBy1(operand, isDesc(ctx.order())));
    }

    @Override
    public Stream<OrderBy1> visitOrderByOperand_Yield(final EQLParser.OrderByOperand_YieldContext ctx) {
        final YieldToken yieldToken = (YieldToken) ctx.yield;
        return Stream.of(new OrderBy1(yieldToken.yieldName, isDesc(ctx.order())));
    }

    @Override
    public Stream<OrderBy1> visitOrderByOperand_OrderingModel(final EQLParser.OrderByOperand_OrderingModelContext ctx) {
        final OrderToken orderToken = (OrderToken) ctx.token;
        final OrderBys1 innerModel = new EqlCompiler(transformer).compile(orderToken.model.getTokenSource(),
                                                                          EqlCompilationResult.StandaloneOrderBy.class)
                .model();
        return innerModel.models();
    }

    private static boolean isDesc(final EQLParser.OrderContext ctx) {
        return switch (ctx.token.getType()) {
            case DESC -> true;
            case ASC -> false;
            default -> unexpectedToken(ctx.token);
        };
    }

}
