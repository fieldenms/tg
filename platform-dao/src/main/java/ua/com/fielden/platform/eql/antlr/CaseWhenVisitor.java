package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.fluent.*;
import ua.com.fielden.platform.eql.antlr.tokens.EndAsDecimalToken;
import ua.com.fielden.platform.eql.antlr.tokens.EndAsStrToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.functions.CaseWhen1;

final class CaseWhenVisitor extends AbstractEqlVisitor<CaseWhen1> {

    CaseWhenVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    private static ITypeCast makeTypeCast(final EQLParser.CaseWhenEndContext ctx) {
        final Token token = ctx.token;
        return switch (token.getType()) {
            case EQLLexer.END -> null;
            case EQLLexer.ENDASBOOL -> TypeCastAsBoolean.INSTANCE;
            case EQLLexer.ENDASINT -> TypeCastAsInteger.INSTANCE;
            case EQLLexer.ENDASSTR -> {
                final EndAsStrToken tok = (EndAsStrToken) token;
                yield TypeCastAsString.getInstance(tok.length);
            }
            case EQLLexer.ENDASDECIMAL -> {
                final EndAsDecimalToken tok = (EndAsDecimalToken) token;
                yield TypeCastAsDecimal.getInstance(tok.precision, tok.scale);
            }
            default -> throw new IllegalStateException("Unexpected token: " + token);
        };
    }

}
