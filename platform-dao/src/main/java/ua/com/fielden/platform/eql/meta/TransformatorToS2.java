package ua.com.fielden.platform.eql.meta;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.elements.IQrySource1;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.elements.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnSubqueries;

public class TransformatorToS2 {
    public List<List<IQrySource2>> sources = new ArrayList<>();
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this.domainInfo = new HashMap<>(domainInfo);
        sources.add(new ArrayList<>());
    }

    public TransformatorToS2 produceBasedOn() {
        final TransformatorToS2 result = new TransformatorToS2(domainInfo);
        result.sources.addAll(sources);
        return result;
    }

    public TransformatorToS2 produceNewOne() {
        return new TransformatorToS2(domainInfo);
    }

    private IQrySource2 transformSource(final IQrySource1<? extends IQrySource2> qrySourceStage1) {
        if (qrySourceStage1 instanceof QrySource1BasedOnPersistentType) {
            final QrySource1BasedOnPersistentType qrySource = (QrySource1BasedOnPersistentType) qrySourceStage1;
            return new QrySource2BasedOnPersistentType(qrySource.sourceType(), domainInfo.get(qrySource.sourceType()), qrySource.getAlias());
        } else {
            final QrySource1BasedOnSubqueries qrySource = (QrySource1BasedOnSubqueries) qrySourceStage1;
            return new QrySource2BasedOnSubqueries(extractQueryModels(qrySource), qrySource.getAlias(), domainInfo);
        }
    }

    private List<EntQuery2> extractQueryModels(final QrySource1BasedOnSubqueries qrySource) {
        return qrySource.getModels().stream().map(q -> q.transform(produceNewOne())).collect(toList());
    }

    public IQrySource2 getTransformedSource(final IQrySource1<? extends IQrySource2> originalSource) {
        final IQrySource2 transformedSource = transformSource(originalSource);
        sources.get(0).add(transformedSource);        // adding source to current query list of sources
        return transformedSource;
    }
}
