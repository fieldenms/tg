package ua.com.fielden.platform.eql.stage3;

import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage2.TreeResultBySources;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.enhance.ExpressionLinks;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathsToTreeTransformer;
import ua.com.fielden.platform.eql.stage2.sources.enhance.Prop2Lite;
import ua.com.fielden.platform.eql.stage2.sources.enhance.Prop3Links;
import ua.com.fielden.platform.eql.stage2.sources.enhance.TreeResult;
import ua.com.fielden.platform.eql.stage3.operands.queries.ResultQuery3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.IDates;

/**
 * A entry point for transforming an EQL query to SQL.
 * <p>
 * There are 3 stages in the transformation from EQL to SQL:
 * <ol>
 * <li> Stage 1 is created from a raw EQL model where a sequence of fluent API calls is transformed into an SQL-like structure.  
 * <li> Stage 2 resolves dot-notated properties to their respective sources. 
 * <li> Stage 3 builds up all implicit table joins, resulting from dot-notations, substitutes calculated property names used in dot-notations with their respective expressions.    
 * </ol>
 * The result of stage 3 is then used to generate the actual SQL select statement. It also includes the information needed to instantiate entities from the SQL query result.
 * 
 * @author TG Team
 *
 */
public class EqlQueryTransformer {
    public static final <E extends AbstractEntity<?>> TransformationResult2<ResultQuery3> transform( 
            final QueryProcessingModel<E, ?> qem, 
            final IFilter filter, 
            final String username, 
            final IDates dates,
            final EqlDomainMetadata eqlDomainMetadata) {
        final EntQueryGenerator gen = new EntQueryGenerator(filter, username, new QueryNowValue(dates), qem.getParamValues());
        final TransformationContext1 context1 = new TransformationContext1(eqlDomainMetadata.querySourceInfoProvider);
        final ResultQuery1 query1 = gen.generateAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel);
		final ResultQuery2 query2 = query1.transform(context1);
        final PathsToTreeTransformer p2tt = new PathsToTreeTransformer(eqlDomainMetadata.querySourceInfoProvider, gen);
        final TreeResult treeResult = p2tt.transform(query2.collectProps());
        final TransformationContext2 context2 = new TransformationContext2(
                new TreeResultBySources(treeResult.implicitNodesMap(), processExpressionsData(treeResult.expressionsData()), processPropsResolutionData(treeResult.propsData())), 
                eqlDomainMetadata.entityMetadataHolder);
		return query2.transform(context2);
    }
    
    private static Map<Integer, Map<String, Expression2>> processExpressionsData(final List<ExpressionLinks> expressionsResolutions) {
        final Map<Integer, Map<String, Expression2>> expressionsData = new HashMap<>();
        for (final ExpressionLinks item : expressionsResolutions) {
            for (final Prop2Lite link : item.links()) {
                Map<String, Expression2> existingSourceMap = expressionsData.get(link.sourceId());
                if (existingSourceMap == null) {
                    existingSourceMap = new HashMap<String, Expression2>();
                    expressionsData.put(link.sourceId(), existingSourceMap);
                }
                existingSourceMap.put(link.name(), item.expr());
            }
        }
        
        return expressionsData;
    }
    
    private static Map<Integer, Map<String, T2<String, Integer>>> processPropsResolutionData(final List<Prop3Links> propsResolutions) {
        final Map<Integer, Map<String, T2<String, Integer>>> resolutionsData = new HashMap<>();
        for (final Prop3Links item : propsResolutions) {
            for (final Prop2Lite link : item.links()) {
                Map<String, T2<String, Integer>> existingSourceMap = resolutionsData.get(link.sourceId());
                if (existingSourceMap == null) {
                    existingSourceMap = new HashMap<String, T2<String, Integer>>();
                    resolutionsData.put(link.sourceId(), existingSourceMap);
                }
                existingSourceMap.put(link.name(), t2(item.propName(), item.sourceId()));
            }
        }
        
        return resolutionsData;
    }
}