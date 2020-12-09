package ua.com.fielden.platform.entity.query;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformator;
import ua.com.fielden.platform.eql.stage2.TablesAndSourceChildren;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;

public class EqlQueryTransformer {
    static <E extends AbstractEntity<?>> TransformationResult<ResultQuery3> transform(final QueryExecutionContext executionContext, final QueryProcessingModel<E, ?> qem, final DbVersion dbVersion, final IFilter filter, final String username) {
        final EntQueryGenerator gen = new EntQueryGenerator(dbVersion, filter, username, executionContext.dates(), qem.getParamValues(), executionContext.getDomainMetadata().lmd);
        final PropsResolutionContext resolutionContext = new PropsResolutionContext(executionContext.getDomainMetadata().lmd);
        final ResultQuery2 tr = gen.generateEntQueryAsResultQuery(qem.queryModel, qem.orderModel, qem.fetchModel).transform(resolutionContext);
        final PathsToTreeTransformator p2tt = new PathsToTreeTransformator(executionContext.getDomainMetadata().lmd, gen);
        final Map<String, List<ChildGroup>> grouped = p2tt.groupChildren(tr.collectProps());
        return tr.transform(new TransformationContext(new TablesAndSourceChildren(executionContext.getDomainMetadata().lmd.getTables(), grouped)));
    }
}