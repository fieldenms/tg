package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.getFirstCalcChunks;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.groupByExplicitSources;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.groupByFirstChunk;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.groupBySource;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.orderHelperNodes;
import static ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.tailFromProp;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.HelperNodeForImplicitJoins;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.FirstChunkGroup;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.PendingTail;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.Prop2Lite;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathToTreeTransformerUtils.SourceTails;

public class PathsToTreeTransformer {

    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final QueryModelToStage1Transformer gen;

    public PathsToTreeTransformer(final QuerySourceInfoProvider querySourceInfoProvider, final QueryModelToStage1Transformer gen) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.gen = gen;
    }

    public final TreeResultBySources transformFinally(final Set<Prop2> props) {
        final TransformationResult treeResult = transform(props);
        return new TreeResultBySources(treeResult.helperNodesMap(), groupByExplicitSources(treeResult.expressionsData()), groupByExplicitSources(treeResult.propsData()));
    }

    private final TransformationResult transform(final Set<Prop2> props) {
        final var nodes = new HashMap<Integer, List<HelperNodeForImplicitJoins>>();
        final var expressionsLinks = new ArrayList<ExpressionLinks>();
        final var propLinks = new ArrayList<Prop3Links>();

        for (final SourceTails sourceTails : groupBySource(props)) {
            final SourceNodesResult genRes = generateHelperNodesForSource(sourceTails.source(), sourceTails.tails());

            nodes.put(sourceTails.source().id(), genRes.sourceNodes);
            nodes.putAll(genRes.transformationResult.helperNodesMap());
            expressionsLinks.addAll(genRes.transformationResult.expressionsData());
            propLinks.addAll(genRes.transformationResult.propsData());
        }

        return new TransformationResult(unmodifiableMap(nodes), unmodifiableList(propLinks), unmodifiableList(expressionsLinks));
    }

    private SourceNodesResult generateHelperNodesForSource(
            final ISource2<?> sourceForCalcPropResolution,
            final List<PendingTail> pendingTails
    ) {
        final Set<String> propsToSkip = sourceForCalcPropResolution.isExplicit() ? new HashSet<String>(pendingTails.stream().map(p -> p.link().name()).toList()) : emptySet();
        final CalcPropsDataAndPendingTails procRes = enhanceWithCalcPropsData(sourceForCalcPropResolution, emptyMap(), propsToSkip, pendingTails);

        final List<HelperNodeForImplicitJoins> listOfNodes = new ArrayList<>();
        final Map<Integer, List<HelperNodeForImplicitJoins>> otherSourcesNodes = new HashMap<>();
        final List<ExpressionLinks> expressionLinks = new ArrayList<>();
        final List<Prop3Links> propLinks = new ArrayList<>();

        for (final FirstChunkGroup propEntry : groupByFirstChunk(procRes.pendingTails)) {

            final CalcPropData cpd = procRes.calcPropsData.get(propEntry.firstChunk().name());

            if (cpd != null) {
                otherSourcesNodes.putAll(cpd.internalsResult.helperNodesMap());
                expressionLinks.addAll(cpd.internalsResult.expressionsData());
                propLinks.addAll(cpd.internalsResult.propsData());
            }

            final Expression2 expression = cpd != null ? cpd.expr : null;

            if (!propEntry.origins().isEmpty()) {
                if (expression != null) {
                    expressionLinks.add(new ExpressionLinks(unmodifiableList(propEntry.origins()), expression));
                } else {
                    propLinks.add(new Prop3Links(unmodifiableList(propEntry.origins()), new Prop3Lite(propEntry.firstChunk().name(), sourceForCalcPropResolution.id())));
                }
            }

            if (!propEntry.tails().isEmpty()) {
                final SourceNodeResult genRes = generateHelperNode(propEntry.tails(), propEntry.firstChunk(), expression, sourceForCalcPropResolution.isPartOfCalcProp());
                listOfNodes.add(genRes.sourceNode);
                otherSourcesNodes.putAll(genRes.transformationResult.helperNodesMap());
                expressionLinks.addAll(genRes.transformationResult.expressionsData());
                propLinks.addAll(genRes.transformationResult.propsData());
            }
        }

        final List<String> orderedCalcPropsForType = isUnionEntityType(sourceForCalcPropResolution.sourceType()) || sourceForCalcPropResolution.sourceType().equals(EntityAggregates.class) ? emptyList() : querySourceInfoProvider.getCalcPropsOrder(sourceForCalcPropResolution.sourceType());

        final List<HelperNodeForImplicitJoins> orderedNodes = orderHelperNodes(listOfNodes, orderedCalcPropsForType);

        return new SourceNodesResult(unmodifiableList(orderedNodes), new TransformationResult(unmodifiableMap(otherSourcesNodes), unmodifiableList(propLinks), unmodifiableList(expressionLinks)));
    }

    private CalcPropsDataAndPendingTails enhanceWithCalcPropsData(
            final ISource2<?> sourceForCalcPropResolution,
            final Map<String, CalcPropData> processedCalcData,
            final Set<String> processedProps, // Prop2 name
            final List<PendingTail> incomingTails) {

        final Set<String> processedPropsLocal = new HashSet<>();
        processedPropsLocal.addAll(processedProps);

        final Map<String, CalcPropData> processedCalcDataLocal = new HashMap<>(); // used to store locally added calc props data (key is the same as PropChunk.name())
        final List<PendingTail> addedTails = new ArrayList<>();

        for (final PropChunk calcChunk : getFirstCalcChunks(incomingTails)) {
            if (!processedCalcData.containsKey(calcChunk.name())) { // consider only calc props that have not yet been processed on the previous iteration(s)
                final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, calcChunk.data().expression.expressionModel())).getResult().getValue();
                final TransformationContext1 prc = (new TransformationContext1(querySourceInfoProvider, true)).cloneWithAdded(sourceForCalcPropResolution);
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

                final TransformationResult localCalcPropSourcesNodes = transform(internalProps);
                processedCalcDataLocal.put(calcChunk.name(), new CalcPropData(exp2, localCalcPropSourcesNodes));

                for (final Prop2 prop : externalProps) {
                    if (!processedPropsLocal.contains(prop.propPath)) {
                        addedTails.add(tailFromProp(prop));
                        processedPropsLocal.add(prop.propPath);
                    }
                }
            }
        }

        processedCalcDataLocal.putAll(processedCalcData); // let's now add incoming data to the locally discovered in order to path it further down the recursive process

        // let's recursively enhance newly added tails (as they can have unprocessed calc props among them)
        final var recursivelyEnhanced = addedTails.isEmpty()
                                        ? new CalcPropsDataAndPendingTails(unmodifiableMap(processedCalcDataLocal), emptyList())
                                        // processedPropsLocal is passed here as enhancement is performed within calc props of the same source and therefore the same level
                                        : enhanceWithCalcPropsData(sourceForCalcPropResolution, processedCalcDataLocal, processedPropsLocal, addedTails);

        final List<PendingTail> allTails = new ArrayList<>();
        allTails.addAll(incomingTails);
        allTails.addAll(recursivelyEnhanced.pendingTails);

        return new CalcPropsDataAndPendingTails(recursivelyEnhanced.calcPropsData, allTails);
    }

    private SourceNodeResult generateHelperNode(final List<PendingTail> tails, final PropChunk firstChunk, final Expression2 expression, final boolean isPartOfCalcProp) {
        final QuerySourceItemForEntityType<?> querySourceInfoItem = (QuerySourceItemForEntityType<?>) firstChunk.data();
        final Source2BasedOnPersistentType implicitSource = new Source2BasedOnPersistentType(querySourceInfoItem.querySourceInfo, gen.nextSourceId(), false, isPartOfCalcProp);
        final SourceNodesResult result = generateHelperNodesForSource(implicitSource, tails);
        final HelperNodeForImplicitJoins node = new HelperNodeForImplicitJoins(firstChunk.name(), expression, querySourceInfoItem.nonnullable, implicitSource, result.sourceNodes);
        return new SourceNodeResult(node, result.transformationResult);
    }

    private static record CalcPropData(Expression2 expr, TransformationResult internalsResult) {
    }

    private static record CalcPropsDataAndPendingTails(Map<String, CalcPropData> calcPropsData, List<PendingTail> pendingTails) {
    }

    private static record SourceNodesResult(List<HelperNodeForImplicitJoins> sourceNodes, TransformationResult transformationResult) {
    }

    private static record SourceNodeResult(HelperNodeForImplicitJoins sourceNode, TransformationResult transformationResult) {
    }

    private static record TransformationResult(
            Map<Integer, List<HelperNodeForImplicitJoins>> helperNodesMap,
            List<Prop3Links> propsData,
            List<ExpressionLinks> expressionsData) {
    }

    static class ExpressionLinks extends AbstractLinks<Expression2> {

        public ExpressionLinks(final List<Prop2Lite> links, final Expression2 resolution) {
            super(links, resolution);
        }
    }

    static class Prop3Links extends AbstractLinks<Prop3Lite>{
        public Prop3Links(final List<Prop2Lite> links, final Prop3Lite resolution) {
            super(links, resolution);
        }
    }

    static class AbstractLinks<T> {
        public final List<Prop2Lite> links;
        public final T resolution;

        public AbstractLinks(final List<Prop2Lite> links, final T resolution) {
            this.links = links;
            this.resolution = resolution;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() +" [links = " + links +", resolution = " + resolution + "]";
        }
    }
}