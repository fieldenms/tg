package ua.com.fielden.platform.eql.meta.utils;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PropChunk;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

/**
 * Utility class used to determine dependencies between those calculated props of the given entity type that are entities themselves.
 * <p>
 * It is regarded that one such calculated property depends on another if its expression directly or transitively (via other calculated properties) refers to sub-property(ies) of this another calculated property.
 * <p>
 * The knowledge of these dependencies is required for correct sequence of SQL JOINs generation.
 *
 * @author TG Team
 *
 */
public class DependentCalcPropsOrder {

    private DependentCalcPropsOrder() {}

    public static List<String> orderDependentCalcProps(final QuerySourceInfoProvider querySourceInfoProvider, final QueryModelToStage1Transformer gen, final QuerySourceInfo<?> querySourceInfo) {
        final Source2BasedOnPersistentType source = new Source2BasedOnPersistentType(querySourceInfo, gen.nextSourceId(), true, true);
        final Map<String, T2<Set<String>, Set<String>>> propDependencies = new HashMap<>();
        final List<String> calcPropsOfEntityType = new ArrayList<>();
        for (final PropChunk calcPropChunk : determineCalcPropChunks(querySourceInfo)) {
                if (isEntityType(calcPropChunk.data().javaType())) {
                    calcPropsOfEntityType.add(calcPropChunk.name());
                }

                final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, calcPropChunk.data().expression.expressionModel())).getResult().getValue();
                final TransformationContext1 prc = (new TransformationContext1(querySourceInfoProvider, true)).cloneWithAdded(source);
                try {
                    final Expression2 exp2 = exp1.transform(prc);
                    final Set<Prop2> expProps = exp2.collectProps();
                    final Set<Prop2> externalProps = new HashSet<>();
                    for (final Prop2 prop2 : expProps) {
                        if (prop2.source.id().equals(source.id())) {
                            externalProps.add(prop2);
                        }
                    }
                    propDependencies.put(calcPropChunk.name(), determineCalcPropChunksSets(externalProps));
                } catch (final Exception e) {
                    throw new EqlException("There is an error in expression of calculated property [" + querySourceInfo.javaType().getSimpleName() + ":" + calcPropChunk.name() + "]: " + e.getMessage());
                }
        }

        return orderDependentCalcProps(calcPropsOfEntityType, propDependencies);
    }


    /**
     * Enlist prop chunks for all calculated properties of the given entity type.
     *
     * @param et
     * @return
     */
    private static List<PropChunk> determineCalcPropChunks(final QuerySourceInfo<?> et) {
        final List<PropChunk> result = new ArrayList<>();
        for (final AbstractQuerySourceItem<?> prop : et.getProps().values()) {
            if (prop.expression != null && !prop.name.equals(KEY)) {
                result.add(new PropChunk(prop.name, prop));
            } else if (prop.hasExpression() && prop instanceof QuerySourceItemForComponentType) {
                for (final AbstractQuerySourceItem<?> subprop : ((QuerySourceItemForComponentType<?>) prop).getSubitems().values()) {
                    if (subprop.expression != null) {
                        result.add(new PropChunk(prop.name + "." + subprop.name, subprop));
                    }
                }
            }
        }
        return result;
    }

    private static List<String> orderDependentCalcProps(final List<String> calcPropsOfEntityType, final Map<String, T2<Set<String>, Set<String>>> calcPropDependencies) {
        final Map<String, Set<String>> mapOfDependencies = new HashMap<>();

        for (final String propName : calcPropsOfEntityType) {
            mapOfDependencies.put(propName, unfoldDependencies(propName, calcPropDependencies));
        }

        return sortTopologically(filterMapOfDependencies(mapOfDependencies));
    }

    /**
     * Gets list of calc props with subprops given calc prop directly or transitively depends on.
     *
     * @param propName
     * @param calcPropDependencies
     * @return
     */
    private static Set<String> unfoldDependencies(final String propName, final Map<String, T2<Set<String>, Set<String>>> calcPropDependencies) {
        final Set<String> dependencies = new HashSet<>();
        dependencies.addAll(calcPropDependencies.get(propName)._2);

        for (final String dependantProp : calcPropDependencies.get(propName)._1) {
            dependencies.addAll(unfoldDependencies(dependantProp, calcPropDependencies));
        }

        return dependencies;
    }

    /**
     * Removes from provided map of dependencies those entries, that have no dependencies and there is no dependencies upon them.
     *
     * @param mapOfDependencies
     * @return
     */
    private static final Map<String, Set<String>> filterMapOfDependencies(final Map<String, Set<String>> mapOfDependencies) {
        final Set<String> usedAsDependency = new HashSet<>();
        for (final Set<String> dependencies : mapOfDependencies.values()) {
            usedAsDependency.addAll(dependencies);
        }

        final Map<String, Set<String>> result = new HashMap<>();

        for (final Entry<String, Set<String>> entry : mapOfDependencies.entrySet()) {
            if (!entry.getValue().isEmpty() || usedAsDependency.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Determines for the given set of Prop2 instances 2 sets of names:
     * <ol>
     * <li> names of first prop chunks of props that are calculated and without subprops
     * <li> names of first prop chunks of props that are calculated and have subprops
     * </ol>
     *
     * @param props
     * @return
     */
    private static T2<Set<String>, Set<String>> determineCalcPropChunksSets(final Set<Prop2> props) {
        final Set<String> calcPropsWithSubprops = new HashSet<>();
        final Set<String> calcPropsWithoutSubprops = new HashSet<>();

        for (final Prop2 prop2 : props) {
            final T3<String, Boolean, Boolean> firstProp = obtainFirstChunkInfo(prop2.getPath());
            if (firstProp._2) {
                if (firstProp._3) {
                    calcPropsWithSubprops.add(firstProp._1);
                } else {
                    calcPropsWithoutSubprops.add(firstProp._1);
                }
            }
        }

        return t2(calcPropsWithoutSubprops, calcPropsWithSubprops);
    }

    /**
     * Obtains from that Prop2 path data of its first chunk (name, is it calculated, does it have succeeding chunks).
     *
     * @param propPath
     * @return
     */
    private static T3<String, Boolean, Boolean> obtainFirstChunkInfo(final List<AbstractQuerySourceItem<?>> propPath) {
        String currentPropName = null;
        int propLength = 0;
        for (final AbstractQuerySourceItem<?> querySourceItemInfo : propPath) {
            propLength = propLength + 1;
            currentPropName = (currentPropName != null) ? currentPropName + "." + querySourceItemInfo.name : querySourceItemInfo.name;
            if (!(querySourceItemInfo instanceof QuerySourceItemForComponentType || querySourceItemInfo instanceof QuerySourceItemForUnionType)) {
                return t3(currentPropName, querySourceItemInfo.hasExpression(), propPath.size() > propLength);
            }
        }
        throw new EqlStage2ProcessingException(currentPropName, null);
    }
}