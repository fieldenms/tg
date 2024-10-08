package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;

public sealed interface EqlCompilationResult {

    String description();

    record Select(IJoinNode1<? extends IJoinNode2<?>> joinRoot,
                  Conditions1 whereConditions,
                  Yields1 yields,
                  GroupBys1 groups,
                  OrderBys1 orderBys,
                  boolean yieldAll)
            implements EqlCompilationResult
    {
        @Override
        public String description() {
            return "Select Query";
        }
    }

    record StandaloneExpression(Expression1 model) implements EqlCompilationResult {
        @Override
        public String description() {
            return "Standalone Expression";
        }
    }

    record StandaloneCondition(Conditions1 model) implements EqlCompilationResult {
        @Override
        public String description() {
            return "Standalone Condition";
        }
    }

    record StandaloneOrderBy(OrderBys1 model) implements EqlCompilationResult {
        @Override
        public String description() {
            return "Standalone Order By";
        }
    }

}
