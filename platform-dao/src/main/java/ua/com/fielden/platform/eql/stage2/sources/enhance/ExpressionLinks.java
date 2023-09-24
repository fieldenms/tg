package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

import ua.com.fielden.platform.eql.stage2.operands.Expression2;

public class ExpressionLinks extends AbstractLinks<Expression2>{

    public ExpressionLinks(final List<Prop2Lite> links, final Expression2 resolution) {
        super(links, resolution);
    }
}