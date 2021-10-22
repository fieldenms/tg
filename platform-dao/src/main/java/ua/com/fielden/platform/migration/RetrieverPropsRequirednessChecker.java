package ua.com.fielden.platform.migration;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;

public class RetrieverPropsRequirednessChecker {
    private final DomainMetadataAnalyser dma;

    public RetrieverPropsRequirednessChecker(final DomainMetadataAnalyser dma) {
        this.dma = dma;
    }

    public Set<String> getSqls(final List<IRetriever<? extends AbstractEntity<?>>> allRetrievers) {
        final Set<String> result = new HashSet<String>();
        for (final IRetriever<? extends AbstractEntity<?>> retriever : allRetrievers) {
            result.addAll(getRetrieverSqls(retriever));
        }
        return result;
    }

    private Set<String> getRetrieverSqls(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final Set<String> result = new HashSet<String>();
        final String retrieverSql = RetrieverSqlProducer.getSql(retriever, false);
        for (final Entry<String, String> entry : retriever.resultFields().entrySet()) {
            if (!dma.isNullable(retriever.type(), entry.getKey())) {
                final StringBuffer sb = new StringBuffer();
                sb.append("SELECT '");
                sb.append(retriever.getClass().getSimpleName());
                sb.append("' RETRIEVER, '");
                sb.append(entry.getKey());
                sb.append("' PROPERTY, COUNT(*) COUNT FROM (");
                sb.append(retrieverSql);
                sb.append(") EEE ");
                sb.append(composeWhereStmt(entry.getKey()));
                result.add(sb.toString());
            }
        }
        return result;
    }

    private String composeWhereStmt(final String propStmt) {
        return " WHERE EEE.\"" + propStmt + "\" IS NULL";
    }
}