package ua.com.fielden.platform.eql.stage3;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.QueryExecutionContext;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformator;
import ua.com.fielden.platform.eql.stage2.TablesAndSourceChildren;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;

public class EqlQueryTransformer {
    public static <E extends AbstractEntity<?>> TransformationResult<ResultQuery3> transform(final QueryExecutionContext executionContext, final QueryProcessingModel<E, ?> qem, final DbVersion dbVersion, final IFilter filter, final String username) {
        final EntQueryGenerator gen = new EntQueryGenerator(dbVersion, filter, username, executionContext.dates(), qem.getParamValues(), executionContext.getDomainMetadata().lmd);
        final TransformationContext context = new TransformationContext(executionContext.getDomainMetadata().lmd);
        final ResultQuery2 query2 = gen.generateAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel).transform(context);
        final PathsToTreeTransformator p2tt = new PathsToTreeTransformator(executionContext.getDomainMetadata().lmd, gen);
        final Map<String, List<ChildGroup>> childGroupsMap = p2tt.groupChildren(query2.collectProps());
        return query2.transform(new ua.com.fielden.platform.eql.stage2.TransformationContext(new TablesAndSourceChildren(executionContext.getDomainMetadata().lmd.getTables(), childGroupsMap)));
    }
}