package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBy1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;

import static ua.com.fielden.platform.eql.stage1.sundries.GroupBys1.groupBys;

final class GroupByVisitor extends AbstractEqlVisitor<GroupBys1>  {

    GroupByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public GroupBys1 visitGroupBy(final EQLParser.GroupByContext ctx) {
        final var visitor = new SingleOperandVisitor(transformer);
        return groupBys(ctx.operands.stream().map(x -> new GroupBy1(x.accept(visitor))).toList());
    }

}
