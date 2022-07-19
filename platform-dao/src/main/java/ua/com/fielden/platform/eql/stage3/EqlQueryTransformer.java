package ua.com.fielden.platform.eql.stage3;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformator;
import ua.com.fielden.platform.eql.stage2.TablesAndSourceChildren;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;
import ua.com.fielden.platform.utils.IDates;

public class EqlQueryTransformer {
    public static final <E extends AbstractEntity<?>> TransformationResult<ResultQuery3> transform( 
            final QueryProcessingModel<E, ?> qem, 
            final IFilter filter, 
            final String username, 
            final IDates dates,
            final EqlDomainMetadata eqlDomainMetadata) {
        final EntQueryGenerator gen = new EntQueryGenerator(filter, username, new QueryNowValue(dates), qem.getParamValues());
        final TransformationContext context = new TransformationContext(eqlDomainMetadata);
        final ResultQuery2 query2 = gen.generateAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel).transform(context);
        final PathsToTreeTransformator p2tt = new PathsToTreeTransformator(eqlDomainMetadata, gen);
        final Map<String, List<ChildGroup>> childGroupsMap = p2tt.groupChildren(query2.collectProps());
        return query2.transform(new ua.com.fielden.platform.eql.stage2.TransformationContext(new TablesAndSourceChildren(eqlDomainMetadata.getTables(), childGroupsMap)));
    }
}