package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;

public record ExpressionLinks(Expression2 expr, List<Prop2Lite> links) {
    public ExpressionLinks {
        links = List.copyOf(links);
    }
}