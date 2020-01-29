package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBy3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBys3;
import ua.com.fielden.platform.eql.stage3.elements.OrderBy3;
import ua.com.fielden.platform.eql.stage3.elements.OrderBys3;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.Yield3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.NullTest3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.elements.sources.JoinedQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.elements.sources.SingleQrySource3;

public class EqlStage3TestCase extends EqlStage1TestCase {
    
    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final QrySource1BasedOnPersistentType sourceForContextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), Integer.toString(sourceForContextId.contextId));
    }

    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final QrySource1BasedOnPersistentType sourceForContextId, final String subcontextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), Integer.toString(sourceForContextId.contextId) + "_" + subcontextId);
    }
    
    protected static QrySource3BasedOnTable source(final Class<? extends AbstractEntity<?>> sourceType, final QrySource3BasedOnTable sourceForContextId, final String subcontextId) {
        return new QrySource3BasedOnTable(tables.get(sourceType.getName()), sourceForContextId.contextId + "_" + subcontextId);
    }
    
    protected static EntQuery3 query(final Sources1 sources, final Conditions1 conditions, final Class<? extends AbstractEntity<?>> resultType) {
        final EntQueryBlocks1 parts1 = qb1(sources, conditions);
        return entResultQry3(new EntQuery1(parts1, resultType, RESULT_QUERY, nextId()), new PropsResolutionContext(metadata), tables).item;
    }

    protected static Expression3 expr(final ISingleOperand3 op1) {
        return new Expression3(op1);
    }

    protected static ISingleOperand3 prop(final String name, final IQrySource3 source) {
        return new EntProp3(name, source);
    }
    
    protected static ComparisonTest3 eq(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, EQ, op2);
    }
    
    protected static ComparisonTest3 ne(final ISingleOperand3 op1, final ISingleOperand3 op2) {
        return new ComparisonTest3(op1, NE, op2);
    }
        
    protected static NullTest3 isNotNull(final ISingleOperand3 op1) {
        return new NullTest3(op1, true);
    }

    protected static NullTest3 isNull(final ISingleOperand3 op1) {
        return new NullTest3(op1, false);
    }

    protected static Conditions3 cond(final ICondition3 condition) {
        return new Conditions3(false, asList(asList(condition)));
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySources3 second, final Conditions3 conditions) {
        return new JoinedQrySource3(main, second, jt, conditions);
    }

    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySources3 second, final Conditions3 conditions) {
        return new JoinedQrySource3(sources(main), second, jt, conditions);
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySource3 second, final Conditions3 conditions) {
        return new JoinedQrySource3(main, sources(second), jt, conditions);
    }
    
    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final Conditions3 conditions) {
        return new JoinedQrySource3(sources(main), sources(second), jt, conditions);
    }
    
    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySources3 second, final ICondition3 condition) {
        return new JoinedQrySource3(main, second, jt, cond(condition));
    }

    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySources3 second, final ICondition3 condition) {
        return new JoinedQrySource3(sources(main), second, jt, cond(condition));
    }

    protected static IQrySources3 sources(final IQrySources3 main, final JoinType jt, final IQrySource3 second, final ICondition3 condition) {
        return new JoinedQrySource3(main, sources(second), jt, cond(condition));
    }
    
    protected static IQrySources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final ICondition3 condition) {
        return new JoinedQrySource3(sources(main), sources(second), jt, cond(condition));
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static IQrySources3 lj(final IQrySource3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }
    
    protected static IQrySources3 lj(final IQrySource3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, LJ, second, conditions);
    }
    
    protected static IQrySources3 lj(final IQrySources3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 lj(final IQrySource3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 lj(final IQrySources3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }
    
    protected static IQrySources3 lj(final IQrySource3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, LJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static IQrySources3 ij(final IQrySource3 main, final IQrySources3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }
    
    protected static IQrySources3 ij(final IQrySource3 main, final IQrySource3 second, final Conditions3 conditions) {
        return sources(main, IJ, second, conditions);
    }
    
    protected static IQrySources3 ij(final IQrySources3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySource3 main, final IQrySources3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 ij(final IQrySources3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }
    
    protected static IQrySources3 ij(final IQrySource3 main, final IQrySource3 second, final ICondition3 condition) {
        return sources(main, IJ, second, condition);
    }

    protected static IQrySources3 sources(final IQrySource3 main) {
        return new SingleQrySource3(main);
    }

    private static EntQuery3 qry(final IQrySources3 sources, final QueryCategory queryCategory) {
        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(), yields(), groups(), orders()), queryCategory);
    }

    private static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final QueryCategory queryCategory) {
        return new EntQuery3(new EntQueryBlocks3(sources, conditions, yields(), groups(), orders()), queryCategory);
    }

    private static EntQuery3 qry(final IQrySources3 sources, final Yields3 yields, final QueryCategory queryCategory) {
        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(), yields, groups(), orders()), queryCategory);
    }

    private static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields, final QueryCategory queryCategory) {
        return new EntQuery3(new EntQueryBlocks3(sources, conditions, yields, groups(), orders()), queryCategory);
    }
    
    protected static EntQuery3 qry(final IQrySources3 sources) {
        return qry(sources, RESULT_QUERY);
    }

    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions) {
        return qry(sources, conditions, RESULT_QUERY);
    }

    protected static EntQuery3 qry(final IQrySources3 sources, final Yields3 yields) {
        return qry(sources, yields, RESULT_QUERY);
    }

    protected static EntQuery3 qry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields) {
        return qry(sources, conditions, yields, RESULT_QUERY);
    }

    protected static EntQuery3 subqry(final IQrySources3 sources) {
        return qry(sources, SUB_QUERY);
    }

    protected static EntQuery3 subqry(final IQrySources3 sources, final Conditions3 conditions) {
        return qry(sources, conditions, SUB_QUERY);
    }

    protected static EntQuery3 subqry(final IQrySources3 sources, final Yields3 yields) {
        return qry(sources, yields, SUB_QUERY);
    }

    protected static EntQuery3 subqry(final IQrySources3 sources, final Conditions3 conditions, final Yields3 yields) {
        return qry(sources, conditions, yields, SUB_QUERY);
    }

    protected static Yields3 yields(final Yield3 ... yields) {
        return new Yields3(asList(yields));
    }

    protected static Yield3 yieldExpr(final String propName, final IQrySource3 source, final String alias) {
        return new Yield3(expr(prop(propName, source)), alias);
    }

    protected static Yield3 yieldProp(final String propName, final IQrySource3 source, final String alias) {
        return new Yield3(prop(propName, source), alias);
    }

    protected static Yield3 yieldSingleExpr(final String propName, final IQrySource3 source) {
        return yieldExpr(propName, source, "");
    }

    protected static Yield3 yieldSingleProp(final String propName, final IQrySource3 source) {
        return yieldProp(propName, source, "");
    }

    protected static GroupBys3 groups(final GroupBy3 ... groups) {
        return new GroupBys3(asList(groups));
    }

    protected static OrderBys3 orders(final OrderBy3 ... orders) {
        return new OrderBys3(asList(orders));
    }

    protected static List<? extends ICondition3> and(final ICondition3 ... conditions) {
        return asList(conditions);
    }

    protected static Conditions3 or(final ICondition3 ... conditions) {
        final List<List<? extends ICondition3>> list = new ArrayList<>();
        for (final ICondition3 cond : conditions) {
            list.add(and(cond));
        }
        return new Conditions3(false, list);
    }

    protected static Conditions3 or(final List<? extends ICondition3> ... conditions) {
        return new Conditions3(false, asList(conditions));
    }
    
    protected static ua.com.fielden.platform.eql.stage2.elements.TransformationResult<EntQuery3> entResultQry3(final EntQuery1 qryModel, final PropsResolutionContext transformator, final Map<String, Table> tables) {
        final TransformationResult<EntQuery2> s1r = qryModel.transform(transformator);
        final TransformationContext context = new TransformationContext(tables, s1r.updatedContext, s1r.item.collectProps());
        return s1r.item.transform(context);
    }
}