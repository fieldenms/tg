package ua.com.fielden.platform.migration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;

public class RetrieverSqlProducer {
    private final DomainMetadataAnalyser dma;

    public RetrieverSqlProducer(final DomainMetadataAnalyser dma) {
        this.dma = dma;
    }

    public String getSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getAllYieldsSql(retriever.resultFields()));
        sb.append(getCoreSql(retriever));
        sb.append(getOrderBySql(retriever));
        return sb.toString();
    }

    public Set<String> getKeyProps(final Class<? extends AbstractEntity<?>> entityType, final DomainMetadataAnalyser dma) {
        return dma.getLeafPropsFromFirstLevelProps(null, entityType, new HashSet<String>(Finder.getFieldNames(Finder.getKeyMembers(entityType))));
    }

    public String getKeyUniquenessViolationSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final Set<String> keyProps = getKeyProps(retriever.type(), dma);

        final StringBuffer sb = new StringBuffer();
        final String baseSql = getKeyResultsOnlySql(retriever, keyProps);
        sb.append("SELECT 1 WHERE EXISTS (SELECT * FROM (" + baseSql + ") MT WHERE 1 < (SELECT COUNT(*) FROM (" + baseSql + ") AA WHERE ");
        for (final Iterator<String> iterator = keyProps.iterator(); iterator.hasNext();) {
            final String keyPropName = iterator.next();
            sb.append("AA.\"" + keyPropName + "\" = MT.\"" + keyPropName + "\"" + (iterator.hasNext() ? " AND " : ""));
        }
        sb.append("))");
        return sb.toString();
    }

    public String getCoreSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getFromSql(retriever));
        sb.append(getWhereSql(retriever));
        sb.append(getGroupBySql(retriever));
        return sb.toString();
    }

    public String getCoreSqlWithCriteria(final IRetriever<? extends AbstractEntity<?>> retriever, final Map<String, String> criteria) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getFromSql(retriever));
        sb.append(getWhereWithAdditionalCriteriaSql(retriever, criteria));
        sb.append(getGroupBySql(retriever));
        return sb.toString();
    }

    private String getKeyResultsOnlySql(final IRetriever<? extends AbstractEntity<?>> retriever, final Set<String> keyProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getAllYieldsSql(getSpecifiedPropsYields(retriever, keyProps)));
        sb.append(getCoreSql(retriever));
        return sb.toString();
    }

    public SortedMap<String, String> getSpecifiedPropsYields(final IRetriever<? extends AbstractEntity<?>> retriever, final Set<String> specifiedProps) {
        final SortedMap<String, String> result = new TreeMap<String, String>();
        for (final Map.Entry<String, String> resultField : retriever.resultFields().entrySet()) {
            if (specifiedProps.contains(resultField.getKey())) {
                result.put(resultField.getKey(), resultField.getValue());
            }
        }
        return result;
    }

    public String getAllYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nSELECT ALL ");
        sb.append(getYieldsSql(resultProps));
        return sb.toString();
    }

    private String getYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator<Entry<String, String>> iterator = resultProps.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<String, String> keyPropName = iterator.next();
            sb.append(keyPropName.getValue() + " \"" + keyPropName.getKey() + "\"" + (iterator.hasNext() ? ", " : ""));
        }
        return sb.toString();
    }

    public String getDistinctYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nSELECT DISTINCT ");
        sb.append(getYieldsSql(resultProps));
        return sb.toString();
    }

    private StringBuffer getWhereSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        if (retriever.whereSql() != null) {
            sb.append("\nWHERE ");
            sb.append(retriever.whereSql());
        }
        return sb;
    }

    private StringBuffer getWhereWithAdditionalCriteriaSql(final IRetriever<? extends AbstractEntity<?>> retriever, final Map<String, String> criteria) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nWHERE ");
        if (retriever.whereSql() != null) {
            sb.append("(" + retriever.whereSql() + ") AND ");
        }
        sb.append("(");
        for (final Iterator<Entry<String, String>> iterator = criteria.entrySet().iterator(); iterator.hasNext();) {
            final Entry<String, String> entry = iterator.next();
            sb.append("(" + retriever.resultFields().get(entry.getKey()) + ") = (" + entry.getValue() + ")");
            sb.append(iterator.hasNext() ? " AND " : ")");
        }

        return sb;
    }

    private StringBuffer getOrderBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        // TODO implementation pending
        return new StringBuffer();
    }

    private StringBuffer getFromSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nFROM ");
        sb.append(retriever.fromSql());
        return sb;
    }

    private StringBuffer getGroupBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.groupSql() != null) {
            sb.append("\nGROUP BY ");
            for (final Iterator<String> iterator = retriever.groupSql().iterator(); iterator.hasNext();) {
                final String keyPropName = iterator.next();
                sb.append(keyPropName + (iterator.hasNext() ? ", " : ""));
            }
        }

        return sb;
    }
}