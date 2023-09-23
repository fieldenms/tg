package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.getFirstCalcChunks;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.groupByFirstChunk;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.groupBySource;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.orderImplicitNodes;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.processExpressionsData;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.processPropsResolutionData;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.tailFromProp;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.EntityTypePropInfo;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformer {

    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final EntQueryGenerator gen;

    public PathsToTreeTransformer(final QuerySourceInfoProvider querySourceInfoProvider, final EntQueryGenerator gen) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.gen = gen;
    }
    
    public final TreeResultBySources transformFinally(final Set<Prop2> props) {
        final TreeResult treeResult = transform(props);
        return new TreeResultBySources(treeResult.implicitNodesMap(), processExpressionsData(treeResult.expressionsData()), processPropsResolutionData(treeResult.propsData()));
    }
    
    private final TreeResult transform(final Set<Prop2> props) {
        final Map<Integer, List<ImplicitNode>> nodes = new HashMap<>();
        final List<ExpressionLinks> expressionsLinks = new ArrayList<>();
        final List<Prop3Links> propLinks = new ArrayList<>();

        for (final SourceTails sourceTails : groupBySource(props)) {
            final T2<List<ImplicitNode>, TreeResult> genRes = generateSourceNodes(sourceTails.source(), sourceTails.tails(), true);

            nodes.put(sourceTails.source().id(), genRes._1);
            nodes.putAll(genRes._2.implicitNodesMap());
            expressionsLinks.addAll(genRes._2.expressionsData());
            propLinks.addAll(genRes._2.propsData());
        }

        return new TreeResult(nodes, propLinks, expressionsLinks);
    }
    
    private T2<List<ImplicitNode>, TreeResult> generateSourceNodes(
            final ISource2<?> sourceForCalcPropResolution, 
            final List<PendingTail> pendingTails,
            final boolean explicitSource // true if sourceForCalcPropResolution is explicit source
    ) {
        final Set<String> propsToSkip = explicitSource ? new HashSet<String>(pendingTails.stream().map(p -> p.link().name()).toList()) : emptySet();
        final T2<Map<String, CalcPropData>, List<PendingTail>> procRes = enhanceWithCalcPropsData(sourceForCalcPropResolution, emptyMap(), propsToSkip, pendingTails);

        final Map<String, CalcPropData> calcPropData = procRes._1;

        final List<ImplicitNode> listOfNodes = new ArrayList<>();
        final Map<Integer, List<ImplicitNode>> otherSourcesNodes = new HashMap<>();
        final List<ExpressionLinks> expressionLinks = new ArrayList<>();
        final List<Prop3Links> propLinks = new ArrayList<>();

        for (final FirstChunkGroup propEntry : groupByFirstChunk(procRes._2)) {

            final CalcPropData cpd = calcPropData.get(propEntry.firstChunk.name());

            if (cpd != null) {
                otherSourcesNodes.putAll(cpd.internalsResult.implicitNodesMap());
                expressionLinks.addAll(cpd.internalsResult.expressionsData());
                propLinks.addAll(cpd.internalsResult.propsData());
            }

            final Expression2 expression = cpd != null ? cpd.expr : null;

            if (!propEntry.origins.isEmpty()) {
                if (expression != null) {
                    expressionLinks.add(new ExpressionLinks(expression, propEntry.origins));
                } else {
                    propLinks.add(new Prop3Links(new Prop3Lite(propEntry.firstChunk.name(), sourceForCalcPropResolution.id()), propEntry.origins));
                }
            }

            if (!propEntry.tails.isEmpty()) {
                final T2<ImplicitNode, TreeResult> genRes = generateNode(propEntry.tails, propEntry.firstChunk, expression);
                listOfNodes.add(genRes._1);
                otherSourcesNodes.putAll(genRes._2.implicitNodesMap());
                expressionLinks.addAll(genRes._2.expressionsData());
                propLinks.addAll(genRes._2.propsData());
            }
        }

        final List<String> orderedCalcPropsForType = isUnionEntityType(sourceForCalcPropResolution.sourceType()) || sourceForCalcPropResolution.sourceType().equals(EntityAggregates.class) ? emptyList() : querySourceInfoProvider.getCalcPropsOrder(sourceForCalcPropResolution.sourceType());

        final List<ImplicitNode> orderedNodes = orderImplicitNodes(listOfNodes, orderedCalcPropsForType);

        return t2(orderedNodes, new TreeResult(otherSourcesNodes, propLinks, expressionLinks));
    }

    private T2<Map<String, CalcPropData>, List<PendingTail>> enhanceWithCalcPropsData(
            final ISource2<?> sourceForCalcPropResolution, 
            final Map<String, CalcPropData> processedCalcData,
            final Set<String> processedProps, // Prop2 name
            final List<PendingTail> incomingTails) {
        
        final Map<String, CalcPropData> processedCalcDataLocal = new HashMap<>(); // used to store processed calc props (key is the same as FirstPropInfoAndItsPathes.firstPropName)
        processedCalcDataLocal.putAll(processedCalcData);

        final Set<String> processedPropsLocal = new HashSet<>();
        processedPropsLocal.addAll(processedProps);

        final List<PendingTail> addedTails = new ArrayList<>();

        for (final PropChunk calcChunk : getFirstCalcChunks(incomingTails)) {
            if (!processedCalcDataLocal.containsKey(calcChunk.name())) { // consider only calc props that have not yet been processed
                final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, calcChunk.data().expression)).getResult().getValue();
                final TransformationContext1 prc = (new TransformationContext1(querySourceInfoProvider)).cloneWithAdded(sourceForCalcPropResolution);
                final Expression2 exp2 = exp1.transform(prc);
                final Set<Prop2> expProps = exp2.collectProps();
                // separate into external and internal
                final Set<Prop2> externalProps = new HashSet<>();
                final Set<Prop2> internalProps = new HashSet<>();

                for (final Prop2 prop2 : expProps) {
                    if (prop2.source.id().equals(sourceForCalcPropResolution.id())) {
                        externalProps.add(prop2);
                    } else {
                        internalProps.add(prop2);
                    }
                }

                final TreeResult localCalcPropSourcesNodes = transform(internalProps);
                processedCalcDataLocal.put(calcChunk.name(), new CalcPropData(exp2, localCalcPropSourcesNodes));

                for (final Prop2 prop : externalProps) {
                    if (!processedPropsLocal.contains(prop.name)) {
                        addedTails.add(tailFromProp(prop));
                        processedPropsLocal.add(prop.name);
                    }
                }
            }
        }

        // let's recursively enhance newly added tails (as they can have unprocessed calc props among them)
        final T2<Map<String, CalcPropData>, List<PendingTail>> recursivelyEnhanced = addedTails.isEmpty() ? t2(unmodifiableMap(processedCalcDataLocal), emptyList())
                : enhanceWithCalcPropsData(sourceForCalcPropResolution, processedCalcDataLocal, processedPropsLocal, addedTails); //processedPropsLocal is passed here as enhancement is done within calc props of the same source -- the same level

        final List<PendingTail> allTails = new ArrayList<>();
        allTails.addAll(incomingTails);
        allTails.addAll(recursivelyEnhanced._2);

        return t2(recursivelyEnhanced._1, allTails);
    }
    
    private T2<ImplicitNode, TreeResult> generateNode(final List<PendingTail> tails, final PropChunk firstChunk, final Expression2 expression) {
        final EntityTypePropInfo<?> propInfo = (EntityTypePropInfo<?>) firstChunk.data();
        final Source2BasedOnPersistentType implicitSource = new Source2BasedOnPersistentType(propInfo.javaType(), propInfo.propQuerySourceInfo, gen.nextSourceId());
        final T2<List<ImplicitNode>, TreeResult> genRes = generateSourceNodes(implicitSource, tails, false);
        final ImplicitNode node = new ImplicitNode(firstChunk.name(), genRes._1, propInfo.nonnullable, implicitSource, expression);
        return t2(node, genRes._2);
    }

    private static record CalcPropData(Expression2 expr, TreeResult internalsResult) {
    }
}