package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.tokens.AsRequiredToken;
import ua.com.fielden.platform.eql.antlr.tokens.AsToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.Yield1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;

import java.util.ArrayList;
import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.stage1.sundries.Yields1.yields;

final class YieldsVisitor extends AbstractEqlVisitor<YieldsVisitor.Result> {

    YieldsVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    record Result(Yields1 yields, boolean yieldAll) {}

    public static final Result DEFAULT_RESULT = new Result(Yields1.EMPTY_YIELDS, false);

    @Override
    public Result visitYieldAll(final YieldAllContext ctx) {
        return new Result(yields(ctx.aliasedYield().stream().map(this::compileAliasedYield).toList()), true);
    }

    @Override
    public Result visitYieldSome(final YieldSomeContext ctx) {
        // no need to process the Model context since that information is represented by the resulting type of a fluent API method call chain
        return ctx.yieldTail().accept(new EQLBaseVisitor<>() {
            @Override
            public Result visitYield1Tail(final Yield1TailContext $) {
                final var operand = ctx.firstYield.accept(new YieldOperandVisitor(transformer));
                return new Result(yields(new Yield1(operand)), false);
            }

            @Override
            public Result visitYieldManyTail(final YieldManyTailContext tailCtx) {
                final List<Yield1> yields = new ArrayList<>(1 + tailCtx.restYields.size());
                yields.add(compileAliasedYield(ctx.firstYield, tailCtx.firstAlias));
                tailCtx.restYields.stream().map(YieldsVisitor.this::compileAliasedYield).forEach(yields::add);
                return new Result(new Yields1(yields), false);
            }
        });
    }

    private Yield1 compileAliasedYield(final YieldOperandContext operandCtx, final YieldAliasContext aliasCtx) {
        final var operand = operandCtx.accept(new YieldOperandVisitor(transformer));
        final String alias;
        final boolean hint;

        final Token token = aliasCtx.token;
        switch (token) {
            case AsToken tok -> {
                alias = tok.alias;
                hint = false;
            }
            case AsRequiredToken tok -> {
                alias = tok.alias;
                hint = true;
            }
            default -> throw new EqlParseException("Unexpected token: %s".formatted(token));
        }

        return new Yield1(operand, alias, hint);
    }

    private Yield1 compileAliasedYield(final AliasedYieldContext ctx) {
        return compileAliasedYield(ctx.yieldOperand(), ctx.yieldAlias());
    }

}
