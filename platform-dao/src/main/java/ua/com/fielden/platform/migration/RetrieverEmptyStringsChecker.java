package ua.com.fielden.platform.migration;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;

public class RetrieverEmptyStringsChecker {
    private final DomainMetadataAnalyser dma;

    public RetrieverEmptyStringsChecker(final DomainMetadataAnalyser dma) {
        this.dma = dma;
    }

    public Set<String> getSqls(final List<IRetriever<? extends AbstractEntity<?>>> allRetrievers) {
        final Set<String> result = new HashSet<>();
        for (final IRetriever<? extends AbstractEntity<?>> retriever : allRetrievers) {
            result.addAll(getRetrieverSqls(retriever));
        }
        return result;
    }

    private Set<String> getRetrieverSqls(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final Set<String> result = new HashSet<>();
        final String retrieverSql = RetrieverSqlProducer.getSql(retriever);
        for (final Entry<String, String> entry : retriever.resultFields().entrySet()) {
            if (retriever == null) {
                System.out.println("retriever == null");
            }
            if (entry == null) {
                System.out.println("retriever == " + retriever);
                System.out.println("entry == null");
            }
            final PropertyMetadata info = dma.getInfoForDotNotatedProp(retriever.type(), entry.getKey());
            if (info == null) {
                System.out.println("retriever == " + retriever);
                System.out.println("entry == " + entry);
                System.out.println("info == null");
            }
            if (dma.getInfoForDotNotatedProp(retriever.type(), entry.getKey()).isEntityOfPersistedType()) {
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
        // TODO take into account RDBMS specific functions - this implementation if MsSql specific
        return " WHERE LTRIM(RTRIM(EEE.\"" + propStmt + "\")) = ''";
    }
}