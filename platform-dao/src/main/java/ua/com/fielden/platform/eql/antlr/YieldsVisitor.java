package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.tokens.AsRequiredToken;
import ua.com.fielden.platform.eql.antlr.tokens.AsToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.Yield1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;
import static ua.com.fielden.platform.eql.stage1.sundries.Yields1.yields;

final class YieldsVisitor extends AbstractEqlVisitor<YieldsVisitor.Result> {

    YieldsVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    record Result(Yields1 yields, boolean yieldAll) {}

    public static final Result DEFAULT_RESULT = new Result(Yields1.EMPTY_YIELDS, false);

    @Override
    public Result visitYield1(final Yield1Context ctx) {
        // no need to process the Model context since that information is represented by the resulting type of a fluent API method call chain
        final var operand = ctx.operand.accept(new YieldOperandVisitor(transformer));
        return new Result(yields(new Yield1(operand)), false);
    }

    @Override
    public Result visitYieldMany(final YieldManyContext ctx) {
        // no need to process the Model context since that information is represented by the resulting type of a fluent API method call chain
        final boolean yieldAll = ctx.YIELDALL() != null;
        return new Result(yields(ctx.aliasedYield().stream().map(this::compileAliasedYield).toList()), yieldAll);
    }

    private Yield1 compileAliasedYield(final AliasedYieldContext ctx) {
        final var operand = ctx.operand.accept(new YieldOperandVisitor(transformer));
        final String alias;
        final boolean hint;

        final Token token = ctx.yieldAlias().token;
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

}
