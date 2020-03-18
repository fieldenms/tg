package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.functions.CountAll2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EqlStage2TestCase extends EqlTestCase {

    protected static final GroupBys2 emptyGroupBys2 = new GroupBys2(emptyList());
    protected static final OrderBys2 emptyOrderBys2 = new OrderBys2(emptyList());
    protected static final Yields2 emptyYields2 = new Yields2(emptyList());

    protected static AbstractPropInfo<?> pi(final Class<?> type, final String propName) {
        return metadata.get(type).getProps().get(propName);
    }
    
    protected static TransformationResult<EntQuery2> entResultQry2(final QueryModel qryModel, final PropsResolutionContext transformator) {
        if (qryModel instanceof EntityResultQueryModel) {
            return qb().generateEntQueryAsResultQuery(from((EntityResultQueryModel) qryModel).model()).transform(transformator);
        } else if (qryModel instanceof AggregatedResultQueryModel) {
            return qb().generateEntQueryAsResultQuery(from((AggregatedResultQueryModel) qryModel).model()).transform(transformator);
        }
        throw new IllegalStateException("Not implemented yet");
    }
    
    protected static TransformationResult<EntQuery2> transform(final EntityResultQueryModel qry) {
        return entResultQry2(qry, new PropsResolutionContext(metadata));
    }

    protected static TransformationResult<EntQuery2> transform(final AggregatedResultQueryModel qry) {
        return entResultQry2(qry, new PropsResolutionContext(metadata));
    }

    protected static Map<String, List<AbstractPropInfo<?>>> getResolvedProps(final Set<EntProp2> props) {
        final Map<String, List<AbstractPropInfo<?>>> result = new HashMap<>();
        for (final EntProp2 el : props) {
            result.put(el.name, el.getPath());
        }
        
        return result;
    }
    
    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions) {
        return new EntQueryBlocks2(sources, conditions, emptyYields2, emptyGroupBys2, emptyOrderBys2);
    }

    protected static EntQueryBlocks2 qb2(final Sources2 sources, final Conditions2 conditions, final Yields2 yields) {
        return new EntQueryBlocks2(sources, conditions, yields, emptyGroupBys2, emptyOrderBys2);
    }

    protected static Yields2 yields(final Yield2 ... yields) {
        return new Yields2(asList(yields));
    }
    
    protected static Yield2 yieldCountAll(final String alias) {
        return new Yield2(new CountAll2(), alias, false);
    }
    
    protected static EntProp2 prop(final IQrySource2<? extends IQrySource3> source, AbstractPropInfo<?> ... propInfos) {
        return new EntProp2(source, asList(propInfos));
    }

    protected static Conditions2 cond(final ICondition2<? extends ICondition3> condition) {
        return new Conditions2(false, asList(asList(condition)));
    }
    
//    protected static Conditions1 conditions(final ICondition1<?> firstCondition, final CompoundCondition1... otherConditions) {
//        return new Conditions1(false, firstCondition, asList(otherConditions));
//    }
//
//    protected static Conditions1 conditions(final ICondition1<?> firstCondition) {
//        return new Conditions1(false, firstCondition, emptyList());
//    }
    
    protected static Sources2 sources(final IQrySource2<? extends IQrySource3> main) {
        return new Sources2(main, emptyList());
    }

//    protected static Sources1 sources(final IQrySource1<? extends IQrySource2<?>> main, final CompoundSource1... otherSources) {
//        return new Sources1(main, asList(otherSources));
//    }
//
//    protected static CompoundSource1 lj(final IQrySource1<? extends IQrySource2<?>> source, final Conditions1 conditions) {
//        return new CompoundSource1(source, LJ, conditions);
//    }
//
//    protected static CompoundSource1 ij(final IQrySource1<? extends IQrySource2<?>> source, final Conditions1 conditions) {
//        return new CompoundSource1(source, IJ, conditions);
//    }
//
//    protected static CompoundSource1 lj(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
//        return new CompoundSource1(source, LJ, conditions(firstCondition));
//    }
//
//    protected static CompoundSource1 ij(final IQrySource1<? extends IQrySource2<?>> source, final ICondition1<?> firstCondition) {
//        return new CompoundSource1(source, IJ, conditions(firstCondition));
//    }
//    
//    protected static CompoundCondition1 and(final ICondition1<?> condition) {
//        return new CompoundCondition1(AND, condition);
//    }
//
//    protected static CompoundCondition1 or(final ICondition1<?> condition) {
//        return new CompoundCondition1(OR, condition);
//    }

    protected static NullTest2 isNull(final ISingleOperand2 operand) {
        return new NullTest2(operand, false);
    }

    protected static NullTest2 isNotNull(final ISingleOperand2 operand) {
        return new NullTest2(operand, true);
    }

    protected static ComparisonTest2 eq(final EntProp2 op1, final EntProp2 op2) {
        return new ComparisonTest2(op1, EQ, op2);
    }
    
    protected static ComparisonTest2 ne(final EntProp2 op1, final EntProp2 op2) {
        return new ComparisonTest2(op1, NE, op2);
    }

    protected static EntProp1 prop(final String name) {
        return new EntProp1(name, false);
    }

    protected static QrySource2BasedOnPersistentType source(final String contextId, final Class<? extends AbstractEntity<?>> sourceType, final String alias) {
        return new QrySource2BasedOnPersistentType(sourceType, metadata.get(sourceType), alias, contextId);
    }

    protected static QrySource2BasedOnPersistentType source(final String contextId, final Class<? extends AbstractEntity<?>> sourceType) {
        return new QrySource2BasedOnPersistentType(sourceType, metadata.get(sourceType), contextId);
    }
}